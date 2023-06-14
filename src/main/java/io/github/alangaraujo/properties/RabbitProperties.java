package io.github.alangaraujo.properties;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RabbitProperties {

    private String username;
    private String password;
    private String virtualHost;
    @Getter(AccessLevel.NONE)
    private String hostname;
    private String processType;
    private String consumeQueue;
    private String publishQueue;
    private String publishFile;
    private int port;

    public String getHostname() {
        return this.hostname.replaceAll("/$", "");
    }

}
