package game.Implementation;

import game.ACharacter;
import game.AEntity;
import game.AItem;
import game.Room;

public class VisibilityModifyingItem extends AItem
{
	static int Instances = 0;
	int m_instance;
	float m_visMod;
	
	public VisibilityModifyingItem(String name, String desc, float weight, float visibility, float visibilityMod)
	{
		this(name, desc, desc, weight, visibility, visibilityMod);
	}
	public VisibilityModifyingItem(String name, String desc, String longDesc, float weight, float visibility, float visibilityMod)
	{
		super(name, desc, weight, visibility, true);
		Instances++;
		m_longDesc = longDesc;
		m_instance = Instances;
		m_visMod = visibilityMod;
	}

	@Override
	public void Use(Room usedIn, ACharacter user)
	{
		super.Use(usedIn, user);

		AEntity[] entities = usedIn.GetEntities();
		if (m_using)
		{
			for (AEntity e : entities)
				e.GetVisibilityAttribute().AddLayer(m_name + m_instance, m_visMod);
		}
		else
		{
			for (AEntity e : entities)
			{
				if (e.GetVisibilityAttribute().HasLayer(m_name + m_instance))
					try { e.GetVisibilityAttribute().RemoveLayer(m_name + m_instance); } catch (Exception e1) { }
			}
		}
	}
	
	public void StopUsing(Room usedIn)
	{
		super.StopUsing(usedIn);
		
		AEntity[] entities = usedIn.GetEntities();
		for (AEntity e : entities)
		{
			if (e.GetVisibilityAttribute().HasLayer(m_name + m_instance))
				try { e.GetVisibilityAttribute().RemoveLayer(m_name + m_instance); } catch (Exception e1) { }
		}
	}

	@Override
	public AItem Copy()
	{
		return new VisibilityModifyingItem(m_name, m_desc, m_longDesc, GetWeight(), GetVisibility(), m_visMod);
	}

}
