package game.Implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import game.ACharacter;
import game.AItem;
import game.Attributes;
import game.Room;

public class Container extends AItem
{
	private HashMap<AItem, Integer> m_items;
	float m_carrying, m_maxCarrying;

	public Container(String name, String desc, float weight, float maxCarrying, float visibility)
	{
		this(name, desc, desc, weight, maxCarrying, visibility);
	}
	
	public Container(String name, String desc, String longDesc, float weight, float maxCarrying, float visibility)
	{
		super(name, desc, weight, visibility);
		m_longDesc = longDesc;
		m_items = new HashMap<AItem, Integer>();
		m_carrying = 0;
		m_maxCarrying = maxCarrying;
	}
	
	public float GetWeight()
	{
		return super.GetWeight() + m_carrying;
	}
	
	public String GetLongDesc()
	{
		return m_longDesc;
	}
	public String GetLongDesc(int tabbing, ACharacter viewer, float difficultyOffset)
	{
		StringBuilder str = new StringBuilder();
		
		if (GetNumVisibleItems(viewer, difficultyOffset) > 0)
		{
			for (int i = 1; i < tabbing; i++)
				str.append("\t");
			if (tabbing > 0)
				str.append("  ");
			str.append(m_name + ":");
		}
		else
			str.append("  An empty " + m_name);
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			float diff = e.getKey().GetVisibility() + difficultyOffset + GetVisibility();
			if (diff > viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
				continue;
			str.append("\n");
			for (int i = 0; i < tabbing; i++)
				str.append("\t");
			str.append(ACharacter.EnglishNumberFromInteger(e.getValue()));
			str.append(" ");
			if (e.getKey() instanceof Container)
				str.append(((Container)e.getKey()).GetLongDesc(tabbing + 1, viewer, difficultyOffset + GetVisibility()));
			else
				str.append(e.getKey().GetName());
		}
		
		return str.toString();
	}
	
	public int GetNumVisibleItems(ACharacter viewer, float visOffset)
	{
		visOffset += GetVisibility();
		int toRet = 0;
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			if (e.getKey().GetVisibility() + visOffset <= viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
				toRet++;
		}
		
		return toRet;
	}
	
	public float GetCarrying()
	{
		return m_carrying;
	}
	
	public float GetCarryingCapacity()
	{
		return m_maxCarrying;
	}
	
	public void SetCarryingCapacity(float newCapacity)
	{
		if (newCapacity > m_maxCarrying)
			m_maxCarrying = newCapacity;
	}
	
	public int GetAmountOf(AItem item)
	{
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			if (e.getKey().GetName().contentEquals(item.GetName()))
				return e.getValue();
		}
		return 0;
	}
	
	public AItem GetItem(String[] name)
	{
		if (name.length < 1)
			return null;
		
		AItem toRet = null;
		float best = ACharacter.SEARCH_THRESHOLD, calc;
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			calc = e.getKey().IsReferredTo(name);
			if (calc > best)
			{
				best = calc;
				toRet = e.getKey();
			}
		}
		return toRet;
	}
	
	public int AddItem(AItem item, int amount)
	{
		int max = (int)((m_maxCarrying - m_carrying) / item.GetWeight());
		int toAdd = amount < max ? amount : max;
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			if (e.getKey().GetName().contentEquals(item.GetName()) && !(item instanceof Container))
			{
				m_carrying -= e.getValue() * e.getKey().GetWeight();
				toAdd += e.getValue();
				m_items.remove(e.getKey());
				break;
			}
		}
		
		m_carrying += toAdd * item.GetWeight();
		m_items.put(item, toAdd);
		
		return toAdd;
	}
	public boolean AddItem(AItem item)
	{
		if (m_carrying + item.GetWeight() <= m_maxCarrying)
		{
			m_carrying += item.GetWeight();
			int amount = 1;
			
			for (Map.Entry<AItem, Integer> e : m_items.entrySet())
			{
				if (e.getKey().GetName().contentEquals(item.GetName()) && !(item instanceof Container))
				{
					m_carrying -= e.getValue() * e.getKey().GetWeight();
					amount += e.getValue();
					m_items.remove(e.getKey());
					break;
				}
			}		
			
			m_items.put(item, amount);
			return true;
		}
		return false;
	}
	
	public int RemoveItem(AItem item, int amount)
	{
		if (GetAmountOf(item) > 0)
		{
			int current = GetAmountOf(item);
			int toRemove = current < amount ? current : amount;
			
			m_carrying -= toRemove * item.GetWeight();
			if (m_carrying < 0)
				m_carrying = 0;
			
			for (Map.Entry<AItem, Integer> e : m_items.entrySet())
			{
				if (e.getKey().GetName().contentEquals(item.GetName()))
				{
					m_items.remove(e.getKey());
					if (current - toRemove > 0)
						m_items.put(e.getKey(), current - toRemove);
					return toRemove;
				}
			}
		}
		return 0;
	}
	public boolean RemoveItem(AItem item)
	{
		int amount = GetAmountOf(item) - 1;
		if (amount >= 0)
		{
			m_carrying -= item.GetWeight();
			if (m_carrying < 0)
				m_carrying = 0;
			
			for (Map.Entry<AItem, Integer> e : m_items.entrySet())
			{
				if (e.getKey() == item)
				{
					m_items.remove(e.getKey());
					if (amount > 0)
						m_items.put(e.getKey(), amount);
					return true;
				}
			}
		}
		return false;
	}
	
	public AItem[] GetItems()
	{
		ArrayList<AItem> cont = new ArrayList<AItem>();
		
		for (AItem a : m_items.keySet())
			cont.add(a);
		
		AItem[] toRet = new AItem[cont.size()];
		cont.toArray(toRet);
		return toRet;
	}
	
	public AItem[] GetVisibleItems(ACharacter viewer, float visOffset)
	{
		visOffset += GetVisibility();
		ArrayList<AItem> cont = new ArrayList<AItem>();
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
		{
			if (e.getKey().GetVisibility() + visOffset <= viewer.GetAttributes().GetAttributeValue(Attributes.Perception))
				cont.add(e.getKey());
		}
		
		AItem[] toRet = new AItem[cont.size()];
		cont.toArray(toRet);
		return toRet;
	}
	
	@Override
	public AItem Copy()
	{
		Container toRet = new Container(m_name, m_desc, m_longDesc, GetWeight(), m_maxCarrying, GetVisibility());
		
		for (Map.Entry<AItem, Integer> e : m_items.entrySet())
			toRet.AddItem(e.getKey().Copy(), e.getValue());
		
		return toRet;
	}
}
