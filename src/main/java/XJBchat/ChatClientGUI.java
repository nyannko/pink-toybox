package XJBchat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

public class ChatClientGUI extends JFrame {

    private URI serverUri;
    private ChatClient client;
    private ChatClientGUI gui;
    private String clientName;

    // GUI component
    private JFrame mainFrame;
    private JPanel controlPanel;
    private JPanel onlinePanel;
    private JLabel statusLabel;
    private JScrollPane onlineScrollPanel;
    private JList onlineList;
    private JTextPane textField;
    private JTextField typingField;
    private JButton connectButton;
    private JButton sendButton;
    private JButton closeButton;
    private JScrollPane scrollPane;

    // message display attributes
    SimpleAttributeSet myStyle;
    SimpleAttributeSet elseStyle;

    // logger
    private Logger logger;


    public ChatClientGUI(URI serverUri) {
        // init GUI
        prepareGUI(600, 400);

        // init client
        this.gui = this;
        this.serverUri = serverUri;

        // logger
        logger = (Logger) LoggerFactory.getLogger(ChatClientGUI.class);
        logger.setLevel(Level.DEBUG);
    }

    public ChatClientGUI(URI serverUri, int width, int height) {
        prepareGUI(width, height);

        this.gui = this;
        this.serverUri = serverUri;

        // logger
        logger = (Logger) LoggerFactory.getLogger(ChatServer.class);
        logger.setLevel(Level.DEBUG);
    }

    private void prepareGUI(int width, int height) {
        String headerMessage = "Chat client";
        mainFrame = new JFrame(headerMessage);
        mainFrame.setSize(width, height);
        // todo: AUI
//        mainFrame.setResizable(false);

        // exit button
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        // create control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // todo: fixed place
        // create buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        sendButton = new JButton("Send");
        connectButton = new JButton("Connect");
        closeButton = new JButton("Close");
        buttonGroup.add(sendButton);
        buttonGroup.add(connectButton);
        buttonGroup.add(closeButton);

        // create statusLabel, unused now
        // statusLabel = new JLabel("Online Users: 0 ");
        // statusLabel.setSize(10, 10);

        onlineList = new JList();
        onlineScrollPanel = new JScrollPane(onlineList);

        onlineScrollPanel.setPreferredSize(new Dimension(160, 100));
        onlineScrollPanel.setBorder(new TitledBorder("Online Users: 0"));

        mainFrame.getContentPane().add(BorderLayout.EAST, onlineScrollPanel);

        // create text field
        textField = new JTextPane();
        textField.setEditable(false);
        scrollPane = new JScrollPane(textField);

        // create typing field
        typingField = new JTextField(25);

        // add components
        controlPanel.add(typingField);
        controlPanel.add(sendButton);
        controlPanel.add(connectButton);
        controlPanel.add(closeButton);

        mainFrame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        mainFrame.getContentPane().add(BorderLayout.SOUTH, controlPanel);

        // bind events
        sendButton.addActionListener(new ButtonClickListener());
        connectButton.addActionListener(new ButtonClickListener());
        closeButton.addActionListener(new ButtonClickListener());

        // bind key shortcut
        typingField.addKeyListener(new MyKeyListener());

        // show mainframe
        mainFrame.setVisible(true);

        // todo: show error message in gray
        // set attributes
        myStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(myStyle, new Color(0, 102, 0));
        StyleConstants.setBold(myStyle, true);

        elseStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(elseStyle, Color.BLUE);
        StyleConstants.setBold(elseStyle, true);
    }


    private void triggerSendButton() {
        // check first connection && replicated connections
        if (client == null || !client.isOpen()) {
            String info = "Not connected...Please check the network status";
            appendMessageBack(null, null, info);
            return;
        }

        String message = typingField.getText();
        if (message.length() == 0) {
            String info = "Say something!";
            appendMessageBack(null, null, info);
            return;
        }

        // set empty field
        typingField.setText("");

        // send messages to server if connection is open
        if (client != null && client.isOpen()) {
            // append timestamp
            String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
            insertText(textField, client.getNickName() + " " + timeStamp + "\n", myStyle);
            insertText(textField, message + "\n", null);

            // send request to server
            JSONObject request = client.createJSONRequest(StringConstants.BROADCAST, message);
            client.send(request.toString());
        }
    }

    private void insertText(JTextPane textField, String s, SimpleAttributeSet attributeSet) {
        StyledDocument doc = textField.getStyledDocument();
        try {
            logger.debug("" + attributeSet);
            doc.insertString(doc.getLength(), s, attributeSet);
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

        scrollToBottom(scrollPane);
    }

    // taken from https://stackoverflow.com/a/31317110
    private void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    private void triggerCloseButton() {
        // check first connection
        if (client == null) {
            String info = "Client not connected";
            appendMessageBack(null, null, info);
            return;
        }
        // check duplicated connections
        if (!client.isOpen()) {
            String info = "Client has already closed";
            logger.debug("Client has already closed");
            appendMessageBack(null, null, info);
            return;
        }
        client.close();

        // remove from jList
        DefaultListModel model = (DefaultListModel) onlineList.getModel();
        model.clear();

        onlineScrollPanel.setBorder(new TitledBorder("Online Users: 0"));
        insertText(textField, "Client closed\n", null);
    }

    private void triggerConnectButton(String clientName) {
        if (client != null && client.isOpen()) {
            String info = "You are already connected to server, close current connection first";
            logger.debug("Client has already closed");
            appendMessageBack(null, null, info);
            return;
        }

        // otherwise, create a new socket for connection
        client = new ChatClient(serverUri);
        // prepare GUI for real-time message check
        // this should be prepared before call connect() in order to receive exceptions from chat client
        client.setGUI(gui);

        client.connect();

        // async..
        if (!client.isOpen()) {
            String info = "Connecting...";
            appendMessageBack(null, null, info);
        }

        this.clientName = clientName;
    }

    // call by client
    public void appendMessageBack(String senderName, String timeStamp, String message) {
        if (senderName != null && timeStamp != null) {
            insertText(textField, senderName, elseStyle);
            insertText(textField, " " + timeStamp + "\n", elseStyle);
        }
        insertText(textField, message + "\n", null);
    }

    // call by client
    public void insertImage(String filePath) {
        // align pic problematic
//        insertText(textField, "sdf\n", null);
        textField.insertIcon(new ImageIcon(filePath));

        scrollToBottom(scrollPane);
    }

    // call by client
    public void appendOnlineList(Set<String> onlineClients) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String p : onlineClients) {
            model.addElement(p);
        }
        onlineList.setModel(model);

        onlineScrollPanel.setBorder(new TitledBorder("Online Users: " + model.size()));
    }

    class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == connectButton) {
                triggerConnectButton(gui.clientName);

            } else if (e.getSource() == sendButton) {
                triggerSendButton();

            } else if (e.getSource() == closeButton) {
                triggerCloseButton();

            }
        }
    }

    class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
                // todo
                // typingField.append("\n");
            } else if (e.getModifiersEx() != KeyEvent.SHIFT_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
                triggerSendButton();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // todo: only if enter key is released, typingField is set to empty
        }
    }


}
