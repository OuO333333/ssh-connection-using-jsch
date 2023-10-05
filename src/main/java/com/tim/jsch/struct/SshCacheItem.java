package com.tim.jsch.struct;

import java.util.Collections;
import java.util.PriorityQueue;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

import lombok.Getter;
import lombok.Setter;

public class SshCacheItem {
    private @Getter @Setter Session session;
    private @Getter @Setter Channel channel;
    private @Getter @Setter long connectTime = 0;
    private @Getter @Setter PriorityQueue<Long> pq = new PriorityQueue<Long>(Collections.reverseOrder());
    private @Getter @Setter int timeOut = 10000;
}
