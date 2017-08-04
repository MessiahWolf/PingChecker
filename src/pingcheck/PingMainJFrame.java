package pingcheck;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;
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
    // Project Classes
    private PingController controller;
    // Data Types
    private boolean wasRunning = false;
    // Java Native Classes
    private static ImageIcon iconGreen;
    private static ImageIcon iconExit;
    private static ImageIcon iconFail;
    private static ImageIcon iconInActive;
    private static ImageIcon iconYellow;
    private static ImageIcon iconRed;
    private static ImageIcon iconTry;
    // Java Swing Classes
    private Timer timer;
    // End of Variable Declaration

    public static void main(String[] args) {

        //
        //final PingMainJFrame frame = new PingMainJFrame("104.160.131.3", "League Of Legends");
        final PingMainJFrame frame = new PingMainJFrame();
        // Attempt to set the look and feel of the application
        try {

            // Set to native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException cnfe) {
            System.err.println(cnfe);
        }

        //
        frame.setBackground(new java.awt.Color(0, 0, 0, 0));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    //
    public PingMainJFrame() {

        //
        initComponents();

        //
        init();
    }

    private void init() {

        //
        timer = new Timer(733, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                //
                if (controller != null) {

                    // @TODO fix: Using updatePing will cause the UI to lag while running the command.
                    final int ping = controller.updatePing();

                    // If the connection failed with a request timed out.
                    switch (ping) {
                        case -3:
                            // Terminate the connection
                            controller.terminateProcess();
                            optionJButton.setIcon(iconFail);
                            break;
                        case -4:
                            // Just stop the timer.
                            optionJButton.setIcon(iconFail);
                            break;
                        default:
                            // Update the ping
                            pingJProgressbar.setValue(ping);
                            updatePing(ping);
                            break;
                    }

                    //
                    if (timer.isRepeats() == false) {

                        //
                        if (controller.hasFiredOnce() == false) {
                            timer.restart();
                        } else {
                            controller.setHasFiredOnce(false);
                            controller.terminateProcess();
                            timer.stop();
                        }
                    }
                }
            }
        });

        // We'll say that 1000 is the red zone.
        pingJProgressbar.setStringPainted(true);
        pingJProgressbar.setMaximum(1000);
        pingJProgressbar.setString("~");
        pingJProgressbar.setBackground(new Color(0, 0, 0, .05f));

        // Making Icons and seting up Frame Icons.
        final Class closs = getClass();
        final Toolkit kit = Toolkit.getDefaultToolkit();

        //
        iconGreen = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-connect-green7882.png")));
        iconYellow = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-connect-yellow7882.png")));
        iconRed = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-connect-red7882.png")));
        iconInActive = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-connect-gray7882.png")));
        iconExit = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-close1616.png")));
        iconFail = new ImageIcon(kit.getImage(closs.getResource("/icons/icon-connect-fail7882.png")));
        iconTry = new ImageIcon(kit.createImage(closs.getResource("/icons/icon-connect-try7882.png")));

        //
        optionJButton.setIcon(iconInActive);
        optionJButton.setRolloverIcon(iconTry);
        //optionJButton.setOpaque(false);

        //
        exitJButton.setIcon(iconExit);
        exitJButton.setRolloverIcon(new ImageIcon(kit.getImage(closs.getResource("/icons/icon-closer1616.png"))));
        //exitJButton.setOpaque(false);

        //
        final ArrayList<Image> images = new ArrayList<>();
        images.add(kit.getImage(closs.getResource("/icons/icon-frame16.png")));
        images.add(kit.getImage(closs.getResource("/icons/icon-search-green32.png")));
        this.setIconImages(images);

        // Since we only need to do something when this application is closed we need only a window adapter instead of the full listener
        // impl.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                // Close the command prompt on exit
                if (controller != null) {
                    controller.terminateProcess();
                }
            }

            @Override
            public void windowClosed(WindowEvent event) {
                // Close the command prompt on exit
                if (controller != null) {
                    controller.terminateProcess();
                }
            }
        });

        // @TODO frame background turns white when other applications go fullscreen.
        setBackground(new Color(0, 0, 0, 0f));
        setForeground(new Color(0, 0, 0, 0f));
        getContentPane().setBackground(new Color(0, 0, 0, 0f));
        getContentPane().setForeground(new Color(0, 0, 0, 0f));
        
        // Place in the bottom-right of the screen.
        setTitle("Don't DDOS");
        setLocation(kit.getScreenSize().width / 2 - getWidth() / 2, kit.getScreenSize().height / 2 - getHeight() / 2);
    }

    public void updatePing(int ping) {

        // Update text
        pingJProgressbar.setString(ping >= 1 ? ping + " (m/s)" : "~");

        // Change color
        Color color;
        if (ping <= 0) {
            color = Color.GRAY;
            pingJProgressbar.setString("~");
        } else if (ping <= 90) {
            color = new Color(183, 204, 224);
            optionJButton.setIcon(iconGreen);
        } else if (ping > 90 && ping < 130) {
            color = new Color(255, 237, 99);
            optionJButton.setIcon(iconYellow);
        } else {
            color = new Color(255, 58, 36);
            optionJButton.setIcon(iconRed);
        }

        // Change the bar value
        pingJProgressbar.setForeground(color);
        pingJProgressbar.setValue(ping);
        pingJProgressbar.repaint();
    }

    public void setController(PingController controller) {
        this.controller = controller;
    }

    public PingController getController() {
        return controller;
    }

    public Timer getTimer() {
        return timer;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pingJProgressbar = new javax.swing.JProgressBar();
        optionJButton = new javax.swing.JButton();
        exitJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setAutoRequestFocus(false);
        setMaximumSize(new java.awt.Dimension(98, 124));
        setUndecorated(true);
        setResizable(false);

        pingJProgressbar.setMaximumSize(new java.awt.Dimension(56, 14));
        pingJProgressbar.setMinimumSize(new java.awt.Dimension(56, 14));
        pingJProgressbar.setPreferredSize(new java.awt.Dimension(56, 14));

        optionJButton.setToolTipText("Left-Click to Start. Right-Click for Options.");
        optionJButton.setBorderPainted(false);
        optionJButton.setContentAreaFilled(false);
        optionJButton.setFocusPainted(false);
        optionJButton.setMaximumSize(new java.awt.Dimension(78, 82));
        optionJButton.setMinimumSize(new java.awt.Dimension(78, 82));
        optionJButton.setPreferredSize(new java.awt.Dimension(78, 82));
        optionJButton.setSelected(true);
        optionJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                optionJButtonMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                optionJButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                optionJButtonMouseReleased(evt);
            }
        });
        optionJButton.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                optionJButtonMouseDragged(evt);
            }
        });

        exitJButton.setToolTipText("Close this Application");
        exitJButton.setBorderPainted(false);
        exitJButton.setContentAreaFilled(false);
        exitJButton.setMaximumSize(new java.awt.Dimension(16, 14));
        exitJButton.setMinimumSize(new java.awt.Dimension(16, 14));
        exitJButton.setPreferredSize(new java.awt.Dimension(16, 14));
        exitJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(optionJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pingJProgressbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(optionJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pingJProgressbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exitJButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void optionJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionJButtonMouseClicked
        // TODO add your handling code here:
        if (evt.getButton() == MouseEvent.BUTTON3) {

            // Stop the timer and kill the process.
            if (controller != null) {
                controller.terminateProcess();
                timer.stop();
            }

            //
            //final java.awt.Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            // TODO add your handling code here:
            final PingOptDialog dialog = new PingOptDialog(this, true);
            //dialog.setLocation(screen.width / 2 - dialog.getWidth() / 2, screen.height / 2 - dialog.getHeight() / 2);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            dialog.dispose();

            // If the controller is ready change the icon to blue
            if (controller != null) {
                optionJButton.setIcon(iconTry);
            } else {
                optionJButton.setIcon(iconInActive);
            }

            // Our controller is the one from the ping options.
            timer.setDelay(dialog.getTimerDelay());
            timer.setRepeats(dialog.getTimerRepeats());
        } else if (evt.getButton() == MouseEvent.BUTTON1) {

            // Activate the controller.
            if (controller != null) {

                //
                controller.reactivateProcess();

                // Switching the timer on and off.
                if (!timer.isRunning()) {
                    timer.start();
                    optionJButton.setIcon(iconGreen);
                    pingJProgressbar.setOpaque(false);
                } else {

                    //
                    controller.terminateProcess();

                    // Stop the timer.
                    timer.stop();

                    // Some visual options
                    optionJButton.setIcon(iconInActive);
                    pingJProgressbar.setOpaque(true);
                    pingJProgressbar.setBackground(new Color(0, 0, 0, .05f));
                }
            }
        }
    }//GEN-LAST:event_optionJButtonMouseClicked

    private void optionJButtonMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionJButtonMouseDragged
        // Move the frame
        setLocation(new Point(evt.getLocationOnScreen().x - getWidth() / 2, evt.getLocationOnScreen().y - getHeight() / 2));
    }//GEN-LAST:event_optionJButtonMouseDragged

    private void optionJButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionJButtonMousePressed
        // Fix for window lag.
        if (timer.isRunning()) {

            //
            pingJProgressbar.setString("~");
            wasRunning = true;

            // Restart the controller
            if (controller != null) {
                controller.terminateProcess();
            }

            // Stop the timer.
            timer.stop();
        }
    }//GEN-LAST:event_optionJButtonMousePressed

    private void optionJButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionJButtonMouseReleased
        // Small Fix to the window lagging while cmd is running.
        if (wasRunning) {

            //
            wasRunning = false;

            // Restart the controller
            if (controller != null) {
                controller.reactivateProcess();
            }

            // Restart the timer
            timer.start();
        }
    }//GEN-LAST:event_optionJButtonMouseReleased

    private void exitJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitJButtonActionPerformed
        // TODO add your handling code here:
        if (controller != null) {
            controller.terminateProcess();
        }

        // Close the application
        System.exit(0);
    }//GEN-LAST:event_exitJButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exitJButton;
    private javax.swing.JButton optionJButton;
    private javax.swing.JProgressBar pingJProgressbar;
    // End of variables declaration//GEN-END:variables
}
