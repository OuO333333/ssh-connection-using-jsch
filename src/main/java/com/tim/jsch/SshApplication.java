package com.tim.jsch;

import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.tim.jsch.shell.Ssh;

@SpringBootApplication
public class SshApplication {

	public static void main(String[] args) {
		// SpringApplication.run(SshApplication.class, args);
		Ssh shell = new Ssh("192.168.220.211", "admin", "admin", "qwer");

		Scanner scanner = null;
		try {
			scanner = new Scanner(System.in);
			while (true) {
				System.out.print(">");
				String str = scanner.nextLine();
				System.out.println("--- start ---");
				long time1, time2;
				time1 = System.currentTimeMillis();
				String result = shell.execCommand(str + "\r", 2);
				time2 = System.currentTimeMillis();
				System.out.println("result: " + result);
				System.out.println("--- end ---");
				System.out.println("花了：" + (time2 - time1) + "毫秒");
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}

}
