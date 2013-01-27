package game;


public abstract class AEntity
{
	public static final float DEFAULT_VISIBILITY = 5;
	
	protected String m_name, m_desc, m_longDesc;
	Attribute m_visibility;
	
	protected AEntity(float visibility)
	{
		m_visibility = new Attribute(Attributes.Visibility, visibility);
	}
	
	public float IsReferredTo(String[] tried)
	{
		String[] nameLookAt = m_name.split(" ");
		String[] descLookAt = m_desc.split(" ");
		
		boolean found;
		float total = 0;
		for (String str : tried)
		{
			found = false;
			for (String n : nameLookAt)
			{
				if (n.compareToIgnoreCase(str) == 0)
				{
					total++;
					found = true;
					break;
				}
			}
			if (!found)
			{
				for (String n : descLookAt)
				{
					if (n.compareToIgnoreCase(str) == 0)
					{
						total++;
						break;
					}
				}
			}
		}
		return total / (nameLookAt.length + descLookAt.length / 2);//weighting description over name
	}
	
	public void Update()
	{
		m_visibility.Update();
	}
	
	public Attribute GetVisibilityAttribute()
	{
		return m_visibility;
	}
	
	public String GetName()
	{
		return m_name == null ? "" : m_name;
	}
	
	public String GetDesc()
	{
		return m_desc == null ? "" : m_desc;
	}
	
	public String GetLongDesc()
	{
		return m_longDesc == null ? "" : m_longDesc;
	}
	
	public float GetVisibility()
	{
		return m_visibility.GetValue();
	}
}
