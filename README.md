# Processador de Mensagens RabbitMQ

## Aplicação para captura e publicação de mensagens no RabbitMQ

### Apresentação
Eventualmente, em operações de ops, é necessário consumir as mensagens que estão em Parking Lot para análise e correção, e dependendo da quantidade, a extração de mensagens através da página do RabbitMQ com regex em ferramentas como VSCode ou SublimeText não é prático, e a página do RabbitMQ não possui a funcionalidade de download de mensagens.

Por isso, foi criado esse app, ele extrai as mensagens de uma fila e cria um arquivo CSV com os payloads em sua máquina.

O processamento desta aplicação com o RabbitMQ Broker se dá com chamadas HTTP em vez da API, devido eventuais bloqueios de acesso à referida API de seu Broker.

As configurações devem estar no arquivo config.properties, no mesmo diretório do arquivo .jar compilado, no qual é definido o necessário para conectar ao broker.

Caso precise publicar uma lista de mensagens, crie um arquivo .csv (Microsoft® Excel® ou LibreOffice Calc) com a palavra "Mensagem" (sem aspas) na célula A1, e "Status" (sem aspas) na célula B1, delimitação de células por vírgulas (é perguntado ao salvar pela primeira vez), e nas linhas abaixo dela, cole as mensagens (uma em cada linha).

Para processo de transferências de mensagens entre filas, basta informar a fila origem e destino.

Caso utilize os processos de publicação ou transferência, será criado um arquivo de publicação (como um log), e haverá uma coluna (B1) com o status da publicação da mensagem ('Publicado' ou 'Erro').

### Atenção: Quando é feito o processo de consumo da fila, as mensagens da fila no RabbitMQ são removidas, porém elas são salvas em um arquivo .csv que será criado na mesma pasta do arquivo .jar em sua máquina. Caso seja feito o processo de transferência entre filas, o arquivo de consumo criado também será usado para publicação, portanto, não efetue nenhuma alteração em qualquer arquivo até que a execução do app seja concluída.

### Requisitos
Java 17  
Maven 3.8.5+

### Execução

Faça o build com:
>mvn clean install

Entre no diretório target:
>cd target

Configure o arquivo config.properties, que se encontra na pasta, para acesso ao RabbitMQ:

*As opções de processamento (process-type) são 'consumir', 'publicar' e 'transferir'.*

> hostname=___<seu-host-url>___  
> port=___<a-porta-necessaria-80-ou-443>___  
> username=___<seu-usuario>___  
> password=___<sua-senha>___  
> vhost=___<virtual-host-da-fila-(queue)>___  
> process-type=___<consumir-publicar-ou-transferir-(consume-queue-para-publish-queue)>___   
> consume-queue=___<fila-a-ser-consumida-(válido-para-opções-consumir-ou-transferir)>___  
> publish-queue=___<fila-a-ser-publicada-(válido-para-opções-publicar-ou-transferir)>___  
> publish-file=___<arquivo-csv-com-mensagens-a-publicar-(válido-para-opção-publicar)>___   

Execute a aplicação:
>java -jar rabbitmq-processor.jar
