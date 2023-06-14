package io.github.alangaraujo.console.deprecated;

import io.github.alangaraujo.config.deprecated.RabbitAmqpConfig;
import io.github.alangaraujo.properties.RabbitProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Deprecated(since = "Consumo por AMQP experimental")
@Slf4j
public class AmqpController {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH-mm-ss");
    private final ZonedDateTime now = ZonedDateTime.now();

    private CSVPrinter csvPrinter;

    public void execute(RabbitProperties rabbitProperties) {
        try {
            System.out.printf("%nIniciando processamento...%n%n");
            final var rabbitConfig = new RabbitAmqpConfig(rabbitProperties);
            final var channel = rabbitConfig.getChannelFromConnection();
            final var initialMessageCount = channel.messageCount(rabbitProperties.getConsumeQueue());

            if (initialMessageCount > 0) {
                final var fileNameWriter = String.format("./%s - %s - %s.csv", "Publicado", rabbitProperties.getConsumeQueue(), now.format(formatter));
                try (var fileWriter = new FileWriter(fileNameWriter)) {
                    csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder().setHeader("Mensagem", "Status").build());
                }

                final var progressBar = new ProgressBar("Progresso", initialMessageCount, ProgressBarStyle.ASCII).start();
                var response = channel.basicGet(rabbitProperties.getConsumeQueue(), false);

                do {
                    String message = new String(response.getBody(), StandardCharsets.UTF_8);
                    csvPrinter.print(message);
                    response = channel.basicGet(rabbitProperties.getConsumeQueue(), false);
                    progressBar.step();
                } while (response != null);

                csvPrinter.close();
                progressBar.stop();
                rabbitConfig.closeConnection();

                System.out.printf("%nMensagens processadas, verifique o novo arquivo .csv na pasta.%n%n");
            } else {
                log.info("Sem mensagens a consumir.%n");
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
