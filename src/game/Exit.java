package game;


public class Exit
{
	String m_dir;
	Room m_leadsTo;
	Attribute m_visible;
	
	public Exit(String dir, Room leadsTo, float visible)
	{
		m_dir = dir;
		m_leadsTo = leadsTo;
		m_visible = new Attribute(Attributes.Visibility, visible);
	}
	
	public String GetDir()
	{
		return m_dir;
	}
	
	public Room GetRoomLedTo()
	{
		return m_leadsTo;
	}
	
	public float GetVisibility()
	{
		return m_visible.GetValue();
	}
	
	public Attribute GetVisibilityAttribute()
	{
		return m_visible;
	}
}
