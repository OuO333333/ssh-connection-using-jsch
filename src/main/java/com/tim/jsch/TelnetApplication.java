package com.tim.jsch;

public class TelnetApplication {

	public static void main(String[] args) {
		com.tim.jsch.shell.Telnet telnet =  new com.tim.jsch.shell.Telnet("192.168.220.211", 23, "admin", "admin");

		telnet.sendCommand("s");
		telnet.sendCommand("h");
		telnet.sendCommand("o");
		telnet.sendCommand("w");
		telnet.sendCommand(" ");
		String str = telnet.sendCommand("i");
		telnet.sendCommand("p");
		telnet.sendCommand("i");
		telnet.sendCommand("f");
		String str1 = telnet.sendCommand("\r");
		System.out.println("result6: \n" + str + "\nend result6");
		System.out.println("result10: \n" + str1 + "\nend result10");
		// String str11 = telnet.sendCommand("show\r");
		// System.out.println("result11: \n" + str11 + "\nend result11");

		// String str3 = telnet.sendCommand("llllllllllllllll");
		// String str4 = telnet.sendCommand("\r");
		// System.out.println("result2: \n" + str4 + "\nend result2");
		telnet.disconnect();
	}

}
