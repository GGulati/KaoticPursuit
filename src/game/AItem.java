package game;

public abstract class AItem extends AEntity
{
	protected boolean m_using;
	boolean m_isUsable;
	float m_weight;
	
	protected AItem(String name, String desc, float weight, float visibility)
	{
		this(name, desc, weight, visibility, false);
	}
	protected AItem(String name, String desc, float weight, float visibility, boolean isUsable)
	{
		super(visibility);
		m_isUsable = isUsable;
		m_using = false;
		m_weight = weight < 0 ? 0 : weight;
		m_name = name;
		m_desc = desc;
		m_longDesc = desc;
	}
	
	public float GetWeight()
	{
		return m_weight;
	}
	
	public boolean IsUsable()
	{
		return m_isUsable;
	}
	
	public boolean IsInUse()
	{
		return m_using;
	}
	
	public void Use(Room usedIn, ACharacter user) 
	{
		m_using = !m_using;
	}
	
	public void StopUsing(Room usedIn)
	{
		m_using = false;
	}
	
	public abstract AItem Copy();
}
