package XJBchat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChatClient extends WebSocketClient {

    private StartClientGUI gui;
    private String nickname;

    public ChatClient(URI serverUri) {
        super(serverUri);
        nickname = "";
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
            case MessagePrefix.ONLINE_CLIENTS:
                handleOnlinelist(jsonObject);
                break;
        }
    }

    private void handleRegister(JSONObject jsonObject) {
        nickname = jsonObject.getString("name");
        gui.appendMessageBack(null, null, "Your nickname is " + nickname);
    }

    private void handleBroadcast(JSONObject jsonObject) {
        // read JSON Object
        String name = jsonObject.getString("name");
        String message = jsonObject.getString("message");
        System.out.println(name + " " + message);
        appendToGUI(name, message);
    }

    private void handleOnlinelist(JSONObject jsonObject) {
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
