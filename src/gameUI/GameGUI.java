package gameUI;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GameGUI extends JFrame implements IOutput, ActionListener
{
	/**
	 * For the parent applet
	 */
	private static final long serialVersionUID = -3819657268475617009L;
	
	private static final Color BG_COLOR = new Color(220, 220, 220);//pale grey

	AbstractDocument docDisp;
	JTextPane textPaneDisp;
	JLabel labelInputDesc; 
	JTextField textFieldInput;
	JScrollPane scrollPaneDisp;
	JButton buttonParse;
	IInput inputCallback;
	
	public GameGUI(IInput callback)
	{
		this(callback, true);
	}
	public GameGUI(IInput callback, boolean show)
	{
		inputCallback = callback;
		
		InitControls();
		setVisible(show);
		this.setLocationRelativeTo(null);//center screen
	}
	
	protected void InitControls()
	{
		this.setTitle("The Kaotic Pursuit");
		this.setSize(800, 600);
        this.setBackground(BG_COLOR);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		Container contentPane = this.getContentPane();
		contentPane.setLayout(null);
		
		textPaneDisp = new JTextPane();
		textPaneDisp.setBounds(5, 5, 773, 525);
		textPaneDisp.setEditable(false);
		textPaneDisp.setBackground(Color.BLACK);
		textPaneDisp.setForeground(Color.GREEN);
		textPaneDisp.setFont(new Font("Courier New", 0, 12));
		textPaneDisp.setAutoscrolls(true);
		textPaneDisp.setMargin(new Insets(5, 5, 5, 5));
		//contentPane.add(textPaneDisp);
		
		StyledDocument doc = textPaneDisp.getStyledDocument();
		if (doc instanceof AbstractDocument)
		{
			docDisp = (AbstractDocument) doc;
		}
		else
		{
			System.err.println("No abstract doc!");
			System.exit(-1);
		}
		
		scrollPaneDisp = new JScrollPane(textPaneDisp);//textAreaDisp);
		scrollPaneDisp.setBounds(5, 5, 773, 525);
		contentPane.add(scrollPaneDisp);
        
		labelInputDesc = new JLabel("Command:");
		labelInputDesc.setBounds(5, 534, 75, 20);
		labelInputDesc.setHorizontalAlignment(JLabel.RIGHT);
		contentPane.add(labelInputDesc);
		
		buttonParse = new JButton("Parse");
		buttonParse.setBounds(690, 535, 88, 20);
		contentPane.add(buttonParse);
		buttonParse.addActionListener(this);
		this.getRootPane().setDefaultButton(buttonParse);
		
		textFieldInput = new JTextField();
		textFieldInput.setBounds(85, 535, 600, 20);
		textFieldInput.requestFocus();
		contentPane.add(textFieldInput);
	}

	
	//BEGIN IOutput
	@Override
	public void Print(String toWrite)
	{
		TryPrint(toWrite, GetTextColor());
	}
	
	public void TryPrint(String toWrite, Color color)
	{
		TryPrint(toWrite, color, false, false, false);
	}
	

	@Override
	public void PrintLine(String toWrite)
	{
		Print(toWrite + "\n");
	}

	public void TryPrintLine(String toWrite, Color color)
	{
		TryPrint(toWrite + "\n", color);
	}
	
	@Override
	public void TryPrint(String toWrite, Color color, boolean bold, boolean italic, boolean underline)
	{
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setForeground(attr, color);
		StyleConstants.setBold(attr, bold);
		StyleConstants.setItalic(attr, italic);
		StyleConstants.setUnderline(attr, underline);
		try { docDisp.insertString(docDisp.getLength(), toWrite, attr); } catch (BadLocationException e) { }
		textPaneDisp.setCaretPosition(docDisp.getLength());
	}
	
	@Override
	public void TryPrintLine(String toWrite, Color color, boolean bold, boolean italic, boolean underline)
	{
		TryPrint(toWrite + "\n", color, bold, italic, underline);
	}
	
	public void PrintLine()
	{
		Print("\n");
	}

	@Override
	public void TrySetTextColor(Color color)
	{
		textPaneDisp.setForeground(color);
	}

	@Override
	public void TrySetBackgroundColor(Color color)
	{
		textPaneDisp.setBackground(color);
	}

	@Override
	public Color GetTextColor()
	{
		return textPaneDisp.getForeground();
	}
	
	@Override
	public Color GetBackgroundColor()
	{
		return textPaneDisp.getBackground();
	}
	
	@Override
	public void Flush()
	{
		//NOTE: This only works with Courier New size 12 font
		try { docDisp.insertString(docDisp.getLength(), "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n", null); } catch (BadLocationException e) { }
	}
	//END IOutput
	
	//BEGIN IActionListener
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == buttonParse)
		{
			if (textFieldInput.getText().length() > 0)
			{
				inputCallback.RecieveInput(textFieldInput.getText(), this);
				textFieldInput.setText("");
			}
		}
	}
	//END IActionListener
}