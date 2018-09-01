package communicationpkg;

/*
 * Protocol:
 * 
 * <ID>:<MSG>
 * 
 * Send IDs:
 * 
 * G  - Send index played
 * A  - Send if it was hit or not (1: hit;  0: not a hit)
 * DA - Send dice answer (my number)
 * E  - Game is over
 * C  - First connection, inform the server
 * M  - Chat message
 * B  - Close connection
 * 
 * Receive IDs:
 * 
 * G  - receive during the game, with shot position
 * A  - receive during the game, if shot position was a hit or not
 * DQ - inform to open the dice handler (to pick a number)
 * DA - inform who will start to play
 * DE - dice error, both players picked the same number, dice will open again
 * E  - Game is over
 * S  - First connection, server informs game can start
 * SN - First connection, server is not ready yet
 * M  - Chat message
 * B  - Server will close connection
 * 
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import interfacepkg.BattleShip;
import interfacepkg.MyDefines;

public class MyClientReceive extends Thread 
{
	//Initialize first so it will get easy to change things in the future
	private Socket socket;
	private ObjectInputStream in;
	private boolean myBreak = false;
	private String msg = "";
	private BattleShip gui;
	
	public MyClientReceive(Socket socket, BattleShip gui) 
	{
		this.socket = socket;
		this.gui = gui;
	}
	
	public void run()
	{
		// Open the InputStream
		try
		{
			this.in = new ObjectInputStream(this.socket.getInputStream());
		}
		catch(IOException e)
		{
			System.out.println("Could not get input stream from "+toString());
		}
		
		// Enter process loop
		while(!this.myBreak)
		{
			try 
			{
				this.msg = (String) this.in.readObject();
				if (!this.msg.equals(""))
				{
					switch (this.msg.charAt(0))
					{
						case 'G': //message from server about game
						{
							int index = Integer.parseInt(this.msg.substring(2));
							this.gui.writeOutputMessage(" - Incoming fire at: "+this.gui.getMyMapUpdate().getLine(index)+this.gui.getMyMapUpdate().getColumn(index));
							this.gui.getMyMapUpdate().setMyTurn(true);
							if (this.gui.getMyMapUpdate().hitSomething(index))
							{
								this.gui.writeOutputMessage(" - Tdakkkk kapalmu terkena serangan !!!");
								this.gui.getMyClient().SendMessage("A:1;"+index);
							}
							else
							{
								this.gui.writeOutputMessage(" - Sekkkk Nyaris bangg!!!");
								this.gui.getMyClient().SendMessage("A:0;"+index);
							}
							this.gui.getMyMapUpdate().updatePosition(index);
							this.gui.repaintMyBoard();
							if (this.gui.getMyMapUpdate().isGameOver())
							{
								this.gui.writeOutputMessage(" - Wkwkwkwkwk cupu kalah, kurang hoki lu !");
								this.gui.getGameOverGui().ShowGameOver(0);
								this.gui.getMyClient().SendMessage("E");
							}
							else
							{
								this.gui.writeOutputMessage(" - Your turn :))) !");
							}
							break;
						}
						case 'A': //message from server hit or not
						{
							String[] impMsg = this.msg.substring(2).split(";");
							if (impMsg[0].equals("1"))
							{
								this.gui.getMyEnemyMapUpdate().setEnemyHit(Integer.parseInt(impMsg[1]), true);
								this.gui.writeOutputMessage(" - Peneteration confirmed !");
							}
							else
							{
								this.gui.getMyEnemyMapUpdate().setEnemyHit(Integer.parseInt(impMsg[1]), false);
								this.gui.writeOutputMessage(" - Yahh, makanya baca doa dulu sebelum nembak");
							}
							this.gui.repaintMyEnemyBoard();
							break;
						}
						case 'D': //dice: talking to server
						{
							if (this.msg.charAt(1)=='Q')
							{
								this.gui.getDiceHandler().ShowMyDice();
								this.gui.getMyClient().SendMessage("DA:"+this.gui.getDiceHandler().getMyNumber());
							}
							else if (this.msg.charAt(1)=='A')
							{
								String[] impMsg = this.msg.substring(3).split(";");
								this.gui.getDiceHandler().setEnemyNumber(Integer.parseInt(impMsg[1]));
								if (impMsg[0].equals("0"))
								{
									this.gui.getMyMapUpdate().setMyTurn(false);
									this.gui.writeOutputMessage(" - You chose: "+this.gui.getDiceHandler().getMyNumber()+", and your enemy chose: "+this.gui.getDiceHandler().getEnemyNumber());
									this.gui.writeOutputMessage(" - You have the 2nd advantage :(");
								}
								else
								{
									this.gui.getMyMapUpdate().setMyTurn(true);
									this.gui.writeOutputMessage(" - You chose: "+this.gui.getDiceHandler().getMyNumber()+", and your enemy chose: "+this.gui.getDiceHandler().getEnemyNumber());
									this.gui.writeOutputMessage(" - You have the 1st advantage :(");
								}
							}
							else if (this.msg.charAt(1)=='E')
							{
								this.gui.writeOutputMessage(" - Wih pilihannya sama, jodoh kali nih wkwkwkwk !");
								this.gui.writeOutputMessage(" - Please, choose another number.");
								this.gui.getDiceHandler().ShowMyDice();
								this.gui.getMyClient().SendMessage("DA:"+this.gui.getDiceHandler().getMyNumber());
							}
							break;
						}
						case 'E': //game is over
						{
							this.gui.writeOutputMessage(" - Ajigile menang lu bang !!, besok traktiran yaa wkwkwkw");
							this.gui.getGameOverGui().ShowGameOver(1);
							break;
						}
						case 'S': //message from server first connection
						{
							if (this.msg.charAt(1)=='N')
							{
								this.gui.writeOutputMessage(" - Sabar yaaa tunggu musuhnya selesai, oran sabar jodahnya cepat :)");
								this.gui.writeOutputMessage(" - Wait a few seconds and try to connect again.");
								this.gui.getMyClient().StopClient();
								this.gui.btnConnect.setEnabled(true);
				    			this.gui.btnStopServer.setEnabled(false);		
							}
							else
							{
								this.gui.writeOutputMessage(" - Enemy says: "+this.msg.substring(2));
								this.gui.btnSend.setEnabled(true);
								this.gui.sendTxtField.setEnabled(true);
								this.gui.getMyClient().setIsConnected(true);
								this.gui.enableShipAllocation(false);
							}
							break;
						}
						case 'M': //message from chat
						{
							this.gui.writeChatMessage(this.msg.substring(2),1);
							break;
						}
						case 'B': //break connection
						{
							this.gui.getMyClient().StopClient();
							this.myBreak = true;
							break;
						}
					}
				}
			}
			catch (ClassNotFoundException cnf) 
			{
				cnf.printStackTrace();
			} 
			catch (IOException io) 
			{
				this.myBreak = true;
			}
			
			// Sleep
			try
			{
				Thread.sleep(MyDefines.DELAY_200MS);
			}
			catch(Exception e)
			{
				System.out.println(toString()+" has input interrupted.");
			}
		}
		
		this.gui.writeOutputMessage(" - Connection closed!");
		this.gui.btnSend.setEnabled(false);
		this.gui.sendTxtField.setEnabled(false);
		this.gui.btnConnect.setEnabled(true);
		this.gui.btnStopServer.setEnabled(false);
		this.gui.getMyClient().setIsConnected(false);
		this.gui.enableShipAllocation(true);
		
		try 
		{
			this.socket.close();
			this.in.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
