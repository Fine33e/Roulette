package client;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client
{
    private static int PORT_NUMBER;
    private static String SERVER_IP;

    public Socket client;
    public static ClientConnection clientConnection;
    private Scanner cmd;

    String playerName;

    public static void main(String[] args)
    {
        new Client();
    }

    public Client()
    {
        loadXMLFile();
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter you nickname");
        playerName = sc.nextLine();

        /** Commands **/
        System.out.println("Available commands:\n" +
                "/ - write message\n" +
                "/!online - to show who is online\n" +
                "/!commands - to show all commands\n"+
                "/!game - play roulette\n" +
                "/quit - leave the game\n" +
                "-----------------------------------------------");

        try
        {
            client = new Socket(SERVER_IP, PORT_NUMBER);
            clientConnection = new ClientConnection(client,this, playerName);
            clientConnection.start();

            listenForInput();
        }
        catch (UnknownHostException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void listenForInput()
    {

        cmd = new Scanner(System.in);
        while(client.isConnected())
        {
            while(!cmd.hasNextLine())
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            String clientMessage = cmd.nextLine();
            Pattern compiledMessage = Pattern.compile("/");
            Matcher matcher = compiledMessage.matcher(clientMessage);
            if (clientMessage.toLowerCase().equals("quit"))
            {
                clientConnection.sendStringToServer(clientMessage);
                clientConnection.close();
                break;
            }
            else if(clientMessage.equals("/!game"))
            {
                clientConnection.run1();
            }
            else if(matcher.find()){
                clientConnection.sendStringToServer(clientMessage);
            }
        }
    }

    /** Load server's port and ip **/
    private static void loadXMLFile()
    {
        try
        {
            File file = new File("xml/server_config.xml");
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.loadFromXML(fileInput);
            fileInput.close();

            Enumeration enuKeys = properties.keys();
            String key;
            while (enuKeys.hasMoreElements())
            {
                switch(key = (String) enuKeys.nextElement())
                {
                    case "port":
                        PORT_NUMBER = Integer.parseInt(properties.getProperty(key));
                        break;
                    case "ip":
                        SERVER_IP = properties.getProperty(key);
                        break;

                }
            }
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }


}
