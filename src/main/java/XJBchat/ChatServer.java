package XJBchat;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import javax.imageio.ImageIO;

public class ChatServer extends WebSocketServer {

    private Set<WebSocket> webSockets;
    private Set<String> onlineClientList;
    private Map<WebSocket, UserInfo> users;
    private Random rand;

    private List<String> lastNames;
    private List<String> surNames;

    private static Logger logger;

    private static int count;

    private static synchronized void clientNumberIncrement() {
        count++;
        logger.debug("Connected client number: " + count);

    }

    private static synchronized void clientNumberDecrement() {
        count--;
        logger.debug("Connected client number: " + count);
    }

    public ChatServer(InetSocketAddress addr) {
        super(addr);
        logger = (Logger) LoggerFactory.getLogger(ChatServer.class);
        logger.setLevel(Level.DEBUG);

        webSockets = new CopyOnWriteArraySet<>();
        onlineClientList = new CopyOnWriteArraySet<>();
        users = new ConcurrentHashMap<>();

        // for username generator
        rand = new Random();
        lastNames = new ArrayList<>(
                Arrays.asList("Lazy", "Angry", "Happy", "Scary", "Fancy", "Nasty", "Crying", "Silent"));
        surNames = new ArrayList<>(
                Arrays.asList("Cat", "Dog", "Sheep", "Onion", "Bird", "Bear", "Cherry", "Mage"));
    }

    @Override
    public void onStart() {
        logger.info("onStart(), Server open on port: " + getPort());
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        logger.debug("Server onOpen()");

        // add webSockets
        webSockets.add(webSocket);
        clientNumberIncrement();

        String userNamePlaceHolder = userNameGenerator();
        UserInfo userInfo = new UserInfo(userNamePlaceHolder);
        users.put(webSocket, userInfo);
        onlineClientList.add(userNamePlaceHolder);

        // send random generated nickname back to client
        JSONObject userInitInfo = createJSONReply(StringConstants.REGISTER, userInfo);
        webSocket.send(userInitInfo.toString());

        // update client online list for others (SHOULD server send one client or the whole online list to client?)
        JSONObject newClient = createJSONReply(StringConstants.UPDATE_NEW_CLIENT, userInfo);
        handleBroadcast(webSocket, newClient);

        // send welcome image
        sendImage(webSocket);
    }

    // taken from https://stackoverflow.com/a/25096332
    // file -> bytes[]
    private void sendImage(WebSocket webSocket) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./pic/Neko.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        webSocket.send(byteArrayOutputStream.toByteArray());
        logger.debug("image sent");
    }

    private JSONObject createJSONReply(String prefix, UserInfo userInfo) {
        JSONObject reply = new JSONObject();
        reply.put(StringConstants.PREFIX, prefix);
        switch (prefix) {
            case StringConstants.REGISTER:
                createRegistrationReply(reply, userInfo);
                break;
            case StringConstants.UPDATE_NEW_CLIENT:
                reply.put(StringConstants.ONLINE_CLIENT, userInfo.getName());
                break;
            case StringConstants.REMOVE_OFFLINE_CLIENT:
                reply.put(StringConstants.OFFLINE_CLIENT, userInfo.getName());
                break;
        }
        // todo: add timestamp
        return reply;
    }


    private void createRegistrationReply(JSONObject reply, UserInfo userInfo) {
        reply.put(StringConstants.NICKNAME, userInfo.getName());
        reply.put(StringConstants.MESSAGE, userInfo.getMessage());
        JSONArray onlineClientArr = new JSONArray();
        for (String onlineClient : onlineClientList) {
            onlineClientArr.put(onlineClient);
        }
        reply.put(StringConstants.ONLINE_LIST, onlineClientArr);
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
        String prefix = jsonObject.getString(StringConstants.PREFIX);

        // demultiplexing
        switch (prefix) {
            case StringConstants.REGISTER:
                handleRegister(webSocket, jsonObject);
                break;
            case StringConstants.BROADCAST:
                handleBroadcast(webSocket, jsonObject);
                break;
            case StringConstants.UPDATE_NEW_CLIENT:
                sendOnlineList(webSocket, jsonObject);
                break;
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
        logger.debug("onClose(), client disconnected");

        // remove client name from online list
        String removedClientName = users.get(webSocket).getName();
        onlineClientList.remove(removedClientName);
        // broadcast again
        UserInfo userInfo = new UserInfo(removedClientName);
        JSONObject offlineClient = createJSONReply(StringConstants.REMOVE_OFFLINE_CLIENT, userInfo);
        handleBroadcast(webSocket, offlineClient);

        webSockets.remove(webSocket);
        users.remove(webSocket);
        clientNumberDecrement();

        logger.debug("remaining client number : " + count + " " + onlineClientList);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        logger.debug("onError(), System error: " + e);
        webSockets.remove(webSocket);
        users.remove(webSocket);
        clientNumberDecrement();
        logger.debug("remaining client: " + count);
    }

    public void broadCast(String s) {
        for (WebSocket w : webSockets) {
            w.send(s);
        }
    }
}
