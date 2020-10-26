package com.rewesi;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyWindow extends JFrame {

    JTextField fieldIP, fieldText;
    JButton button;
    JLabel label;
    JTextArea area;

    ServerSocket providerSocket;
    Socket connection = null;
    ObjectOutputStream out;
    ObjectInputStream in;


    // ------------------- Classes -------------------


    String FormattedTime() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss");

        return myDateObj.format(myFormatObj);
    }

    class MyThread extends Thread {
        public void run() {

            while (true) {

                try{
                    String message = (String)in.readObject();
                    area.setText(FormattedTime() + ": -> " + message + "\n" + area.getText());
                }
                catch(ClassNotFoundException er){
                    area.setText(FormattedTime() + ": Recieved data has unknown format!\n" + area.getText());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    break;
                }
            }
        }
    }

    class ServerStart implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            try{
                providerSocket = new ServerSocket(2004, 10);              //1. vytvoření server socketu
                area.setText(FormattedTime() + ": Waiting for connection");     //2. čekání na spojení
                connection = providerSocket.accept();
                area.setText(FormattedTime() + ": Connection established with computer " + connection.getInetAddress().getHostName() + "\n" + area.getText());
                out = new ObjectOutputStream(connection.getOutputStream());     //3. ziskani vstupnich a vystupnich proudu
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                MyThread thread = new MyThread();
                thread.start();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

    class ServerConnect implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                connection= new Socket(fieldIP.getText(), 2004);              //1. vytvoření socketového spojení k serveru na dané adrese
                area.setText(FormattedTime() + ": Connected to " + connection.getInetAddress().getHostName() + " via port 2004" + "\n" + area.getText());
                out = new ObjectOutputStream(connection.getOutputStream());     //2. získání vstupního a výstupního proudu
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                MyThread thread = new MyThread();
                thread.start();
            }
            catch (UnknownHostException unknownHost) {
                area.setText(FormattedTime() + ": You're trying to connect to unknown server!" + "\n" + area.getText());
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    class ServerSend implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            try {
                String msg;
                msg = fieldText.getText();
                out.writeObject(msg);
                out.flush();
                area.setText(FormattedTime() + ": <- " + msg + "\n" + area.getText());
                fieldText.setText("");
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    // ------------------- End of classes  -------------------


    public MyWindow(String title) {
        super(title);

        this.setMinimumSize(new Dimension(600, 400));
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;


        // ------------------- First row -------------------

        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(1,4, 10 , 10));

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10,10,10,10);

        button = new JButton("Start server");
        button.addActionListener(new ServerStart());
        panel1.add(button);

        panel1.add(new JLabel("IP address:", SwingConstants.RIGHT));

        fieldIP = new JTextField("127.0.0.1", SwingConstants.LEFT);
        panel1.add(fieldIP);

        button = new JButton("Connect to server");
        button.addActionListener(new ServerConnect());
        panel1.add(button);

        this.add(panel1, c);


        // ------------------- Second row -------------------

        JPanel panel2 = new JPanel();

        panel2.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(10,10,10,10);


        label = new JLabel("Message to be send:", SwingConstants.RIGHT);

        c2.fill = GridBagConstraints.BOTH;
        c2.gridx = 0;
        c2.gridy = 0;
        panel2.add(label, c2);


        fieldText = new JTextField("");

        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.5;
        c2.gridx = 1;
        c2.gridy = 0;
        c2.insets = new Insets(0,10,0,10);
        panel2.add(fieldText, c2);


        button = new JButton("Send");
        button.addActionListener(new ServerSend());

        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0;
        c2.gridx = 2;
        c2.gridy = 0;
        c2.insets = new Insets(0,0,0,0);
        panel2.add(button, c2);


        this.add(panel2, c);


        // ------------------- Third row -------------------

        JPanel panel3 = new JPanel();

        panel3.setLayout(new GridBagLayout());
        GridBagConstraints c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.HORIZONTAL;

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridx = 1;
        c.gridwidth = 3;
        c.gridy = 2;

        area = new JTextArea(8, 16);

        Border border = BorderFactory.createLineBorder(Color.GRAY);
        area.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JScrollPane scroll = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        c3.fill = GridBagConstraints.BOTH;
        c3.weightx = 1.0;
        c3.weighty = 1.0;
        c3.gridx = 1;
        c3.gridy = 0;
        c3.insets = new Insets(0,0,0,0);
        panel3.add(scroll, c3);


        this.add(panel3, c);


        // ------------------- Final maintenance -------------------

        this.setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));

        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }
}