package io.github.alangaraujo;

import io.github.alangaraujo.console.ApiController;
import io.github.alangaraujo.properties.PropertiesFactory;
import io.github.alangaraujo.properties.RabbitProperties;

import java.util.Scanner;

public class App {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        RabbitProperties rabbitProperties = PropertiesFactory.getRabbitProperties();
        ApiController apiController = new ApiController(rabbitProperties);
        processSelector(rabbitProperties, apiController);
    }

    private static void processSelector(RabbitProperties rabbitProperties, ApiController apiController) {
        switch (rabbitProperties.getProcessType()) {
            case "consumir" -> {
                System.out.println("Verifique a operação de consumo das mensagens:");
                System.out.printf("Fila origem: %s%n", rabbitProperties.getConsumeQueue());
                System.out.println("As mensagens serão excluídas da fila.");
                verifyResponse();
                apiController.consume();
            }
            case "publicar" -> {
                System.out.println("Verifique se o arquivo está com delimitadores por vírgulas e se possui as mensagens corretas.");
                verifyResponse();
                apiController.publish();
            }
            case "transferir" -> {
                System.out.println("Verifique a operação de transferência de mensagens:");
                System.out.printf("Fila origem: %s%n", rabbitProperties.getConsumeQueue());
                System.out.printf("Fila destino: %s%n", rabbitProperties.getPublishQueue());
                verifyResponse();
                apiController.transfer();
            }
            default ->
                    System.out.println("Opção inválida! Opções disponíveis para processamento: consumir, publicar ou transferir.");
        }
    }

    private static void verifyResponse() {
        String response;
        do {
            System.out.print("Confirma? (s/n): ");
            response = scanner.next();
            if ("n".equalsIgnoreCase(response)) {
                System.out.println("\nProcesso não iniciado.");
                System.exit(-1);
            }
        } while (!"s".equalsIgnoreCase(response));
    }
}
