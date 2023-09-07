package com.tim.jsch.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;

public class Telnet {
    private TelnetClient telnet = new TelnetClient("VT100");
    private InputStream in;
    private OutputStream out;
    private static final List<String> loginPrompt = Arrays.asList("username:", "login:", "Username:", "Login:");
    private static final List<String> passwordPrompt = Arrays.asList("password:", "Password:");
    private static final List<String> commandPrompt = Arrays.asList("#", ">", "$", "aaaaa");

    public Telnet(String ip, int port, String user, String password) {
        try {
            telnet.connect(ip, port);
            System.out.println("--- telnet connect successfully ---");
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
        login(user, password);
    }

    private void login(String user, String password) {
        System.out.println("--- start login ---");
        readUntil(loginPrompt);
        write(user);
        write("\r");
        readUntil(passwordPrompt);
        write(password);
        write("\r");
        System.out.println("--- login successfully ---");
        readUntil(commandPrompt);
    }

    public void write(String value) {
        try {
            ((PrintStream) out).print(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) {
        try {
            System.out.println("sendCommand: " + command);
            write(command);
            // if (command.compareTo("\r") == 0)
            List<String> s = new ArrayList<>();
            ;
            // s.add("aaaaa");
            return readChar();
            // return readUntil(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readUntil(List<String> pattern) {
        StringBuffer sb = new StringBuffer();
        int timeOut = 40;
        List<Character> lastChar = new ArrayList<>();
        for (int i = 0; i < pattern.size(); i++) {
            lastChar.add(i, pattern.get(i).charAt(pattern.get(i).length() - 1));
        }

        while (true) {
            char ch = 'a';
            for (int count = 0; count < timeOut; count++) {
                try {
                    if (in.available() > 0) {
                        while (in.available() > 0) {
                            ch = (char) in.read();
                            sb.append(ch);
                        }
                        break;
                    } else {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (count == timeOut - 1) {
                    return sb.toString();
                }
            }
            for (int i = 0; i < pattern.size(); i++) {
                if (ch == lastChar.get(i)) {
                    if (sb.toString().endsWith(pattern.get(i))) {
                        return sb.toString();
                    }
                }
            }

        }
    }

    private String readChar() {
        StringBuffer sb = new StringBuffer();
        int timeOut = 40;

        while (true) {
            // char ch = 'a';
            for (int count = 0; count < timeOut; count++) {
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
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (count == timeOut - 1) {
                    return sb.toString();
                }
            }
        }
    }
}