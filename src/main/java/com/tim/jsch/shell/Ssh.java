package com.tim.jsch.shell;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tim.jsch.struct.SshCacheItem;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Ssh {
    private String host;
    private String username;
    private String password;
    private String sessionId;
    private int port = 22;
    private int timeout = 60 * 60 * 1000;
    @Getter
    @Setter
    private static Map<String, SshCacheItem> sshCache = new HashMap<String, SshCacheItem>(20);

    public Ssh(String host, String username, String password, String sessionId, int port, int timeout) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.sessionId = sessionId;
        this.port = port;
        this.timeout = timeout;
    }

    public Ssh(String host, String username, String password, String sessionId) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.sessionId = sessionId;
    }

    public String execCommand(String cmd) {

        System.out.println("\ncmd:" + cmd);
        // disconnect ssh if command is disconnect
        if (cmd.compareTo("disconnect") == 0) {
            removeSshCacheItem(sessionId);
            return "Ssh disconnect successfully.";
        }

        // get ssh session
        Session session = getSession();
        if (!session.isConnected())
            try {
                session.connect();
            } catch (JSchException e) {
                System.out.println("Session connect failed");
                e.printStackTrace();
            }
        sshCache.get(sessionId).getPq().add(System.currentTimeMillis());
        // get channel
        Channel channel = getChannel(session);
        if (!channel.isConnected())
            try {
                channel.connect(timeout);
            } catch (JSchException e) {
                System.out.println("Channel connect failed");
                e.printStackTrace();
            }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        ChannelShell ssh = (ChannelShell) channel;
        ssh.setPtyType("vt100");
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
            for (int i = 0; i < 1; i++) {
                outputStream.write(cmd.getBytes());
            }
            // System.out.println("Successfully write outputStream: " + cmd);
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

        StringBuffer sb = new StringBuffer();
        int timeOut = 40;
        while (true) {
            for (int idleTime = 0; idleTime < timeOut; idleTime++) {
                try {
                    if (inputStream.available() > 0) {
                        while (inputStream.available() > 0) {
                            char ch = (char) inputStream.read();
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

    public Session getSession() {
        // System.out.println("Before get Session.");
        JSch jSch = new JSch();
        Session session = null;

        if (sshCache.get(sessionId) != null) {
            if (sshCache.get(sessionId).getSession().isConnected() == true) {
                // System.out.println("Session already exist and connect");
                // System.out.println("After get Session.");
                return sshCache.get(sessionId).getSession();
            } else {
                // System.out.println("Session already exist but not connect");
                removeSshCacheItem(sessionId);
            }
        }

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

        session.setConfig("StrictHostKeyChecking", "no");
        try {
            session.setServerAliveInterval(2000);
        } catch (JSchException e) {
            System.out.println("set session ServerAliveInterval exception");
            e.printStackTrace();
        }
        try {
            session.connect(timeout);
        } catch (JSchException e) {
            System.out.println("session connect exception");
            e.printStackTrace();
        }

        // start session cache
        SshCacheItem  item = new SshCacheItem();
        item.setSession(session);
        item.setConnectTime(System.currentTimeMillis());
        // AutoDisconnectJob autoDisconnectJob = new AutoDisconnectJob(item);
        // FutureTask<String> futureTask = new FutureTask<>(autoDisconnectJob);
        // ExecutorService executor = Executors.newFixedThreadPool(10);
        // executor.execute(futureTask);
        addSshCacheItem(sessionId, item);
        // System.out.println("After get Session.");
        return session;
    }

    public Channel getChannel(Session session) {
        Channel channel = null;

        if (sshCache.get(sessionId).getChannel() != null) {
            if (sshCache.get(sessionId).getChannel().isConnected() == true) {
                // System.out.println("Channel already exist and connect");
                return sshCache.get(sessionId).getChannel();
            } else {
                // System.out.println("Channel already exist but not connected");

            }
        }

        try {
            channel = session.openChannel("shell");
        } catch (JSchException e) {
            System.out.println("session open channel execption");
            e.printStackTrace();
        }
        sshCache.get(sessionId).setChannel(channel);
        return channel;
    }

    public void checkStatus(Session session, Channel channel){
        System.out.println("--- in checkStatus ---");
        if(session.isConnected()){
            System.out.println("Session connect");
        }
        else{
            System.out.println("Session not connect");
        }
        if(channel.isConnected()){
            System.out.println("Channel connect");
        }
        else{
            System.out.println("Channel not connect");
        }
    }

    public void addSshCacheItem(String id, SshCacheItem sshCacheItem){
        if(sshCache.size() < 20){
            sshCache.put(id, sshCacheItem);
            return;
        }
        Set<String> keys = sshCache.keySet();
        long oldTime = 0;
        String maxId = "";
        for (String key : keys) {
            SshCacheItem connection = sshCache.get(key);
            if (connection.getConnectTime() <= oldTime || oldTime == 0) {
                oldTime = connection.getConnectTime();
                maxId = key;
            }
        }
        removeSshCacheItem(maxId);
        sshCache.put(id, sshCacheItem);
    }

    public void removeSshCacheItem(String id) {
        SshCacheItem connection = sshCache.get(id);
        if (connection == null) {
            return;
        }
        if (connection.getSession().isConnected()) {
            connection.getSession().disconnect();
        }
        sshCache.remove(id);
    }
}