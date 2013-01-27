package game;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Room
{
	String m_title, m_desc;
	ArrayList<Exit> m_exits;
	HashMap<AEntity, Integer> m_entities;
	
	public Room(String title, String desc)
	{
		m_title = title;
		m_desc = desc;
		
		m_exits = new ArrayList<Exit>();
		m_entities = new HashMap<AEntity, Integer>();
	}
	
	public void Update()
	{
		for (AEntity e : m_entities.keySet())
			e.Update();
	}
	
	public String GetTitle()
	{
		return m_title;
	}
	
	public String GetDesc()
	{
		return "You are in " + m_title + ".";
	}
	
	public String GetLongDescription(ACharacter viewer)
	{
		StringBuilder str = new StringBuilder();
		
		/*str.append("You are in ");
		str.append(m_title);
		str.append("\n");*/
		str.append(m_desc);
		str.append("\n");
		
		for (Map.Entry<AEntity, Integer> e : m_entities.entrySet())
		{
			if (e.getKey().GetVisibility() > viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
				continue;
			if (e.getKey() instanceof AItem)
			{
				str.append(ACharacter.EnglishNumberFromInteger(e.getValue()));
				str.append(" ");
				str.append(e.getKey().GetName());
			}
			else if (e.getKey() != viewer)
				str.append(e.getKey().GetDesc());
			str.append("\n");
		}
		
		int obvExits = GetNumVisibleExits(viewer), descExits = 0;
		if (obvExits > 0)
			str.append("Obvious Exits: ");
		for (int i = 0; i < m_exits.size(); i++)
		{
			if (descExits > 0)
				str.append(", ");
			if (m_exits.get(i).GetVisibility() <= viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
			{
				str.append(m_exits.get(i).GetDir());
				descExits++;
			}
		}
		if (obvExits > 0)
			str.append("\n");
		
		return str.toString();
	}
	
	public int GetNumVisibleEntities(ACharacter viewer)
	{
		int amount = 0;
		
		for (Map.Entry<AEntity, Integer> e : m_entities.entrySet())
			amount += e.getKey().GetVisibility() <= viewer.GetAttributes().GetAttributeValue(Attributes.Perception) ? e.getValue() : 0;
		
		return amount;
	}
	
	public boolean RemoveEntity(AEntity toRemove)
	{
		if (HasEntity(toRemove))
		{
			m_entities.remove(toRemove);
			return true;
		}
		return false;
	}
	
	public int RemoveItems(AItem toRemove, int amount)
	{
		if (HasEntity(toRemove))
		{
			int current = m_entities.get(toRemove);
			int removed = current < amount ? current : amount;
			m_entities.remove(toRemove);
			if (current - removed > 0)
				m_entities.put(toRemove, current - removed);
			return removed;
		}
		return 0;
	}
	
	public boolean AddEntity(AEntity toAdd)
	{
		if (HasEntity(toAdd))
			return toAdd instanceof AItem ? AddItems((AItem)toAdd, 1) : false;
		
		m_entities.put(toAdd, 1);
		return true;
	}
	
	public boolean AddItems(AItem toAdd, int amount)
	{
		if (HasEntity(toAdd))
		{
			amount += this.GetAmountOfEntity(toAdd);
			RemoveEntity(toAdd);
		}
		
		m_entities.put(toAdd, amount);
		return true;
	}
	
	public boolean HasEntity(AEntity entity)
	{
		return m_entities.containsKey(entity);
	}
	
	public AEntity[] GetEntities()
	{
		AEntity[] toRet = new AEntity[m_entities.size()];
		m_entities.keySet().toArray(toRet);
		return toRet;
	}
	
	public int GetAmountOfEntity(AEntity entity)
	{
		return m_entities.containsKey(entity) ? m_entities.get(entity) : 0;
	}
	
	public int GetNumVisibleExits(ACharacter viewer)
	{
		int toRet = 0;
		
		for (Exit e : m_exits)
		{
			if (e.GetVisibility() <= viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
				toRet++;
		}
		
		return toRet;
	}
	
	public void RemoveExit(String dir)
	{
		if (HasExit(dir))
		{
			for (int i = 0; i < m_exits.size(); i++)
			{
				if (m_exits.get(i).GetDir().equalsIgnoreCase(dir))
				{
					m_exits.remove(i);
					break;
				}
			}
		}
	}
	
	public boolean AddExit(Exit exit)
	{
		if (HasExit(exit.GetDir()))
			return false;
		m_exits.add(exit);
		return true;
	}
	
	public boolean HasExit(String dir)
	{
		for (Exit e : m_exits)
		{
			if (e.GetDir().equalsIgnoreCase(dir))
				return true;
		}
		return false;
	}

	public Exit GetExit(String dir)
	{
		for (Exit e : m_exits)
		{
			if (e.GetDir().equalsIgnoreCase(dir))
				return e;
		}
		return null;
	}
}
