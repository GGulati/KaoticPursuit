package game.Implementation;

import game.ACharacter;
import game.AItem;
import game.Room;

public class Decoration extends AItem
{
	public Decoration(String name, String desc, float weight, float visibility)
	{
		super(name, desc, weight, visibility);
	}
	public Decoration(String name, String desc, String longDesc, float weight, float visibility)
	{
		super(name, desc, weight, visibility);
		m_longDesc = longDesc;
	}
	
	@Override
	public AItem Copy()
	{
		return new Decoration(m_name, m_desc, m_longDesc, GetWeight(), GetVisibility());
	}
}
