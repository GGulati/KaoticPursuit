package game.Implementation;

import game.ACharacter;
import game.AItem;
import game.Room;

public class Weapon extends AItem
{
	float m_bonusDmgMod, m_maxDmgMod;
	WeaponLocation m_held;
	
	public Weapon(String name, String desc, WeaponLocation held, float weight, float bonusDmgMod, float maxDmgMod, float visibility)
	{
		this(name, desc, desc, held, weight, bonusDmgMod, maxDmgMod, visibility);
	}
	public Weapon(String name, String desc, String longDesc, WeaponLocation held, float weight, float bonusDmgMod, float maxDmgMod, float visibility)
	{
		super(name, desc, weight, visibility);
		m_longDesc = longDesc;
		m_bonusDmgMod = bonusDmgMod;
		m_maxDmgMod = maxDmgMod;
		m_held = held;
	}

	public float CalcAdditionalDamage(float baseDmg)
	{
		return baseDmg * m_bonusDmgMod > m_maxDmgMod ? m_maxDmgMod : baseDmg * m_bonusDmgMod;
	}
	
	public WeaponLocation GetWeaponLocation()
	{
		return m_held;
	}
	
	@Override
	public AItem Copy()
	{
		return new Weapon(m_name, m_desc, m_longDesc, m_held, GetWeight(), m_bonusDmgMod, m_maxDmgMod, GetVisibility());
	}
}
