package game.Implementation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import game.ACharacter;
import game.AItem;
import game.AttributeLayer;
import game.Attributes;
import game.KaoticWorldEngine;
import game.Implementation.NPCDialogueExchange.TalkingData;

public class NPC extends ACharacter
{
	KaoticWorldEngine m_engineRef;
	NPCDialogue m_dialogue;
	Integer m_currentExchange;
	String[] m_referredTo;
	int m_playerTalkFail = 0, m_playerTalkFailTolerance = 3;

	public NPC(String npcFileName, KaoticWorldEngine engine) throws Exception
	{
		super(null, null, null);
		m_engineRef = engine;
		
		InputStream in = new FileInputStream("data/NPC/" + npcFileName + ".xml");
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		
		ArrayList<String> referredTo = new ArrayList<String>();
		Float[] attrs = new Float[Attributes.values().length];
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();

				if (name.contentEquals("npc"))
				{
					if (started)
						throw new Exception("Nodes of type 'NPC' cannot be nested.");
					started = true;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("name"))
							m_name = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("desc") || attribute.getName().toString().equalsIgnoreCase("description"))
							m_desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("dialogue"))
						{
							if (m_name == null)
								throw new Exception("Name must be initialized before Dialogue.");
							m_dialogue = new NPCDialogue("data/NPC/" + npcFileName + "." + attribute.getValue() + ".xml", engine);
						}
					}
					
					if (m_name == null || m_desc == null)
						throw new Exception("NPCs must have a name and description.");
				}
				else if (!started)
					throw new Exception("A node of type 'NPC' must be instantiated as the parent node.");
				else if (name.contentEquals("othername") || name.contentEquals("referredto"))
				{
					e = reader.nextEvent();
					referredTo.add(e.asCharacters().getData());
				}
				else if (name.contentEquals("item"))
				{
					int amount = 1;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("amount"))
							amount = Integer.parseInt(attribute.getValue());
					}
					if (amount < 1)
						throw new Exception("You can only have positive amounts of items.");
					
					e = reader.nextEvent();
					AItem toAdd = engine.GetItem(e.asCharacters().getData());
					if (toAdd != null)
						AddItem(toAdd, amount);
				}
				else if (name.contentEquals("attribute"))
				{
					float attrVal = -1;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("value") || attribute.getName().toString().equalsIgnoreCase("base"))
							attrVal = Float.parseFloat(attribute.getValue());
					}
					if (attrVal < 0)
						throw new Exception("Attributes must be initialized.");
					
					e = reader.nextEvent();
					attrs[KaoticWorldEngine.IndexFromString(Attributes.valueOf(e.asCharacters().toString()))] = attrVal;
				}
			}
		}
		
		m_referredTo = new String[referredTo.size()];
		referredTo.toArray(m_referredTo);
		m_desc = m_name + " - " + m_desc;
		m_longDesc = m_desc;
		m_currentExchange = 0;
		m_attr = new AttributeLayer(attrs);
	}

	public float IsReferredTo(String[] name)
	{
		float total = 0;
		
		for (String aka : m_referredTo)
		{
			for (String tried : name)
				total += tried.equalsIgnoreCase(aka) ? 1 : 0;
		}
		
		return total / m_referredTo.length;
	}

	public NPCDialogueExchange GetCurrentExchange()
	{
		return m_dialogue.GetExchange(m_currentExchange);
	}
	
	public NPCDialogueExchange.TalkingData Talk(String[] talk)
	{
		NPCDialogueExchange.TalkingData data = m_dialogue.GetExchange(0).new TalkingData();
		if (talk.length == 0)
		{
			m_playerTalkFail++;
			if (m_playerTalkFail > m_playerTalkFailTolerance)
			{
				m_playerTalkFail = 0;
				data.Index = -1;
				return data;
			}
			data.Index = 0;
			return data;
		}
		
		NPCDialogueExchange ex = m_dialogue.GetExchange(m_currentExchange);
		NPCDialogueExchange.TalkingData next = ex.TryTalk(talk, this, m_engineRef.HumanPlayer, m_dialogue);
		if (next.Index < 0)//failure to find a valid reply
		{
			m_playerTalkFail++;
			if (m_playerTalkFail > m_playerTalkFailTolerance)
			{
				m_playerTalkFail = 0;
				data.Index = -1;
				return data;
			}
			data.Index = 0;
			return data;
		}
		m_currentExchange = next.Index;
		m_playerTalkFail = 0;
		
		data.Index = 1;
		data.ToExecute = next.ToExecute;
		
		return data;
	}
}