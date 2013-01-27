package game.Implementation;

import game.ACharacter;
import game.AItem;
import game.Room;

public class Armor extends AItem
{
	float m_bonusDefenseMod, m_maxDefenseMod;
	ArmorLocation m_attachedTo;

	public Armor(String name, String desc, ArmorLocation attachedTo, float weight, float bonusDefenseMod, float maxDefenseMod, float visibility)
	{
		this(name, desc, desc, attachedTo, weight, bonusDefenseMod, maxDefenseMod, visibility);
	}
	public Armor(String name, String desc, String longDesc, ArmorLocation attachedTo, float weight, float bonusDefenseMod, float maxDefenseMod, float visibility)
	{
		super(name, desc, weight, visibility);
		m_longDesc = longDesc;
		m_bonusDefenseMod = bonusDefenseMod;
		m_maxDefenseMod = maxDefenseMod;
		m_attachedTo = attachedTo;
	}
	
	public float CalcPreventedDamage(float baseDmg)
	{
		return baseDmg * m_bonusDefenseMod > m_maxDefenseMod ? m_maxDefenseMod : baseDmg * m_bonusDefenseMod; 
	}
	
	public ArmorLocation GetArmorLocation()
	{
		return m_attachedTo;
	}
	
	@Override
	public AItem Copy()
	{
		return new Armor(m_name, m_desc, m_longDesc, m_attachedTo, GetWeight(), m_bonusDefenseMod, m_maxDefenseMod, GetVisibility());
	}
}
