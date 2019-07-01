package XJBchat;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONObject; // add to pom.xml

public class ChatServer extends WebSocketServer {

    private Set<WebSocket> webSockets;
    // <clientNickName, clientWebSocket>
    private Map<WebSocket, UserInfo> users;
    private Random rand;

    private List<String> lastNames;
    private List<String> surNames;

    private static int count;

    private static synchronized void clientNumberIncrement() {
        count++;
        System.out.println("Connected client number: " + count); // change to log
    }

    private static synchronized void clientNumberDecrement() {
        count--;
        System.out.println("Connected client number: " + count); // change to log
    }

    public ChatServer(InetSocketAddress addr) {
        super(addr);
        webSockets = new CopyOnWriteArraySet<>();
        users = new ConcurrentHashMap<>();

        // for user name generator
        rand = new Random();
        lastNames = new ArrayList<>(
                Arrays.asList("Lazy", "Angry", "Happy", "Scary", "Fancy", "Nasty", "Crying", "Silent"));
        surNames = new ArrayList<>(
                Arrays.asList("Cat", "Dog", "Sheep", "Onion", "Bird", "Bear", "Cherry", "Mage"));
    }

    @Override
    public void onStart() {
        System.out.println("onStart(), Server open on port: " + getPort());
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Server onOpen()");

        // add webSockets
        webSockets.add(webSocket);
        clientNumberIncrement();

        String userNamePlaceHolder = userNameGenerator();
        UserInfo userInfo = new UserInfo(userNamePlaceHolder);
        users.put(webSocket, userInfo);

        // send random generated nickname back to client
        JSONObject userInitInfo = createJSONReply(MessagePrefix.REGISTER, userInfo);
        webSocket.send(userInitInfo.toString());
    }

    public JSONObject createJSONReply(String prefix, UserInfo userInfo) {
        JSONObject reply = new JSONObject();
        reply.put("prefix", prefix);
        reply.put("name", userInfo.getName());
        reply.put("message", userInfo.getMessage());
        // todo: add timestamp
        return reply;
    }

    // todo: improve username random generator
    private String userNameGenerator() {
        String userNamePlaceHolder =
                lastNames.get(rand.nextInt(8)) + " "
                        + surNames.get(rand.nextInt(8));
        while (users.values().contains(userNamePlaceHolder)) {
            userNamePlaceHolder =
                    lastNames.get(rand.nextInt(8)) + " "
                            + surNames.get(rand.nextInt(8));
        }
        return userNamePlaceHolder;
    }


    @Override
    public void onMessage(WebSocket webSocket, String s) {
        // deserialize string to JSONObject
        JSONObject jsonObject = new JSONObject(s);

        // get message prefix
        String prefix = jsonObject.getString("prefix");

        // demultiplexing
        switch (prefix) {
            case MessagePrefix.REGISTER:
                handleRegister(webSocket, jsonObject);
                break;
            case MessagePrefix.BROADCAST:
                handleBroadcast(webSocket, jsonObject);
                break;
            case MessagePrefix.ONLINE_CLIENTS:
                sendOnlineList(webSocket, jsonObject);
        }
    }

    private void sendOnlineList(WebSocket webSocket, JSONObject jsonObject) {
    }

    private void handleRegister(WebSocket w, JSONObject jsonObject) {
    }

    public void handleBroadcast(WebSocket webSocket, JSONObject jsonObject) {
        // simply forward(broadcast) to other clients
        for (WebSocket ws : webSockets) {
            if (webSocket != ws) {
                ws.send(jsonObject.toString());
            }
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("onClose(), Server stop");
        webSockets.remove(webSocket);
        users.remove(webSocket);
        clientNumberDecrement();
        System.out.println("remaining client: " + count);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("onError(), System error: " + e);
        webSockets.remove(webSocket);
        users.remove(webSocket);
        clientNumberDecrement();
        System.out.println("remaining client: " + count);
    }

    public void broadCast(String s) {
        String message = "broadcast message";
        for (WebSocket w : webSockets) {
            w.send(s);
        }
    }
}
