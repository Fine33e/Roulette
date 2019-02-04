package server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class ServerConnection extends Thread
{

    private int connectionIndex;
    private String playerName = "unknown";
    private String total;

    private Socket socket;
    private Server server;

    private DataInputStream din;
    private DataOutputStream dout;

    ServerConnection(Socket socket, Server server, int connectionIndex)
    {
        super("ServerConnectionThread");
        this.socket = socket;
        this.server = server;
        this.connectionIndex = connectionIndex;
    }

    public void run()
    {
        try
        {
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            /** Identify player **/
            setPlayerName(din.readUTF());
            setTotal(din.readUTF());
            sendStringToClient("Chat avtivated. Your name: "+playerName);
            sendStringToAllClients(playerName+" has joined the chat.");
            server.notifyServer(playerName+" is connected.");

            communicationStart();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** Start SERVER<->PLAYER communication **/
    private void communicationStart()
    {
        try
        {
            while(socket.isConnected())
            {
                /** Get message from the player **/
                while(din.available() == 0)
                {
                    try
                    {
                        Thread.sleep(1);
                    } catch (InterruptedException e) { e.printStackTrace(); }
                }
                String receivedMessage = din.readUTF();
                handleCommand(receivedMessage);
                if(receivedMessage.equals("/quit")){break;}

            }
        } catch (IOException e) { server.notifyServer("Tried to send a message, but stream is closed. (user:"+playerName+")"); }
    }

    /** Handle player's command **/
    private void handleCommand(String receivedMessage)
    {
        try {
            if (receivedMessage.equals("/quit"))
            {
                socket.close();
                din.close();
                dout.close();
                server.connections.remove(connectionIndex);
                for (int i = connectionIndex; i < server.connections.size(); i++) {
                    server.connections.get(i).setConnectionIndex(server.connections.get(i).getConnectionIndex() - 1);
                }
                sendStringToAllClients(playerName + " has left the chat.");
                server.notifyServer(playerName + " is disconnected.");
                server.connIndex--;
            }

            else
            {
                chat(receivedMessage);
            }
                if (receivedMessage.equals("/!online"))
                {
                    sendStringToAllClients("Currently online:----------------------");
                    for (int i = 0; i < server.connections.size(); i++) {
                        sendStringToAllClients((i + 1) + ". " + server.connections.get(i).getPlayerName()+" money: $"+getTotal() );
                    }
                    sendStringToAllClients("---------------------------------------");
                } else if (receivedMessage.equals("/!commands")) {
                    sendStringToAllClients("Currently online:----------------------\n" +
                            "Available commands:\n" +
                            "/ - write message\n" +
                            "/!game - play roulette\n" +
                            "/!online - to show who is online\n" +
                            "/!commands - to show all commands\n" +
                            "-----------------------------------------------");
                }else if(receivedMessage.equals("/!money")){

                }



        }
        catch(IOException e) { e.printStackTrace(); }
    }

    /** Sends message to connected client**/
    private void sendStringToClient(String message)
    {
        try
        {
            dout.writeUTF(message);
            dout.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Sends message to all connected client**/
    private void sendStringToAllClients(String message)
    {
        for (int i = 0; i < server.connections.size(); i++)
        {
            ServerConnection serverConnection = server.connections.get(i);
            serverConnection.sendStringToClient("SERVER>>"+message);
        }
    }

    /** Broadcasts message to other clients**/
    private void chat(String message)
    {
        for (int i = 0; i < server.connections.size(); i++)
        {
            ServerConnection serverConnection = server.connections.get(i);
            if(!(serverConnection.getPlayerName()==this.playerName))
                serverConnection.sendStringToClient(this.getPlayerName()+">>"+message);
        }
    }


    /** Setter and Getters **/
    private String getPlayerName() {
        return playerName;
    }
    private String getTotal() {
        return total;
    }
    private void setTotal(String total) {
        this.total = total;
    }
    private void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    private int getConnectionIndex() {
        return connectionIndex;
    }

    private void setConnectionIndex(int connectionIndex) {
        this.connectionIndex = connectionIndex;
    }


}
