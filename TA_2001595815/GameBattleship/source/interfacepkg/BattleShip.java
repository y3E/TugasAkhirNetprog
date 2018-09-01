package interfacepkg;

import java.awt.Point;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import communicationpkg.MyClient;
import communicationpkg.MyServer;

import javax.swing.border.LineBorder;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class BattleShip extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//board
	private JList myEnemyList;
	private JList myBoardList;
	
	//board renderer
	private MyListRenderer myRenderer;
	private MyListRenderer myEnemyRenderer;
	
	//mouse move
	private int mHoveredJListIndex = -1;
	private ArrayList<Integer> mHoveredJListIndexList = new ArrayList<Integer>();
	private int orientation = MyDefines.ORIENTATION_HORIZONTAL;

	//board update
	private MapUpdateHandler myMapUpdate;
	private MapUpdateHandler myEnemyMapUpdate;
	
	//output panel
	private JTextPane outputTextField;
	private HTMLEditorKit outputKit;
	private HTMLDocument outputDoc;
	    
	//allocation panel
	private JButton btnNewAllocation;
	private JRadioButton rdbtnAutomaticAllocation;
	private JRadioButton rdbtnManualAllocation;
	private JButton btnRestartAllocation;
	
	//connection panel
	private JLabel lblMyIpAddress;
	private JTextField ipAddressTextField;
	public JButton btnConnect;
	public JButton btnStopServer;
	private boolean IsServer = true;
	private JTextField portTextField;
	private String serverIP = "";
	
	//server
	private MyServer myServerObj;
	private MyClient myClientObj;
	
	//chat panel
	private JTextPane chatHistory;
	private HTMLEditorKit chatKit;
	private HTMLDocument chatDoc;
	public  JButton btnSend;
	public JTextPane sendTxtField;
	
	//main frame
	private JFrame myFrame;
	
	//who starts handler
	private DiceHandler myDiceHandler;
	
	//game over interface
	private GameOverGui myGOScreen;
	
	//handle submit (chat)
	private static final String TEXT_SEND = "text-submit";
	private static final String INSERT_BREAK = "insert-break";
	
	public BattleShip()
	{
		//start handlers
		startMyHandlers();
		
		//my board creation
		initializeMyBoard();
		
		//my enemy's board creation
		initializeMyEnemyBoard();
		
		//output panel creation
		initializeOutputPanel();

		//allocation panel creation
		initialiazeAllocationPanel();
		
		//connection panel creation
		initializeConnectionPanel();
		
		//chat panel creation
		initializeChatPanel();
		
		//rest of components
		initializeComponents();
		
		//splash screen
		splashScreenHandler();
	
		setVisible(true);
	}

	private void startMyHandlers() 
	{
		//take application path
		File currDir = new File("");
				
		//main frame for all JOptionPane
		this.myFrame = this;
				
		//dive handler (who starts)
		this.myDiceHandler = new DiceHandler(myFrame);
				
		//game over screen
		this.myGOScreen = new GameOverGui(currDir.getAbsolutePath()+"\\img\\", this);
				
		//board update handler
		this.myMapUpdate = new MapUpdateHandler(currDir.getAbsolutePath()+"\\img\\");
		this.myEnemyMapUpdate = new MapUpdateHandler(currDir.getAbsolutePath()+"\\img\\");

		//board renderer
		this.myRenderer = new MyListRenderer(this.myMapUpdate.getImageMap(), this.myMapUpdate.getImageMapHelp(), this.myMapUpdate.getApplicationPath(), this.myMapUpdate.getMatrixMap());
		this.myEnemyRenderer = new MyListRenderer(this.myEnemyMapUpdate.getImageMap(), this.myMapUpdate.getImageMapHelp(), this.myEnemyMapUpdate.getApplicationPath(), this.myEnemyMapUpdate.getMatrixMap());
	
		//client handler
		this.myClientObj = new MyClient(this);
	}
	
	private void initializeMyBoard() 
	{
		//my boar panel
		JPanel myShipsPanel = new JPanel();
		myShipsPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Your ships:", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		myShipsPanel.setBounds(10, 11, 349, 369);
		getContentPane().add(myShipsPanel);
		
		//my boar list
		this.myBoardList = new JList(MyDefines.NAME_LIST);
		myShipsPanel.add(this.myBoardList);
		this.myBoardList.setCellRenderer(this.myRenderer);
		this.myBoardList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.myBoardList.setVisibleRowCount(11);
		this.myBoardList.setFixedCellHeight(30);
		this.myBoardList.setFixedCellWidth(29);
		
		//allocate a ship or change orientation listener
		this.myBoardList.addMouseListener(new MouseListener() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				//if allocation is done, the listener returns because there is no ship to be allocated
				if (myMapUpdate.isSelectionDone() || btnNewAllocation.isEnabled())
				{
					return;
				}
				
				//left mouse button place the ship
				if (e.getButton()==MouseEvent.BUTTON1)
				{
					//tried to update the map with selected position
					if (myMapUpdate.updateMap(orientation,mHoveredJListIndexList))
					{
						//repaint board
						myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
						myBoardList.repaint();
						
						//tells user which boat was allocated
						if (myMapUpdate.isSelectionDone())
						{
							writeOutputMessage(" - CV Dipasang !");
							writeOutputMessage(" - Siap tempur nih ????!!!");
						}
						else if ((myMapUpdate.getBoatType()-1)==MyDefines.PATROL_BOAT)
						{
							writeOutputMessage(" - PTB Dipasang !");
						}
						else if ((myMapUpdate.getBoatType()-1)==MyDefines.DESTROYER)
						{
							writeOutputMessage(" - U-Boat Dipasang !");
						}
						else if ((myMapUpdate.getBoatType()-1)==MyDefines.SUBMARINE)
						{
							writeOutputMessage(" - DD Dipasang !");
						}
						else
						{
							writeOutputMessage(" - BB Dipasang !");
						}
					}
					else
					{
						//in case of a not valid position
						JOptionPane.showMessageDialog(myFrame, "Mau narok mana lu ? wkwkwkwkwkw");
					}
				}
				//right mouse button changes ship orientation
				else if (e.getButton()==MouseEvent.BUTTON3)
				{
					//horizontal to vertical
					if (orientation==MyDefines.ORIENTATION_HORIZONTAL)
					{
						orientation = MyDefines.ORIENTATION_VERTICAL;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(mHoveredJListIndex);
						mHoveredJListIndexList.add(mHoveredJListIndex-11);
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
					//vertical to horizontal
					else
					{
						orientation = MyDefines.ORIENTATION_HORIZONTAL;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(mHoveredJListIndex);
						mHoveredJListIndexList.add(mHoveredJListIndex+1);
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
				}
				else
				{
					//nothing to do
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//highlight ship position listener
		this.myBoardList.addMouseMotionListener(new MouseAdapter() 
		{
			public void mouseMoved(MouseEvent me) 
			{
				//get mouse position
				Point p = new Point(me.getX(),me.getY());
				
				//get matrix index according to mouse position
				int index = myBoardList.locationToIndex(p);
				
				//check is allocation is done (in case of yes, the board does not need to be highlighted)
				if (myMapUpdate.isSelectionDone() || btnNewAllocation.isEnabled())
				{
					mHoveredJListIndex = index;
					mHoveredJListIndexList.clear();
					myBoardList.repaint();
				}
				
				//only perform something if current index is different than the last index
				if (index != mHoveredJListIndex) 
				{
					//if ship type is Patrol Boat, only two cells need to be highlighted
					if (myMapUpdate.getBoatType()==MyDefines.PATROL_BOAT)
					{
						mHoveredJListIndex = index;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(index);
						if (orientation==MyDefines.ORIENTATION_HORIZONTAL)
						{
							mHoveredJListIndexList.add(index+1);
						}
						else
						{
							mHoveredJListIndexList.add(index-11);
						}
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
					//if ship type is Destroyer or Submarine, three cells need to be highlighted
					else if (myMapUpdate.getBoatType()==MyDefines.DESTROYER || myMapUpdate.getBoatType()==MyDefines.SUBMARINE)
					{
						mHoveredJListIndex = index;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(index);
						if (orientation==MyDefines.ORIENTATION_HORIZONTAL)
						{
							mHoveredJListIndexList.add(index+1);
							mHoveredJListIndexList.add(index+2);
						}
						else
						{
							mHoveredJListIndexList.add(index-11);
							mHoveredJListIndexList.add(index-22);
						}
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
					//if ship type is Battleship, four cells need to be highlighted
					else if (myMapUpdate.getBoatType()==MyDefines.BATTLESHIP)
					{
						mHoveredJListIndex = index;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(index);
						if (orientation==MyDefines.ORIENTATION_HORIZONTAL)
						{
							mHoveredJListIndexList.add(index+1);
							mHoveredJListIndexList.add(index+2);
							mHoveredJListIndexList.add(index+3);
						}
						else
						{
							mHoveredJListIndexList.add(index-11);
							mHoveredJListIndexList.add(index-22);
							mHoveredJListIndexList.add(index-33);
						}
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
					//if ship type is Aircraft carrier, five cells need to be highlighted
					else
					{
						mHoveredJListIndex = index;
						mHoveredJListIndexList.clear();
						mHoveredJListIndexList.add(index);
						if (orientation==MyDefines.ORIENTATION_HORIZONTAL)
						{
							mHoveredJListIndexList.add(index+1);
							mHoveredJListIndexList.add(index+2);
							mHoveredJListIndexList.add(index+3);
							mHoveredJListIndexList.add(index+4);
						}
						else
						{
							mHoveredJListIndexList.add(index-11);
							mHoveredJListIndexList.add(index-22);
							mHoveredJListIndexList.add(index-33);
							mHoveredJListIndexList.add(index-44);
						}
						myRenderer.setIndexHover(mHoveredJListIndexList);
						myBoardList.repaint();
					}
				}
			}
		});
	}

	private void initializeMyEnemyBoard() 
	{
		JPanel myEnemyPanel = new JPanel();
		myEnemyPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Musuh :", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		myEnemyPanel.setBounds(369, 11, 349, 369);
		getContentPane().add(myEnemyPanel);
		
		//my enemy's board
		this.myEnemyList = new JList(MyDefines.NAME_LIST);
		myEnemyPanel.add(this.myEnemyList);
		this.myEnemyList.setCellRenderer(this.myEnemyRenderer);
		this.myEnemyList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.myEnemyList.setVisibleRowCount(11);
		this.myEnemyList.setFixedCellHeight(30);
		this.myEnemyList.setFixedCellWidth(29);
		
		//play listener
		this.myEnemyList.addMouseListener(new MouseListener() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				//left mouse button clicked
				if (e.getButton()==MouseEvent.BUTTON1)
				{
					//get mouse position
					Point p = new Point(e.getX(),e.getY());
					
					//convert mouse position in matrix index 
					int index = myBoardList.locationToIndex(p);
					
					//check if connection is on (if not connected cannot play)
					if (!btnConnect.isEnabled())
					{
						//check if is playing as server
						if (IsServer)
						{
							//check if there is a client connected and if it is my turn
							if (myServerObj.getHasClient() && myMapUpdate.getMyTurn())
							{
								//check if this position is legal
								if (myEnemyMapUpdate.isPositionLegal(index))
								{
									//check if the position was already played
									if (myMapUpdate.positionPlayed(index))
									{
										JOptionPane.showMessageDialog(myFrame, "Dah ditembak oi, cari lagi");
									}
									else
									{
										//legal position and not yet played, it sends to the enemy where it was played
										myServerObj.SendMessage("G:"+index);
										
										//add to played list
										myMapUpdate.addPlayedPosition(index);
										
										//set my turn false
										myMapUpdate.setMyTurn(false);
										
										//inform
										writeOutputMessage(" -Your Salvo goes to: "+myEnemyMapUpdate.getLine(index)+myEnemyMapUpdate.getColumn(index));
									}
								}
								//not a legal position
								else
								{
									JOptionPane.showMessageDialog(myFrame, "Mau kemana ? :v");
								}
							}
						}
						//playing as client
						else
						{
							//check if it is connected and if it is my turn
							if (myClientObj.getIsConnected() && myMapUpdate.getMyTurn())
							{
								//check legal position
								if (myEnemyMapUpdate.isPositionLegal(index))
								{
									//check if the position was already played
									if (myMapUpdate.positionPlayed(index))
									{
										JOptionPane.showMessageDialog(myFrame, "Dah ditembak oi, cari lagi");
									}
									else
									{
										//legal position and not yet played, it sends to the enemy where it was played
										myClientObj.SendMessage("G:"+index);
										
										//add to played list
										myMapUpdate.addPlayedPosition(index);
										
										//set my turn false
										myMapUpdate.setMyTurn(false);
										
										//inform
										writeOutputMessage(" - Your Salvo goes to: "+myEnemyMapUpdate.getLine(index)+myEnemyMapUpdate.getColumn(index));
									}
								}
								//not a legal position
								else
								{
									JOptionPane.showMessageDialog(myFrame, "Mau kemana ? :v");
								}
							}
						}
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private void initializeOutputPanel()
	{
		this.outputKit = new HTMLEditorKit();
		this.outputDoc = new HTMLDocument();
		
		this.outputTextField = new JTextPane();
		this.outputTextField.setContentType("text/html");
		this.outputTextField.setEditorKit(this.outputKit);
		this.outputTextField.setDocument(this.outputDoc);
		
		JScrollPane outputScrollPane = new JScrollPane(this.outputTextField);
		
		this.outputTextField.setFont(this.outputTextField.getFont().deriveFont(this.outputTextField.getFont().getStyle() | Font.BOLD));
		this.outputTextField.setEditable(false);
		this.outputTextField.setBounds(10, 21, 688, 141);
		writeOutputMessage("<b> - Wellcome to batleship :D</b>");
		
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Output:", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		outputPanel.setBounds(10, 404, 708, 207);
		outputPanel.setLayout(new BorderLayout());
		outputPanel.add(outputScrollPane,BorderLayout.CENTER);
		getContentPane().add(outputPanel);
	}
	
	private void initialiazeAllocationPanel()
	{
		JPanel shipsAllocationPanel = new JPanel();
		shipsAllocationPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Ships allocation:", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		shipsAllocationPanel.setBounds(740, 11, 244, 107);
		getContentPane().add(shipsAllocationPanel);
		shipsAllocationPanel.setLayout(null);
		
		this.rdbtnAutomaticAllocation = new JRadioButton("Automatic allocation");
		this.rdbtnAutomaticAllocation.setBounds(6, 44, 203, 23);
		shipsAllocationPanel.add(this.rdbtnAutomaticAllocation);
		
		this.rdbtnManualAllocation = new JRadioButton("Manual allocation");
		this.rdbtnManualAllocation.setSelected(true);
		this.rdbtnManualAllocation.setBounds(6, 18, 109, 23);
		shipsAllocationPanel.add(this.rdbtnManualAllocation);
		
		this.rdbtnAutomaticAllocation.addActionListener(this);
		this.rdbtnManualAllocation.addActionListener(this);
		
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(this.rdbtnAutomaticAllocation);
	    group.add(this.rdbtnManualAllocation);
	    
	    this.btnRestartAllocation = new JButton("Clear allocation");
	    this.btnRestartAllocation.setBounds(6, 74, 109, 23);
	    shipsAllocationPanel.add(this.btnRestartAllocation);
	    
	    //restart map (sets all to water and clear positions)
	    //clear own map and enemy's map
	    this.btnRestartAllocation.addActionListener(new ActionListener() 
	    {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		myMapUpdate.restartAllocation();
	    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
				myBoardList.repaint();
				
				myEnemyMapUpdate.restartAllocation();
	    		myEnemyRenderer.updateMatrix(myEnemyMapUpdate.getMatrixMap());
				myEnemyList.repaint();
	    	}
	    });
	    
	    this.btnNewAllocation = new JButton("New allocation");
	    this.btnNewAllocation.setEnabled(false);
	    this.btnNewAllocation.setBounds(125, 74, 109, 23);
	    shipsAllocationPanel.add(this.btnNewAllocation);
	    
	    //automatic allocation
	    this.btnNewAllocation.addActionListener(new ActionListener() 
	    {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		//clear the map
	    		myMapUpdate.restartAllocation();
	    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
				myBoardList.repaint();
				
				//create a timeout counter
				int timeOutCounter = 0;
				
				//it tries to allocate until find a valid position for all boats or the timeout happens
	    		while (!myMapUpdate.randomAllocation())
	    		{
	    			try 
	    			{
	    				//wait 100 ms to perform a better random position (random is using as seed System.currentTimeMillis())
	    				//and has a counter up to 5 s for timeout
	    				Thread.sleep(MyDefines.DELAY_100MS);
	    			}
	    			catch ( java.lang.InterruptedException ie) 
	    			{
	    				JOptionPane.showMessageDialog(myFrame, "Thread wait error. Please press \"New Allocation\" again.");
	    				myMapUpdate.restartAllocation();
			    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
						myBoardList.repaint();
	    				return;
	    			}
	    			timeOutCounter += 100;
	    			//timeout of 5 seconds to stop trying
	    			if (timeOutCounter>=MyDefines.DELAY_5S)
	    			{
	    				JOptionPane.showMessageDialog(myFrame, "Timeout error. Please press \"New Allocation\" again.");
	    				myMapUpdate.restartAllocation();
			    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
						myBoardList.repaint();
						return;
	    			}
	    			myMapUpdate.restartAllocation();
		    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
					myBoardList.repaint();
	    		}
	    		myRenderer.updateMatrix(myMapUpdate.getMatrixMap());
				myBoardList.repaint();
	    	}
	    });
	}
	
	private void initializeConnectionPanel()
	{
		JPanel connectionPanel = new JPanel();
	    connectionPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Connection Configuration:", TitledBorder.LEFT, TitledBorder.TOP, null, null));
	    connectionPanel.setBounds(740, 129, 244, 178);
	    getContentPane().add(connectionPanel);
	    connectionPanel.setLayout(null);
	    
	    JRadioButton rdbtnPlayAsServer = new JRadioButton("Play as server");
	    rdbtnPlayAsServer.setSelected(true);
	    rdbtnPlayAsServer.setBounds(6, 17, 109, 23);
	    connectionPanel.add(rdbtnPlayAsServer);
	    
	    JRadioButton rdbtnPlayAsClient = new JRadioButton("Play as client");
	    rdbtnPlayAsClient.setBounds(6, 43, 109, 23);
	    connectionPanel.add(rdbtnPlayAsClient);
	    
	    ButtonGroup group1 = new ButtonGroup();
	    group1.add(rdbtnPlayAsServer);
	    group1.add(rdbtnPlayAsClient);
	    
	    rdbtnPlayAsServer.addActionListener(this);
	    rdbtnPlayAsClient.addActionListener(this);
	    
	    JSeparator separator = new JSeparator();
	    separator.setBounds(6, 73, 226, 2);
	    connectionPanel.add(separator);
	    
	    this.lblMyIpAddress = new JLabel("My IP Address:");
	    this.lblMyIpAddress.setBounds(6, 86, 97, 14);
	    connectionPanel.add(this.lblMyIpAddress);
	    InetAddress ip = null;
	    
	    try 
	    {
	    	//gets the ip address of the machine
	    	ip = Inet4Address.getLocalHost();
	    	this.serverIP = ip.toString();
	    	this.serverIP = this.serverIP.substring(this.serverIP.indexOf("/")+1, this.serverIP.length());
		}
	    catch (UnknownHostException e) 
	    {
			e.printStackTrace();
		}
	    
	    this.portTextField = new JTextField("9090");
	    this.portTextField.setBounds(123, 111, 109, 20);
	    connectionPanel.add(this.portTextField);
	    this.portTextField.setColumns(10);
	    
	    this.ipAddressTextField = new JTextField(serverIP);
	    this.ipAddressTextField.setBounds(6, 111, 109, 20);
	    connectionPanel.add(this.ipAddressTextField);
	    this.ipAddressTextField.setColumns(10);
	    
	    this.btnConnect = new JButton("Start Server");
	    
	    //start connection listener
	    this.btnConnect.addActionListener(new ActionListener() 
	    {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		//check if it is the server
	    		if (IsServer)
	    		{
	    			//check if ip address and port are not empty
	    			if (ipAddressTextField.getText().equals("") || portTextField.getText().equals(""))
    				{
	    				JOptionPane.showMessageDialog(myFrame, "Salah masukinnya :(");	
    				}
	    			else
	    			{
	    				//create my server thread
	    				myServerObj = new MyServer(getMyClassObject());
	    				
	    				//start socket
	    				myServerObj.StartSocket(Integer.parseInt(portTextField.getText()), ipAddressTextField.getText());
	    				
	    				//start thread
	    				myServerObj.start();
	    				
	    				//disable buttons
	    				btnConnect.setEnabled(false);
	    				btnStopServer.setEnabled(true);
	    				myMapUpdate.setMyTurn(false);
	    			}
	    		}
	    		//player as a client
	    		else
	    		{
	    			//check if selection is done (it cannot start connection without the ships been allocated)
	    			if (myMapUpdate.isSelectionDone())
	    			{
	    				//check if ip address and port are not empty
	    				if (ipAddressTextField.getText().equals("") || portTextField.getText().equals(""))
	    				{
		    				JOptionPane.showMessageDialog(myFrame, "Salah masukin :(");	
	    				}
	    				else
	    				{
	    					//try to start connection to the server
	    					if (myClientObj.StartConnection(ipAddressTextField.getText(), Integer.parseInt(portTextField.getText())))
	    					{
	    						//disable buttons
	    						btnConnect.setEnabled(false);
	    	    				btnStopServer.setEnabled(true);
	    	    				myMapUpdate.setMyTurn(false);
	    					}
	    				}
	    			}
	    			else
	    			{
	    				JOptionPane.showMessageDialog(myFrame, "Susun dulu kapalnya :)");
	    			}
	    		}
	    	}
	    });
	    this.btnConnect.setBounds(6, 142, 109, 23);
	    connectionPanel.add(btnConnect);
	    
	    JLabel lblPort = new JLabel("Port:");
	    lblPort.setBounds(123, 86, 46, 14);
	    connectionPanel.add(lblPort);
	    
	    this.btnStopServer = new JButton("Stop Server");
	    this.btnStopServer.setEnabled(false);
	    
	    //stop connection listener
	    this.btnStopServer.addActionListener(new ActionListener() 
	    {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		//check if it is server
	    		if (IsServer)
	    		{
	    			//check is there is a client connected
	    			if (myServerObj.hasClient())
	    			{
	    				//inform the client
	    				myServerObj.SendMessage("B");
	    				
	    				//stop communication
	    				myServerObj.StopCommunication();
	    				
	    				//stop server
		    			myServerObj.StopServer();
	    			}
	    			else
	    			{
	    				//not client, just need to stop the server
	    				myServerObj.StopServer();
	    			}
	    			//stop thread
	    			myServerObj.stop();
	    			
	    			//enable buttons
	    			btnConnect.setEnabled(true);
    				btnStopServer.setEnabled(false);
	    		}
	    		//playing as client
	    		else
	    		{
	    			//inform server
	    			myClientObj.SendMessage("B");
	    			
	    			//stop client
	    			myClientObj.StopClient();
	    			
	    			//enable buttons
	    			btnConnect.setEnabled(true);
    				btnStopServer.setEnabled(false);
	    		}
	    		myMapUpdate.setMyTurn(false);
	    	}
	    });
	    this.btnStopServer.setBounds(123, 142, 109, 23);
	    connectionPanel.add(this.btnStopServer);
	}
	
	private void initializeChatPanel()
	{
		JPanel chatHistoryPanel = new JPanel();
	    chatHistoryPanel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Chat History", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
	    chatHistoryPanel.setBounds(739, 318, 245, 207);
	    getContentPane().add(chatHistoryPanel);
	    chatHistoryPanel.setLayout(null);
	    
	    this.chatKit = new HTMLEditorKit();
	    this.chatDoc = new HTMLDocument();
	    
		this.chatHistory = new JTextPane();
	    this.chatHistory.setContentType("text/html");
	    this.chatHistory.setEditorKit(this.chatKit);
	    this.chatHistory.setDocument(this.chatDoc);
	    this.chatHistory.setEditable(false);
	    this.chatHistory.setBounds(10, 21, 196, 123);
	    
	    JScrollPane chatScrollPane = new JScrollPane(chatHistory);
	    
		chatHistoryPanel.setLayout(new BorderLayout());
		chatHistoryPanel.add(chatScrollPane,BorderLayout.CENTER);
		
		JPanel sendMsgPanel = new JPanel();
		sendMsgPanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		sendMsgPanel.setBounds(740, 536, 244, 75);
		getContentPane().add(sendMsgPanel);
		
		this.btnSend = new JButton("Send");
		this.btnSend.setEnabled(false);
		this.btnSend.setBounds(177, 11, 57, 53);
		this.btnSend.addActionListener(new ActionListener() 
	    {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		SendMessage2Enemy();
	    	}
	    });
	    
		JScrollPane charHistoryScrollPane = new JScrollPane();
		
		sendMsgPanel.setLayout(new BorderLayout());
		sendMsgPanel.add(charHistoryScrollPane,BorderLayout.CENTER);
		
		this.sendTxtField = new JTextPane();
		this.sendTxtField.setEnabled(false);
		this.sendTxtField.setText("Talk to your enemy...");
		this.sendTxtField.setContentType("text/html");
		charHistoryScrollPane.setViewportView(this.sendTxtField);

		this.sendTxtField.addKeyListener(new KeyListener()
		{
		    @Override
		    public void keyPressed(KeyEvent e)
		    {
		        if(e.getKeyCode() == KeyEvent.VK_TAB)
		        {
		        	//jump to Send button
		        	btnSend.requestFocusInWindow();
		        }
		    }

		    @Override
		    public void keyTyped(KeyEvent e)
		    {
		    }

		    @Override
		    public void keyReleased(KeyEvent e)
		    {
		    }
		});
		
		/*
		 *Input map to handle ENTER key:
		 *SHIFT+ENTER = new line
		 *ENTER       = send message
		 */
		InputMap input = this.sendTxtField.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
	    KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
	    input.put(shiftEnter, INSERT_BREAK);
	    input.put(enter, TEXT_SEND);
	    
	    ActionMap actions = this.sendTxtField.getActionMap();
	    actions.put(TEXT_SEND, new AbstractAction() 
	    {
	        @Override
	        public void actionPerformed(ActionEvent e) 
	        {
	        	SendMessage2Enemy();
	        }
	    });		
		
	    sendMsgPanel.add(this.btnSend,BorderLayout.SOUTH);
	}
	
	private void initializeComponents()
	{
		JSeparator separatorH = new JSeparator();
		separatorH.setBounds(10, 391, 719, 2);
		getContentPane().add(separatorH);
		
		JSeparator separatorV = new JSeparator();
		separatorV.setOrientation(SwingConstants.VERTICAL);
		separatorV.setBounds(728, 11, 2, 600);
		getContentPane().add(separatorV);
	    
		getContentPane().setLayout(null);
		
		setTitle("BattleShip v1.0");
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.myMapUpdate.getApplicationPath()+"\\icon.png"));
		setResizable(false);
		setSize(1000, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void splashScreenHandler()
	{
		final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) 
        {
        	Graphics2D g = splash.createGraphics();
        	int progressBar = 0;
            if (g != null) 
            {
            	for(int i=0; i<100; i++)
                {
            		if ((i%25)==0)
            		{
            			progressBar += 50; 
            		}
                    renderSplashFrame(g, i, progressBar);
                    splash.update();
                    try 
                    {
                        Thread.sleep(90);
                    }
                    catch(InterruptedException e) 
                    {
                    }
                }
            }
            splash.close();   
        }
	}
	
	private void renderSplashFrame(Graphics2D g, int frame, int progressBar) 
	{
        final String[] comps = {".", "..","...","....",".....","......", ".......","........",".........","..........","...........","...........","...........","...........","..........."};
        g.setComposite(AlphaComposite.Clear);
        g.setPaintMode();
        g.setColor(Color.GRAY);
        g.drawRect(163,85,200,7);
        g.setColor(Color.RED);
        g.fillRect(163,85,progressBar,7);
        g.setColor(Color.BLACK);
        g.drawString("Loading "+comps[(frame/5)%15], 163, 105);
    }
	
	//radio buttons listener
	public void actionPerformed(ActionEvent e) 
	{
		//radio button Automatic allocation pressed
		if (e.getActionCommand().equals("Automatic allocation"))
		{
			this.myMapUpdate.restartAllocation();
			this.myRenderer.updateMatrix(this.myMapUpdate.getMatrixMap());
			this.myBoardList.repaint();
			this.btnNewAllocation.setEnabled(true);
		}
		//radio button Manual allocation pressed
		else if (e.getActionCommand().equals("Manual allocation"))
		{
			this.myMapUpdate.restartAllocation();
			this.myRenderer.updateMatrix(this.myMapUpdate.getMatrixMap());
			this.myBoardList.repaint();
			this.btnNewAllocation.setEnabled(false);
		}
		//radio button Play as server pressed
		else if (e.getActionCommand().equals("Play as server"))
		{
			this.btnConnect.setText("Start server");
			this.btnStopServer.setText("Stop server");
			this.lblMyIpAddress.setText("My IP Address:");
			this.ipAddressTextField.setText(serverIP);
			this.IsServer = true;
		}
		//radio button Play as client pressed
		else
		{
			this.btnConnect.setText("Connect");
			this.btnStopServer.setText("Disconnect");
			this.lblMyIpAddress.setText("Server IP Address:");
			this.IsServer = false;
		}
	}
	
	//write messages to the output panel
	public void writeOutputMessage(String msg)
	{
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		
		try 
		{
			this.outputKit.insertHTML(this.outputDoc, this.outputDoc.getLength(), dateFormat.format(date)+msg, 0, 0, null);
		}
		catch (BadLocationException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.outputTextField.setCaretPosition(this.outputDoc.getLength());
	}
	
	//send chat message
	private void SendMessage2Enemy()
	{
		//write in my own history
		writeChatMessage(sendTxtField.getText(), 0);
		
		//send to the enemy
		if (this.IsServer)
		{
			this.myServerObj.SendMessage("M:"+this.sendTxtField.getText());
		}
		else
		{
			this.myClientObj.SendMessage("M:"+this.sendTxtField.getText());
		}
		this.sendTxtField.setText("");
	}
	
	/*
	 * write messages to the chat history
	 * flag==0 - my message
	 * flag==1 - enemy message
	 */
	public void writeChatMessage(String msg, int flag)
	{
		String header = "";
		switch (flag)
		{
			case 0:
			{
				header =  "<font color=\"blue\">Me: </font>";
				break;
			}
			case 1:
			{
				header =  "<font color=\"red\">Enemy: </font>";
				break;
			}
		}
		
		try 
		{
			this.chatKit.insertHTML(this.chatDoc, this.chatDoc.getLength(), header, 0, 0, null);
			this.chatKit.insertHTML(this.chatDoc, this.chatDoc.getLength(), msg, 0, 0, null);
		}
		catch (BadLocationException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.chatHistory.setCaretPosition(this.chatDoc.getLength());
	}
	
	//repaint my board
	public void repaintMyBoard()
	{
		this.myBoardList.repaint();
	}
	
	//repaint enemy's board
	public void repaintMyEnemyBoard()
	{
		this.myEnemyList.repaint();
	}
	
	//enable/disable allocation buttons
	public void enableShipAllocation(boolean value)
	{
		if (value==true && this.rdbtnManualAllocation.isSelected())
		{
			this.btnNewAllocation.setEnabled(false);	
		}
		else
		{
			this.btnNewAllocation.setEnabled(value);
		}
		this.rdbtnAutomaticAllocation.setEnabled(value);
		this.rdbtnManualAllocation.setEnabled(value);
		this.btnRestartAllocation.setEnabled(value);
	}
	
	//restart allocations
	public void restartAllocations()
	{
		this.myMapUpdate.restartAllocation();
		this.myRenderer.updateMatrix(this.myMapUpdate.getMatrixMap());
		this.myBoardList.repaint();
		
		this.myEnemyMapUpdate.restartAllocation();
		this.myEnemyRenderer.updateMatrix(this.myEnemyMapUpdate.getMatrixMap());
		this.myEnemyList.repaint();
	}
	
	//return BattleShip class object
	public BattleShip getMyClassObject()
	{
		return this;
	}
	
	//return my MapUpdateHandler object
	public MapUpdateHandler getMyMapUpdate()
	{
		return this.myMapUpdate;
	}
	
	//return enemy's MapUpdateHandler object
	public MapUpdateHandler getMyEnemyMapUpdate()
	{
		return this.myEnemyMapUpdate;
	}
	
	//return server object
	public MyServer getMyServer()
	{
		return this.myServerObj;
	}
	
	//return client object
	public MyClient getMyClient()
	{
		return this.myClientObj;
	}
	
	//check if the player is server
	public boolean IsServer()
	{
		return this.IsServer;
	}
	
	//return dice object (gui to choose a number to decide who starts playing)
	public DiceHandler getDiceHandler()
	{
		return this.myDiceHandler;
	}
	
	//return game over interface
	public GameOverGui getGameOverGui()
	{
		return this.myGOScreen;
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) 
		{
    		// handle exception
    	}
		catch (ClassNotFoundException e) 
		{
    		//handle exception
    	}
		catch (InstantiationException e) 
		{
    		// handle exception
    	}
		catch (IllegalAccessException e) 
		{
			// handle exception
    	}
		new BattleShip();
	}
}
