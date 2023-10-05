package com.tim.jsch.job;

import com.tim.jsch.shell.Ssh;

public class CmdThread extends Thread {
    private Ssh ssh;
    private String command;
    private int threadNum;
    public CmdThread(Ssh ssh, String command, int threadNum) {
      this.ssh = ssh;
      this.command = command;
      this.threadNum = threadNum;
    }
    public void run() {
      String str = ssh.execCommand(command);
      System.out.println("ThreadNum: " + threadNum + "\ncommand: " + command + "\nresult: " + str + "\nend result");
    }
}
