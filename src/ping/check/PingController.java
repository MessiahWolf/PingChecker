/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ping.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author rcher
 */
public class PingController implements ActionListener {

    // Variable Declaration
    // Java Native Classes
    public BufferedReader reader;
    public Timer timer;
    //private final Timer statTimer;
    private ProcessBuilder builder;
    private Process process;
    // Project Classes
    private final PingMainJFrame frame;
    // Data Types
    private boolean trackPing = false;
    private final String host;
    private final String name;
    private Integer pingPID;
    private int ping;
    private int pingCount;
    private int pingTotal;
    private int pingMin, pingMed, pingMax;
    private int byteRate = 1000; // b/s
    // End of Variable Declaration

    public PingController(PingMainJFrame frame, String host, String name) {

        //
        this.frame = frame;
        this.host = host;
        this.name = name;

        //
        initializeProcess();
    }

    private void initializeProcess() {

        // Process Builder so we get our errors here instead of in cmd.
        builder = new ProcessBuilder("cmd.exe", "/c", "ping " + host + " -t -l " + byteRate);
        builder.redirectErrorStream(true);

        //
        timer = new Timer(733, this);
    }

    public void reactivateProcess() {

        // Always terminate first
        terminateProcess();

        //
        try {

            // So we're going to figure our pid by assuming its the ping.exe that comes after the others on the tasklist
            final HashMap<Integer, String> processes = getWindowsProcessMap();

            // Start new connection
            builder = new ProcessBuilder("cmd.exe", "/c", "ping " + host + " -t -l " + byteRate);
            builder.redirectErrorStream(true);

            //
            process = builder.start();

            // Reading what the command brings back from cmd.
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Track the entries we've found thus.
            final ArrayList<Integer> list1 = new ArrayList<>();
            final ArrayList<Integer> list2 = new ArrayList<>();

            // Grab the current tasklist from the Environment.
            for (Map.Entry<Integer, String> map : getWindowsProcessMap().entrySet()) {

                // Add all PID with host name of PING.EXE
                if (map.getValue().equals("PING.EXE")) {
                    list1.add(map.getKey());
                }
            }

            // Now onto the old set
            for (Map.Entry<Integer, String> map : processes.entrySet()) {

                if (map.getValue().equals("PING.EXE")) {
                    list2.add(map.getKey());
                }
            }

            // This should give us a list containing only a single PID; the one for our ping cmd.
            list1.removeAll(list2);

            // This should be our PID
            pingPID = list1.get(0);

            //
            timer.start();
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Kills the TCP/IP Ping command window.
    public void terminateProcess() {

        // Reset Progress bar
        pingCount = 0;
        pingTotal = 0;

        // Stop the timer.
        timer.stop();
        trackPing = false;
        frame.updatePing(0);

        // Kill the current process in cmd by PID.
        try {
            
            // Attempt to close the TCP/IP Ping executable that the cmd started.
            Runtime.getRuntime().exec("taskkill /pid " + pingPID + " /f  /T");

            // Process kill.
            if (process != null) {
                // Close the input stream and destroy the initial cmd.
                process.getInputStream().close();
                process.destroy();
            }
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private HashMap<Integer, String> getWindowsProcessMap() {

        //
        final HashMap<Integer, String> map = new HashMap<>();

        //
        try {

            // Create our command prompt command
            final ProcessBuilder taskBuilder = new ProcessBuilder("cmd.exe", "/c", "tasklist.exe /fo csv /nh");
            final Process taskProcess = taskBuilder.start();
            final BufferedReader taskReader = new BufferedReader(new InputStreamReader(taskProcess.getInputStream()));

            // Redirect errors here.
            taskBuilder.redirectErrorStream(true);

            //
            String[] pair = null;
            String line;

            // Do loop
            do {

                // Grab the next line from the command line process.
                line = taskReader.readLine();

                // Split the line we read in by the commas.
                if (line != null) {
                    //
                    try {

                        //
                        pair = line.split(",");
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, aioobe);
                    }
                }

                //
                final Integer pi = Integer.parseInt(pair[1].substring(1, pair[1].length() - 1));

                // Removes the \"'s and places into map
                map.put(pi, (String) pair[0].substring(1, pair[0].length() - 1));
                
                //
                taskProcess.destroy();
            } while (line != null);
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Return the map of PID's and Process Names.
        return map;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        //
        try {

            // Base case.
            if (process == null) {
                return;
            }

            // Grab it here.
            final String output = reader.readLine();

            // output usually in the form of. "Reply from xxx.xxx.xxx.x: bytes=32 time=73ms TTL=48"
            if (output.contains("time=") && output.contains("TTL")) {

                //
                final int index = output.indexOf("time=");
                final int indexOut = output.indexOf("TTL");

                // Just moving to after time= but before TTL in the output from cmd.
                ping = Integer.parseInt(output.substring(index > 1 ? index + 5 : 0, (indexOut - 3) > 1 ? indexOut - 3 : 0));
            }
            
            // Are we tracking the 5 second average?
            if (trackPing == true) {

                //
                pingCount += (int) timer.getDelay();
                pingTotal += ping;

                // Once ping count == pingMax, reset
                if (pingCount == (int) (timer.getDelay() * 5)) {
                    frame.updateAverage();
                    pingCount = 0;
                    pingTotal = 0;
                }
            }
            
            // Update the frame.
            frame.updatePing(ping);
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Accessors and Mutators
    public String getHost() {
        return host;
    }

    public String getHostName() {
        return name;
    }

    public int getCurrentPing() {
        return ping;
    }

    public int getAveragePing() {
        return (pingTotal / 5);
    }

    public int getByteTransferRate() {
        return byteRate;
    }
    
    public int[] getPingBounds() {
        return new int[]{pingMin, pingMed, pingMax};
    }

    public boolean isTracking() {
        return trackPing;
    }

    public void setTracking(boolean trackPing) {
        this.trackPing = trackPing;

        //
        if (!trackPing) {
            pingCount = 0;
            pingTotal = 0;
        }
    }

    public void setByteTransferRate(int byteRate) {
        this.byteRate = byteRate;
    }
    
    public void setPingBounds(int pingMin, int pingMed, int pingMax) {
        this.pingMin = pingMin;
        this.pingMed = pingMed;
        this.pingMax = pingMax;
    }
}
