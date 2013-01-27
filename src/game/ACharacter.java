package game;

import game.Implementation.Armor;
import game.Implementation.ArmorLocation;
import game.Implementation.Container;
import game.Implementation.Weapon;
import game.Implementation.WeaponLocation;

import java.util.HashMap;
import java.util.Map;

public abstract class ACharacter extends AEntity
{
	public static final float SEARCH_THRESHOLD = .10f;
	
	protected Container m_backpack;
	protected AttributeLayer m_attr;
	private HashMap<WeaponLocation, Weapon> m_weapons;
	private HashMap<ArmorLocation, Armor> m_armors;
	
	protected ACharacter(String name, String desc, AttributeLayer defAttr)
	{
		super(0);
		m_backpack = new Container("Backpack", "An adventurer's backpack.", 0, 50, 0);
		m_name = name;
		m_desc = desc;
		m_longDesc = desc;
		m_attr = defAttr;

		m_weapons = new HashMap<WeaponLocation, Weapon>();
		m_armors = new HashMap<ArmorLocation, Armor>();
	}
	
	public void Update()
	{
		super.Update();
		m_attr.Update();
	}
	
	public float GetVisibility()
	{
		return m_attr.GetAttributeValue(Attributes.Visibility);
	}
	
	public Attribute GetVisibilityAttribute()
	{
		return m_attr.GetAttribute(Attributes.Visibility);
	}
	
	public AttributeLayer GetAttributes()
	{
		return m_attr;
	}
	
	static final int ATTR_LOW = 5, ATTR_HIGH = 15, ATTR_GOD = 25;
	public String GetLongDesc()
	{
		StringBuilder str = new StringBuilder();
		
		str.append(m_longDesc);
		
		str.append("\nYou are endowed with ");
		Attributes[] attrs = Attributes.values();
		for (int i = 0; i < attrs.length; i++)
		{
			if (i == attrs.length - 1)
				str.append(" and ");
			else if (i != 0)
				str.append(", ");
			str.append(m_attr.GetAttribute(attrs[i]).GetFuzzyDesc(ATTR_LOW, ATTR_HIGH, ATTR_GOD));
		}
		
		if (m_weapons.size() > 0)
		{
			str.append("\nEQUIPPED WEAPONS");
			for (Map.Entry<WeaponLocation, Weapon> e : m_weapons.entrySet())
			{
				str.append("\n\t");
				str.append(e.getKey() + ": " + e.getValue().GetName());
			}
		}
		if (m_armors.size() > 0)
		{
			str.append("\nEQUIPPED ARMORS");
			for (Map.Entry<ArmorLocation, Armor> e : m_armors.entrySet())
			{
				str.append("\n\t");
				str.append(e.getKey() + ": " + e.getValue().GetName());
			}
		}
		
		str.append("\n");
		str.append(m_backpack.GetLongDesc(1, this, -5));//since it is EASIER to see things in your backpack, visDiff of -5
		
		return str.toString();
	}
	
	public float GetCarrying()
	{
		return m_backpack.GetCarrying();
	}
	
	public float GetCarryingCapacity()
	{
		return m_backpack.GetCarryingCapacity();
	}
	
	public Weapon GetEquippedWeapon(WeaponLocation location)
	{
		if (m_weapons.containsKey(location))
			return m_weapons.get(location);
		return null;
	}
	
	public Armor GetEquippedArmor(ArmorLocation location)
	{
		if (m_armors.containsKey(location))
			return m_armors.get(location);
		return null;
	}
	
	public boolean Equip(Weapon weapon)
	{
		if (m_weapons.containsKey(weapon.GetWeaponLocation()) && m_weapons.get(weapon.GetWeaponLocation()) == weapon)
			return true;
		if (m_backpack.GetAmountOf(weapon) == 0)
			return false;
		if (m_weapons.containsKey(weapon.GetWeaponLocation()))
			m_weapons.remove(weapon.GetWeaponLocation());
		m_weapons.put(weapon.GetWeaponLocation(), weapon);
		return true;
	}
	public boolean Equip(Armor armor)
	{
		if (m_armors.containsKey(armor.GetArmorLocation()) && m_armors.get(armor.GetArmorLocation()) == armor)
			return true;
		if (m_backpack.GetAmountOf(armor) == 0)
			return false;
		if (m_armors.containsKey(armor.GetArmorLocation()))
			m_armors.remove(armor.GetArmorLocation());
		m_armors.put(armor.GetArmorLocation(), armor);
		return true;
	}
	
	public void UnequipWeapon(WeaponLocation location)
	{
		m_weapons.remove(location);
	}
	
	public void UnequipArmor(ArmorLocation location)
	{
		m_armors.remove(location);
	}
	
	private static final Integer BILLION = 1000000000,
								   MILLION = 1000000,
								   THOUSAND = 1000,
								   HUNDRED = 100,
								   TEN = 10;
	public static String EnglishNumberFromInteger(Integer toConvert)
	{
		return EnglishNumberFromInteger(toConvert, true);
	}
	public static String EnglishNumberFromInteger(Integer toConvert, boolean uppercase)
	{
		StringBuilder str = new StringBuilder();
		
		if (toConvert < 0)
		{
			str.append("negative ");
			toConvert = -toConvert;
		}
		else if (toConvert == 0)
			return "zero";
		
		boolean precede = false;
		if (toConvert >= BILLION)
		{
			str.append(EnglishNumberFromInteger(toConvert / BILLION, false));
			str.append(" billion");
			precede = true;
		}
		if (toConvert % BILLION >= MILLION)
		{
			if (precede)
				str.append(", ");
			str.append(EnglishNumberFromInteger((toConvert % BILLION) / MILLION, false));
			str.append(" million");
			precede = true;
		}
		if (toConvert % MILLION >= THOUSAND)
		{
			if (precede)
				str.append(", ");
			str.append(EnglishNumberFromInteger((toConvert % MILLION) / THOUSAND, false));
			str.append(" thousand");
			precede = true;
		}
		if (toConvert % THOUSAND >= HUNDRED)
		{
			if (precede)
				str.append(", ");
			str.append(EnglishNumberFromInteger((toConvert % THOUSAND) / HUNDRED, false));
			str.append(" hundred");
			precede = true;
		}
		if (toConvert % HUNDRED < 20 && toConvert % HUNDRED != 0)
		{
			if (precede)
				str.append(", ");
			switch (toConvert % HUNDRED)
			{
				case 1:
					str.append("one");
					break;
				case 2:
					str.append("two");
					break;
				case 3:
					str.append("three");
					break;
				case 4:
					str.append("four");
					break;
				case 5:
					str.append("five");
					break;
				case 6:
					str.append("six");
					break;
				case 7:
					str.append("seven");
					break;
				case 8:
					str.append("eight");
					break;
				case 9:
					str.append("nine");
					break;
				case 10:
					str.append("ten");
					break;
				case 11:
					str.append("eleven");
					break;
				case 12:
					str.append("twelve");
					break;
				case 13:
					str.append("thirteen");
					break;
				case 14:
					str.append("fourteen");
					break;
				case 15:
					str.append("fifteen");
					break;
				case 16:
					str.append("sixteen");
					break;
				case 17:
					str.append("seventeen");
					break;
				case 18:
					str.append("eighteen");
					break;
				case 19:
					str.append("nineteen");
					break;
			}
		}
		else if (toConvert % HUNDRED != 0)
		{
			if (precede)
				str.append(", ");
			switch ((toConvert % HUNDRED) / TEN)
			{
				case 2:
					str.append("twenty");
					break;
				case 3:
					str.append("thirty");
					break;
				case 4:
					str.append("fourty");
					break;
				case 5:
					str.append("fifty");
					break;
				case 6:
					str.append("sixty");
					break;
				case 7:
					str.append("seventy");
					break;
				case 8:
					str.append("eighty");
					break;
				case 9:
					str.append("ninety");
					break;
			}
			if (toConvert % TEN > 0)
			{
				str.append("-");
				str.append(EnglishNumberFromInteger(toConvert % TEN, false));
			}
		}
		
		if (uppercase)
			str.setCharAt(0, Character.toUpperCase(str.charAt(0)));
		return str.toString();
	}
	
	public int GetAmountOf(AItem item)
	{
		return m_backpack.GetAmountOf(item);
	}
	
	public AItem GetItem(String[] name)
	{
		if (m_backpack.IsReferredTo(name) > ACharacter.SEARCH_THRESHOLD)
			return m_backpack;
		return m_backpack.GetItem(name);
	}
	
	public int AddItem(AItem item, int amount)
	{
		return m_backpack.AddItem(item, amount);
	}
	public boolean AddItem(AItem item)
	{
		return m_backpack.AddItem(item);
	}
	
	public int RemoveItem(AItem item, int amount)
	{
		int toRet = m_backpack.RemoveItem(item, amount);
		if (m_backpack.GetAmountOf(item) == 0)
		{
			if (m_weapons.containsValue(item))
				m_weapons.remove(((Weapon)item).GetWeaponLocation());
			else if (m_armors.containsValue(item))
				m_armors.remove(((Armor)item).GetArmorLocation());
		}
		return toRet;
	}
	public boolean RemoveItem(AItem item)
	{
		boolean toRet = m_backpack.RemoveItem(item);
		if (m_backpack.GetAmountOf(item) == 0)
		{
			if (m_weapons.containsValue(item))
				m_weapons.remove(((Weapon)item).GetWeaponLocation());
			else if (m_armors.containsValue(item))
				m_armors.remove(((Armor)item).GetArmorLocation());
		}
		return toRet;
	}
}
