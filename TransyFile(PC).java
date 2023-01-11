
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TransyFile extends javax.swing.JFrame {

    private javax.swing.JButton send;
    private javax.swing.JButton receive;
    private javax.swing.JLabel title;
    private ReceiveFile rf;
    private SendFile sf;

    public TransyFile() {
        initComponents();
    }

    private void initComponents() {

        title = new javax.swing.JLabel();
        send = new javax.swing.JButton();
        receive = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        title.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        title.setText("      TransyFile");

        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        receive.setText("Receive");
        receive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                receiveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 149,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(122, 122, 122))
                        .addGroup(layout.createSequentialGroup()
                                .addGap(72, 72, 72)
                                .addComponent(send, javax.swing.GroupLayout.PREFERRED_SIZE, 98,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(71, 71, 71)
                                .addComponent(receive, javax.swing.GroupLayout.PREFERRED_SIZE, 94,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(65, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(64, 64, 64)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(receive, javax.swing.GroupLayout.PREFERRED_SIZE, 48,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(send, javax.swing.GroupLayout.PREFERRED_SIZE, 48,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(126, Short.MAX_VALUE)));

        pack();
    }

    private void sendActionPerformed(java.awt.event.ActionEvent evt) {
        send.setEnabled(false);
        if (!receive.isEnabled()) {
            rf.closeConnection();
            receive.setEnabled(true);
        }

        String mobIP = JOptionPane.showInputDialog("Enter IP Adddress");

        JFileChooser chooser = new JFileChooser();
        chooser.showSaveDialog(chooser);

        sf = new SendFile(chooser.getSelectedFile().getAbsolutePath(), chooser.getSelectedFile().getName(), mobIP);
        sf.start();

    }

    private void receiveActionPerformed(java.awt.event.ActionEvent evt) {
        receive.setEnabled(false);
        if (!send.isEnabled())
            send.setEnabled(true);

        try {
            rf = new ReceiveFile();
            rf.start();
        } catch (IOException ex) {
        }
    }

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TransyFile().setVisible(true);
            }
        });

    }

    static class SendFile extends Thread {
        private Socket Soc;

        private String path;
        private String fileName;
        private FileInputStream fin;
        private DataOutputStream dout;
        private DataInputStream din;
        private String mobIP;

        public SendFile(String path, String fileName, String mobIP) {
            this.path = path;
            this.fileName = fileName;
            this.mobIP = mobIP;
        }

        @Override
        public void run() {
            try {

                Soc = new Socket(mobIP, 1809);
                fin = new FileInputStream(path);
                dout = new DataOutputStream(Soc.getOutputStream());

                dout.writeUTF(fileName);

                int readByte;
                while ((readByte = fin.read()) != -1)
                    dout.writeUTF(String.valueOf(readByte));

                dout.writeUTF("-1");

                din = new DataInputStream(Soc.getInputStream());
                while (!din.readUTF().equals("Done"))
                    ;

                JOptionPane.showMessageDialog(null, "File Sent Successfully");

                fin.close();
                dout.flush();
                dout.close();
                din.close();
                Soc.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    static class ReceiveFile extends Thread {

        private boolean exit = true;
        private ServerSocket ServerSoc;
        private Process fetchIP;
        private BufferedReader br;
        private Socket ClientSoc;
        private DataInputStream din;
        private FileOutputStream fout;
        private String tmp;
        private JButton receive;
        private File fp;
        private StringBuilder filepath;
        private DataOutputStream dout;
        private String ipv4;

        public ReceiveFile() throws IOException {
            ServerSoc = new ServerSocket(1908);

            // For Linux
            // fetchIP = new ProcessBuilder("/bin/sh", "-c", "ip addr | grep \"inet \" |
            // grep -v 127 | cut -d\" \" -f6 | cut -d\"/\" -f1").start();
            // br = new BufferedReader(new InputStreamReader(fetchIP.getInputStream()));
            // ipv4 = br.readLine();

            // For Windows
            fetchIP = new ProcessBuilder("cmd", "/c", "ipconfig | findstr /i \"ipv4\"").start();
            br = new BufferedReader(new InputStreamReader(fetchIP.getInputStream()));

            ipv4 = br.readLine().split(": ")[1];
            JOptionPane.showMessageDialog(null, "IP address: " + ipv4);
        }

        @Override
        public void run() {
            while (exit) {

                try {
                    ClientSoc = ServerSoc.accept();

                    if (ClientSoc.isConnected())
                        System.out.println("Connected..");

                    din = new DataInputStream(ClientSoc.getInputStream());

                    String file = din.readUTF();
                    din = new DataInputStream(ClientSoc.getInputStream());
                    filepath = new StringBuilder("C:\\Users\\Prasanna\\Downloads\\" + file);

                    System.out.println(file);

                    int i = 1;
                    fp = new File(filepath.toString());

                    while (fp.exists()) {
                        if (i == 1)
                            filepath.insert(filepath.indexOf("."), "(" + i + ")");
                        else {
                            int j = filepath.lastIndexOf(String.valueOf(i - 1));
                            filepath.replace(j, j + 1, "" + i + "");
                        }
                        fp = new File(filepath.toString());
                        i++;
                    }

                    fout = new FileOutputStream(fp);

                    try {
                        while (!(tmp = din.readUTF()).equals("-1"))
                            fout.write(Integer.parseInt(tmp));
                    } catch (Exception ex) {
                    }

                    dout = new DataOutputStream(ClientSoc.getOutputStream());
                    dout.writeUTF("Done");

                    JOptionPane.showMessageDialog(null, "File received and stored in Downloads");

                    fout.flush();
                    fout.close();
                    din.close();
                    ClientSoc.close();
                } catch (IOException ie) {
                }

            }
        }

        public void closeConnection() {
            try {
                exit = false;
                ServerSoc.close();
                br.close();
            } catch (Exception e) {
            }
        }

    }

}