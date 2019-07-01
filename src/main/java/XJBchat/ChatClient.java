package XJBchat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatClient extends WebSocketClient {

    private StartClientGUI gui;
    private String nickname;
    // todo: better not String
    private Set<String> onlineClients;

    public ChatClient(URI serverUri) {
        super(serverUri);
        nickname = "";
        onlineClients = new CopyOnWriteArraySet<>();
    }

    public void setGUI(StartClientGUI gui) {
        this.gui = gui;
    }

    public String getNickName() {
        return nickname;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        String s = "Connect to server: " + getURI();
        System.out.println(s);
        gui.appendMessageBack(null, null, s);
    }

    @Override
    public void onMessage(String s) {
        // deserialization str --> json -- var
        JSONObject jsonObject = new JSONObject(s);
        System.out.println(jsonObject.toString());
        String prefix = jsonObject.getString("prefix");

        switch (prefix) {
            case MessagePrefix.REGISTER:
                handleRegister(jsonObject);
                break;
            case MessagePrefix.BROADCAST:
                handleBroadcast(jsonObject);
                break;
            case MessagePrefix.UPDATE_NEW_CLIENT:
                handleUpdateNewClient(jsonObject);
                break;
            case MessagePrefix.REMOVE_OFFLINE_CLIENT:
                handleRemoveOfflineClient(jsonObject);
                break;
        }
    }

    private void handleRegister(JSONObject jsonObject) {
        nickname = jsonObject.getString("name");
        JSONArray arr = jsonObject.getJSONArray("onlinelist");
        for (int i = 0; i < arr.length(); i++) {
            String client = arr.getString(i);
            onlineClients.add(client);
        }
        System.out.println(onlineClients);
        gui.appendMessageBack(null, null, "Your nickname is " + nickname);
    }

    private void handleBroadcast(JSONObject jsonObject) {
        // read JSON Object
        String name = jsonObject.getString("name");
        String message = jsonObject.getString("message");
        System.out.println(name + " " + message);
        appendToGUI(name, message);
    }

    private void handleUpdateNewClient(JSONObject jsonObject) {
        String name = jsonObject.getString("newClient");
        System.out.println(name + " is now online");
        onlineClients.add(name);
        int onlineClientsNum = onlineClients.size();

        // append back to client GUI
        System.out.println("online client number: " +  onlineClientsNum + " " + onlineClients);
    }

    private void handleRemoveOfflineClient(JSONObject jsonObject) {
        String name = jsonObject.getString("offlineClient");
        System.out.println(name + " is now offline");
        onlineClients.remove(name);
        int onlineClientsNum = onlineClients.size();

        // append back to client GUI
        System.out.println("remaining client number: " +  onlineClientsNum + " " + onlineClients);
    }


    private void appendToGUI(String senderName, String message) {
        String timeStamp = createReceivedTimeStamp();
        gui.appendMessageBack(senderName, timeStamp, message);
    }


    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Client closed");
    }

    // create client side timestamp
    private String createReceivedTimeStamp() {
        String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
        return timeStamp;
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Client error " + e);
    }

    public JSONObject createJSONRequest(String prefix, String message) {
        JSONObject request = new JSONObject();
        request.put("prefix", prefix);
        request.put("name", getNickName());
        request.put("message", message);
        return request;
    }
}
