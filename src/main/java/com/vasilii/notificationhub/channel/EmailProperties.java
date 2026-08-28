package com.vasilii.notificationhub.channel;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    private String smtpHost;
    private String stpUsername;
    private int smtpPort = 25;
    private int retryAttempts = 3;
    private long retryDelayMs = 1000;

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getStpUsername() {
        return stpUsername;
    }

    public void setStpUsername(String stpUsername) {
        this.stpUsername = stpUsername;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}
