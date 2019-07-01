package XJBchat;

import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StartClientGUI {

    private URI serverUri;
    private ChatClient client;
    private StartClientGUI gui;
    private String clientName;

    // GUI component
    private JFrame mainFrame;
    private JPanel controlPanel;
    private JLabel statusLabel;
    private JTextPane textField;
    private JTextField typingField;
    private JButton connectButton;
    private JButton sendButton;
    private JButton closeButton;
    private JScrollPane scrollPane;

    // message display attributes
    SimpleAttributeSet myStyle;
    SimpleAttributeSet elseStyle;


    public StartClientGUI(URI serverUri) {
        // init GUI
        prepareGUI();

        // init client
        this.gui = this;
        this.serverUri = serverUri;
    }

    private void prepareGUI() {
        String headerMessage = "Chat client";
        mainFrame = new JFrame(headerMessage);
        mainFrame.setSize(600, 400);

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
        statusLabel = new JLabel();
        statusLabel.setSize(700, 700);

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
        controlPanel.add(statusLabel);

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
        String message = typingField.getText();
        if (message.length() == 0) return;

        // set empty field
        typingField.setText("");

        // send messages to server if connection is open
        if (client != null && client.isOpen()) {
            // append timestamp
            String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
            insertText(textField, client.getNickName() + " " + timeStamp + "\n", myStyle);
            insertText(textField, message + "\n", null);

            // send request to server
            JSONObject request = client.createJSONRequest(MessagePrefix.BROADCAST, message);
            client.send(request.toString());
        } else {
            insertText(textField, "Not connected...Please check the network status\n", null);
        }
    }

    private void insertText(JTextPane textField, String s, SimpleAttributeSet attributeSet) {
        StyledDocument doc = textField.getStyledDocument();
        try {
            System.out.println(attributeSet);
            doc.insertString(doc.getLength(), s, attributeSet);
        } catch (Exception e) {
            System.out.println(e);
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
        if (client == null) {
            String info = "Client not connected";
            appendMessageBack(null, null, info);
            return;
        }
        if (client != null && !client.isOpen()) {
            String info = "Client has already closed";
            System.out.println("Client has already closed");
            appendMessageBack(null, null, info);
            return;
        }
        client.close();

        insertText(textField, "Client closed\n", null);

        statusLabel.setText("client " + clientName + " disconnect to server");
    }

    private void triggerConnectButton(String clientName) {
        // boundary check: if client in not null and client is in use, reuse the same connection
        if (client != null && client.isOpen()) return;

        // otherwise, create a new socket for connection
        client = new ChatClient(serverUri);
        client.connect();

        // prepare GUI for real-time message check
        client.setGUI(gui);

        // set client status
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

    public static void main(String[] args) {
        URI serverUri = URI.create("ws://localhost:8877");
        StartClientGUI gui = new StartClientGUI(serverUri);
    }
}
