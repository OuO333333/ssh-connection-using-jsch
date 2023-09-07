package com.tim.jsch.struct;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

import lombok.Getter;
import lombok.Setter;

public class SshCacheItem {
    private @Getter @Setter Session session;
    private @Getter @Setter Channel channel;
    private @Getter @Setter long connectTime = 0;
}
