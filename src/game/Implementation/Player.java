package game.Implementation;

import game.ACharacter;
import game.Attribute;
import game.AttributeLayer;
import game.KaoticWorldEngine;

public class Player extends ACharacter
{
	public Player(String name, String desc, KaoticWorldEngine engine, AttributeLayer attrs)
	{
		super(name, desc, attrs);
		GetVisibilityAttribute().AddLayer("Player", Attribute.MAX_VALUE);
	}
	
	public void SetName(String name)
	{
		m_name = name;
	}
	
	public void SetDesc(String desc)
	{
		m_desc = desc;
		m_longDesc = m_name + " - " + m_desc;
	}
	
	public String GetLongDesc()
	{
		return super.GetLongDesc() + "\n" + GetFuzzyCarryingDesc();
	}
	
	public String GetFuzzyCarryingDesc()
	{
		String desc = "few items.";
		if (m_backpack.GetCarrying() >= m_backpack.GetCarryingCapacity() * .7f)
			desc = "quite a lot of items.";
		else if (m_backpack.GetCarrying() >= m_backpack.GetCarryingCapacity() * .35f)
			desc = "a moderate quantity of items.";
		
		return "You are carrying " + desc;
	}
}
