package game;


import java.util.HashMap;
import java.util.Map;

public class Attribute
{
	public static float MAX_VALUE = 10000;
	Attributes m_type;
	float m_base, m_total;
	HashMap<String, Float> m_layered;
	
	public Attribute(Attributes type, float base)
	{
		m_type = type;
		m_base = base;
		m_total = m_base;
		m_layered = new HashMap<String, Float>();
	}
	
	public void Update()
	{
		m_total = m_base;
		for (Map.Entry<String, Float> e : m_layered.entrySet())
		{
			m_total += e.getValue();
			if (m_total > MAX_VALUE)
			{
				m_total = MAX_VALUE;
				break;
			}
		}
	}
	
	public String GetFuzzyDesc(int low, int high, int god)
	{
		if (m_total < low)
			return "a low " + m_type.toString().toLowerCase();
		else if (m_total > god)
			return "godly " + m_type.toString().toLowerCase();
		else if (m_total > high)
			return "high " + m_type.toString().toLowerCase();
		else
			return "normal " + m_type.toString().toLowerCase();
	}
	
	public Attributes GetType()
	{
		return m_type;
	}
	
	public float GetValue()
	{
		return m_total;
	}
	
	public float GetBaseValue()
	{
		return m_base;
	}
	
	public boolean AddLayer(String source, float mod)
	{
		if (HasLayer(source))
			try { RemoveLayer(source); } catch (Exception e) { }
		m_layered.put(source, mod);
		return true;
	}
	
	public boolean HasLayer(String source)
	{
		for (Map.Entry<String, Float> e : m_layered.entrySet())
		{
			if (e.getKey().contentEquals(source))
				return true;
		}
		return false;
	}
	
	public boolean RemoveLayer(String source) throws Exception
	{
		for (Map.Entry<String, Float> e : m_layered.entrySet())
		{
			if (e.getKey().contentEquals(source))
			{
				m_layered.remove(e.getKey());
				return true;
			}
		}
		return false;
	}
	
	public Attribute Copy()
	{
		Attribute toRet = new Attribute(m_type, m_base);
		for (Map.Entry<String, Float> e : m_layered.entrySet())
			toRet.m_layered.put(e.getKey(), e.getValue().floatValue());
		return toRet;
	}
}
