package game.Implementation;

import game.KaoticWorldEngine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public final class NPCDialogue
{
	public static final float DEFAULT_PARSING_THRESHOLD = .33f;
	
	HashMap<Integer, NPCDialogueExchange> m_conversations;
	
	public NPCDialogue(String file, KaoticWorldEngine engine) throws Exception
	{
		m_conversations = new HashMap<Integer, NPCDialogueExchange>();
		
		InputStream in = new FileInputStream(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		EndElement end;
		
		boolean exchange = false;
		String exchangeNPCSays = null;
		Integer exchangeID = 0;
		float threshold = DEFAULT_PARSING_THRESHOLD;
		HashMap<NPCDialogueResponse, Integer> responses = new HashMap<NPCDialogueResponse, Integer>();
		HashMap<NPCDialogueResponse, Integer> responseFails = new HashMap<NPCDialogueResponse, Integer>();
		
		ArrayList<String> flagNames = new ArrayList<String>(), itemNames = new ArrayList<String>();
		ArrayList<Integer> flagVals = new ArrayList<Integer>(), itemVals = new ArrayList<Integer>();
		ArrayList<ItemOp> itemOps = new ArrayList<ItemOp>();
		ArrayList<FlagOp> flagOps = new ArrayList<FlagOp>();
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();

				if (name.contentEquals("npcdialogue"))
				{
					if (started)
						throw new Exception("Nodes of type 'NPCDialogue' cannot be nested.");
					started = true;
				}
				else if (!started)
					throw new Exception("A node of type 'NPCDialogue' must be instantiated as the parent node.");
				else if (name.contentEquals("exchange"))
				{
					if (exchange)
						throw new Exception("Exchanges cannot be nested.");
					e = reader.nextEvent();
					exchangeID = 0;
					exchange = true;
					exchangeNPCSays = null;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("id"))
							exchangeID = Integer.valueOf(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("npcsays"))
							exchangeNPCSays = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("threshold"))
							threshold = Float.parseFloat(attribute.getValue());
					}

					if (exchangeNPCSays == null)
						throw new Exception("The NPC must say something initially.");
				}
				else if (name.contentEquals("response"))
				{
					if (!exchange)
						throw new Exception("Responses must be nested inside Exchanges");

					Integer navID = 0, failID = 0;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("navid"))
							navID = Integer.valueOf(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("failid"))
							failID = Integer.valueOf(attribute.getValue());
						else
						{
							String[] split = attribute.getName().toString().toLowerCase().split("_");
							String manipName = attribute.getValue();
							if (split.length != 2)
								throw new Exception("Manipulation must be differentiated with an underscore.");
							Integer val = Integer.parseInt(split[1]);
							
							if (split[0].startsWith("flag"))
							{
								split[0] = split[0].replace("flag", "");
								
								FlagOp op = FlagOp.Set;
								if (split[0].startsWith("eq"))
									op = FlagOp.CompEq;
								else if (split[0].startsWith("noteq"))
									op = FlagOp.CompNotEq;
								else if (split[0].startsWith("greater"))
									op = FlagOp.CompGreater;
								else if (split[0].startsWith("less"))
									op = FlagOp.CompLess;
								else if (split[0].startsWith("add"))
									op = FlagOp.Add;
								else if (split[0].startsWith("sub"))
									op = FlagOp.Sub;
								else if (split[0].startsWith("mul"))
									op = FlagOp.Mul;
								else if (split[0].startsWith("div"))
									op = FlagOp.Div;
								else if (split[0].startsWith("set"))
									;
								else
									throw new Exception("Unrecognized Flag Operation");
								
								flagNames.add(manipName);
								flagVals.add(val);
								flagOps.add(op);
							}
							else if (split[0].startsWith("item"))
							{
								split[0] = split[0].replace("item", "");
								
								ItemOp op = ItemOp.Give;
								if (split[0].startsWith("give"))
									;
								else if (split[0].startsWith("take"))
									op = ItemOp.Take;
								else
									throw new Exception("Unrecognized Item Operation");
								
								itemNames.add(manipName);
								itemVals.add(val);
								itemOps.add(op);
							}
						}
					}

					e = reader.nextEvent();
					NPCDialogueResponse toAdd = new NPCDialogueResponse(e.asCharacters().getData(), engine,
							flagNames, flagOps, flagVals, itemNames, itemOps, itemVals);
					responses.put(toAdd, navID);
					responseFails.put(toAdd, failID);
					
					flagNames.clear();
					itemNames.clear();
					flagOps.clear();
					itemOps.clear();
					flagVals.clear();
					itemVals.clear();
				}
			}
			else if (e.isEndElement())
			{
				end = e.asEndElement();
				name = end.getName().getLocalPart().toLowerCase();
				
				if (name.contentEquals("exchange"))
				{
					if (!exchange)
						throw new Exception("Formatting error with Exchange.");
					exchange = false;
					HashMap<NPCDialogueResponse, Integer> arg = (HashMap<NPCDialogueResponse, Integer>)responses.clone();
					HashMap<NPCDialogueResponse, Integer> arg2 = (HashMap<NPCDialogueResponse, Integer>)responseFails.clone();
					NPCDialogueExchange toAdd = new NPCDialogueExchange(this, exchangeNPCSays, threshold, arg, arg2);
					m_conversations.put(exchangeID, toAdd);

					threshold = DEFAULT_PARSING_THRESHOLD;
					responses.clear();
					responseFails.clear();
				}
			}
		}
		
		if (GetExchange(0) == null)
			throw new Exception("You must have a root conversation.");
	}

	public NPCDialogueExchange GetExchange(Integer ID)
	{
		for (Map.Entry<Integer, NPCDialogueExchange> e : m_conversations.entrySet())
		{
			if (e.getKey().compareTo(ID) == 0)
				return e.getValue();
		}
		return null;
	}
}
