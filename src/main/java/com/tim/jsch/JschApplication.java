package com.tim.jsch;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.tim.jsch.shell.Shell;

@SpringBootApplication
public class JschApplication {

	public static void main(String[] args) {
		SpringApplication.run(JschApplication.class, args);
		Shell shell = new Shell("192.168.220.184", "admin", "admin");

		Scanner scanner = null;
		try {
			scanner = new Scanner(System.in);
			while (true) {
				System.out.print(">");
				String str = scanner.nextLine();
				System.out.println("--- start ---");
				long time1, time2;

				time1 = System.currentTimeMillis();
				String result = shell.execCommand(str);
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
