package io.github.alangaraujo.console;

import static me.tongfei.progressbar.ProgressBarStyle.ASCII;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.github.alangaraujo.api.client.RabbitApiClient;
import io.github.alangaraujo.model.Payload;
import io.github.alangaraujo.properties.RabbitProperties;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ApiController {

  private final String PROGRESSO = "Progresso";
  private final String CONSUMIDO = "Consumido";
  private final String PUBLICADO = "Publicado";
  private final String[] HEADERS = {"Mensagem", "Status"};
  private final ObjectMapper MAPPER = new ObjectMapper();

  private final RabbitApiClient rabbitApiClient;
  private final RabbitProperties rabbitProperties;
  private String fileNameWriter;

  public ApiController(RabbitProperties rabbitProperties) {
    this.rabbitProperties = rabbitProperties;
    this.rabbitApiClient = new RabbitApiClient(rabbitProperties);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
  }

  public boolean consume() {
    try {
      System.out.println("\nIniciando processo de consumo...\n");
      var fullPayload = rabbitApiClient.consume();

      if (fullPayload != null && !fullPayload.isEmpty()) {
        List<Payload> payload = MAPPER.readValue(fullPayload, MAPPER.getTypeFactory().constructCollectionType(List.class, Payload.class));
        final var progressBar = new ProgressBar(PROGRESSO, (payload.get(0).getMessageCount() + 1), ASCII).start();
        final var csvPrinter = createCsvPrinter(CONSUMIDO, rabbitProperties.getConsumeQueue(), HEADERS[0]);

        do  {
          payload = MAPPER.readValue(fullPayload, MAPPER.getTypeFactory().constructCollectionType(List.class, Payload.class));
          csvPrinter.print(payload.get(0).getPayload());
          csvPrinter.println();
          fullPayload = rabbitApiClient.consume();
          progressBar.step();
        } while (!fullPayload.isEmpty());

        csvPrinter.close();
        progressBar.stop();

        System.out.printf("%nMensagens da fila %s consumidas.%n", rabbitProperties.getConsumeQueue());

        return true;
      } else {
        System.out.println("Sem mensagens a consumir.");
        System.out.println("\nSe não estava esperando essa mensagem:");
        System.out.println("Verifique se suas configurações estão corretas.");
        System.out.println("Veja o nome da fila e vhost no RabbitMQ.");
        System.out.println("Se pegou o nome da fila no Grafana, provavelmente está incorreto.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  public void publish() {
    try {
      System.out.println("\nIniciando processo de publicação...\n");
      final var listCsvRecord = getCsvRecordsFromFile();

      if (!listCsvRecord.isEmpty()) {
        final var progressBar = new ProgressBar(PROGRESSO, listCsvRecord.size(), ASCII).start();
        final var csvPrinter = createCsvPrinter(PUBLICADO, rabbitProperties.getPublishQueue(), HEADERS);

        for (var registry : listCsvRecord) {
          final var correctedJson = registry.get(0).replaceAll("\"", "\\\\\"");
          final var statusOk = rabbitApiClient.publish(correctedJson);
          csvPrinter.print(registry.get(0));
          csvPrinter.print(statusOk ? "Publicado" : "Erro");
          csvPrinter.println();
          progressBar.step();
        }

        csvPrinter.close();
        progressBar.stop();

        System.out.printf("%nMensagens da fila %s publicadas.%n", rabbitProperties.getPublishQueue());
      } else {
        System.out.println("Não foram encontrados registros no arquivo origem para publicar.");
        System.out.println("\nSe não estava esperando essa mensagem:");
        System.out.println("Verifique seu arquivo .csv ou suas configurações no config.properties.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void transfer() {
    if (consume()) {
      rabbitProperties.setPublishFile(this.fileNameWriter);
      publish();
      System.out.println("\nTransferência concluída.");
    } else {
      System.out.println("Veja se os nomes das filas estão invertidas no config.properties.");
    }
  }

  private CSVPrinter createCsvPrinter(String action, String type, String... header) throws IOException {
    final var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy - HH-mm-ss");
    final var now = ZonedDateTime.now();
    this.fileNameWriter = String.format("./%s - %s - %s.csv", action, type, now.format(formatter));
    final var fileWriter = new FileWriter(fileNameWriter);
    return new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder().setHeader(header).setDelimiter(",").build());
  }

  private ArrayList<CSVRecord> getCsvRecordsFromFile() throws IOException {
    final var fileName = String.format("./%s", rabbitProperties.getPublishFile());
    final var fileReader = new FileReader(fileName);
    final var records = CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setSkipHeaderRecord(true).build().parse(fileReader);
    return new ArrayList<CSVRecord>(records.getRecords());
  }

}
