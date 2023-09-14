package com.tim.jsch.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.telnet.TelnetClient;
import com.tim.jsch.struct.TelnetCacheItem;

import lombok.Getter;
import lombok.Setter;

/**
 * Telnet functions.
 *
 * @author Tim Lee
 */
public class Telnet {
    private TelnetClient telnet = new TelnetClient("VT100");
    private InputStream in;
    private OutputStream out;
    private static final List<String> loginPrompt = Arrays.asList("username:", "login:");
    private static final List<String> passwordPrompt = Arrays.asList("password:");
    private static final List<String> commandPrompt = Arrays.asList("#", ">", "$");
    private String ip;
    private int port;
    private String user;
    private String password;
    private String telnetId;
    @Getter
    @Setter
    private static Map<String, TelnetCacheItem> telnetCache = new HashMap<String, TelnetCacheItem>(20);

    public Telnet(String ip, int port, String user, String password, String telnetId) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
        this.telnetId = telnetId;
    }

    /**
     * Login.
     *
     * @param user
     * @param password
     * @return String
     */
    private String login(String user, String password) {
        String str = "";
        System.out.println("--- start login ---");
        str += readUntil(loginPrompt);
        write(user);
        write("\r");
        str += readUntil(passwordPrompt);
        write(password);
        write("\r");
        str += readUntil(commandPrompt);
        System.out.println("--- login successfully ---");
        return str;
    }

    /**
     * Write value to machine.
     *
     * @param value
     */
    public void write(String value) {
        try {
            ((PrintStream) out).print(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect TeletClient.
     *
     * @return String
     */
    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set TelnetClient, and send command.
     * If TelnetClient already exist, use it.
     * If not exist, create a new one.
     *
     * @param command
     * @return String
     */
    public String sendCommand(String command) {
        String str = setTelnetClient();
        try {
            System.out.println("sendCommand: " + command);
            write(command);
            return str + readChar();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * If TelnetClient already exist, use it and return command response.
     * If TelnetClient not exist, create a new one, return login response and
     * command response.
     *
     * @return String
     */
    public String setTelnetClient() {
        String str = "";
        if (telnetCache.get(telnetId) != null) {
            telnet = telnetCache.get(telnetId).getTelnetClient();
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
        } else {
            try {
                telnet.connect(ip, port);
                System.out.println("--- telnet connect successfully ---");
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
            str += login(user, password);
            TelnetCacheItem telnetCacheItem = new TelnetCacheItem();
            telnetCacheItem.setConnectTime(System.currentTimeMillis());
            telnetCacheItem.setTelnetClient(telnet);
            addTelnetCacheItem(telnetId, telnetCacheItem);
        }
        return str;
    }

    /**
     * Return characters before matching patterns.
     *
     * @param pattern
     * @return String
     */
    private String readUntil(List<String> pattern) {
        StringBuffer sb = new StringBuffer();
        int timeOut = 100000;
        List<Character> lastChar = new ArrayList<>();
        for (int i = 0; i < pattern.size(); i++) {
            lastChar.add(i, pattern.get(i).charAt(pattern.get(i).length() - 1));
        }

        while (true) {
            char ch = 'a';
            for (int idleTime = 0; idleTime < timeOut; idleTime++) {
                try {
                    if (in.available() > 0) {
                        ch = (char) in.read();
                        sb.append(ch);
                        break;
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // if (idleTime == timeOut - 1) {
                //     Locale locale = LocaleContextHolder.getLocale();
                //     String message = messageSource.getMessage("common.cli.telnet.login.error", null, locale);
                //     throw new ServiceException(DViewError.ErrorTag.SYSTEM_TIPS, message, "common.cli.telnet.login.error", null);
                // }
            }
            for (int i = 0; i < pattern.size(); i++) {
                if (ch == lastChar.get(i)) {
                    if (sb.toString().toLowerCase().endsWith(pattern.get(i))) {
                        return sb.toString();
                    }
                }
            }

        }
    }

    /**
     * Return characters.
     *
     * @return String
     */
    private String readChar() {
        StringBuffer sb = new StringBuffer();
        int timeOut = 40;
        // for loop for a available input stream
        // break from for loop if current input stream is not available
        // sleep 1 ms if input stream is not available
        // return if input stream idle for 40 ms
        while (true) {
            for (int idleTime = 0; idleTime < timeOut; idleTime++) {
                try {
                    if (in.available() > 0) {
                        while (in.available() > 0) {
                            char ch = (char) in.read();
                            sb.append(ch);
                        }
                        break;
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (idleTime == timeOut - 1) {
                    return sb.toString();
                }
            }
        }
    }

    /**
     * Add TelnetClient to telnetCache.
     *
     * @param id
     * @param telnetCacheItem
     */
    public void addTelnetCacheItem(String id, TelnetCacheItem telnetCacheItem) {
        if (telnetCache.size() < 20) {
            telnetCache.put(id, telnetCacheItem);
            return;
        }
        Set<String> keys = telnetCache.keySet();
        long oldTime = 0;
        String maxId = "";
        for (String key : keys) {
            TelnetCacheItem connection = telnetCache.get(key);
            if (connection.getConnectTime() <= oldTime || oldTime == 0) {
                oldTime = connection.getConnectTime();
                maxId = key;
            }
        }
        removeTelnetCacheItem(maxId);
        telnetCache.put(id, telnetCacheItem);
    }

    /**
     * Remove TelnetClient from telnetCache.
     *
     * @param id
     */
    private void removeTelnetCacheItem(String id) {
        TelnetCacheItem connection = telnetCache.get(id);
        if (connection == null) {
            return;
        }
        if (connection.getTelnetClient().isConnected() == true) {
            try {
                connection.getTelnetClient().disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }
        telnetCache.remove(id);
    }
}