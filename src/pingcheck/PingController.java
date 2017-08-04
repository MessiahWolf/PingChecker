/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rcher
 */
public class PingController {

    // Variable Declaration
    // Java Native Classes
    public BufferedReader reader;
    //private final Timer statTimer;
    private ProcessBuilder builder;
    private Process process;
    // Project Classes
    // Data Types
    private boolean hasFiredOnce = false;
    private final String address;
    private final String host;
    private Integer pingPID;
    private int ping;
    private int bufferSize = 1000; // b/s
    private final int MAX_ATTEMPTS = 3;
    private int FAIL_COUNT;
    // End of Variable Declaration

    public PingController(String address, String host) {

        //
        this.address = address;
        this.host = host;
    }

    public void reactivateProcess() {

        // Always terminate first so we never have multiple processes open.
        terminateProcess();

        //
        if (address == null || address.isEmpty()) {
            return;
        }

        //
        try {

            // So we're going to figure our pid by assuming its the ping.exe that comes after the others on the tasklist
            final HashMap<Integer, String> processes = createWindowsProcessMap();

            // Start new connection
            builder = new ProcessBuilder("cmd.exe", "/c", "ping " + address + " -w 499 -n 10 -l " + bufferSize);
            builder.redirectErrorStream(true);

            //
            process = builder.start();

            // Reading what the command brings back from cmd.
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Track the entries we've found thus.
            final ArrayList<Integer> list1 = new ArrayList<>();
            final ArrayList<Integer> list2 = new ArrayList<>();

            // Grab the current tasklist from the Environment.
            for (Map.Entry<Integer, String> map : createWindowsProcessMap().entrySet()) {

                // Add all PID with address name of PING.EXE
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
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Kills the TCP/IP Ping command window.
    public void terminateProcess() {

        // Kill the current process in cmd by PID.
        try {

            // Attempt to close the TCP/IP Ping executable that the cmd started.
            Runtime.getRuntime().exec("taskkill /pid " + pingPID + " /f  /T");

            // Process kill.
            if (process != null) {
                // Close the input stream and destroy the initial cmd.
                process.getInputStream().close();
                process.destroy();

                //
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private HashMap<Integer, String> createWindowsProcessMap() {

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

    public int updatePing() {

        // The lag this method creates could be because of the timer being offset from
        // the return time of the actual command.
        try {

            // Base case.
            if (process == null || reader == null) {
                return -1;
            }

            // Grab it here.
            final String output = reader.readLine();
            System.out.println("UP: " + output);

            // Base case
            if (output == null) {
                FAIL_COUNT++;
                tryTimeout();
                return -1;
            } else if (output.isEmpty() || output.equalsIgnoreCase("null")) {
                FAIL_COUNT++;
                tryTimeout();
                return -2;
            }

            // output usually in the form of. "Reply from xxx.xxx.xxx.x: bytes=32 time=73ms TTL=48"
            if (output.contains("time=") && output.contains("TTL")) {

                //
                final int index = output.indexOf("time=");
                final int indexOut = output.indexOf("TTL");

                // Has fired
                hasFiredOnce = true;

                // Just moving to after time= but before TTL in the output from cmd.
                return Integer.parseInt(output.substring(index > 1 ? index + 5 : 0, (indexOut - 3) > 1 ? indexOut - 3 : 0));
            } else if (output.contains("Request timed out")) {
                FAIL_COUNT++;
                tryTimeout();
                // Request times out.
                return -3;
            } else {
                FAIL_COUNT++;
                tryTimeout();
                // BAD ERROR this is thrown when the connection can't be made or is interupted.
                return -4;
            }
        } catch (IOException ex) {
            //System.out.println("Failed: " + ex);
            Logger.getLogger(PingMainJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        //
        return 0;
    }

    private void tryTimeout() {
        
        //
        System.out.println("Fails: " + FAIL_COUNT + " | Max: " + MAX_ATTEMPTS);
        
        //
        if (FAIL_COUNT >= MAX_ATTEMPTS) {
            FAIL_COUNT = 0;
            
            // Kill the process @TODO update the icon in pingMainJFrame
            terminateProcess();
        }
    }

    // Accessors and Mutators
    public boolean hasFiredOnce() {
        return hasFiredOnce;
    }

    public String getAddress() {
        return address;
    }

    public String getHost() {
        return host;
    }

    public int getCurrentPing() {
        return ping;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setHasFiredOnce(boolean hasFiredOnce) {
        this.hasFiredOnce = hasFiredOnce;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
