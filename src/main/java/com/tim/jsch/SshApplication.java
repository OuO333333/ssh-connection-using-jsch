package com.tim.jsch;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.tim.jsch.shell.Ssh;

@SpringBootApplication
public class SshApplication {

	public static void main(String[] args) {
		Ssh shell = new Ssh("192.168.220.211", "admin", "admin", "qwer");
		shell.execCommand("s");
		shell.execCommand("h");
		shell.execCommand("o");
		shell.execCommand("w");
		shell.execCommand(" ");
		shell.execCommand("s");
		shell.execCommand("w");
		String result10 = shell.execCommand("\r");
		System.out.println("start result10:\n" + result10 + "\nend result 10");
		shell.removeSshCacheItem("qwer");
	}
}
