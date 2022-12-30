package io.github.alangaraujo.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFactory {

    private PropertiesFactory() {}

    public static RabbitProperties getRabbitProperties() {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("./config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return RabbitProperties.builder()
            .username(properties.getProperty("username"))
            .password(properties.getProperty("password"))
            .virtualHost(properties.getProperty("vhost"))
            .hostname(properties.getProperty("hostname"))
            .processType(properties.getProperty("process-type"))
            .consumeQueue(properties.getProperty("consume-queue"))
            .publishQueue(properties.getProperty("publish-queue"))
            .publishFile(properties.getProperty("publish-file"))
            .port(Integer.parseInt(properties.getProperty("port")))
            .build();
    }
}
