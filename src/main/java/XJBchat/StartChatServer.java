package XJBchat;

import java.net.InetSocketAddress;

public class StartChatServer {
    public static void main(String[] args) {
        int port = 8877;
        InetSocketAddress addr = new InetSocketAddress(port);
        ChatServer server = new ChatServer(addr);
        server.start();
    }
}
