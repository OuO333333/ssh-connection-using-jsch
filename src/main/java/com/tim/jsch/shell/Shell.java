package com.tim.jsch.shell;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Shell {
    private String host;
    private String username;
    private String password;
    private int port = 22;
    private int timeout = 60 * 60 * 1000;
    // @Getter
    // private static Map<String, Session> sessionCache = new HashMap<String,
    // Session>(200);
    // private static Map<String, Channel> channelCache = new HashMap<String,
    // Channel>(200);

    public Shell(String host, String username, String password, int port, int timeout) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.port = port;
        this.timeout = timeout;
    }

    public Shell(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public String execCommand(String cmd) {

        System.out.println("cmd: " + cmd);

        // get ssh session
        Session session = getSession();
        if (!session.isConnected())
            try {
                session.connect();
            } catch (JSchException e) {
                // TODO Auto-generated catch block
                System.out.println("Session connect failed");
                e.printStackTrace();
            }

        // get channel
        Channel channel = getChannel(session);
        if (!channel.isConnected())
            try {
                channel.connect();
            } catch (JSchException e) {
                // TODO Auto-generated catch block
                System.out.println("Channel connect failed");
                e.printStackTrace();
            }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        ChannelShell ssh = (ChannelShell) channel;
        ssh.setPtyType("dump");
        ssh.setPty(true);
        if (!ssh.isConnected()) {
            try {
                ssh.connect(3600000);
            } catch (JSchException e) {
                System.out.println("channel connect execption");
                e.printStackTrace();
            }
        }

        try {
            outputStream = channel.getOutputStream();
        } catch (IOException e) {
            System.out.println("channel get outputStream execption");
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < 2; i++) {
                outputStream.write(cmd.getBytes());
                outputStream.write("\n".getBytes());
            }
            System.out.println("Successfully write outputStream: " + cmd);
        } catch (IOException e) {
            System.out.println("channel write outputStream execption");
            e.printStackTrace();
        }

        try {
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("channel flush outputStream execption");
            e.printStackTrace();
        }

        try {
            inputStream = ssh.getInputStream();
        } catch (IOException e) {
            System.out.println("channel get inputStream execption");
            e.printStackTrace();
        }

        byte[] tmp = new byte[1024];
        ByteArrayOutputStream resultByteArray = new ByteArrayOutputStream();
        int count = 0;
        try {

            // listen on inputStream for 5 times with a 0.6s sleep before every time
            while (count < 1) {
                try {
                    Thread.sleep(600);
                } catch (Exception ee) {
                    System.out.println("Thread Sleep execption");
                }
                while (inputStream.available() > 0) {
                    inputStream.read(tmp, 0, 1024);
                    resultByteArray.write(tmp);
                }
                count++;
            }
        } catch (IOException e) {
            System.out.println("channel write inputStream execption");
            e.printStackTrace();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            System.out.println("channel inputStream close execption");
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            System.out.println("channel outputStream close execption");
            e.printStackTrace();
        }
        channel.disconnect();
        session.disconnect();
        byte result[] = resultByteArray.toByteArray();
        return new String(result, 0, result.length);
    }

    public Session getSession() {
        JSch jSch = new JSch();
        Session session = null;
        // if (sessionCache.get(host + username + password) != null) {
        // if (sessionCache.get(host + username + password).isConnected() == true) {
        // System.out.println("Session already connected");
        // return sessionCache.get(host + username + password);
        // } else {
        // System.out.println("Session already exist but not connected");
        // return sessionCache.get(host + username + password);
        // // sessionCache.remove(host + username + password);
        // }
        // // return sessionCache.get(host + username + password);
        // }
        try {
            session = jSch.getSession(username, host, port);
        } catch (JSchException e) {
            System.out.println("get session exception");
            e.printStackTrace();
        }
        session.setPassword(password);
        try {
            session.setTimeout(timeout);
        } catch (JSchException e) {
            System.out.println("set session timeout exception");
            e.printStackTrace();
        }
        Properties sshConfig = new Properties();
        // sshConfig.put("kex", "diffie-hellman-group1-sha1");
        sshConfig.put("StrictHostKeyChecking", "no");
        session.setConfig(sshConfig);
        // session.setConfig("StrictHostKeyChecking", "no");
        try {
            session.connect(60000);
        } catch (JSchException e) {
            System.out.println("session connect exception");
            e.printStackTrace();
        }
        // sessionCache.put(host + username + password, session);
        return session;
    }

    public Channel getChannel(Session session) {
        Channel channel = null;
        // if (channelCache.get(host + username + password) != null) {
        // if (channelCache.get(host + username + password).isConnected() == true) {
        // System.out.println("Channel already connected");
        // return channelCache.get(host + username + password);
        // } else {
        // System.out.println("Channel already exist but not connected");
        // // return channelCache.get(host + username + password);
        // channelCache.remove(host + username + password);
        // }
        // // return channelCache.get(host + username + password);
        // }
        try {
            channel = session.openChannel("shell");
        } catch (JSchException e) {
            System.out.println("session open channel execption");
            e.printStackTrace();
        }
        // channelCache.put(host + username + password, channel);
        return channel;
    }
}