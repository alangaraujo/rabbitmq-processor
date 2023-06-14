package io.github.alangaraujo.config;

import static java.lang.String.format;

import io.github.alangaraujo.properties.RabbitProperties;

import java.util.Base64;

public class RabbitApiConfig {

  private static final String RABBITMQ_CREDENTIALS_FORMAT = "%s:%s";
  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private RabbitApiConfig() {
  }

  public static String getAuthorization(RabbitProperties rabbitProperties) {
    final var credentials = format(
        RABBITMQ_CREDENTIALS_FORMAT,
        rabbitProperties.getUsername(),
        rabbitProperties.getPassword());

    final var encodedCredentials = ENCODER.encodeToString(credentials.getBytes());

    return "Basic " + encodedCredentials;
  }

}
