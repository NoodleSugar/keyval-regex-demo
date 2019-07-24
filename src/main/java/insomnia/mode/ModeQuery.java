package insomnia.mode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class ModeQuery implements IMode
{
	public enum Language
	{
		MONGO, COUCH;
		
		public static Language getLanguage(String l)
		{
			switch(l)
			{
			case "mongo":
				return MONGO;
			case "couch":
				return COUCH;
			default:
				throw new InvalidParameterException("Unknow database language");
			}
		}
	}
	
	private Options options;
	private String[] args;
	private CommandLine cmdLine;
	private String templatePath;

	protected String regex;
	protected String summary;
	protected String jsonFile;
	protected String path;
	protected Language language;
	protected int blockSize;

	private VelocityContext velocityContext;

	public ModeQuery()
	{
		blockSize = 10;
		options = new Options();
		options.addOption("h", "print the help");
		options.addOption("t", "template", true, "select the template to use");
		options.addOption("s", "summary", true, "select the summary to use");
		options.addOption("j", "json", true, "select a json file");
		options.addOption("p", "path", true, "select a path");
		options.addOption("b", "block-size", true, "specify the block size for queries");
		options.addOption("l", "language", true, "specify the database language to use for queries");

		Properties prop = new Properties();
		try
		{
			URL resource = this.getClass().getResource("velocity.properties");
			prop.load(new InputStreamReader(resource.openStream(), "UTF8"));
		}
		catch(IOException e)
		{
			System.err.println("Impossible de charger velocity.properties : " + e.getMessage());
		}
		Velocity.init(prop);
	}

	private void parseCmdLine() throws ParseException
	{
		CommandLineParser parser = new DefaultParser();
		cmdLine = parser.parse(options, args);
	}

	private void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Querex mquery <query> [<option1>...]", options);
	}

	private void createVelocityContext() throws Exception
	{
		velocityContext = new VelocityContext();
		AppQuery appQuery = new AppQuery(this);
		velocityContext.put("q", appQuery);
	}

	private void executeVelocity() throws IOException, URISyntaxException
	{
		if(templatePath == null)
			templatePath = "/mode/extemp.vm";

		Template template = Velocity.getTemplate(this.getClass().getResource(templatePath).getPath());
		StringWriter sw = new StringWriter();
		template.merge(velocityContext, sw);
		System.out.println(sw);
	}

	@Override
	public void run(String[] args)
	{
		this.args = args;
		regex = args[1];

		try
		{
			parseCmdLine();
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			return;
		}

		String tmp;
		if(cmdLine.hasOption("h"))
		{
			printHelp();
			return;
		}
		if((tmp = cmdLine.getOptionValue("s")) != null)
			summary = tmp;
		if((tmp = cmdLine.getOptionValue("b")) != null)
			blockSize = Integer.valueOf(tmp);
		if((tmp = cmdLine.getOptionValue("j")) != null)
			jsonFile = tmp;
		if((tmp = cmdLine.getOptionValue("p")) != null)
			path = tmp;
		if((tmp = cmdLine.getOptionValue("t")) != null)
			templatePath = tmp.endsWith(".vm") ? tmp : tmp + ".vm";
		if((tmp = cmdLine.getOptionValue("l")) != null)
			language = Language.getLanguage(tmp);

		try
		{
			createVelocityContext();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

		try
		{
			executeVelocity();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
