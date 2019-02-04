package server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;

public class Server
{
    private static int PORT_NUMBER;
    private static String SERVER_IP;
    protected static String SERVER_DATA_DIR;

    private ServerSocket serverSocket;
    private Socket socket;

    private boolean online = true;

    public ArrayList<ServerConnection> connections = new ArrayList<ServerConnection>();
    public int connIndex;

    public static void main(String[] args)
    {
        new Server();
    }

    public Server ()
    {
        loadXMLFile();

        startServer();


    }

    /** Load server configuration **/
    public static void loadXMLFile()
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
                    case "serverdata":
                        SERVER_DATA_DIR = properties.getProperty(key);
                        break;

                }
            }
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /** Launches server*/
    private void startServer()
    {
        System.out.printf("SERVER IS ONLINE (PORT:"+PORT_NUMBER+")\n");

        /** Connections **/
        try
        {
            serverSocket = new ServerSocket(PORT_NUMBER);
            connIndex = 0;
            while(online)
            {
                /** Wait for the connection **/
                socket = serverSocket.accept();
                System.out.printf("Estabilishing connection(IP: "+SERVER_IP+"; PORT: "+PORT_NUMBER+")...\n");

                /** Start connection, add to Array**/
                ServerConnection serverConnection = new ServerConnection(socket, this, connIndex);
                serverConnection.start();
                connections.add(serverConnection);
                connIndex++;
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }


    /** ServerConnection->Server communication **/
    public static void notifyServer(String message)
    {
        System.out.println(message);
    }
}
