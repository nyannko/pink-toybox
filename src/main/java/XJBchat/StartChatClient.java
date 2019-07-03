package XJBchat;

import java.awt.*;
import java.net.URI;

public class StartChatClient {

    final static int clientNum = 3;
    final static int windowWidth = 800;
    final static int windowHeight = 400;

    public static void main(String[] args) {
        URI serverUri = URI.create("ws://localhost:8877");

        // https://stackoverflow.com/a/22534931
        EventQueue.invokeLater(() ->
        {
            for (int i = 0; i < clientNum; i++) {
                new ChatClientGUI(serverUri);
            }
        });
    }
}
