package ping.check;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rcher
 */
public class PingMainJFrame extends javax.swing.JFrame {

    // Variable Decalration
    // Java Native Classes
    private ArrayList<PingController> controllers;
    private HashMap<String, String> knownHosts;
    // Project Classes
    private PingController controller;
    // End of Variable Declaration

    /**
     * Creates new form PingMainJFrame
     *
     * @param hostAddress
     * @param hostName
     */
    public PingMainJFrame(final String hostAddress, final String hostName) {

        //
        initComponents();

        //
        controllers = new ArrayList<>();

        //
        init();
    }

    public static void main(String[] args) {

        //
        final PingMainJFrame frame = new PingMainJFrame("104.160.131.3", "League Of Legends");

        // Attempt to set the look and feel of the application
        try {

            // Set to native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException cnfe) {
            System.err.println(cnfe);
        }

        //
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void init() {

        // We'll say that 1000 is the red zone.
        pingJProgressbar.setMaximum(1000);
        pingJSlider.setValue(0);

        // Making Icons and seting up Frame Icons.
        final Class closs = getClass();

        //
        searchJButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closs.getResource("/icon-search24.png"))));
        timerJButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(closs.getResource("/icon-timer24.png"))));

        //
        ArrayList<Image> images = new ArrayList<>();
        images.add(Toolkit.getDefaultToolkit().getImage(closs.getResource("/icon-frame16.png")));
        images.add(Toolkit.getDefaultToolkit().getImage(closs.getResource("/icon-ping32.png")));
        this.setIconImages(images);
        this.setTitle("Ping Checker");

        // Since we only need to do something when this application is closed we need only a window adapter instead of the full listener
        // impl.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {

                // Close the command prompt on exit
                controller.terminateProcess();
                createPropertyFile();
            }
        });

        // Not implemented yet.
        knownHosts = new HashMap<>();
        
        //
        loadPropertyFile();

        // Get the first item on the list
        controller = controllers.get(1);

        //
        pingJLabel.setText("0 m/s");
        pingJSlider.setValue(controller.getByteTransferRate());
        hostNameJLabel.setText(controller.getHostName());
        hostNameJLabel.setToolTipText("Host Address: " + controller.getHost());
        trackJLabel.setText("Not Tracking Ping");
        timerJButton.setToolTipText("Track ping over 10 second intervals.");
    }

    private void loadPropertyFile() {

        // Attmept to find the property file
        final Properties propertyFile = new Properties();

        //
        try {

            // Load the property file
            propertyFile.load(new FileInputStream("config.properties"));

            //
            for (Map.Entry<Object, Object> map : propertyFile.entrySet()) {

                // Parse some bs from it.
                final String name = String.valueOf(map.getKey()).replace("_", " ");
                final String value = String.valueOf(map.getValue());
                final String[] values = value.split(",");
                final String address = values[0];

                //
                final int pingMin = Integer.parseInt(values[1]);
                final int pingMed = Integer.parseInt(values[2]);
                final int pingMax = Integer.parseInt(values[3]);
                
                // Setup the new ping controller
                PingController p = new PingController(this, address, name);
                p.setPingBounds(pingMin, pingMed, pingMax);
                
                // Create a controller for ever item in the property file.
                controllers.add(p);

                // Also add to our knownMap
                knownHosts.put(name, value);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException me) {
            createPropertyFile();
        }
    }

    private void createPropertyFile() {

        try {

            // Property File :) example time
            final Properties propertyFile = new Properties();

            // Strings
            for (PingController c : controllers) {
                
                //
                final int[] pingBounds = c.getPingBounds();
                
                //
                final String value = c.getHost() + "," + pingBounds[0] + "," + pingBounds[1] + "," + pingBounds[2];
                
                //
                propertyFile.put(c.getHostName(), value);
            }

            // Save all used plugins for load order next time -- will severely slow load time; but ask
            propertyFile.store(new FileOutputStream("config.properties"), null);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public void updatePing(int ping) {

        // Update text
        pingJLabel.setText("Ping: " + ping + "m/s");
        pingJLabel.setToolTipText(pingJLabel.getText());

        //
        int[] pingBounds = controller.getPingBounds();

        // Change color
        Color color;
        if (ping <= 0) {
            color = Color.GRAY;
        } else if (ping <= pingBounds[0]) {
            color = Color.GREEN;
        } else if (ping > pingBounds[0] && ping < pingBounds[1]) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }

        // Change the bar value
        pingJProgressbar.setForeground(color);
        pingJProgressbar.setValue(ping);
        pingJProgressbar.repaint();
    }

    public void updateAverage() {
        trackJLabel.setText("Average: " + controller.getAveragePing());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        hostJPanel1 = new javax.swing.JPanel();
        activeJRadio = new javax.swing.JRadioButton();
        pingJProgressbar = new javax.swing.JProgressBar();
        pingJLabel = new javax.swing.JLabel();
        hostNameJLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        searchJButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        timerJButton = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        trackJLabel = new javax.swing.JLabel();
        pingJSlider = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        java.awt.GridBagLayout hostJPanel1Layout = new java.awt.GridBagLayout();
        hostJPanel1Layout.columnWidths = new int[] {0, 10, 0};
        hostJPanel1Layout.rowHeights = new int[] {0, 4, 0};
        hostJPanel1.setLayout(hostJPanel1Layout);

        activeJRadio.setText("Active");
        activeJRadio.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        activeJRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeJRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        hostJPanel1.add(activeJRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        hostJPanel1.add(pingJProgressbar, gridBagConstraints);

        pingJLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        pingJLabel.setText("[Ping Placement]");
        pingJLabel.setPreferredSize(new java.awt.Dimension(64, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        hostJPanel1.add(pingJLabel, gridBagConstraints);

        hostNameJLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        hostNameJLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        hostNameJLabel.setText("[Host Placement]");
        hostNameJLabel.setPreferredSize(new java.awt.Dimension(128, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        hostJPanel1.add(hostNameJLabel, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        searchJButton.setFocusPainted(false);
        searchJButton.setMaximumSize(new java.awt.Dimension(33, 28));
        searchJButton.setMinimumSize(new java.awt.Dimension(33, 8));
        searchJButton.setPreferredSize(new java.awt.Dimension(28, 28));
        searchJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchJButtonActionPerformed(evt);
            }
        });
        jPanel1.add(searchJButton);
        jPanel1.add(filler1);

        timerJButton.setText("Track Ping");
        timerJButton.setFocusPainted(false);
        timerJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        timerJButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        timerJButton.setMaximumSize(new java.awt.Dimension(98, 28));
        timerJButton.setMinimumSize(new java.awt.Dimension(98, 28));
        timerJButton.setPreferredSize(new java.awt.Dimension(98, 28));
        timerJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timerJButtonActionPerformed(evt);
            }
        });
        jPanel1.add(timerJButton);
        jPanel1.add(filler2);

        trackJLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        trackJLabel.setText("[Track Placement]");
        trackJLabel.setMaximumSize(new java.awt.Dimension(41, 28));
        trackJLabel.setMinimumSize(new java.awt.Dimension(128, 28));
        trackJLabel.setPreferredSize(new java.awt.Dimension(128, 28));
        jPanel1.add(trackJLabel);

        pingJSlider.setMajorTickSpacing(1000);
        pingJSlider.setMaximum(10000);
        pingJSlider.setMinimum(1);
        pingJSlider.setMinorTickSpacing(500);
        pingJSlider.setPaintTicks(true);
        pingJSlider.setValue(0);
        pingJSlider.setPreferredSize(new java.awt.Dimension(200, 24));
        pingJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pingJSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(hostJPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pingJSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hostJPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(pingJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void activeJRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeJRadioActionPerformed

        // TODO add your handling code here:
        if (activeJRadio.isSelected()) {

            //
            controller.reactivateProcess();
        } else {

            // Kill connection
            controller.terminateProcess();
        }
    }//GEN-LAST:event_activeJRadioActionPerformed

    private void searchJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchJButtonActionPerformed

        // 
        if (!controller.isTracking()) {

            //
            controller.terminateProcess();

            // TODO add your handling code here:
            final String address = JOptionPane.showInputDialog(this, "Enter new Address", controller.getHost());
            if (address == null) {
                return;
            }
            final String name = JOptionPane.showInputDialog(this, "Name new Connection", controller.getHostName());
            if (name == null) {
                return;
            }

            // Create our new controller.
            controller = new PingController(this, address, name);

            // Update the host name
            hostNameJLabel.setText(controller.getHostName());

            // Update the address and start pinging it.
            controller.reactivateProcess();
        } else {
            JOptionPane.showMessageDialog(this, "Ping track in progress.");
        }
    }//GEN-LAST:event_searchJButtonActionPerformed

    private void timerJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timerJButtonActionPerformed

        // Base Case of bad host address
        if (controller.getHost() == null || controller.getHost().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Connect to Host first.");
            return;
        }

        // Base cast of bad read.
        if (activeJRadio.isSelected() == false) {
            JOptionPane.showMessageDialog(this, "Activate service first.");
            return;
        }

        //
        controller.setTracking(!controller.isTracking());
        trackJLabel.setText(controller.isTracking() ? "Tracking..." : "Not Tracking Ping.");
    }//GEN-LAST:event_timerJButtonActionPerformed

    private void pingJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pingJSliderStateChanged

        // TODO add your handling code here:
        if (!pingJSlider.getValueIsAdjusting()) {

            //
            if (pingJSlider.getValue() != controller.getByteTransferRate()) {

                // Set the transfer rate
                controller.setByteTransferRate(pingJSlider.getValue());

                //
                activeJRadio.setSelected(false);
                controller.terminateProcess();

                //
                pingJSlider.setToolTipText("Sending Packets of size: " + controller.getByteTransferRate() + " bytes (" + ((double) controller.getAveragePing()) + "kb/s)");
            }
        }
    }//GEN-LAST:event_pingJSliderStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activeJRadio;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JPanel hostJPanel1;
    private javax.swing.JLabel hostNameJLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel pingJLabel;
    private javax.swing.JProgressBar pingJProgressbar;
    private javax.swing.JSlider pingJSlider;
    private javax.swing.JButton searchJButton;
    private javax.swing.JButton timerJButton;
    private javax.swing.JLabel trackJLabel;
    // End of variables declaration//GEN-END:variables
}
