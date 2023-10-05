package com.tim.jsch;

public class TelnetApplication {

	public static void main(String[] args) {
		com.tim.jsch.shell.Telnet telnet =  new com.tim.jsch.shell.Telnet("192.168.220.211", 23, "admin", "admin", "telnetId");

		String str1 = telnet.sendCommand("s");
		telnet.sendCommand("h");
		telnet.sendCommand("o");
		telnet.sendCommand("w");
		telnet.sendCommand(" ");
		String str6 = telnet.sendCommand("s");
		telnet.sendCommand("w");
		String str10 = telnet.sendCommand("\r");
		System.out.println("result1:\n" +str1 + "\nend result1");
		System.out.println("result6:\n" + str6 + "\nend result6");
		System.out.println("result10:\n" + str10 + "\nend result10");
		telnet.disconnect();
	}

}
