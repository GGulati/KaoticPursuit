package game.Implementation;

import java.util.ArrayList;
import java.util.HashMap;

import game.AItem;
import game.KaoticWorldEngine;

public class NPCDialogueResponse
{
	String[] m_flagNames;
	FlagOp[] m_flagOps;
	Integer[] m_flagVals;
	
	AItem[] m_items;
	ItemOp[] m_itemOps;
	Integer[] m_itemVals;
	
	KaoticWorldEngine m_engineRef;
	
	String m_response;
	String[] m_said;
	
	public NPCDialogueResponse(String response, KaoticWorldEngine engine,
			ArrayList<String> flagNames, ArrayList<FlagOp> flagOps, ArrayList<Integer> flagVals,
			ArrayList<String> itemNames, ArrayList<ItemOp> itemOps, ArrayList<Integer> itemVals) throws Exception
	{
		if (flagNames.size() != flagOps.size() || flagNames.size() != flagVals.size())
			throw new Exception("Flag attributes must be the same in quantity.");
		if (itemNames.size() != itemOps.size() || itemNames.size() != itemVals.size())
			throw new Exception("Item attributes must be the same in quantity.");
		
		m_response = response;
		m_said = engine.Parser.GetVerbNoun(response);
		m_engineRef = engine;

		m_flagNames = new String[flagNames.size()];
		m_flagOps = new FlagOp[flagOps.size()];
		m_flagVals = new Integer[flagVals.size()];
		flagNames.toArray(m_flagNames);
		flagOps.toArray(m_flagOps);
		flagVals.toArray(m_flagVals);
		
		for (String name : m_flagNames)
		{
			if (!m_engineRef.HasFlag(name))
				throw new Exception("Flag '" + name +  "' doesn't exist.");	
		}

		m_items = new AItem[itemNames.size()];
		m_itemOps = new ItemOp[itemOps.size()];
		m_itemVals = new Integer[itemVals.size()];
		itemOps.toArray(m_itemOps);
		itemVals.toArray(m_itemVals);
		
		String iName;
		for (int i = 0; i < itemNames.size(); i++)
		{
			iName = itemNames.get(i);
			if (!m_engineRef.HasItem(iName))
				throw new Exception("Item '" + iName + "' doesn't exist.");
			else
				m_items[i] = m_engineRef.GetItem(iName);
		}
	}
	
	public float IsValid(String[] talk)
	{		
		float success = 0;
		for (int i = 0; i < m_said.length; i++)
		{
			for (int j = 0; j < talk.length; j++)
			{
				if (m_said[i].compareToIgnoreCase(talk[j]) == 0)
				{
					success++;
					break;
				}
			}
		}
		
		return success / m_said.length;
	}
	
	public String GetResponse()
	{
		return m_response;
	}

	public String[] GetResponses()
	{
		return m_said;
	}
	
	public boolean FlagsEval()
	{
		Integer val;
		
		for (int i = 0; i < m_flagNames.length; i++)
		{
			val = m_engineRef.GetFlag(m_flagNames[i]);
			switch (m_flagOps[i])
			{
				case CompEq:
					if (val != m_flagVals[i])
						return false;
					break;
				case CompNotEq:
					if (val == m_flagVals[i])
						return false;
					break;
				case CompLess:
					if (val >= m_flagVals[i])
						return false;
					break;
				case CompGreater:
					if (val <= m_flagVals[i])
						return false;
					break;
			}
		}
		
		return true;
	}

	public void FlagsExecute()
	{
		for (int i = 0; i < m_flagNames.length; i++)
		{
			switch(m_flagOps[i])
			{
				case Set:
					m_engineRef.SetFlag(m_flagNames[i], m_flagVals[i]);
					break;
				case Add:
					m_engineRef.SetFlag(m_flagNames[i], m_engineRef.GetFlag(m_flagNames[i]) + m_flagVals[i]);
					break;
				case Sub:
					m_engineRef.SetFlag(m_flagNames[i], m_engineRef.GetFlag(m_flagNames[i]) - m_flagVals[i]);
					break;
				case Mul:
					m_engineRef.SetFlag(m_flagNames[i], m_engineRef.GetFlag(m_flagNames[i]) * m_flagVals[i]);
					break;
				case Div:
					m_engineRef.SetFlag(m_flagNames[i], m_engineRef.GetFlag(m_flagNames[i]) / m_flagVals[i]);
					break;
			}
		}
	}

	public boolean CanTakeItems(NPC taker)
	{
		float totalWeight = 0;
		
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Take && m_engineRef.HumanPlayer.GetAmountOf(m_items[i]) < m_itemVals[i])
				return false;
			else
				totalWeight += m_items[i].GetWeight();
		}
		
		return taker.GetCarrying() + totalWeight <= taker.GetCarryingCapacity();
	}
	
	public boolean CanGiveItems(NPC giver)
	{
		float totalWeight = 0;
		
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Give)
				totalWeight += m_items[i].GetWeight() * m_itemVals[i];
		}
		
		return m_engineRef.HumanPlayer.GetCarrying() + totalWeight <= m_engineRef.HumanPlayer.GetCarryingCapacity();
	}

	public void TakeItems(NPC taker)
	{
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Take)
				taker.AddItem(m_items[i], m_engineRef.HumanPlayer.RemoveItem(m_items[i], m_itemVals[i]));
		}
	}

	public void GiveItems(NPC giver)
	{
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Give)
				m_engineRef.HumanPlayer.AddItem(m_items[i], giver.RemoveItem(m_items[i], m_itemVals[i]));
		}
	}

	public HashMap<AItem, Integer> GetGivenItems()
	{
		HashMap<AItem, Integer> fill = new HashMap<AItem, Integer>();
		
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Give)
				fill.put(m_items[i], m_itemVals[i]);
		}
		
		return fill;
	}

	public HashMap<AItem, Integer> GetTakenItems()
	{
		HashMap<AItem, Integer> fill = new HashMap<AItem, Integer>();
		
		for (int i = 0; i < m_items.length; i++)
		{
			if (m_itemOps[i] == ItemOp.Take)
				fill.put(m_items[i], m_itemVals[i]);
		}
		
		return fill;
	}
}
