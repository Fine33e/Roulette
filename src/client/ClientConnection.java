package client;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends Thread {
    private Socket socket;
    private String playerName;
    private String total= "500";
    public Client client;

    private DataInputStream din;
    private DataOutputStream dout;
    protected RouletteGame theGame;

    protected ClientConnection(Socket socket, Client client, String playerName) {
        this.socket = socket;
        this.playerName = playerName;
        this.client = client;
    }

    /** Send message to server (methods used for chat and for commands)**/
    protected void sendStringToServer(String clientMessage) {
        try {
            dout.writeUTF(clientMessage);
            dout.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

        public void run1(){
        /** Start the game **/

        String[] launchArgs = new String[2];
        launchArgs[0] = playerName;
        launchArgs[1] = total;
        theGame = new RouletteGame();
        theGame.main(launchArgs);
        theGame.getClient(this.client);
        total= theGame.getTotal();
            sendStringToServer(total);
    }
    public void run2(){
        sendStringToServer(total);
    }
    public void run()
    {
        try
        {

            /** Prepare chat **/
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            sendStringToServer(playerName);
            sendStringToServer(total);

        }
        catch (IOException e) { e.printStackTrace(); }
        while (socket.isConnected())
        {
            try
            {
                while (din.available() == 0)
                {
                    Thread.sleep(1);
                }
                String serverReply = din.readUTF();
                System.out.println(serverReply);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        close();
    }

    protected void close()
    {
        try
        {
            theGame.close();
            din.close();
            dout.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void updatePlayerWallet(Double money)
    {
        try {
            dout.writeUTF("/!wUpdate");
            dout.writeDouble(money);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
