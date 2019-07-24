package insomnia.mode;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import insomnia.automaton.AutomatonException;
import insomnia.query.CouchQueryFactory;
import insomnia.query.MongoQueryFactory;
import insomnia.summary.MongoSummaryFactory;
import insomnia.summary.Summary;
import insomnia.json.JsonParser;
import insomnia.json.JsonValueExtractor;
import insomnia.mode.ModeQuery.Language;
import insomnia.regex.RegexParser;
import insomnia.regex.automaton.RegexAutomaton;
import insomnia.regex.automaton.RegexAutomaton.Builder.BuilderException;
import insomnia.regex.automaton.RegexToAutomatonConverter;

public class AppQuery
{
	private int blockSize;
	private RegexAutomaton automaton;
	private RegexAutomaton regexSummary;
	private Summary summary;
	private boolean isValidPath;
	private List<String> summaryPaths;
	private List<String> queries;
	private List<Object> values;

	public AppQuery(ModeQuery mq) throws AutomatonException, BuilderException, IOException, ParseException
	{
		blockSize = mq.blockSize;

		RegexParser parser = new RegexParser();
		InputStream in;

		// RegexAutomaton creation
		in = new ByteArrayInputStream(mq.regex.getBytes());
		automaton = RegexToAutomatonConverter.convert(parser.readRegexStream(in));
		in.close();

		if(mq.summary != null)
		{
			if(mq.summary.toCharArray()[0] == '#')
			{
				// RegexAutomaton creation (for summary)
				in = new ByteArrayInputStream(mq.summary.substring(1).getBytes());
				regexSummary = RegexToAutomatonConverter.convert(parser.readRegexStream(in));
				in.close();
			}
			else
			{
				// Summary creation
				in = new BufferedInputStream(new FileInputStream(mq.summary));
				summary = MongoSummaryFactory.getInstance().load(in);
				in.close();
			}
		}

		if(regexSummary != null)
			summaryPaths = automaton.getPathsFromAutomaton(regexSummary);
		else if(summary != null)
			summaryPaths = automaton.getPathsFromSummary(summary);

		if(mq.jsonFile != null)
		{
			JsonParser jparser = new JsonParser();
			in = new BufferedInputStream(new FileInputStream(mq.jsonFile));
			Object jsonData = jparser.readJsonStream(in);
			values = JsonValueExtractor.getValues(jsonData, summaryPaths);
		}

		if(mq.path != null)
			isValidPath = automaton.isValidPath(mq.path);

		if(mq.language == Language.MONGO)
			queries = MongoQueryFactory.getInstance().getQueries(summaryPaths, blockSize);
		else if(mq.language == Language.COUCH)
			queries = CouchQueryFactory.getInstance().getQueries(summaryPaths, summary);
	}

	public RegexAutomaton getAutomaton()
	{
		return automaton;
	}

	public List<String> getPaths() throws AutomatonException
	{
		return automaton.generatePaths();
	}

	public List<String> getSummaryPaths() throws AutomatonException
	{
		return summaryPaths;
	}

	public List<String> getQueries()
	{
		return queries;
	}

	public boolean existPath() throws AutomatonException
	{
		if(summary == null)
			return false;
		return automaton.existPath(summary);
	}

	public boolean isValidPath() throws AutomatonException
	{
		return isValidPath;
	}

	public List<Object> getValues() throws AutomatonException
	{
		return values;
	}
}
