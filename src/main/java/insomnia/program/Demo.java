package insomnia.program;

import insomnia.mode.IMode;
import insomnia.mode.ModeQuery;
import insomnia.mode.ModeSumMongo;

/*
 * Commandes : Querex <mode> ...
 *   Querex mquery <query> [<option1> <option2>...]
 *     <query> : #regex ou fichier contenant une regex
 *   Querex msummarise <database> <collection> [<option1> <option2>...]
 *
 * Options mode mquery:
 *   -t --template   <template> Selectionne le template à utiliser (par défaut le template exemple)
 *   -s --summary    <summary>  Selectionne le résumé de donnée à utiliser
 *   -j --json       <json>     Selectionne un fichier Json à traiter
 *   -p --path       <path>     Spécifie un chemin à traiter
 *   -b --block-size <size>     Précise la taille des blocs de requétes (valeur par défaut 10)
 *   -l --language   <language> Précise le langage de requéte à utiliser
 *
 * Options mode msummarise:
 *   -u --user        <name>     spécifie le nom d'utilisateur
 *   -p --password    <password> spécifie le mot de passe
 *   -d --destination <dest>     spécifie le fichier de sauvegarde (par défaut summary.sum)
 */
public class Demo
{

	public static void main(String[] args)
	{
		String arg0 = args[0];
		IMode mode;

		if(arg0.equals("mquery"))
			mode = new ModeQuery();
		else if(arg0.equals("msummarise"))
			mode = new ModeSumMongo();
		else if(arg0.equals("-h") && args.length == 1)
		{
			System.out.println(
					"Usage: Querex <mode> ...\n<mode> est à choisir parmis:\n - mquery pour traiter un requéte\n - msummarise pour obtenir le résumé d'une MongoDB");
			return;
		}
		else
		{
			System.out.println("Unknown args\nType Querex -h to get help");
			return;
		}

		mode.run(args);
	}

}
