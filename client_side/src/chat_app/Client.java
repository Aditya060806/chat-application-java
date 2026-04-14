package chat_app;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Client implements Runnable {
    private static final String JOIN_SIGNAL = "__join__";

    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private String name;

    // GUI Components
    private JFrame frame;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;

    private Font emojiFont(int style, int size) {
        Font primary = new Font("Segoe UI Emoji", style, size);
        if (primary.canDisplayUpTo("😀🎉❤️") == -1) {
            return primary;
        }

        Font fallback = new Font("Dialog", style, size);
        if (fallback.canDisplayUpTo("😀🎉❤️") == -1) {
            return fallback;
        }

        return primary;
    }

    Client(String host, int port, String name) {
        this.name = name;
        
        // Safety: Auto-remove protocols if user pastes full URL by mistake (like "tcp://blcai-...")
        host = host.replace("tcp://", "").replace("http://", "").replace("https://", "");
        
        setupGUI(); // Initialize GUI first

        try {
            socket = new Socket(host, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            
            appendChatMessage("Server", "Connected to " + host + ":" + port, false);
            new Thread(this).start(); // Listen for incoming messages
            processMessage(name + " " + JOIN_SIGNAL);
            
        } catch (Exception e) {
            appendChatMessage("Server", "Connection failed to " + host + ":" + port, false);
            appendChatMessage("Server", "Error: " + e.getMessage(), false);
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
            
            // Show popup error message
            JOptionPane.showMessageDialog(frame, 
                "Could not connect to the server.\n\nPlease check that the server is running and the address is correct.\n\nError: " + e.getMessage(),
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupGUI() {
        frame = new JFrame("ChatApp - " + name);
        frame.setSize(440, 740);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 246, 250));

        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(new Color(245, 246, 250));
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(new Color(245, 246, 250));
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input panel at the bottom
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Input field styling with rounded shape and placeholder
        inputField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2.dispose();
                }
                super.paintComponent(g);
                
                // Placeholder
                if(getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(150, 150, 150));
                    g2.setFont(getFont());
                    int fm = g.getFontMetrics().getAscent();
                    g2.drawString("Message...", 15, (getHeight() + fm) / 2 - 2);
                    g2.dispose();
                }
            }
        };
        inputField.setFont(emojiFont(Font.PLAIN, 15));
        inputField.setPreferredSize(new Dimension(0, 45));
        inputField.setOpaque(false);
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Circular send button
        sendButton = new JButton(">");
        sendButton.setBackground(new Color(90, 210, 245)); // Match reference light blue
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(45, 45));
        sendButton.setBorder(BorderFactory.createEmptyBorder()); // Make it borderless
        sendButton.setContentAreaFilled(false);
        sendButton.setOpaque(false);
        sendButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (((AbstractButton) c).getModel().isPressed()) {
                    g2.setColor(new Color(70, 190, 225));
                } else {
                    g2.setColor(sendButton.getBackground());
                }
                g2.fillOval(0, 0, c.getWidth(), c.getHeight());

                // Draw a centered paper-plane style icon.
                g2.setColor(Color.BLACK);
                Polygon plane = new Polygon();
                int w = c.getWidth();
                int h = c.getHeight();
                plane.addPoint(w / 2 - 6, h / 2 - 8);
                plane.addPoint(w / 2 + 9, h / 2);
                plane.addPoint(w / 2 - 6, h / 2 + 8);
                g2.fillPolygon(plane);
                g2.dispose();
            }
        });

        // Event listeners for sending message
        ActionListener sendListener = e -> sendMessage();
        inputField.addActionListener(sendListener);
        sendButton.addActionListener(sendListener);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(socket != null && !socket.isClosed()) {
                    processMessage(Client.this.name + " quit");
                }
            }
        });

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String nowTime() {
        return new SimpleDateFormat("hh:mm a").format(new Date());
    }

    private String displayUserName(String username) {
        if (username != null && username.equalsIgnoreCase(this.name)) {
            return "you";
        }
        return username;
    }

    private String formatServerMessage(String content) {
        if (content == null) {
            return "";
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("__online__")) {
            String csvUsers = trimmed.substring("__online__".length()).trim();
            if (csvUsers.isEmpty()) {
                return "No one else is in the chat.";
            }

            String[] users = csvUsers.split(",");
            List<String> displayUsers = new ArrayList<String>();
            for (String user : users) {
                String cleanUser = user.trim();
                if (!cleanUser.isEmpty()) {
                    displayUsers.add(displayUserName(cleanUser));
                }
            }

            if (displayUsers.isEmpty()) {
                return "No one else is in the chat.";
            }
            return "People already in the chat: " + String.join(", ", displayUsers) + ".";
        }

        String joinedSuffix = " joined the chat.";
        if (trimmed.endsWith(joinedSuffix)) {
            String user = trimmed.substring(0, trimmed.length() - joinedSuffix.length()).trim();
            return displayUserName(user) + joinedSuffix;
        }

        String leftSuffix = " left the chat.";
        if (trimmed.endsWith(leftSuffix)) {
            String user = trimmed.substring(0, trimmed.length() - leftSuffix.length()).trim();
            return displayUserName(user) + leftSuffix;
        }

        return trimmed;
    }

    private void appendChatMessage(String sender, String content, boolean isMe) {
        SwingUtilities.invokeLater(() -> {
            final int maxBubbleWidth = 280;
            final int minBubbleWidth = 54;

            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel metaRow = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            metaRow.setOpaque(false);
            JLabel senderLabel = new JLabel(isMe ? "You" : sender);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            senderLabel.setForeground(new Color(86, 92, 102));
            JLabel timeLabel = new JLabel("  " + nowTime().toLowerCase());
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            timeLabel.setForeground(new Color(145, 150, 158));
            metaRow.add(senderLabel);
            metaRow.add(timeLabel);
            row.add(metaRow);

            RoundedBubble bubble = new RoundedBubble(isMe ? new Color(94, 208, 240) : new Color(231, 233, 237), 20);
            bubble.setLayout(new BorderLayout());
            bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            JTextArea messageText = new JTextArea(content);
            messageText.setLineWrap(true);
            messageText.setWrapStyleWord(true);
            messageText.setEditable(false);
            messageText.setOpaque(false);
            messageText.setFont(emojiFont(Font.PLAIN, 16));
            messageText.setForeground(Color.BLACK);
            messageText.setBorder(null);
            messageText.setFocusable(false);

            FontMetrics fm = messageText.getFontMetrics(messageText.getFont());
            int rawWidth = fm.stringWidth(content.replace("\n", " ")) + 6;
            int textWidth = Math.max(minBubbleWidth, Math.min(rawWidth, maxBubbleWidth));
            messageText.setSize(new Dimension(textWidth, Short.MAX_VALUE));
            Dimension measured = messageText.getPreferredSize();
            textWidth = Math.max(minBubbleWidth, Math.min(measured.width, maxBubbleWidth));
            messageText.setSize(new Dimension(textWidth, 1));
            int textHeight = messageText.getPreferredSize().height;
            messageText.setPreferredSize(new Dimension(textWidth, textHeight));

            bubble.add(messageText, BorderLayout.CENTER);
            bubble.setPreferredSize(new Dimension(textWidth + 28, textHeight + 20));
            bubble.setMaximumSize(new Dimension(maxBubbleWidth + 28, textHeight + 20));

            JPanel bubbleRow = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            bubbleRow.setOpaque(false);
            bubbleRow.add(bubble);
            row.add(bubbleRow);

            // Prevent BoxLayout from allocating extra vertical space to message rows.
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
            row.setMinimumSize(new Dimension(0, row.getPreferredSize().height));

            messagesPanel.add(row);
            messagesPanel.revalidate();
            messagesPanel.repaint();

            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty() && socket != null && !socket.isClosed()) {
            if (msg.equals("quit")) {
                processMessage(name + " quit");
                System.exit(0);
            } else {
                appendChatMessage(name, msg, true); // Append my own message locally
                processMessage(name + " " + msg);
                inputField.setText("");
            }
        }
    }

    private void processMessage(String message) {
        try {
            if (output != null) output.writeUTF(message);
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }

    public void run() { 
        try {
            while (true) {
                String message = input.readUTF();
                if (message.equals("quited")) {
                    appendChatMessage("Server", "Server closed the connection.", false);
                    input.close();
                    output.close();
                    socket.close();
                    SwingUtilities.invokeLater(() -> {
                        inputField.setEnabled(false);
                        sendButton.setEnabled(false);
                    });
                    break;
                } else {
                    String[] parts = message.split(" ", 2);
                    if (parts.length >= 2) {
                        String sender = parts[0];
                        String content = parts[1];

                        if ("Server".equalsIgnoreCase(sender)) {
                            appendChatMessage("Server", formatServerMessage(content), false);
                        } else if (!sender.equals(this.name)) {
                            appendChatMessage(sender, content, false);
                        }
                    }
                }
            } 
        } catch (IOException ie) {
            appendChatMessage("Server", "Disconnected from server.", false);
            SwingUtilities.invokeLater(() -> {
                inputField.setEnabled(false);
                sendButton.setEnabled(false);
            });
        }
    }

    public static void main(String args[]) {
        if (args.length < 1 || args.length > 3) {
            System.out.println("Usage: java -cp bin chat_app.Client <name> [server_host] [server_port]");
            System.out.println("Example (LAN): java -cp bin chat_app.Client Bob 192.168.1.20 5055");
            return;
        }

        String name = args[0];
        String host = args.length >= 2 ? args[1] : "localhost";
        int port = args.length == 3 ? Integer.parseInt(args[2]) : 5055;

        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new Client(host, port, name));
    }

    private static class RoundedBubble extends JPanel {
        private final Color color;
        private final int arc;

        RoundedBubble(Color color, int arc) {
            this.color = color;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
