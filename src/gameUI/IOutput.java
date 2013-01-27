package gameUI;

import java.awt.Color;

public interface IOutput
{
	public void Print(String toWrite);
	public void PrintLine(String toWrite);
	public void TryPrint(String toWrite, Color color);
	public void TryPrintLine(String toWrite, Color color);
	public void TryPrint(String toWrite, Color color, boolean bold, boolean italic, boolean underline);
	public void TryPrintLine(String toWrite, Color color, boolean bold, boolean italic, boolean underline);
	public void PrintLine();
	
	public void Flush();
	
	public void TrySetTextColor(Color color);
	public void TrySetBackgroundColor(Color color);
	public Color GetTextColor();
	public Color GetBackgroundColor();
}
