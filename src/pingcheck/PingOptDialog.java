/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingcheck;

import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author rcher
 */
public class PingOptDialog extends javax.swing.JDialog {

    // Variable Delcaration
    // Data Types
    private boolean timerRepeats = false;
    // Java Native Classes
    private HashMap<String, String[]> configAppsMap;
    private static ImageIcon iconTimerOff;
    private static ImageIcon iconTimerOn;
    private static ImageIcon iconSave;
    private static ImageIcon iconAdd;
    private static ImageIcon iconCancel;
    private static ImageIcon iconPinOff;
    private static ImageIcon iconPinOn;
    // End of Variable Declaration

    public PingOptDialog(PingMainJFrame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        //
        init();
    }

    private void init() {

        //
        configAppsMap = new HashMap<>();

        //
        final PingMainJFrame pf = (PingMainJFrame) getParent();
        final Toolkit kit = Toolkit.getDefaultToolkit();
        final Class closs = getClass();

        //
        iconTimerOff = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-timer24.png")));
        iconSave = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-save24.png")));
        iconAdd = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-add24.png")));
        iconCancel = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-cancel24.png")));
        iconTimerOn = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-timer-green24.png")));
        iconPinOff = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-pin24.png")));
        iconPinOn = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-pin-green24.png")));

        //
        timerJToggle.setIcon(iconTimerOff);
        timerJToggle.setSelectedIcon(iconTimerOn);
        timerJToggle.setSelected(!pf.getTimer().isRepeats());
        timerJToggle.doClick();

        //
        pinJToggle.setIcon(iconPinOff);
        pinJToggle.setSelectedIcon(iconPinOn);
        pinJToggle.setSelected(!pf.isAlwaysOnTop());
        pinJToggle.doClick();

        //
        saveJButton.setIcon(iconSave);
        finishJButton.setIcon(iconAdd);
        closeJButton.setIcon(iconCancel);

        //
        loadPropertyFile();

        //
        setTitle("Connection Options");

        // Set default selected index based on controller
        if (pf.getController() != null) {
            hostJComboBox.setSelectedItem(pf.getController().getHost());
        }
    }

    private void createPropertyFile() {

        try {

            // Property File :) example time
            final Properties propertyFile = new Properties();

            // Strings
            for (Map.Entry<String, String[]> map : configAppsMap.entrySet()) {

                String[] values = map.getValue();

                // The format doesn't matter so we just add the 'App[]'
                String s = "";
                s += "[" + values[0] + ",";
                s += values[1] + ",";
                s += values[2] + "]";

                //
                propertyFile.put(map.getKey(), s);
            }

            // Save all used plugins for load order next time -- will severely slow load time; but ask
            propertyFile.store(new FileOutputStream("apps.properties"), null);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private void loadPropertyFile() {

        //
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        // Attmept to find the property file
        final Properties propertyFile = new Properties();

        //
        try {

            // Load the property file
            propertyFile.load(new FileInputStream("apps.properties"));

            // Iterate over the map
            for (Map.Entry<Object, Object> map : propertyFile.entrySet()) {

                // Name is first property and PID is the parsed value after '=' which we are not concerned with.
                final String name = String.valueOf(map.getKey());
                String value = String.valueOf(map.getValue());
                value = value.substring(1, value.length() - 1);
                final String[] values = value.split(",");

                // Also add to our knownMap
                configAppsMap.put(name, values);
                model.addElement(name);
            }

            //
            hostJComboBox.setModel(model);
            hostJComboBox.setSelectedIndex(hostJComboBox.getItemCount() > 0 ? 1 : -1);
        } catch (IOException | NumberFormatException nfe) {

            // Recreate the property file in that case
            createPropertyFile();

            // The message we wish to show the user
            final String msg = "Failed to load configuration file.\nNew configuration file written.\nTry again?";

            // Prompt to retry
            if (JOptionPane.showConfirmDialog(null, msg, "Retry?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                // Re-initialize
                init();
            } else {

                // Close the program
                System.exit(0);
            }
        }
    }

    private void storeChanges() {

        //
        final String newHost = hostJField.getText();
        final String currentHost = String.valueOf(hostJComboBox.getSelectedItem());
        final String[] values = new String[]{addressJField.getText(),
            String.valueOf(bufferJSlider.getValue()),
            String.valueOf(timerJSlider.getValue())};

        // Now create a brand new model
        final DefaultComboBoxModel newModel = new DefaultComboBoxModel();
        final HashMap<String, String[]> newMap = new HashMap<>(configAppsMap.size());

        // Null cases
        if (newHost != null && !newHost.isEmpty()) {

            // Find this entry in teh configAppsMap and replace it
            for (Map.Entry<String, String[]> map : configAppsMap.entrySet()) {

                //
                final String s = map.getKey();
                final String[] v = map.getValue();

                // We found the existing entry
                if (s.equalsIgnoreCase(currentHost)) {

                    // Replace it with the new hostName and all new entries
                    newMap.put(newHost, values);
                    newModel.addElement(newHost);
                } else {

                    // Add regardless
                    newMap.put(s, v);
                    newModel.addElement(s);
                }
            }
        }

        // Assign new map contents
        configAppsMap.clear();
        configAppsMap.putAll(newMap);

        // Set this and be done.
        hostJComboBox.setModel(newModel);
        hostJComboBox.setSelectedItem(newHost);
    }

    public int getTimerDelay() {

        // Grab from timerJSlider
        return timerJSlider.getValue();
    }

    public boolean getTimerRepeats() {
        return timerRepeats;
    }

    public int getPacketSize() {
        return bufferJSlider.getValue();
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

        controlJPanel = new javax.swing.JPanel();
        bufferJLabel = new javax.swing.JLabel();
        addressJField = new javax.swing.JTextField();
        hostJLabel = new javax.swing.JLabel();
        hostJField = new javax.swing.JTextField();
        bufferJSlider = new javax.swing.JSlider();
        addressJLabel = new javax.swing.JLabel();
        timerJLabel = new javax.swing.JLabel();
        timerJSlider = new javax.swing.JSlider();
        buttonJPanel = new javax.swing.JPanel();
        timerJToggle = new javax.swing.JToggleButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        pinJToggle = new javax.swing.JToggleButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        finishJButton = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 0), new java.awt.Dimension(8, 32767));
        closeJButton = new javax.swing.JButton();
        hostJComboBox = new javax.swing.JComboBox<>();
        saveJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        controlJPanel.setMaximumSize(new java.awt.Dimension(182, 32767));
        controlJPanel.setMinimumSize(new java.awt.Dimension(182, 100));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 10, 0};
        jPanel1Layout.rowHeights = new int[] {0, 4, 0, 4, 0, 4, 0};
        controlJPanel.setLayout(jPanel1Layout);

        bufferJLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        bufferJLabel.setText("Buffer:");
        bufferJLabel.setMaximumSize(new java.awt.Dimension(64, 20));
        bufferJLabel.setMinimumSize(new java.awt.Dimension(64, 20));
        bufferJLabel.setPreferredSize(new java.awt.Dimension(64, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(bufferJLabel, gridBagConstraints);

        addressJField.setMaximumSize(new java.awt.Dimension(88, 20));
        addressJField.setMinimumSize(new java.awt.Dimension(88, 20));
        addressJField.setPreferredSize(new java.awt.Dimension(88, 20));
        addressJField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressJFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(addressJField, gridBagConstraints);

        hostJLabel.setText("Host ID:");
        hostJLabel.setMaximumSize(new java.awt.Dimension(64, 20));
        hostJLabel.setMinimumSize(new java.awt.Dimension(64, 20));
        hostJLabel.setPreferredSize(new java.awt.Dimension(64, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(hostJLabel, gridBagConstraints);

        hostJField.setMaximumSize(new java.awt.Dimension(88, 20));
        hostJField.setMinimumSize(new java.awt.Dimension(88, 20));
        hostJField.setPreferredSize(new java.awt.Dimension(88, 20));
        hostJField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostJFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(hostJField, gridBagConstraints);

        bufferJSlider.setMajorTickSpacing(100);
        bufferJSlider.setMaximum(655);
        bufferJSlider.setMinimum(3);
        bufferJSlider.setMinorTickSpacing(50);
        bufferJSlider.setPaintTicks(true);
        bufferJSlider.setToolTipText("100 bytes.");
        bufferJSlider.setValue(100);
        bufferJSlider.setMaximumSize(new java.awt.Dimension(88, 26));
        bufferJSlider.setMinimumSize(new java.awt.Dimension(88, 26));
        bufferJSlider.setPreferredSize(new java.awt.Dimension(88, 26));
        bufferJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bufferJSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        controlJPanel.add(bufferJSlider, gridBagConstraints);

        addressJLabel.setText("Address:");
        addressJLabel.setMaximumSize(new java.awt.Dimension(64, 20));
        addressJLabel.setMinimumSize(new java.awt.Dimension(64, 20));
        addressJLabel.setPreferredSize(new java.awt.Dimension(64, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(addressJLabel, gridBagConstraints);

        timerJLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        timerJLabel.setText("Wait:");
        timerJLabel.setMaximumSize(new java.awt.Dimension(64, 20));
        timerJLabel.setMinimumSize(new java.awt.Dimension(64, 20));
        timerJLabel.setPreferredSize(new java.awt.Dimension(64, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        controlJPanel.add(timerJLabel, gridBagConstraints);

        timerJSlider.setMajorTickSpacing(1000);
        timerJSlider.setMaximum(3000);
        timerJSlider.setMinimum(500);
        timerJSlider.setMinorTickSpacing(250);
        timerJSlider.setPaintTicks(true);
        timerJSlider.setSnapToTicks(true);
        timerJSlider.setToolTipText("500 milliseconds delay.");
        timerJSlider.setMaximumSize(new java.awt.Dimension(88, 26));
        timerJSlider.setMinimumSize(new java.awt.Dimension(88, 26));
        timerJSlider.setPreferredSize(new java.awt.Dimension(88, 26));
        timerJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timerJSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        controlJPanel.add(timerJSlider, gridBagConstraints);

        buttonJPanel.setMaximumSize(new java.awt.Dimension(161, 24));
        buttonJPanel.setMinimumSize(new java.awt.Dimension(161, 24));
        buttonJPanel.setPreferredSize(new java.awt.Dimension(161, 24));
        buttonJPanel.setLayout(new javax.swing.BoxLayout(buttonJPanel, javax.swing.BoxLayout.LINE_AXIS));

        timerJToggle.setBorderPainted(false);
        timerJToggle.setContentAreaFilled(false);
        timerJToggle.setFocusPainted(false);
        timerJToggle.setMaximumSize(new java.awt.Dimension(24, 24));
        timerJToggle.setMinimumSize(new java.awt.Dimension(24, 24));
        timerJToggle.setPreferredSize(new java.awt.Dimension(24, 24));
        timerJToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timerJToggleActionPerformed(evt);
            }
        });
        buttonJPanel.add(timerJToggle);
        buttonJPanel.add(filler3);

        pinJToggle.setToolTipText("");
        pinJToggle.setBorderPainted(false);
        pinJToggle.setContentAreaFilled(false);
        pinJToggle.setFocusPainted(false);
        pinJToggle.setMaximumSize(new java.awt.Dimension(24, 24));
        pinJToggle.setMinimumSize(new java.awt.Dimension(24, 24));
        pinJToggle.setPreferredSize(new java.awt.Dimension(24, 24));
        pinJToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pinJToggleActionPerformed(evt);
            }
        });
        buttonJPanel.add(pinJToggle);
        buttonJPanel.add(filler1);

        finishJButton.setMaximumSize(new java.awt.Dimension(24, 24));
        finishJButton.setMinimumSize(new java.awt.Dimension(24, 24));
        finishJButton.setPreferredSize(new java.awt.Dimension(24, 24));
        finishJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishJButtonActionPerformed(evt);
            }
        });
        buttonJPanel.add(finishJButton);
        buttonJPanel.add(filler2);

        closeJButton.setMaximumSize(new java.awt.Dimension(24, 24));
        closeJButton.setMinimumSize(new java.awt.Dimension(24, 24));
        closeJButton.setPreferredSize(new java.awt.Dimension(24, 24));
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });
        buttonJPanel.add(closeJButton);

        hostJComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        hostJComboBox.setMaximumSize(new java.awt.Dimension(128, 24));
        hostJComboBox.setMinimumSize(new java.awt.Dimension(128, 24));
        hostJComboBox.setPreferredSize(new java.awt.Dimension(128, 24));
        hostJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostJComboBoxActionPerformed(evt);
            }
        });

        saveJButton.setToolTipText("Save Changes");
        saveJButton.setBorderPainted(false);
        saveJButton.setContentAreaFilled(false);
        saveJButton.setFocusPainted(false);
        saveJButton.setFocusable(false);
        saveJButton.setMaximumSize(new java.awt.Dimension(24, 24));
        saveJButton.setMinimumSize(new java.awt.Dimension(24, 24));
        saveJButton.setPreferredSize(new java.awt.Dimension(24, 24));
        saveJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buttonJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(controlJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void finishJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishJButtonActionPerformed
        // TODO add your handling code here:
        final PingMainJFrame pf = (PingMainJFrame) getParent();

        //
        final String at = addressJField.getText();
        final String ht = hostJField.getText();

        // Null case
        if (at != null && !at.isEmpty() && ht != null && !ht.isEmpty()) {

            // Destroy old; create new.
            if (pf.getController() != null) {
                pf.getController().terminateProcess();
            }

            // Create the controller.
            final PingController controller = new PingController(at, ht);
            controller.setBufferSize(bufferJSlider.getValue());

            // Set the controller and exit.
            pf.setController(controller);
            pf.setAlwaysOnTop(pinJToggle.isSelected());

            // Don't save changes
            //createPropertyFile();
            // Then close this dialog.
            setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Host and Address Fields cannot be blank.");
        }
    }//GEN-LAST:event_finishJButtonActionPerformed

    private void hostJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostJComboBoxActionPerformed
        // TODO add your handling code here:        // TODO add your handling code here:
        final String host = String.valueOf(hostJComboBox.getSelectedItem());

        // Null case
        if (host != null && !host.isEmpty()) {

            // Match that to the string in the configAppMap
            final String[] values = configAppsMap.get(host);

            //
            if (values != null && values.length == 3) {
                //
                hostJField.setText(host);
                addressJField.setText(values[0]);
                bufferJSlider.setValue(Integer.parseInt(values[1]));
                timerJSlider.setValue(Integer.parseInt(values[2]));

                //
                bufferJLabel.setText("Buffer:" + bufferJSlider.getValue());
                timerJLabel.setText("Wait:" + timerJSlider.getValue());
            }
        }
    }//GEN-LAST:event_hostJComboBoxActionPerformed

    private void timerJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timerJSliderStateChanged
        // TODO add your handling code here:
        timerJSlider.setToolTipText(String.valueOf(timerJSlider.getValue()) + " milliseconds delay.");
        timerJLabel.setText("Wait:" + timerJSlider.getValue() + "m/s");

        //
        if (!timerJSlider.getValueIsAdjusting()) {

            //
            final String host = String.valueOf(hostJComboBox.getSelectedItem());
            final String[] values = configAppsMap.get(host);

            // Null case
            if (values != null && values.length == 3) {

                // Grab the value array and adjust the timer index
                try {
                    values[2] = String.valueOf(timerJSlider.getValue());
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    System.err.println("NUUUUUUUUUU!!:\n" + aioobe);
                }
            }
        }
    }//GEN-LAST:event_timerJSliderStateChanged

    private void bufferJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bufferJSliderStateChanged
        // Update TooltipText
        bufferJSlider.setToolTipText(String.valueOf(bufferJSlider.getValue()) + " bytes.");
        bufferJLabel.setText("Buffer:" + bufferJSlider.getValue());

        //
        if (!bufferJSlider.getValueIsAdjusting()) {

            //
            final String host = String.valueOf(hostJComboBox.getSelectedItem());
            final String[] values = configAppsMap.get(host);

            // Null case
            if (values != null && values.length == 3) {

                // Grab the value array and adjust the timer index
                try {
                    values[1] = String.valueOf(bufferJSlider.getValue());
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    System.err.println("BUUUUUUUUUU!!:\n" + aioobe);
                }
            }
        }
    }//GEN-LAST:event_bufferJSliderStateChanged

    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
        // Just close.
        setVisible(false);
    }//GEN-LAST:event_closeJButtonActionPerformed

    private void saveJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveJButtonActionPerformed
        //
        final String ht = hostJField.getText();
        final String at = addressJField.getText();

        // Add the entry to the combo box model if we have good info.
        if (ht != null && !ht.isEmpty() && at != null && !at.isEmpty()) {

            //
            boolean contains = false;

            //
            final DefaultComboBoxModel m = (DefaultComboBoxModel) hostJComboBox.getModel();

            // If the model dos
            for (int i = 0; i < m.getSize(); i++) {
                if (String.valueOf(m.getElementAt(i)).equalsIgnoreCase(ht)) {
                    contains = true;
                }
            }

            // Store only if the model doesn't contain the new text
            if (!contains) {
                //
                configAppsMap.put(ht, new String[]{at, String.valueOf(bufferJSlider.getValue()), String.valueOf(timerJSlider.getValue())});
                m.addElement(ht);
            }

            //
            hostJComboBox.setModel(m);
        }

        //
        createPropertyFile();

        //
        JOptionPane.showMessageDialog(this, "Changes Saved.");
    }//GEN-LAST:event_saveJButtonActionPerformed

    private void hostJFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostJFieldActionPerformed
        //
        final String ht = hostJField.getText();
        boolean contains = false;

        //
        final DefaultComboBoxModel m = (DefaultComboBoxModel) hostJComboBox.getModel();

        // If the model dos
        for (int i = 0; i < m.getSize(); i++) {
            if (String.valueOf(m.getElementAt(i)).equalsIgnoreCase(ht)) {
                contains = true;
            }
        }

        // Store only if the model doesn't contain the new text
        if (!contains) {
            storeChanges();
        } else {
            JOptionPane.showMessageDialog(this, ht + " already exists in model.");
        }
    }//GEN-LAST:event_hostJFieldActionPerformed

    private void addressJFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressJFieldActionPerformed
        // TODO add your handling code here:
        storeChanges();
    }//GEN-LAST:event_addressJFieldActionPerformed

    private void timerJToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timerJToggleActionPerformed
        // TODO add your handling code here:
        timerRepeats = timerJToggle.isSelected();
        timerJToggle.setToolTipText(timerRepeats ? "Ping will Cycle Continuously." : "Ping will Report only one Cycle.");
    }//GEN-LAST:event_timerJToggleActionPerformed

    private void pinJToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pinJToggleActionPerformed
        // TODO add your handling code here:
        pinJToggle.setToolTipText(pinJToggle.isSelected() ? "Windows are Always on Top." : "Windows are not Always on Top");
    }//GEN-LAST:event_pinJToggleActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressJField;
    private javax.swing.JLabel addressJLabel;
    private javax.swing.JLabel bufferJLabel;
    private javax.swing.JSlider bufferJSlider;
    private javax.swing.JPanel buttonJPanel;
    private javax.swing.JButton closeJButton;
    private javax.swing.JPanel controlJPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JButton finishJButton;
    private javax.swing.JComboBox<String> hostJComboBox;
    private javax.swing.JTextField hostJField;
    private javax.swing.JLabel hostJLabel;
    private javax.swing.JToggleButton pinJToggle;
    private javax.swing.JButton saveJButton;
    private javax.swing.JLabel timerJLabel;
    private javax.swing.JSlider timerJSlider;
    private javax.swing.JToggleButton timerJToggle;
    // End of variables declaration//GEN-END:variables
}
