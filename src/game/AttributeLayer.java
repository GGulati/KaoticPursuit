package game;


import java.util.HashMap;
import java.util.Map;

public class AttributeLayer
{
	HashMap<Attributes, Attribute> m_attributes;
	
	public AttributeLayer(Float[] bases) throws Exception
	{
		Attributes[] arr = Attributes.values();
		if (bases.length != arr.length)
			throw new Exception("Error with init.");
		m_attributes = new HashMap<Attributes, Attribute>();
		for (int i = 0; i < bases.length; i++)
			m_attributes.put(arr[i], new Attribute(arr[i], bases[i]));
	}
	protected AttributeLayer()
	{
		m_attributes = new HashMap<Attributes, Attribute>();
	}
	
	public void Update()
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
			e.getValue().Update();
	}
	
	public Attribute GetAttribute(Attributes attr)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
				return e.getValue();
		}
		return null;
	}
	
	public float GetAttributeValue(Attributes attr)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
				return e.getValue().GetValue();
		}
		return 0;
	}
	
	public boolean HasAttribute(Attributes attr)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
				return true;
		}
		return false;
	}
	
	public boolean HasAttributeLayer(Attributes attr, String layer)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
				return e.getValue().HasLayer(layer);
		}
		return false;
	}
	
	public boolean AddLayerToAttribute(Attributes attr, String layer, float val)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
			{
				if (e.getValue().HasLayer(layer))
					try { e.getValue().RemoveLayer(layer); } catch (Exception e1) { }
				e.getValue().AddLayer(layer, val);
				return true;
			}
		}
		return false;
	}
	
	public boolean RemoveLayerFromAttribute(Attributes attr, String layer)
	{
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
		{
			if (e.getKey() == attr)
			{
				if (e.getValue().HasLayer(layer))
				{
					try { e.getValue().RemoveLayer(layer); } catch (Exception e1) { }
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public AttributeLayer Copy()
	{		
		AttributeLayer toRet = new AttributeLayer();
		for (Map.Entry<Attributes, Attribute> e : m_attributes.entrySet())
			toRet.m_attributes.put(e.getKey(), e.getValue().Copy());
		
		return toRet;
	}
}
