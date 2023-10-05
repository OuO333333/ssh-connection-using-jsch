package com.tim.jsch.job;

import java.util.concurrent.Callable;

import com.tim.jsch.struct.SshCacheItem;

import lombok.Getter;
import lombok.Setter;

public class AutoDisconnectJob implements Callable<String> {

    /**
     * SshCacheItem
     */
    @Getter
    @Setter
    private SshCacheItem sshCacheItem;

    public AutoDisconnectJob(SshCacheItem sshCacheItem) {
        this.sshCacheItem = sshCacheItem;
    }

    // @Getter
    // @Setter
    // private String message;

    /**
     * 檢查輸入參數，返回結果為true才會執行該任務
     *
     * @return true/false
     */
    public boolean check() {
        if (this.sshCacheItem == null)
            return false;
        return true;
    }

    /**
     * 處理任務
     *
     * @return String
     * @throws InterruptedException
     * @throws Exception            異常
     */
    public String handle() throws InterruptedException {
        while (true) {
            if (this.sshCacheItem.getPq().size() == 0) {
                // continue;
            } else if (this.sshCacheItem.getPq().size() == 1) {
                if (System.currentTimeMillis() - this.sshCacheItem.getPq().peek() > this.sshCacheItem.getTimeOut()) {
                    this.sshCacheItem.getSession().disconnect();
                    System.out.println("Session auto disconnect.");
                    return "Session auto disconnect.";
                }
            } else {
                Long time1 = this.sshCacheItem.getPq().poll();
                Long time2 = this.sshCacheItem.getPq().peek();
                this.sshCacheItem.getPq().add(time1);
                if (time1 - time2 > this.sshCacheItem.getTimeOut()) {
                    this.sshCacheItem.getSession().disconnect();
                    System.out.println("Session auto disconnect.");
                    return "Session auto disconnect.";
                }
            }
        }
    }

    /**
     * 處理完成後執行的操作,即在handle之後執行的操作
     *
     * @param String handle方法返回的處理結果
     */
    public void postHandle(String output) {
    }

    @Override
    public String call() throws Exception {
        String result = null;

        // 先進行參數檢查
        boolean checkRs = this.check();
        if (!checkRs) {
            return "Invalid input parameters for the task";
        }
        try {
            // 處理任務
            result = this.handle();
        } catch (InterruptedException e) {
            System.out.println("---handle() error---");
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            // 執行postHandle方法
            try {
                this.postHandle(result);
            } catch (Exception e) {
                System.out.println("---postHandle() error---");
                e.printStackTrace();
            }
        }
        return result;
    }

}
