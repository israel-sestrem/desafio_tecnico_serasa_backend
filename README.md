Título: Grain Weighing Service – Desafio Técnico Serasa Experian

Descrição geral:
Este projeto implementa uma API para receber, estabilizar e armazenar pesagens de caminhões em balanças industriais, calcular custo da carga, estimar lucro e fornecer relatórios operacionais. A solução atende ao cenário proposto no desafio técnico da vaga de Analista de Desenvolvimento de Software Sênior da Serasa Experian.

Tecnologias utilizadas:

Java 17

Spring Boot 3

Spring Web

Spring Data JPA

Spring Validation

PostgreSQL

Lombok

Docker e Docker Compose (opcional)

Springdoc OpenAPI (Swagger)

JUnit 5 e Spring Boot Test

Arquitetura:
O projeto segue arquitetura em camadas:

controller: rotas e comunicação HTTP

service: regras de negócio e orquestração

repository: camada de persistência via JPA

domain: entidades e modelos persistidos

dto: objetos de entrada e saída

stabilization: componente responsável pela estabilização das leituras

Modelagem das entidades:

Filial (Branch):
id, name, code

Tipo de Grão (GrainType):
id, name, purchasePrice (valor por tonelada), currentStockTons (estoque atual)

Caminhão (Truck):
id, plate, taraKg (peso do caminhão vazio), active

Balança (Scale):
id, scaleCode (id recebido do ESP32), branchId, authToken (opcional)

Transação de Transporte (TransportTransaction):
id, truckId, grainTypeId, branchId, startTime, endTime, status (OPEN ou CLOSED)

Pesagem Estabilizada (Weighing):
id, transportId, truckId, grainTypeId, branchId, scaleId, plate, grossWeightKg, tareKg, netWeightKg, weighingTime, costValue, saleValue, profitValue

Recepção das leituras (ESP32):
O ESP32 envia leituras aproximadamente a cada 100ms no seguinte formato:
{
"id": "scale-01",
"plate": "ABC1234",
"weight": 32450
}
O endpoint implementado é:
POST /api/weighings/stream

Fluxo do endpoint:

Valida se a balança existe.

Valida autorização da balança (opcional).

Busca o caminhão pela placa.

Localiza a transação de transporte aberta.

Envia a leitura para o estabilizador.

Quando estabilizar, calcula peso líquido, custo, margem, lucro e grava no banco.

Estratégia de estabilização:
Foi adotada uma janela deslizante de 30 leituras por combinação balança + placa, equivalentes a aproximadamente 3 segundos de leitura.

A cada leitura:

O valor é inserido no buffer.

Se houver menos de 30 leituras, a leitura ainda é considerada instável.

Quando houver 30 leituras, são calculados mínimo, máximo e média.

A pesagem é considerada estabilizada se:
diferença entre máximo e mínimo for menor ou igual ao threshold.
O threshold é calculado como o maior valor entre 20 kg e 0.5% da média das leituras.
O valor final armazenado no banco é a média da janela.
Após estabilizar, o buffer é limpo e somente uma nova pesagem é permitida quando o peso voltar abaixo de 1000 kg, indicando saída do caminhão da balança.

Cálculos:

Peso líquido:
peso líquido = peso bruto estabilizado – tara

Custo:
custo = (peso líquido / 1000) multiplicado pelo preço de compra por tonelada

Margem:
A margem varia entre 5% e 20% dependendo do estoque do tipo de grão:
estoque baixo → margem 20%
estoque médio → margem entre 12% e 15%
estoque alto → margem mínima de 5%

Preço de venda:
preço de venda = custo multiplicado por (1 + margem)

Lucro:
lucro = preço de venda – custo

Relatórios (endpoints):
Todos com filtro via query params.

Pesagens:
GET /api/reports/weighings
Parâmetros opcionais: branchId, truckId, grainTypeId, startDate, endDate

Custos:
GET /api/reports/costs

Lucros possíveis:
GET /api/reports/profits

Execução sem Docker:

Criar banco grain_db no PostgreSQL

Ajustar application.yml

Executar:
mvn clean package
java -jar target/grain-weighing-service.jar
A API ficará disponível em http://localhost:8080

Execução com Docker (opcional):
Gerar o jar:
mvn clean package

Dockerfile:
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

docker-compose.yml:
Define serviços app e db com PostgreSQL e aplicação Spring Boot.

Swagger:
Disponível em:
http://localhost:8080/swagger-ui.html

Testes implementados:

Testes unitários para a lógica de estabilização

Testes de integração para principais endpoints

Testes de regra de negócio para cálculos de peso, custo e lucro

Estrutura de pastas:
controller, service, repository, domain, dto, stabilization, main application class.

Decisões técnicas:

Janela deslizante de 30 leituras

Threshold híbrido (valor fixo + percentual)

BigDecimal para precisão

Banco relacional para agregações dos relatórios

Melhorias futuras:

Cache distribuído

Kafka para streaming real

Dashboard realtime

Alertas automáticos

Autenticação JWT

Tokens individuais para balanças

Autor:
Israel Ricardo Sestrem
