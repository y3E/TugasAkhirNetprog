package interfacepkg;

import javax.swing.JDialog;
import java.awt.Toolkit;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.event.ActionEvent;

public class GameOverGui extends JDialog
{	
	private static final long serialVersionUID = 1L;

	private BattleShip gui;
	private String appPath;
	private JLabel congratLbl;
	private JLabel congratMsgLbl;
	private JLabel picLbl;
	
	public GameOverGui(String appPath, BattleShip myGui)
	{
		this.gui = myGui;
		this.appPath = appPath;
		initializeComponents();		
	}
	
	private void initializeComponents() 
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.appPath+"icon.png"));
		setTitle("Game Over");
		setAlwaysOnTop(true);
		setResizable(false);
		setSize(600, 600);
		getContentPane().setLayout(null);
		
		this.congratLbl = new JLabel("Ajigile Menang lu !");
		this.congratLbl.setHorizontalAlignment(SwingConstants.CENTER);
		this.congratLbl.setFont(new Font("Arial", Font.BOLD, 28));
		this.congratLbl.setBounds(10, 11, 574, 33);
		getContentPane().add(this.congratLbl);
		
		this.congratMsgLbl = new JLabel("Besok traktiran ya wkwkwkwk !");
		this.congratMsgLbl.setHorizontalAlignment(SwingConstants.CENTER);
		this.congratMsgLbl.setFont(new Font("Arial", Font.PLAIN, 22));
		this.congratMsgLbl.setBounds(20, 55, 574, 33);
		getContentPane().add(this.congratMsgLbl);
				
		JButton btnNewGame = new JButton("New Game");
		btnNewGame.setFont(new Font("Arial", Font.PLAIN, 18));
		btnNewGame.setBounds(10, 508, 264, 53);
		btnNewGame.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				gui.enableShipAllocation(true);
				gui.restartAllocations();
				gui.getDiceHandler().clearResults();
				if (gui.IsServer())
				{
					gui.writeOutputMessage(" - Ganti posisi kapalmu !");	
				}
				else
				{
					gui.getMyClient().SendMessage("B");
					gui.getMyClient().StopClient();
					gui.btnConnect.setEnabled(true);
					gui.btnStopServer.setEnabled(false);
					gui.writeOutputMessage(" - Now connect to server lagi !");
				}
				dispose();
			}
		});
		getContentPane().add(btnNewGame);
		
		JButton btnClose = new JButton("Close");
		btnClose.setFont(new Font("Arial", Font.PLAIN, 18));
		btnClose.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				gui.enableShipAllocation(true);
				gui.restartAllocations();
				gui.getDiceHandler().clearResults();
				if (gui.IsServer())
				{
					if (gui.getMyServer().hasClient())
	    			{
						gui.getMyServer().SendMessage("B");
	    				gui.getMyServer().StopCommunication();
	    				gui.getMyServer().StopServer();
	    			}
	    			else
	    			{
	    				gui.getMyServer().StopServer();
	    			}
					gui.getMyServer().stop();
	    			gui.btnConnect.setEnabled(true);
	    			gui.btnStopServer.setEnabled(false);
					gui.writeOutputMessage(" - Thank you for playing BattleShip !");
					gui.writeOutputMessage(" - Maaf kak ");	
				}
				else
				{
					gui.getMyClient().SendMessage("B");
					gui.getMyClient().StopClient();
					gui.btnConnect.setEnabled(true);
					gui.btnStopServer.setEnabled(false);
					gui.writeOutputMessage(" - Thank you for playing BattleShip !");
					gui.writeOutputMessage(" - If you decide to play again, you have to reallocate your ships and connect to the server!");
				}
				dispose();
			}
		});
		
		addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{
				gui.enableShipAllocation(true);
				gui.restartAllocations();
				gui.getDiceHandler().clearResults();
				if (gui.IsServer())
				{
					if (gui.getMyServer().hasClient())
	    			{
						gui.getMyServer().SendMessage("B");
	    				gui.getMyServer().StopCommunication();
	    				gui.getMyServer().StopServer();
	    			}
	    			else
	    			{
	    				gui.getMyServer().StopServer();
	    			}
					gui.getMyServer().stop();
	    			gui.btnConnect.setEnabled(true);
	    			gui.btnStopServer.setEnabled(false);
					gui.writeOutputMessage(" - Thank you for playing");
				}
				else
				{
					gui.getMyClient().SendMessage("B");
					gui.getMyClient().StopClient();
					gui.btnConnect.setEnabled(true);
					gui.btnStopServer.setEnabled(false);
					gui.writeOutputMessage(" - Thank you for playing");
				}
			}
		});
	}

	public void ShowGameOver(int flag)
	{
		switch (flag)
		{
			case 0: //lose
			{
				this.congratLbl.setText("Lose :(");
				this.congratMsgLbl.setText("Mission failed we'll get em next toime !");
				break;
			}
			case 1: //win
			{
				this.congratLbl.setText("Ciieee Menang !");
				this.congratMsgLbl.setText("Besok traktiran yaa wkwkwkwk !");
				break;
			}
		}
		setLocationRelativeTo(this.gui);
		show();
	}
}
