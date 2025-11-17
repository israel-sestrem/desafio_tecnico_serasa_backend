## Grain Weighing System – Backend

Sistema para inserção, estabilização e registro de pesagens enviadas por balanças ESP32.  

Construído com Spring Boot 3, PostgreSQL, Flyway, Docker e Swagger.


## Objetivo

- Receber leituras de peso (100ms cada) das balanças
- Detectar automaticamente o momento de estabilização
- Registrar pesagens (bruto, tara, líquido, custo)
- Controlar transações de transporte (início/fim)
- Fornecer relatórios de custo e lucro possível


## Arquitetura

Tecnologias:
- Java 17, Spring Web, Spring Data JPA, Flyway  
- PostgreSQL  
- Lombok  
- Docker + Docker Compose  
- Springdoc OpenAPI (Swagger)


## Modelagem (Resumo das Entidades)

Branch
`id`, `code`, `name`, `city`, `state`

Truck
`id`, `licensePlate`, `tareWeightKg`, `model`, `active`

GrainType
`id`, `name`, `purchasePricePerTon`, `minMargin`, `maxMargin`

Scale
`id`, `externalId`, `description`, `branch`, `apiToken`, `active`

TransportTransaction
`id`, `truck`, `branch`, `grainType`, `startTimestamp`, `endTimestamp`,  
`purchasePricePerTon`, `salePricePerTon`,  
`totalNetWeightKg`, `totalLoadCost`, `totalEstimatedRevenue`, `estimatedProfit`

Weighing
`id`, `licensePlate`, `grossWeightKg`, `tareWeightKg`, `netWeightKg`,  
`weighingTimestamp`, `scale`, `truck`, `grainType`,  
`transportTransaction`, `loadCost`, `weighingType`

IDs são UUID. Migrações via Flyway (`V1__create_tables.sql`).


## Estabilização do Peso

As leituras chegam a cada 100ms.

Estratégia padrão (implementada)

Baseada em N leituras consecutivas similares:

yaml

grain-weighing.stabilization:
  required-stable-readings: 5
  max-diff-between-readings-kg: 50

Regra:

Se a diferença ≤ limite → contador++

Se a diferença > limite → contador = 0

Se contador = required-stable-readings → peso estabilizado → salva no banco


Estratégia alternativa (opcional)

Peso estável por janela de tempo (ex.: 3 segundos).

Código já preparado para essa possível troca.

## Autenticação das Balanças

Toda balança possui apiToken.


O ESP32 deve enviar:

X-Scale-Token: <token>


Fluxo:

Busca balança pelo externalId

Valida apiToken

Bloqueia requisições não autorizadas

## Endpoint de Inserção (ESP32)

POST /api/weighings/insert

Headers:

X-Scale-Token: <token>

Body:

{
  "id": "scale-001",
  "plate": "ABC1D23",
  "weight": 31800.75
}

Respostas:

201 Created → pesagem estabilizada

202 Accepted → ainda estabilizando

Erros padronizados via @RestControllerAdvice

## Relatórios

Listagem

GET /api/reports/weighings?start=...&end=...&branchId=...&truckId=...&grainTypeId=...


Resumo

GET /api/reports/summary?...

Retorno:

{
  "totalNetWeightKg": 38400,
  "totalLoadCost": 18250.75,
  "estimatedRevenue": 22398,
  "estimatedProfit": 4150.25
}

## Transações de Transporte

Abrir

POST /api/transport-transactions


Fechar

POST /api/transport-transactions/{id}/close

Ao fechar:

Soma pesos líquidos das pesagens

Soma custo total

Calcula receita e lucro

Salva consolidação na transação

## Execução Local

Criar banco:

CREATE DATABASE grain_weighing;

Configurar application.properties ou application.yml:

spring.datasource.url=jdbc:postgresql://localhost:15432/grain_weighing

spring.datasource.username=user

spring.datasource.password=pass

spring.jpa.hibernate.ddl-auto=validate


Rodar:

mvn spring-boot:run

## Executar via Docker

Build:

mvn clean package -DskipTests


Subir:

docker compose up --build

API → http://localhost:8080

Banco → localhost:15432

## Swagger

http://localhost:8080/swagger-ui.html

http://localhost:8080/v3/api-docs

## Evoluções Futuras
Idempotência mais pesada utilizando chave baseada em external_id + timestamp, por exemplo.

Kafka/SQS para inserção assíncrona

Métricas com grafana

Dashboard realtime via WebSockets
