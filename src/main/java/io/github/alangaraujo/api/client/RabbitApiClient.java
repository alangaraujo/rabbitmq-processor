package io.github.alangaraujo.api.client;

import static java.lang.String.format;

import io.github.alangaraujo.config.RabbitApiConfig;
import io.github.alangaraujo.properties.RabbitProperties;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class RabbitApiClient {

  private static final String ACK_TRUE = "ack_requeue_false";
  private static final String RABBITMQ_GET_URL_FORMAT = "%s:%d/api/queues/%s/%s/get";
  private static final String RABBITMQ_POST_URL_FORMAT = "%s:%d/api/exchanges/%s/amq.default/publish";
  private static final String GET_MESSAGE = "{\"vhost\": \"%s\", \"name\": \"%s\", \"truncate\": \"50000\", \"ackmode\": \"%s\", \"encoding\": \"auto\", \"count\": \"%d\"}";
  private static final String POST_MESSAGE = "{ \"vhost\": \"%s\", \"name\": \"amq.default\", \"properties\": { \"delivery_mode\": 2, \"headers\": {} }, \"routing_key\": \"%s\", \"delivery_mode\": \"2\", \"payload\": \"%s\", \"headers\": {}, \"props\": {}, \"payload_encoding\": \"string\" }";
  private static final HttpClient CLIENT = HttpClient.newHttpClient();
  private final String authorizationBearer;
  private RabbitProperties rabbitProperties;

  public RabbitApiClient(RabbitProperties rabbitProperties) {
    this.rabbitProperties = rabbitProperties;
    authorizationBearer = RabbitApiConfig.getAuthorization(rabbitProperties);
  }

  public String consume() {
      try {
        var body = format(GET_MESSAGE,
            rabbitProperties.getVirtualHost(), rabbitProperties.getConsumeQueue(), ACK_TRUE, 1);

        final var rabbitUri = new URI(
            format(RABBITMQ_GET_URL_FORMAT, rabbitProperties.getHostname(), rabbitProperties.getPort(),
                rabbitProperties.getVirtualHost(), rabbitProperties.getConsumeQueue()));

        final var request = getHttpRequest(rabbitUri, body);

        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 && !"[]".equals(response.body())) {
          return response.body();
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    return "";
  }

  public boolean publish(String message) {
    try {
      var body = format(POST_MESSAGE,
          rabbitProperties.getVirtualHost(), rabbitProperties.getPublishQueue(), message);

      final var rabbitUri = new URI(
          format(RABBITMQ_POST_URL_FORMAT, rabbitProperties.getHostname(), rabbitProperties.getPort(),
              rabbitProperties.getVirtualHost()));

      final var request = getHttpRequest(rabbitUri, body);

      var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

      return (response.statusCode() == 200 && "{\"routed\":true}".equalsIgnoreCase(response.body()));

    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private HttpRequest getHttpRequest(URI rabbitUri, String body) {
    return HttpRequest.newBuilder().uri(rabbitUri)
        .header("authorization", authorizationBearer)
        .version(HttpClient.Version.HTTP_1_1)
        .POST(HttpRequest.BodyPublishers
            .ofInputStream(() -> new ByteArrayInputStream(body.getBytes()))).build();
  }
}
