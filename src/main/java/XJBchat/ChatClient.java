package XJBchat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatClient extends WebSocketClient {

    private ChatServer server;
    private ChatClientGUI gui;
    private String nickname;

    // todo: better not String
    private Set<String> onlineClients;

    private Logger logger;

    public ChatClient(URI serverUri) {
        super(serverUri);

        logger = (Logger) LoggerFactory.getLogger(ChatClient.class);
        logger.setLevel(Level.DEBUG);

        nickname = "";
        onlineClients = new CopyOnWriteArraySet<>();
    }

    public Set<String> getOnlineClients() {
        return onlineClients;
    }

    public void setGUI(ChatClientGUI gui) {
        this.gui = gui;
    }

    public String getNickName() {
        return nickname;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        String s = "Connect to server: " + getURI();
        logger.debug(s);
        gui.appendMessageBack(null, null, s);
        // when a client online, it must call server to notify others it's online
        // server.notify();
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(getByteArrayFromByteBuffer(bytes)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.debug("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());

        // todo: image filename generator
        String fileName = "./pic/nyanko.jpg";
        try {
            ImageIO.write(image, "jpg", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        gui.insertImage(fileName);
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    @Override
    public void onMessage(String s) {
        // deserialization str --> json -- var
        JSONObject jsonObject = new JSONObject(s);
        logger.debug(jsonObject.toString());
        String prefix = jsonObject.getString("prefix");

        switch (prefix) {
            case StringConstants.REGISTER:
                handleRegister(jsonObject);
                break;
            case StringConstants.BROADCAST:
                handleBroadcast(jsonObject);
                break;
            case StringConstants.UPDATE_NEW_CLIENT:
                handleUpdateNewClient(jsonObject);
                break;
            case StringConstants.REMOVE_OFFLINE_CLIENT:
                handleRemoveOfflineClient(jsonObject);
                break;
        }
    }

    private void handleRegister(JSONObject jsonObject) {
        boolean status = jsonObject.getBoolean(StringConstants.REGISTER_STATUS);
        if (!status) return;

        // else online
        nickname = jsonObject.getString(StringConstants.NICKNAME);
        JSONArray arr = jsonObject.getJSONArray(StringConstants.ONLINE_LIST);
        for (int i = 0; i < arr.length(); i++) {
            String client = arr.getString(i);
            onlineClients.add(client);
        }
        logger.debug(" " + onlineClients);
        gui.appendMessageBack(null, null, "Your nickname is " + nickname);

        // update client to online list
        gui.appendOnlineList(onlineClients);
    }

    private void handleBroadcast(JSONObject jsonObject) {
        // read JSON Object
        String name = jsonObject.getString(StringConstants.NICKNAME);
        String message = jsonObject.getString(StringConstants.MESSAGE);
        logger.debug(name + " " + message);
        appendToGUI(name, message);
    }

    private void handleUpdateNewClient(JSONObject jsonObject) {
        String name = jsonObject.getString(StringConstants.ONLINE_CLIENT);
        logger.debug(name + " is now online");
        onlineClients.add(name);
        int onlineClientsNum = onlineClients.size();

        // append back to client GUI
        logger.debug("online client number: " + onlineClientsNum + " " + onlineClients);
        gui.appendOnlineList(onlineClients);
    }

    private void handleRemoveOfflineClient(JSONObject jsonObject) {
        String name = jsonObject.getString(StringConstants.OFFLINE_CLIENT);
        logger.debug(name + " is now offline");
        onlineClients.remove(name);
        int onlineClientsNum = onlineClients.size();

        // append back to client GUI
        logger.debug("remaining client number: " + onlineClientsNum + " " + onlineClients);
        gui.appendOnlineList(onlineClients);
    }


    private void appendToGUI(String senderName, String message) {
        String timeStamp = createReceivedTimeStamp();
        gui.appendMessageBack(senderName, timeStamp, message);
    }


    @Override
    public void onClose(int i, String s, boolean b) {
        logger.debug("Client closed");
    }

    // create client side timestamp
    private String createReceivedTimeStamp() {
        String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    @Override
    public void onError(Exception e) {
        // server may refused connection
        gui.appendMessageBack(null, null,
                "Server refused connection...please check the network status");
        logger.debug("Client error " + e);
    }

    public JSONObject createJSONRequest(String prefix, String message) {
        JSONObject request = new JSONObject();
        request.put(StringConstants.PREFIX, prefix);
        request.put(StringConstants.NICKNAME, getNickName());
        request.put(StringConstants.MESSAGE, message);
        return request;
    }
}
