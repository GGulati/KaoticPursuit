package game.Implementation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import game.IVerbNounParser;

public final class SimpleVerbNounParser implements IVerbNounParser
{
	String[] m_dismiss;
	Character[] m_punctuation;
	
	public SimpleVerbNounParser(String parserConfigPath) throws Exception
	{
		ArrayList<String> input = new ArrayList<String>();
		ArrayList<Character> punct = new ArrayList<Character>();
		
		InputStream in = new FileInputStream("data/ParserConfig/" + parserConfigPath);
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();

				if (name.contentEquals("simpleverbnounparser"))
				{
					if (started)
						throw new Exception("Nodes of type 'SimpleVerbNounParser' cannot be nested.");
					started = true;
				}
				else if (!started)
					throw new Exception("A node of type 'SimpleVerbNounParser' must be instantiated as the parent node.");
				else if (name.contentEquals("ignoredword"))
				{
					e = reader.nextEvent();
					input.add(e.asCharacters().getData().toLowerCase());
				}
				else if (name.contentEquals("punctuation"))
				{
					e = reader.nextEvent();
					punct.add(Character.toLowerCase(e.asCharacters().getData().toCharArray()[0]));
				}
			}
		}

		m_dismiss = new String[input.size()];
		m_punctuation = new Character[punct.size()];
		input.toArray(m_dismiss);
		punct.toArray(m_punctuation);
	}
	
	@Override
	public String[] GetVerbNoun(String input)
	{
		input = input.toLowerCase();
		for (Character c : m_punctuation)
			input = input.replace(c, ' ');
		String[] split = input.split(" ");
		
		String current;
		ArrayList<String> toRet = new ArrayList<String>();
		for (String in : split)
			toRet.add(in);
		
		for (int i = 0; i < toRet.size(); i++)
		{
			current = toRet.get(i);
			if (current.contentEquals(""))
			{
				toRet.remove(i);
				i--;
				continue;
			}
			for (String ignore : m_dismiss)
			{
				if (current.contentEquals(ignore))
				{
					toRet.remove(i);
					i--;
					break;
				}
			}
		}
		
		split = new String[toRet.size()];
		toRet.toArray(split);
		return split;
	}

}
