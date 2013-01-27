package gameUI;

import java.awt.Color;
import java.util.Scanner;

public class ConsoleWrapper implements IOutput
{
	private class InputThread implements Runnable
	{
		IInput m_inputCallback;
		ConsoleWrapper m_parent;
		
		public InputThread(IInput callback, ConsoleWrapper parent)
		{
			m_inputCallback = callback;
			m_parent = parent;
		}
		
		@Override
		public void run()
		{
			boolean running = true;
			while (running)
			{
				synchronized (m_parent)
				{
					if (m_parent == null || !m_parent.running)
						running = false;
					else if (m_parent.scanner.hasNext())
					{
						synchronized (m_inputCallback)
						{
							m_inputCallback.RecieveInput(m_parent.scanner.nextLine(), m_parent);
						}
					}
				}
			}
		}
	}
	
	Thread inputThread;
	Scanner scanner;
	boolean running = true;
	
	public ConsoleWrapper(IInput callback)
	{
		scanner = new Scanner(System.in);
		inputThread = new Thread(new InputThread(callback, this));
		inputThread.start();
	}
	protected void finalize()
	{
		running = false;
		scanner.close();
	}
	
	//BEGIN IOutput
	@Override
	public void Print(String toWrite)
	{
		System.out.print(toWrite);
	}

	@Override
	public void PrintLine(String toWrite)
	{
		System.out.println(toWrite);
	}

	@Override
	public void TryPrint(String toWrite, Color color)
	{
		Print(toWrite);
	}
	
	@Override
	public void TryPrintLine(String toWrite, Color color)
	{
		PrintLine(toWrite);
	}
	
	@Override
	public void TryPrint(String toWrite, Color color, boolean bold, boolean italic, boolean underline)
	{
		Print(toWrite);
	}
	
	@Override
	public void TryPrintLine(String toWrite, Color color, boolean bold, boolean italic, boolean underline)
	{
		PrintLine(toWrite);
		
	}
	
	@Override
	public void PrintLine()
	{
		System.out.println();
	}
	
	@Override
	public void TrySetTextColor(Color color) { }
	
	@Override
	public void TrySetBackgroundColor(Color color) { }
	
	@Override
	public Color GetTextColor()
	{
		return Color.WHITE;
	}
	
	@Override
	public Color GetBackgroundColor()
	{
		return Color.BLACK;
	}
	@Override
	public void Flush()
	{
		System.out.flush();
	}
	//END IOutput
}
