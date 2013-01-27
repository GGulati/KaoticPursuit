package entry;

import game.IVerbNounParser;
import game.KaoticGame;
import game.Implementation.SimpleVerbNounParser;
import gameUI.ConsoleWrapper;
import gameUI.GameGUI;
import gameUI.IOutput;

public class Program
{
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		//try
		//{
			KaoticGame game = new KaoticGame();
			IOutput output = new GameGUI(game);
			//IOutput output = new ConsoleWrapper(game);
			IVerbNounParser parser = new SimpleVerbNounParser("simpleVerbNoun.xml");
			game.Init(output, parser);
		//}
		//catch (Exception e) { e.printStackTrace(); }
	}
}