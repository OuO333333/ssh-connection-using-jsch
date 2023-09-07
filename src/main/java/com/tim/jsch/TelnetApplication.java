package com.tim.jsch;

public class TelnetApplication {

	public static void main(String[] args) {
		com.tim.jsch.shell.Telnet telnet =  new com.tim.jsch.shell.Telnet("192.168.220.211", 23, "admin", "admin");

		String str1 = telnet.sendCommand("s");
		String str2 = telnet.sendCommand("h");
		String str3 = telnet.sendCommand("o");
		String str4 = telnet.sendCommand("w");
		String str5 = telnet.sendCommand(" ");
		String str6 = telnet.sendCommand("i");
		String str7 = telnet.sendCommand("p");
		String str8 = telnet.sendCommand("i");
		String str9 = telnet.sendCommand("f");
		String str10 = telnet.sendCommand("\r");
		System.out.println("result6: \n" + str6 + "\nend result6");
		System.out.println("result10: \n" + str10 + "\nend result10");
		// String str11 = telnet.sendCommand("show\r");
		// System.out.println("result11: \n" + str11 + "\nend result11");

		// String str3 = telnet.sendCommand("llllllllllllllll");
		// String str4 = telnet.sendCommand("\r");
		// System.out.println("result2: \n" + str4 + "\nend result2");
		telnet.disconnect();
	}

}
