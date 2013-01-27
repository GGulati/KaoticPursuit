package game;

import java.awt.Color;
import java.util.HashMap;

import game.Implementation.Armor;
import game.Implementation.NPC;
import game.Implementation.NPCDialogueExchange;
import game.Implementation.NPCDialogueResponse;
import game.Implementation.Weapon;
import game.Implementation.Container;
import gameUI.IInput;
import gameUI.IOutput;

public class KaoticGame implements IInput
{
	boolean m_init = false, m_exclusiveTalk = false,
			m_waitForName = false, m_waitForDesc = false;
	
	KaoticWorldEngine m_engine;
	KaoticCombatEngine m_combat;
	
	boolean m_inMainMenu = false, m_inOpt = false;
	IOutput m_out;
	
	public KaoticGame() throws Exception
	{
		m_engine = new KaoticWorldEngine();
		m_combat = new KaoticCombatEngine();
	}
	
	public void Init(IOutput out, IVerbNounParser parser) throws Exception
	{
		if (m_init)
			throw new Exception("Already initialized.");
		
		m_out = out;
		m_engine.Parser = parser;
		
		m_engine.Init();
		m_combat.Init();
		
		m_init = true;
		
		ShowMainMenu();
	}
	private void ShowMainMenu()
	{
		m_out.TryPrintLine(SPLASH_SCREEN, Color.RED, true, false, false);
		m_out.PrintLine();
		m_out.TryPrintLine("KYAOS PURSUIT - MAIN MENU", Color.WHITE);
		m_out.TryPrintLine("1. Single Player", Color.WHITE);
		m_out.TryPrintLine("2. Exit", Color.WHITE);
		m_out.TryPrintLine("3. Instructions", Color.WHITE);
		m_out.TryPrintLine("4. Options", Color.WHITE);
		m_inMainMenu = true;
		m_waitForName = false;
		m_waitForDesc = false;
		m_inOpt = false;
	}
	private void ShowOptions()
	{
		m_out.PrintLine();
		m_out.TryPrintLine("OPTIONS MENU", Color.WHITE);
		m_out.TryPrintLine("1. EXCLUSIVE CONVERSATIONS: " + m_exclusiveTalk, Color.WHITE);
		m_out.TryPrintLine("Type in the ID of the option you wish to toggle, or 0 to leave the options menu.", Color.ORANGE);
		m_inOpt = true;
	}
	
	@Override
	public void RecieveInput(String input, IOutput sender)
	{
		if (!m_init)
			return;
		else if (m_waitForName)
		{
			if (input.length() < 5)
			{
				m_out.TryPrintLine("Your name must be at least 5 letters long.", Color.WHITE);
				return;
			}
			m_engine.HumanPlayer.SetName(input);
			m_waitForName = false;
			m_waitForDesc = true;
			m_out.TryPrintLine("Describe your character.", Color.WHITE);
		}
		else if (m_waitForDesc)
		{
			if (input.length() < 5)
			{
				m_out.TryPrintLine("Your description must be at least 5 letters long.", Color.WHITE);
				return;
			}
			m_engine.HumanPlayer.SetDesc(input);
			m_waitForDesc = false;
			m_inMainMenu = false;
			m_inOpt = false;
			m_out.Flush();
			m_out.TryPrintLine(m_engine.GetCurrentRoom().GetDesc(), Color.ORANGE);
			m_out.TryPrintLine("(Type in commands)", Color.WHITE);
		}
		else if (m_inOpt)
		{
			int toDo = -1;
			try { toDo = Integer.parseInt(input); }
			catch (NumberFormatException e) { m_out.TryPrintLine("You can only type in numbers.", Color.WHITE); }
			if (toDo == 0)
				m_inOpt = false;
			else if (toDo == 1)
			{
				m_exclusiveTalk = !m_exclusiveTalk;
				ShowOptions();
			}
			else
				m_out.TryPrintLine("There is only 1 option.", Color.WHITE);
		}
		else if (m_inMainMenu)
		{
			int toDo = -1;
			try { toDo = Integer.parseInt(input); }
			catch (NumberFormatException e) { m_out.TryPrintLine("You can only type in numbers.", Color.WHITE); }
			if (toDo == 1)
			{
				m_waitForName = true;
				m_out.TryPrintLine("What is your name?", Color.WHITE);
			}
			else if (toDo == 2)
				System.exit(0);
			else if (toDo == 3)
			{
				m_out.TryPrint("The Kaotic Pursuit is a text RPG built with a verb-noun parsing architecture.", Color.ORANGE);
				m_out.TryPrint(" Supported commands include \"Look around\", \"Look at me\", \"Move to [direction]\" and \"Exit\".", Color.ORANGE);
				m_out.TryPrint(" Additional known verbs include \"take\", \"use\", \"talk\", \"attack\", \"stop talking\", \"equip\" and \"unequip\".", Color.ORANGE);
				m_out.TryPrintLine("You can also type in \"Options\" to change your settings.", Color.ORANGE);
			}
			else if (toDo == 4)
			{
				ShowOptions();
			}
			else
				m_out.TryPrintLine("Can't enter in anything other than 1, 2, 3 or 4 in the MAIN MENU", Color.WHITE);
		}
		else//actually playing the game...
		{
			ParseInput(input);
			m_out.TryPrintLine("(Type in commands)", Color.WHITE);
		}
	}
	private void ParseInput(String typed)
	{
		boolean toUpdate = true;
		String[] input = m_engine.Parser.GetVerbNoun(typed);
		
		if (m_exclusiveTalk && m_engine.Conversing != null)
		{
			if (input.length >= 2 && input[0].contentEquals("stop") && input[1].startsWith("talk"))
			{
				if (m_engine.Conversing == null)
					m_out.TryPrintLine("You can't end a conversation you haven't started.", Color.WHITE);
				else
				{
					m_out.TryPrintLine("You end the conversation and move away from " + m_engine.Conversing.GetName(), Color.ORANGE);
					m_engine.Conversing = null;
				}
			}
			else
				Talk(input, typed);
		}
		else if (input.length >= 1 && input[0].contentEquals("options"))
		{
			ShowOptions();
		}
		else if (input.length >= 2 && input[0].contentEquals("look") && input[1].contentEquals("around"))
			m_out.TryPrint(m_engine.GetCurrentRoom().GetLongDescription(m_engine.HumanPlayer), Color.ORANGE);
		else if (input.length >= 2 && input[0].contentEquals("look") && (input[1].contentEquals("me") || (input.length >= 3 && input[1].contentEquals("at") && input[2].contentEquals("me"))))
			m_out.TryPrintLine(m_engine.HumanPlayer.GetLongDesc(), Color.ORANGE);
		else if (input.length >= 2 && input[0].contentEquals("look"))
		{
			int push = 1;
			if (input[1].contentEquals("at") || input[1].contentEquals("my"))
				push++;
			if (input.length >= 3 && (input[2].contentEquals("at") || input[2].contentEquals("my")))
				push++;
			
			if (input.length <= push)
			{
				m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
				return;
			}
			String[] name = new String[input.length - push];
			for (int i = push; i < input.length; i++)
				name[i - push] = input[i];
			AEntity look;
			if (input[1].contentEquals("my") || (input.length >= 3 && input[2].contentEquals("my")))
				look = m_engine.HumanPlayer.GetItem(name);
			else
				look = m_engine.FindInRoom(name);
			
			if (look == null)
				m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
			else if (look instanceof Container)
				m_out.TryPrintLine(((Container)look).GetLongDesc(1, m_engine.HumanPlayer, 0), Color.ORANGE);
			else
				m_out.TryPrintLine(look.GetLongDesc(), Color.ORANGE);
		}
		else if (input.length >= 2 && input[0].contentEquals("empty"))
		{
			int push = 1;
			if (input[1].contentEquals("at") || input[1].contentEquals("my"))
				push++;
			if (input.length >= 3 && (input[2].contentEquals("at") || input[2].contentEquals("my")))
				push++;
			
			if (input.length <= push)
			{
				m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
				return;
			}
			String[] name = new String[input.length - push];
			for (int i = push; i < input.length; i++)
				name[i - push] = input[i];
			
			AEntity look;
			if (input[1].contentEquals("my") || (input.length >= 3 && input[2].contentEquals("my")))
				look = m_engine.HumanPlayer.GetItem(name);
			else
				look = m_engine.FindInRoom(name);
			
			if (look == null)
				m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
			else if (!(look instanceof AItem))
				m_out.TryPrintLine("There is no item of that description.", Color.ORANGE);
			else if (!(((AItem)look) instanceof Container))
				m_out.TryPrintLine("There is container of that description.", Color.ORANGE);
			else
			{
				Container toEmpty = (Container)look;
				AItem[] take = toEmpty.GetVisibleItems(m_engine.HumanPlayer, 0);
				for (AItem a : take)
				{
					int rem = toEmpty.RemoveItem(a, toEmpty.GetAmountOf(a));
					m_engine.GetCurrentRoom().AddItems(a, rem);
				}
				m_out.TryPrintLine("You emptied " + look.GetName(), Color.ORANGE);
			}
		}
		else if (input.length >= 1 && input[0].contentEquals("quit"))
		{
			m_out.Flush();
			ShowMainMenu();
		}
		else if (input.length >= 2 && input[0].contentEquals("stop") && input[1].startsWith("talk"))
		{
			if (m_engine.Conversing == null)
				m_out.TryPrintLine("You can't end a conversation you haven't started.", Color.WHITE);
			else
			{
				m_out.TryPrintLine("You end the conversation and move away from " + m_engine.Conversing.GetName(), Color.ORANGE);
				m_engine.Conversing = null;
			}
		}
		else if (input.length >= 2 && input[0].contentEquals("talk"))
		{
			String[] said = new String[input.length - 1];
			for (int i = 1; i < input.length; i++)
				said[i - 1] = input[i];
			Talk(said, typed.replace("talk ", ""));
		}
		else if (input.length >= 2 && input[0].contentEquals("take"))
		{
			int amount = 0, fromIndex = KaoticWorldEngine.FirstIndexOf(input, "from");
			AItem toTake = null;
			Container takenFrom = null;
			
			if (fromIndex < 0)
			{
				AEntity tmp = m_engine.FindInRoom(KaoticWorldEngine.TrawlString(input, 1));
				if (tmp == null)
				{
					m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
					return;
				}
				else if (!(tmp instanceof AItem))
				{
					m_out.TryPrintLine("You can only take items!", Color.WHITE);
					return;
				}
				toTake = (AItem)tmp;
				amount = m_engine.GetCurrentRoom().GetAmountOfEntity(toTake);
			}
			else
			{
				AEntity container = m_engine.FindInRoom(KaoticWorldEngine.TrawlString(input, fromIndex));
				if (!(container instanceof Container))
				{
					m_out.TryPrintLine("You can only take items from containers!", Color.WHITE);
					return;
				}
				takenFrom = (Container)container;

				AEntity tmp = takenFrom.GetItem(KaoticWorldEngine.TrawlString(input, 1, fromIndex));
				if (tmp == null)
				{
					m_out.TryPrintLine("There is nothing that looks like that.", Color.WHITE);
					return;
				}
				else if (!(tmp instanceof AItem))
				{
					m_out.TryPrintLine("You can only take items!", Color.WHITE);
					return;
				}
				toTake = (AItem)tmp;
				amount = takenFrom.GetAmountOf(toTake);
			}
			
			AItem toAdd = (AItem)toTake;
			int added = m_engine.HumanPlayer.AddItem(toAdd, amount);
			if (added > 0)
			{
				if (takenFrom == null)
					m_engine.GetCurrentRoom().RemoveItems(toAdd, added);
				else
					takenFrom.RemoveItem(toAdd, added);
				m_out.TryPrintLine("You take " + ACharacter.EnglishNumberFromInteger(amount, false) + " " + toAdd.GetName(), Color.ORANGE);
				m_out.TryPrintLine(m_engine.HumanPlayer.GetFuzzyCarryingDesc(), Color.WHITE);
			}
			else
				m_out.TryPrintLine("You can't carry that item.", Color.WHITE);
		}
		else if (input.length >= 2 && input[0].contentEquals("drop"))
		{
			int amount = 1;
			try { amount = Integer.parseInt(input[1]); }
			catch (Exception e) { }

			String[] name = new String[input.length - 1];
			for (int i = 1; i < input.length; i++)
				name[i - 1] = input[i];
			AItem toDrop = m_engine.HumanPlayer.GetItem(name);
			if (toDrop == null)
				m_out.TryPrintLine("You aren't carrying any items of that description.", Color.WHITE);
			else
			{
				int dropped = m_engine.HumanPlayer.RemoveItem(toDrop, amount);
				if (dropped > 0)
				{
					m_engine.GetCurrentRoom().AddItems(toDrop, dropped);
					m_out.TryPrintLine("You dropped " + ACharacter.EnglishNumberFromInteger(dropped, false) + " " + toDrop.GetName(), Color.ORANGE);
				}
				else
					m_out.TryPrintLine("You didn't drop anything.", Color.WHITE);
			}
		}
		else if (input.length >= 2 && input[0].contentEquals("use"))
		{
			int offset = 1;
			if (input.length >= 3 && input[1].contentEquals("my"))
				offset++;
			
			String[] name = KaoticWorldEngine.TrawlString(input, offset);
			AItem toMod = null;
			if (offset == 1)
			{
				AEntity get = m_engine.FindInRoom(name);
				if (!(get instanceof AItem))
				{
					m_out.TryPrintLine("You can only use items.", Color.WHITE);
					return;
				}
				toMod = (AItem)get;
			}
			else
				toMod = m_engine.HumanPlayer.GetItem(name);
			
			if (toMod == null)
			{
				m_out.TryPrintLine("No such item exists.", Color.WHITE);
				return;
			}
			else if (!toMod.IsUsable())
			{
				m_out.TryPrintLine("That item is unusable.", Color.WHITE);
				return;
			}
			
			toMod.Use(m_engine.GetCurrentRoom(), m_engine.HumanPlayer);
			m_out.TryPrintLine("You used " + toMod.GetName() + " and it is now " + (toMod.IsInUse() ? "" : "not ") + "in use.", Color.ORANGE);
		}
		else if (input.length >= 2 && input[0].contentEquals("equip"))
		{
			String[] name = new String[input.length - 1];
			for (int i = 1; i < input.length; i++)
				name[i - 1] = input[i];
			AItem toEquip = m_engine.HumanPlayer.GetItem(name);
			if (toEquip == null)
				m_out.TryPrintLine("You aren't carrying any items of that description.", Color.WHITE);
			else if (toEquip instanceof Weapon)
			{
				if (m_engine.HumanPlayer.GetEquippedWeapon(((Weapon)toEquip).GetWeaponLocation()) == toEquip)
					m_out.TryPrintLine("You already have that weapon equipped.", Color.WHITE);
				else
				{
					m_engine.HumanPlayer.Equip((Weapon)toEquip);
					m_out.TryPrintLine("You equipped " + toEquip.GetName() + ".", Color.WHITE);
				}
			}
			else if (toEquip instanceof Armor)
			{
				if (m_engine.HumanPlayer.GetEquippedArmor(((Armor)toEquip).GetArmorLocation()) == toEquip)
					m_out.TryPrintLine("You already have that armor equipped.", Color.WHITE);
				else
				{
					m_engine.HumanPlayer.Equip((Armor)toEquip);
					m_out.TryPrintLine("You equipped " + toEquip.GetName() + ".", Color.WHITE);
				}
			}
			else
				m_out.TryPrintLine("That item is neither a weapon nor a piece of armor.", Color.WHITE);
		}
		else if (input.length >= 2 && input[0].contentEquals("unequip"))
		{
			if (input.length >= 3)
			{			
				String[] name = new String[input.length - 1];
				for (int i = 1; i < input.length; i++)
					name[i - 1] = input[i];
				AItem toEquip = m_engine.HumanPlayer.GetItem(name);
				if (toEquip == null)
					m_out.TryPrintLine("You aren't carrying any items of that description.", Color.WHITE);
				else if (toEquip instanceof Weapon)
				{
					if (m_engine.HumanPlayer.GetEquippedWeapon(((Weapon) toEquip).GetWeaponLocation()) == null)
						m_out.TryPrintLine("You had no weapon equipped.", Color.WHITE);
					else
					{
						m_engine.HumanPlayer.UnequipWeapon(((Weapon) toEquip).GetWeaponLocation());
						m_out.TryPrintLine("You unequipped " + toEquip.GetName() + ".", Color.WHITE);
					}
				}
				else if (toEquip instanceof Armor)
				{
					if (m_engine.HumanPlayer.GetEquippedArmor(((Armor) toEquip).GetArmorLocation()) == null)
						m_out.TryPrintLine("You had no weapon equipped.", Color.WHITE);
					else
					{
						m_engine.HumanPlayer.UnequipArmor(((Armor) toEquip).GetArmorLocation());
						m_out.TryPrintLine("You unequipped " + toEquip.GetName() + ".", Color.WHITE);
					}
				}
				else
					m_out.TryPrintLine("That item is neither a weapon nor a piece of armor.", Color.WHITE);
			}
		}
		else if (input.length >= 2 && input[0].contentEquals("attack"))
		{
			//TODO
			m_out.TryPrintLine("Unsupported.", Color.PINK);
		}
		else if (!ManipItem(input))
		{
			m_out.TryPrintLine("What?", Color.WHITE);
			toUpdate = false;
		}
		
		if (toUpdate)
			m_engine.GetCurrentRoom().Update();
	}
	private void Talk(String[] talk, String said)
	{
		if (m_engine.Conversing != null)
		{
			m_out.TryPrint(m_engine.HumanPlayer.GetName() + ": ", Color.LIGHT_GRAY);
			m_out.TryPrintLine(said, Color.GREEN);
			NPCDialogueExchange.TalkingData data = m_engine.Conversing.Talk(talk);
			switch (data.Index)
			{
				case -1:
					NPCDialogueResponse[] responses = m_engine.Conversing.GetCurrentExchange().GetResponses();
					for (NPCDialogueResponse r : responses)
						m_out.TryPrintLine("Possible Response: " + r.GetResponse(), Color.LIGHT_GRAY);
					break;
				case 0:
					m_out.TryPrint(m_engine.Conversing.GetName() + ": ", Color.LIGHT_GRAY);
					m_out.TryPrintLine("I'm not sure what you're talking about. Can you repeat that?", Color.GREEN);
					break;
				case 1:
					m_out.TryPrint(m_engine.Conversing.GetName() + ": ", Color.LIGHT_GRAY);
					m_out.TryPrintLine(m_engine.Conversing.GetCurrentExchange().GetNPCSays(), Color.GREEN);
					if (data.ToExecute == null)
						m_out.TryPrintLine("You don't have the requisite items or haven't fulfilled the requirements.", Color.WHITE);
					else
					{
						data.ToExecute.FlagsExecute();
						data.ToExecute.GiveItems(m_engine.Conversing);
						data.ToExecute.TakeItems(m_engine.Conversing);
						HashMap<AItem, Integer> items = data.ToExecute.GetTakenItems();
						StringBuilder builder = new StringBuilder();
						boolean show = false;
						if (items.size() > 0)
						{
							show = true;
							builder.append("You gave\n");
							builder.append(KaoticWorldEngine.ConstructItemString(items));
						}
						items = data.ToExecute.GetGivenItems();
						if (items.size() > 0)
						{
							show = true;
							builder.append("You got\n");
							builder.append(KaoticWorldEngine.ConstructItemString(items));
						}
						m_out.TryPrintLine(builder.toString(), Color.ORANGE);
						if (show)
							m_out.TryPrintLine(m_engine.HumanPlayer.GetFuzzyCarryingDesc(), Color.WHITE);
					}
					break;
			}
		}
		else
		{
			AEntity found = m_engine.FindInRoom(talk);
			if (found == null)
				m_out.TryPrintLine("There is no NPC in the room with that name.", Color.WHITE);
			else if (found instanceof NPC)
			{
				m_engine.Conversing = (NPC)found;
				m_out.TryPrint(m_engine.Conversing.GetName() + ": ", Color.LIGHT_GRAY);
				m_out.TryPrintLine(m_engine.Conversing.GetCurrentExchange().GetNPCSays(), Color.GREEN);
			}
			else
				m_out.TryPrintLine("You can only talk to NPCs.", Color.WHITE);
		}
	}
	private boolean ManipItem(String[] input)
	{
		if (input.length <= 1)
			return false;
		if (input[0].contentEquals("move"))
		{
			if (m_engine.GetCurrentRoom().HasExit(input[1]))
			{
				Exit exit = m_engine.GetCurrentRoom().GetExit(input[1]);
				for (int i = 0; i < m_engine.Rooms.size(); i++)
				{
					if (m_engine.Rooms.get(i) == exit.GetRoomLedTo())
					{
						m_engine.MoveToRoom(i);
						break;
					}
				}
				m_engine.Conversing = null;
				m_out.TryPrint("You head " + input[1] + " and enter ", Color.ORANGE);
				m_out.TryPrintLine(m_engine.GetCurrentRoom().GetTitle().toUpperCase(), Color.ORANGE);
			}
			else
				m_out.TryPrintLine("There is no exit in that direction.", Color.ORANGE);
			return true;
		}
		else if (input[0].contentEquals("put"))
		{
			String[] remain = KaoticWorldEngine.TrawlString(input, 1);
			int inIndex = KaoticWorldEngine.FirstIndexOf(remain, "in");
			if (inIndex < 0)
			{
				m_out.TryPrintLine("You must *put* items *in* a container.", Color.WHITE);
				return true;
			}
			AItem toMove = null;
			Container in;
			
			int itemIsMine = KaoticWorldEngine.FirstIndexOf(remain, "my");
			String[] search;
			if (itemIsMine >= 0 && itemIsMine < inIndex)
			{
				search = KaoticWorldEngine.TrawlString(remain, itemIsMine + 1, inIndex);
				if (search == null)
				{
					m_out.TryPrintLine("You can't put those items.", Color.WHITE);
					return true;
				}
				toMove = m_engine.HumanPlayer.GetItem(search);
			}
			else
			{
				search = KaoticWorldEngine.TrawlString(remain, 0, inIndex);
				if (search == null)
				{
					m_out.TryPrintLine("You can't put those items.", Color.WHITE);
					return true;
				}
				AEntity found = m_engine.FindInRoom(search);
				if (!(found instanceof AItem))
				{
					m_out.TryPrintLine("You can only put items.", Color.WHITE);
					return true;
				}
				toMove = (AItem)found;
			}
			
			int toIsMine = KaoticWorldEngine.FirstIndexOf(search, "my");
			if (toIsMine >= 0)
			{
				search = KaoticWorldEngine.TrawlString(remain, toIsMine + 1);
				if (search == null)
				{
					m_out.TryPrintLine("You can't put those items.", Color.WHITE);
					return true;
				}
				AItem found = m_engine.HumanPlayer.GetItem(search);
				if (!(found instanceof Container))
				{
					m_out.TryPrintLine("You can only put items in containers.", Color.WHITE);
					return true;
				}
				in = (Container)found;
			}
			else
			{
				search = KaoticWorldEngine.TrawlString(remain, inIndex + 1);
				if (search == null)
				{
					m_out.TryPrintLine("You can't put those items.", Color.WHITE);
					return true;
				}
				AEntity found = m_engine.FindInRoom(search);
				if (!(found instanceof Container))
				{
					m_out.TryPrintLine("You can only put items in containers.", Color.WHITE);
					return true;
				}
				in = (Container)found;
			}
			
			int moved = itemIsMine >= 0 ?
					m_engine.HumanPlayer.RemoveItem(toMove, m_engine.HumanPlayer.GetAmountOf(toMove)) :
						m_engine.GetCurrentRoom().RemoveItems(toMove, m_engine.GetCurrentRoom().GetAmountOfEntity(toMove));
			if (moved > 0)
			{
				if (toIsMine >= 0)
					m_engine.HumanPlayer.AddItem(toMove, moved);
				else
					m_engine.GetCurrentRoom().AddItems(toMove, moved);
				m_out.TryPrintLine("You successfully put " + (itemIsMine >= 0 ? "your " : "") + ACharacter.EnglishNumberFromInteger(moved, false) + " " + toMove.GetName()
						+ " in " + (toIsMine >= 0 ? "your " : "") + in.GetName() + ".", Color.ORANGE);
			}
			else
				m_out.TryPrintLine("You didn't move anything.", Color.ORANGE);
			return true;
		}
		return false;
	}
	
	public IOutput GetOut()
	{
		return m_out;
	}

	static final String SPLASH_SCREEN =
		"                      /\\\r\n" + 
		"                      \\ \\\r\n" + 
		"                       \\ \\    /\\\r\n" + 
		"                        \\ \\  /  \\\r\n" + 
		"                         \\ \\/ ___\\\r\n" + 
		"                          \\/\\/\r\n" + 
		"                          //\\\\\r\n" + 
		"                         //  \\\\\r\n" + 
		"                        //    \\\\\r\n" + 
		"                       //      \\\\\r\n" + 
		"                      //        \\\\\r\n" + 
		"                     //          \\\\\r\n" + 
		"                    //            \\\\\r\n" + 
		"                   //              \\\\\r\n" + 
		" _____  _         //     _   _      \\\\\r\n" + 
		"(_   _)( )       //     ( ) ( )      \\\\\r\n" + 
		"  | |  | |___   ____    | |/ /  _   _ \\\\____   ___   ____\r\n" + 
		"  | |  |  _  \\ / __ \\   | , <  ( ) ( ) / _  ) / _ \\ /  __)\r\n" + 
		"  | |  | | | |(  ___/   | |\\ \\ | (_) |( (_| |( (_) )\\__  \\\r\n" + 
		"  (_)  (_) (_) \\____)   (_) (_)`\\__, | \\____) \\___/ (____/\r\n" + 
		"                               ( )_| |\r\n" + 
		"                                \\___/'\r\n" + 
		"           ____                                _\r\n" + 
		"          (  _ \\                            _ ( )_\r\n" + 
		"          | |_) ) _   _  ____  ____  _   _ (_)| ,_)\r\n" + 
		"          | ,__/ ( ) ( )( ___)/  __)( ) ( )| || |\r\n" + 
		"          | |    | (_) || |   \\__  \\| (_) || || |_ \r\n" + 
		"          (_)    `\\___/'(_)   (____/`\\___/'(_)`\\__)";
}