package com.tim.jsch.shell;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tim.jsch.struct.SshCacheItem;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
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

    public String execCommand(String cmd, int waitingTimes) {

        // System.out.println("cmd: " + cmd);

        if (cmd.compareTo("disconnect") == 0) {
            // sessionCache.remove(host + username + password);
            // sshCache.remove(sessionId);
            removeSshCacheItem(sessionId);
            return "disconnect successfully";
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
                //outputStream.write("\n".getBytes());
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

            // listen on inputStream for 2 times with a 0.6s sleep before every time
            while (count < waitingTimes) {
                try {
                    Thread.sleep(300);
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

        // checkStatus(session, channel);

        /*
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
        */
        // channel.disconnect();
        // session.disconnect();
        byte result[] = resultByteArray.toByteArray();
        return new String(result, 0, result.length);
    }

    public Session getSession() {
        JSch jSch = new JSch();
        Session session = null;

        // start session cache
        if (sshCache.get(sessionId) != null) {
            if (sshCache.get(sessionId).getSession().isConnected() == true) {
                System.out.println("Session already exist and connect");
                return sshCache.get(sessionId).getSession();
            } else {
                System.out.println("Session already exist but not connect");
                // return sessionCache.get(host + username + password);
                removeSshCacheItem(sessionId);
                // sshCache.remove(host + username + password);
            }
        }
        // end session cache

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
        addSshCacheItem(sessionId, item);
        // sshCache.get(host + username + password).setSession(session);
        // end session cache

        return session;
    }

    public Channel getChannel(Session session) {
        Channel channel = null;

        // start channel cache
        if (sshCache.get(sessionId).getChannel() != null) {
            if (sshCache.get(sessionId).getChannel().isConnected() == true) {
                System.out.println("Channel already exist and connect");
                return sshCache.get(sessionId).getChannel();
            } else {
                System.out.println("Channel already exist but not connected");
                // sshCache.get(host + username + password).getChannel().disconnect();
                // return channelCache.get(host + username + password);
                // sshCache.get(host + username + password).setChannel(null);
            }
        }
        // end channel cache

        try {
            channel = session.openChannel("shell");
        } catch (JSchException e) {
            System.out.println("session open channel execption");
            e.printStackTrace();
        }

        // start channel cache
        // SshCacheItem item = new SshCacheItem();
        // item.setChannel(channel);
        // sshCache.put(host + username + password, item);
        sshCache.get(sessionId).setChannel(channel);
        // end channel cache

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
        // System.out.println("--- in addSshCacheItem ---");
        // System.out.println("origin sshcache size: " + sshCache.size());
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
        // System.out.println("modify sshcache size: " + sshCache.size());
    }

    private void removeSshCacheItem(String id) {
        // System.out.println("--- in removeSshCacheItem ---");
        // System.out.println("origin sshcache size: " + sshCache.size());
        SshCacheItem connection = sshCache.get(id);
        if (connection == null) {
            return;
        }
        if (connection.getSession().isConnected()) {
            connection.getSession().disconnect();
        }
        sshCache.remove(id);
        // System.out.println("modify sshcache size: " + sshCache.size());
    }
}