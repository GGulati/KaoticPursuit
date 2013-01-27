package game;

import game.Implementation.Armor;
import game.Implementation.ArmorLocation;
import game.Implementation.Container;
import game.Implementation.Decoration;
import game.Implementation.NPC;
import game.Implementation.Player;
import game.Implementation.VisibilityModifyingItem;
import game.Implementation.Weapon;
import game.Implementation.WeaponLocation;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class KaoticWorldEngine
{
	public static final float NAME_THRESHOLD = .10f;
	
	public Player HumanPlayer;
	public NPC Conversing;
	public ArrayList<Room> Rooms;
	public int CurrentRoomIndex;
	public HashMap<String, AItem> AllowedItems;
	public HashMap<String, Integer> Flags;
	public IVerbNounParser Parser;
	
	public void Init() throws Exception
	{
		Rooms = new ArrayList<Room>();
		AllowedItems = new HashMap<String, AItem>();
		Flags = new HashMap<String, Integer>();
		CurrentRoomIndex = 0;
		
		LoadItems("items.xml");
		LoadFlags("flags.xml");
		LoadWorld("world.xml");
		LoadPlayer("player.xml");
		//CreateItems();
		//CreateWorld();
	}
	private void LoadPlayer(String path) throws Exception
	{
		InputStream in = new FileInputStream("data/" + path);
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		
		String playerName = null, playerDesc = null;
		Float[] attrs = new Float[Attributes.values().length];
	
		while (reader.hasNext())
		{
			e = reader.nextEvent();
		
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();
	
				if (name.contentEquals("player"))
				{
					if (started)
						throw new Exception("Nodes of type 'Player' cannot be nested.");
					started = true;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("name"))
							playerName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("desc"))
							playerDesc = attribute.getValue();
					}
					
					if (playerName == null || playerDesc == null)
						throw new Exception("The Player must have a name and a description.");
				}
				else if (!started)
					throw new Exception("A node of type 'Player' must be instantiated as the parent node.");
				else if (name.contentEquals("attribute"))
				{
					float attrVal = -1;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("value") || attribute.getName().toString().equalsIgnoreCase("base"))
							attrVal = Float.parseFloat(attribute.getValue());
					}
					if (attrVal < 0)
						throw new Exception("Attributes must be initialized.");
					
					e = reader.nextEvent();
					attrs[IndexFromString(Attributes.valueOf(e.asCharacters().toString()))] = attrVal;
				}
			}
		}
		HumanPlayer = new Player(playerName, playerDesc, this, new AttributeLayer(attrs));
	}
	private void LoadFlags(String path) throws Exception
	{
		InputStream in = new FileInputStream("data/" + path);
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();
	
				if (name.contentEquals("flags"))
				{
					if (started)
						throw new Exception("Nodes of type 'Flags' cannot be nested.");
					started = true;
				}
				else if (!started)
					throw new Exception("A node of type 'Flags' must be instantiated as the parent node.");
				else if (name.contentEquals("flag"))
				{					
					Integer val = 0;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("value"))
							val = Integer.parseInt(attribute.getValue());
					}
					
					e = reader.nextEvent();
					Flags.put(e.asCharacters().getData(), val);
				}
			}
		}
	}
	private void LoadItems(String path) throws Exception
	{
		InputStream in = new FileInputStream("data/" + path);
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;

		float visibility;
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();
	
				visibility = AEntity.DEFAULT_VISIBILITY;
				if (name.contentEquals("items"))
				{
					if (started)
						throw new Exception("Nodes of type 'Items' cannot be nested.");
					started = true;
				}
				else if (!started)
					throw new Exception("A node of type 'Items' must be instantiated as the parent node.");
				else if (name.contentEquals("decoration"))
				{
					String desc = null, longDesc = null, itemName = null;
					float weight = -1;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("longdesc"))
							longDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("weight"))
							weight = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
					}
					
					if (weight < 0 || desc == null)
						throw new Exception("All items must have a weight and description.");
					
					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().toString();
					}
					if (GetItem(itemName) != null)
						throw new Exception("Each item's name must be unique.");
					
					if (longDesc == null)
						AllowedItems.put(itemName, new Decoration(itemName, desc, weight, visibility));
					else
						AllowedItems.put(itemName, new Decoration(itemName, desc, longDesc, weight, visibility));
				}
				else if (name.contentEquals("vismod"))
				{
					String desc = null, longDesc = null, itemName = null;
					float weight = -1, visMod = 0;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("longdesc"))
							longDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("weight"))
							weight = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().startsWith("visMod"))
							visMod = Float.parseFloat(attribute.getValue());
					}
					
					if (weight < 0 || desc == null)
						throw new Exception("All items must have a weight and description.");
					if (visMod == 0)
						throw new Exception("Visibility modifying items must modify visibility.");
					
					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().toString();
					}
					if (GetItem(itemName) != null)
						throw new Exception("Each item's name must be unique.");
					
					if (longDesc == null)
						AllowedItems.put(itemName, new VisibilityModifyingItem(itemName, desc, weight, visibility, visMod));
					else
						AllowedItems.put(itemName, new VisibilityModifyingItem(itemName, desc, longDesc, weight, visibility, visMod));
				}
				else if (name.contentEquals("container"))
				{
					String desc = null, longDesc = null, itemName = null;
					float weight = -1, maxCarrying = -1;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("longdesc"))
							longDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("weight"))
							weight = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("capacity"))
							maxCarrying = Float.parseFloat(attribute.getValue());
					}
					
					if (weight < 0 || desc == null)
						throw new Exception("All items must have a weight and description.");
					if (maxCarrying < 0)
						throw new Exception("All containers must have a max weight capacity.");

					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().toString();
					}
					if (GetItem(itemName) != null)
						throw new Exception("Each item's name must be unique.");
					
					if (longDesc == null)
						AllowedItems.put(itemName, new Container(itemName, desc, weight, maxCarrying, visibility));
					else
						AllowedItems.put(itemName, new Container(itemName, desc, longDesc, weight, maxCarrying, visibility));
				}
				else if (name.contentEquals("weapon"))
				{
					String desc = null, longDesc = null, itemName = null;
					float weight = -1, mod = -1, maxMod = -1;
					WeaponLocation loc = WeaponLocation.Primary;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("longdesc"))
							longDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("weight"))
							weight = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("mod") || attribute.getName().toString().equalsIgnoreCase("modifier"))
							mod = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("maxmod") || attribute.getName().toString().equalsIgnoreCase("maxmodifier"))
							maxMod = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("location") || attribute.getName().toString().equalsIgnoreCase("attachedto"))
							loc = WeaponLocation.valueOf(attribute.getValue());
					}
					
					if (weight < 0 || desc == null)
						throw new Exception("All items must have a weight and description.");
					if (mod <= 0 || maxMod <= 0)
						throw new Exception("Weapons must have a modifier greater than 0 and a max modifier greater than 0.");

					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().toString();
					}
					if (GetItem(itemName) != null)
						throw new Exception("Each item's name must be unique.");
					
					if (longDesc == null)
						AllowedItems.put(itemName, new Weapon(itemName, desc, loc, weight, mod, maxMod, visibility));
					else
						AllowedItems.put(itemName, new Weapon(itemName, desc, longDesc, loc, weight, mod, maxMod, visibility));
				}
				else if (name.contentEquals("armor"))
				{
					String desc = null, longDesc = null, itemName = null;
					float weight = -1, mod = -1, maxMod = -1;
					ArmorLocation loc = ArmorLocation.Chest;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							desc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("longdesc"))
							longDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("weight"))
							weight = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("mod") || attribute.getName().toString().equalsIgnoreCase("modifier"))
							mod = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("maxmod") || attribute.getName().toString().equalsIgnoreCase("maxmodifier"))
							maxMod = Float.parseFloat(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("location") || attribute.getName().toString().equalsIgnoreCase("attachedto"))
							loc = ArmorLocation.valueOf(attribute.getValue());
					}
					
					if (weight < 0 || desc == null)
						throw new Exception("All items must have a weight and description.");
					if (mod <= 0 || maxMod <= 0)
						throw new Exception("Armors must have a modifier greater than 0 and a max modifier greater than 0.");

					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().toString();
					}
					if (GetItem(itemName) != null)
						throw new Exception("Each item's name must be unique.");
					
					if (longDesc == null)
						AllowedItems.put(itemName, new Armor(itemName, desc, loc, weight, mod, maxMod, visibility));
					else
						AllowedItems.put(itemName, new Armor(itemName, desc, longDesc, loc, weight, mod, maxMod, visibility));
				}
			}
		}
	}
	private void LoadWorld(String path) throws Exception
	{
		InputStream in = new FileInputStream("data/" + path);
		XMLEventReader reader =  XMLInputFactory.newInstance().createXMLEventReader(in);
		XMLEvent e;
		String name;
		boolean started = false;
		StartElement start;
		EndElement end;
		
		Room room = null;
		ArrayList<Container> containers = new ArrayList<Container>();
		ArrayList<Boolean> itemsAreCont = new ArrayList<Boolean>();
		
		while (reader.hasNext())
		{
			e = reader.nextEvent();
			
			if (e.isStartElement())
			{
				start = e.asStartElement();
				name = start.getName().getLocalPart().toLowerCase();
	
				if (name.contentEquals("world"))
				{
					if (started)
						throw new Exception("Nodes of type 'World' cannot be nested.");
					started = true;
				}
				else if (!started)
					throw new Exception("A node of type 'World' must be instantiated as the parent node.");
				else if (name.contentEquals("room"))
				{
					if (room != null)
						throw new Exception("Rooms cannot be nested inside rooms!");
					if (containers.size() > 0)
						throw new Exception("Rooms cannnot be nested inside containers!");
					
					String roomDesc = null, roomName = null;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("desc"))
							roomDesc = attribute.getValue();
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							roomName = attribute.getValue();
					}
					if (roomDesc == null || roomName == null)
						throw new Exception("Each room needs a description and a name.");
					room = new Room(roomName, roomDesc);
					Rooms.add(room);
				}
				else if (name.contentEquals("exit"))
				{
					int fromIndex = -1, toIndex = -1;
					float visibility = 0;
					if (room != null)
						fromIndex = Rooms.size() - 1;
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("from"))
							fromIndex = Integer.parseInt(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("to"))
							toIndex = Integer.parseInt(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("visibility"))
							visibility = Float.parseFloat(attribute.getValue());
					}
					if (fromIndex < 0 || toIndex < 0 || fromIndex >= Rooms.size() || toIndex >= Rooms.size())
						throw new Exception("Each exit must lead to and from a valid room.");

					e = reader.nextEvent();
					Rooms.get(fromIndex).AddExit(new Exit(e.asCharacters().getData(), Rooms.get(toIndex), visibility));
				}
				else if (name.contentEquals("npc"))
				{
					if (room == null)
						throw new Exception("Each NPC needs to belong to a room.");
					e = reader.nextEvent();
					room.AddEntity(new NPC(e.asCharacters().getData(), this));
				}
				else if (name.contentEquals("item"))
				{
					if (room == null)
						throw new Exception("Each Item needs to belong to a room.");
					
					int amount = 1;
					String itemName = null;
					
					Iterator<Attribute> attributes = start.getAttributes();
					while (attributes.hasNext())
					{
						Attribute attribute = attributes.next();
						if (attribute.getName().toString().equalsIgnoreCase("amount"))
							amount = Integer.parseInt(attribute.getValue());
						else if (attribute.getName().toString().equalsIgnoreCase("name"))
							itemName = attribute.getValue();
					}
					if (amount < 1)
						throw new Exception("You can only have positive amounts of items.");
					
					if (itemName == null)
					{
						e = reader.nextEvent();
						itemName = e.asCharacters().getData();
					}
					AItem toAdd = GetItem(itemName);
					if (toAdd != null)
					{
						toAdd = toAdd.Copy();
						if (toAdd instanceof Container)
						{
							itemsAreCont.add(true);
							containers.add((Container)toAdd);
							room.AddItems(toAdd, amount);
						}
						else
						{
							itemsAreCont.add(false);
							if (containers.size() == 0)
								room.AddItems(toAdd, amount);
							else
								containers.get(containers.size() - 1).AddItem(toAdd, amount);
						}
					}
					else
						itemsAreCont.add(false);
				}
			}
			else if (e.isEndElement())
			{
				end = e.asEndElement();
				name = end.getName().getLocalPart().toLowerCase();
				
				if (name.contentEquals("room"))
				{
					if (room == null)
						throw new Exception("Formatting error with Room.");
					room = null;
					containers.clear();
				}
				else if (name.contains("item"))
				{
					if (itemsAreCont.get(itemsAreCont.size() - 1))
						containers.remove(containers.size() - 1);
					itemsAreCont.remove(itemsAreCont.size() - 1);
				}
			}
		}
	}
	private void CreatePlayer() throws Exception
	{
		HumanPlayer = new Player("Skorr", "A young adventurer in the land of Kaotic.", this, new AttributeLayer(new Float[Attributes.values().length]));
	}
	private void CreateFlags()
	{
		Flags.put("TestQuestState", 0);
	}
	private void CreateItems()
	{
		AllowedItems.put("Gold", new Decoration("Gold", "A bright shiny metal valued in commerce.", .001f, 5));
	}
	private void CreateWorld() throws Exception
	{		
		Room r1 = new Room("The Second Pint", "A busy tavern with a door to the east.");
		Room r2 = new Room("Main Street", "A quiet street with a tavern on the west side that continues into the north and the south.");
		Room r3 = new Room("North Street", "A busy hub of commerce with a shop on the east side and a refined bar on the west side, with Main Street to the south.");
		Room r4 = new Room("The Dry Bottle", "A well-known bar that serves well-aged wine to the wealthy merchants... and to experienced adventurers. It has a door on the east side.");
		Room r5 = new Room("The Snake and the Merchant", "A reputable weapons store whose owner deals in enchanted arms and armor. Its entry can be found on the west side.");
		Room r6 = new Room("South Street", "An undeveloped area with a broken-down tavern on the east side and a thrift store in the west. Main Street lies to the north of this shadowy part of town.");
		Room r7 = new Room("The Vulgar Ape", "A so-called tavern that sells filthy swill. Most customers are literally swept off their feet by the bouncers, right through the door on the west of the ramshackle wooden construction.");
		Room r8 = new Room("The Ogre's Fist", "A infamous thrift store that sells various kinds of weapons, especially those of the illegal variety. Most customers only notice the doorway cut out of the east wall.");
		
		Exit e1 = new Exit("east", r2, 0);
		Exit e2 = new Exit("west", r1, 0);
		Exit e3 = new Exit("north", r3, 0);
		Exit e4 = new Exit("south", r6, 0);
		Exit e5 = new Exit("east", r5, 0);
		Exit e6 = new Exit("west", r4, 0);
		Exit e7 = new Exit("west", r3, 0);
		Exit e8 = new Exit("east", r3, 0);
		Exit e9 = new Exit("south", r2, 0);
		Exit e10 = new Exit("north", r2, 0);
		Exit e11 = new Exit("east", r7, 0);
		Exit e12 = new Exit("west", r8, 0);
		Exit e13 = new Exit("west", r6, 0);
		Exit e14 = new Exit("east", r6, 0);
		
		r1.AddExit(e1);
		r2.AddExit(e2);
		r2.AddExit(e3);
		r2.AddExit(e4);
		r3.AddExit(e5);
		r3.AddExit(e6);
		r5.AddExit(e7);
		r4.AddExit(e8);
		r3.AddExit(e9);
		r6.AddExit(e10);
		r6.AddExit(e11);
		r6.AddExit(e12);
		r7.AddExit(e13);
		r8.AddExit(e14);
		
		NPC npc1 = new NPC("SecondPintBarkeep", this);
		r1.AddEntity(npc1);
		
		Rooms.add(r1);
		Rooms.add(r2);
		Rooms.add(r3);
		Rooms.add(r4);
		Rooms.add(r5);
		Rooms.add(r6);
		Rooms.add(r7);
		Rooms.add(r8);	
	}
	
	public static int IndexFromString(Attributes attr)
	{
		Attributes[] attrs =  Attributes.values();
		for (int i = 0; i < attrs.length; i++)
		{
			if (attrs[i] == attr)
				return i;
		}
		return -1;
	}
	
	public Room GetCurrentRoom()
	{
		return Rooms.get(CurrentRoomIndex);
	}
	
	public AItem GetItem(String name)
	{
		for (Map.Entry<String, AItem> e : AllowedItems.entrySet())
		{
			if (e.getKey().compareToIgnoreCase(name) == 0)
				return e.getValue();
		}
		return null;
	}
	
	public AEntity FindInRoom(String[] name)
	{
		if (name.length < 1)
			return null;
		
		int index = 1, calcIndex = FirstIndexOf(name, "no");
		if (calcIndex >= 0 && name.length > 2)
		{
			String[] assign = new String[name.length - 2];
			for (int i = 0; i < name.length; i++)
			{
				if (i == calcIndex) ;
				else if (i == calcIndex + 1)
				{
					try { index = Integer.parseInt(name[i]); }
					catch (NumberFormatException e) { return null; }
				}
				else
					assign[i < calcIndex ? i : (i - 2)] = name[i];
			}
			name = assign;
		}
		
		AEntity toRet = null;
		float best = NAME_THRESHOLD, calc;
		AEntity[] all = Rooms.get(CurrentRoomIndex).GetEntities();
		int current = 0;
		
		for (AEntity e : all)
		{
			calc = e.IsReferredTo(name);
			if (calc > best)
			{
				best = calc;
				toRet = e;
				current = 1;
			}
			else if (calc == best && toRet != null && toRet.GetName().contentEquals(e.GetName()))
			{
				current++;
				if (current == index)
					return e;
			}
		}
		
		return toRet;
	}

	public void MoveToRoom(int nextIndex)
	{
		AEntity[] entities = Rooms.get(CurrentRoomIndex).GetEntities();
		for (AEntity e : entities)
		{
			if (e instanceof AItem)
				((AItem)e).StopUsing(Rooms.get(CurrentRoomIndex));
		}
		CurrentRoomIndex = nextIndex;
	}
	
	public boolean HasFlag(String name)
	{
		for (Map.Entry<String, Integer> e : Flags.entrySet())
		{
			if (e.getKey().compareToIgnoreCase(name) == 0)
				return true;
		}
		return false;
	}
	
	public Integer GetFlag(String name)
	{
		for (Map.Entry<String, Integer> e : Flags.entrySet())
		{
			if (e.getKey().compareToIgnoreCase(name) == 0)
				return e.getValue();
		}
		return 0;
	}
	
	public boolean SetFlag(String name, Integer val)
	{
		for (Map.Entry<String, Integer> e : Flags.entrySet())
		{
			if (e.getKey().compareToIgnoreCase(name) == 0)
			{
				Flags.remove(e.getKey());
				Flags.put(e.getKey(), val);
				return true;
			}
		}
		return false;
	}

	public boolean HasItem(String name)
	{
		for (Map.Entry<String, AItem> e : AllowedItems.entrySet())
		{
			if (e.getKey().compareToIgnoreCase(name) == 0)
				return true;
		}
		return false;
	}

	public static int FirstIndexOf(String[] toSearch, String searchFor)
	{
		for (int i = 0; i < toSearch.length; i++)
		{
			if (toSearch[i].contentEquals(searchFor))
				return i;
		}
		return -1;
	}
	
	public static String ConstructItemString(HashMap<AItem, Integer> input)
	{
		StringBuilder builder = new StringBuilder();
		
		for (Map.Entry<AItem, Integer> e : input.entrySet())
		{
			builder.append("\t");
			builder.append(ACharacter.EnglishNumberFromInteger(e.getValue()) + " " + e.getKey().GetName());
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	public static String[] TrawlString(String[] toTrawl, int amount)
	{
		if (toTrawl.length <= amount)
			return null;
		String[] toRet = new String[toTrawl.length - amount];
		for (int i = amount; i < toTrawl.length; i++)
			toRet[i - amount] = toTrawl[i];
		return toRet;
	}
	
	public static String[] TrawlString(String[] toTrawl, int amount, int max)
	{
		if (toTrawl.length <= amount || max > toTrawl.length)
			return null;
		String[] toRet = new String[max - amount];
		for (int i = amount; i < max; i++)
			toRet[i - amount] = toTrawl[i];
		return toRet;
	}
}