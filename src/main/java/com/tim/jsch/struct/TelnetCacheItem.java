package com.tim.jsch.struct;

import org.apache.commons.net.telnet.TelnetClient;

import lombok.Getter;
import lombok.Setter;

public class TelnetCacheItem {
    private @Getter @Setter TelnetClient telnetClient;
    private @Getter @Setter long connectTime = 0;
}
