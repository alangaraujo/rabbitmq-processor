package io.github.alangaraujo.config.deprecated;

import io.github.alangaraujo.properties.RabbitProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Deprecated(since = "Consumo por AMQP experimental")
public class RabbitAmqpConfig {

    private final Connection connection;

    public RabbitAmqpConfig(RabbitProperties rabbitProperties) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername(rabbitProperties.getUsername());
        factory.setPassword(rabbitProperties.getPassword());
        factory.setVirtualHost(rabbitProperties.getVirtualHost());
        factory.setHost(rabbitProperties.getHostname());
        factory.setPort(rabbitProperties.getPort());

        connection = factory.newConnection();
    }

    public Channel getChannelFromConnection() throws IOException {
        return connection.createChannel();
    }

    public void closeConnection() throws IOException {
        connection.close();
    }

}
