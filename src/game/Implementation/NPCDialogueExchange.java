package game.Implementation;

import java.util.HashMap;
import java.util.Map;

public final class NPCDialogueExchange
{
	NPCDialogue m_dialogue;
	String m_says;
	HashMap<NPCDialogueResponse, Integer> m_responses;
	HashMap<NPCDialogueResponse, Integer> m_responseFails;
	float m_threshold;
	
	public NPCDialogueExchange(NPCDialogue parent, String NPCSays, float threshold, HashMap<NPCDialogueResponse, Integer> responses, HashMap<NPCDialogueResponse, Integer> responseFails)
	{
		m_dialogue = parent;
		m_says = NPCSays.replace("[newline]", "\n");
		m_responses = responses;
		m_responseFails = responseFails;
		m_threshold = threshold;
	}
	
	public String GetNPCSays()
	{
		return m_says;
	}
	
	public NPCDialogueResponse[] GetResponses()
	{
		NPCDialogueResponse[] toRet = new NPCDialogueResponse[m_responses.size()];
		m_responses.keySet().toArray(toRet);
		return toRet;
	}
	
	public Integer GetResponseNavigationID(String response)
	{
		if (m_responses.containsKey(response))
			return m_responses.get(response);
		return 0;
	}

	public class TalkingData
	{
		public Integer Index;
		public NPCDialogueResponse ToExecute;
		
		public TalkingData()
		{
			Index = 0;
			ToExecute = null;
		}
	}
	
	//-1 indicates failure to find a valid reply
	//anything else is the index of the next NPCDialogueExchange
	public TalkingData TryTalk(String[] talk, NPC talker, Player player, NPCDialogue dialogue)
	{
		int toRet = -1;
		float best = m_threshold, current;
		NPCDialogueResponse toExec = null;
		
		for (Map.Entry<NPCDialogueResponse, Integer> e : m_responses.entrySet())
		{
			current = e.getKey().IsValid(talk);
			if (current > best)
			{
				current = best;
				toRet = e.getValue();
				toExec = e.getKey();
			}
		}
		
		if (toExec != null)
		{
			if (!(toExec.CanGiveItems(talker) && toExec.CanTakeItems(talker) && toExec.FlagsEval() == true))
			{
				NPCDialogueResponse copy = toExec;
				toExec = null;
				toRet = m_responseFails.get(copy);
				NPCDialogueResponse[] responses = dialogue.GetExchange(toRet).GetResponses();
				if (responses.length == 1)
				{
					TalkingData data = dialogue.GetExchange(toRet).TryTalk(responses[0].GetResponses(), talker, player, dialogue);
					toExec = data.ToExecute;
					toRet = data.Index;
				}
			}
		}
		
		TalkingData data = new TalkingData();
		data.Index = toRet;
		data.ToExecute = toExec;
		return data;
	}
}