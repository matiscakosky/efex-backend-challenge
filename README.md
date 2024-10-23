# efex-backend-challenge

Esta es una API de gestión de estudiantes, desarrollada con **Micronaut** y conectada a **DynamoDB** mediante **LocalStack**. Permite administrar información básica de estudiantes como nombre, grado y datos de contacto.

## Levantar la API

### Opción 1: Docker Compose

1. Ejecutar el siguiente comando para levantar la API y los servicios:

```bash
docker-compose up
```

### Opción 2: Solo servicios externos

Se brinda otra opcion para ejecutar la API de forma local, en caso de querer tener una forma fácil para debuggear, se puede levantar los servicios externos con

```bash
docker-compose -f docker-compose-services.yml up
```

Luego, inicia la API de forma local usando Gradle:

```bash
./gradlew clean build run
```


**Variables de entorno necesarias para esta opción:**

```bash
export aws.region=us-east-1
export aws.accessKeyId=aws-local-key
export aws.secretKey=aws-local-secret-key
export aws.dynamo.endpoint=http://localstack:4566
```

### En ambos casos la API estará disponible en

- **Micronaut-API**: http://localhost:8080
- **OpenAPI UI-Redoc**: http://localhost:8081
- **DynamoDB Admin UI**: http://localhost:8001

### Pruebas Locales

Añadí una colección de Postman para facilitar las pruebas. La misma está configurada para apuntar al host localhost y a los puertos mencionados arriba.
Se puede descargar [aquí](https://github.com/matiscakosky/efex-backend-challenge/blob/a5385b55fa2f8bc06919b34fec626c2c8c50dcb6/postman_collection.json)

### Pruebas Locales

Para correr las pruebas unitarias en local

```bash
./gradlew clean build test
```

### Pruebas Locales

Para correr las pruebas unitarias en local

```bash
./gradlew clean build test
```

### Pruebas de integración
Las pruebas de integración se ejecutan mediante la tarea integrationTest. Usa el siguiente comando:

```bash
./gradlew clean build integrationTest
```

### Task de test dockerizadas 
En caso de querer correr las pruebas dentro de un container de docker, se agregó un script para eso también en el que puede ejecutarse de la siguiente manera

```bash
chmod +x scripts/run_tests.sh
```

```bash
./scripts/run_tests.sh
```


## Comentarios y decisiones empleadas
Este apartado contiene comentarios especificos del challenge y decisiones que fui tomando para poder discutirlas mas adelante


### 1. Decisión de utilizar DynamoDB como base de datos
La elección de DynamoDB como base de datos surgió a partir de menciones explícitas durante el proceso de entrevista, lo que trajo el interés en utilizar esta tecnología. 
El challenge también representó una oportunidad para explorar un enfoque serverless y NoSQL, 
mas allá de las estrategias tradicionales de bases de datos relacionales que suelen aplicarse en este tipo de sistemas (y que para estos casos son acertadas). 

#### Ventajas de utilizar DynamoDB

**Escalabilidad automática**: Dynamo gestiona la capacidad de forma dinámica, garantizando que pueda manejar tanto pequeñas como grandes cantidades de tráfico sin intervención manual. Aunque no es necesario para est tipo de problemas, resulta interesante mencionarlo

#### Desentajas encontradas

El problema mas grande fue que DynamoDB no soporta identificadores auto-incrementales nativamente, lo que generó la necesidad de desarrollar una solución personalizada para generar IDs únicos. Para lograrlo, se añadió una tabla adicional en DynamoDB encargada de mantener un contador global de IDs, imitando el comportamiento de auto-incremento presente en sistemas SQL.
Cabe aclarar que esto solo se agrega porque la especificación de IDs auto incrementales era requerimiento del challenge, en otro caso, se puede generar con KSUID o algún otro generador

#### Adaptacion

El diseño del proyecto sigue los principios de desacoplamiento mediante la programación orientada a interfaces. Esto significa que la implementación de acceso a datos se realiza a través de repositorios, permitiendo sustituir DynamoDB por cualquier base de datos relacional sin modificar la lógica del dominio. En caso de querer migrar a una base de datos SQL (como PostgreSQL o MySQL), se debería:

- Añadir la implementación específica del repositorio en la capa de infraestructura.
- Configurar el proyecto para seleccionar, mediante inyección de dependencias, el Bean apropiado según el tipo de base de datos requerido (SQL o DynamoDB).

- Este enfoque garantiza flexibilidad tecnológica y facilita la migración entre diferentes tipos de bases de datos en función de las necesidades futuras del proyecto o del entorno en el que se despliegue


#### Mappings manuales entre Dominio y DynamoDB
Para asegurar que los datos se almacenen de acuerdo con los requisitos del dominio, se implementaron mapeos manuales que transforman la estructura de los datos. Esto se logra mediante la clase StudentEntity, que representa la forma en que los datos son almacenados en DynamoDB, y el EntityMapper, que realiza las conversiones necesarias entre la entidad del dominio y la entidad de persistencia. Este enfoque proporciona un mayor control sobre la persistencia y permite una transformación de datos más flexible, garantizando así que la aplicación mantenga la integridad de los datos y cumpla con los requisitos del negocio.

### Configuración de Jackson

Para simplificar el proceso de serialización y deserialización en la API, se implementó una configuración de Jackson utilizando un @Factory llamado ObjectMapperFactory. Esta configuración permite que los objetos del dominio se manejen automáticamente, sin necesidad de añadir anotaciones en cada clase. Además, se asegura la conversión entre `snake_case` y `camelCase`

### Task de Linter en el build
Se ha integrado Kotlinter en la etapa de build del proyecto. Esta tarea se ejecuta automáticamente durante el proceso de construcción y verifica el estilo del código Kotlin. Si se encuentran problemas, el build fallará, lo que ayuda a mantener la calidad y la legibilidad del código en todo momento.

### Servicios adicionales: OpenAPI y DynamoDB Viewer
Se habilitan dos servicios adicionales para facilitar la interacción con la API: OpenAPI se encuentra disponible en `localhost:8081` para tener un detalle de los endpoints y la API en general. Además, el DynamoDB Viewer está disponible en `localhost:8001`, proporcionando una interfaz gráfica para gestionar y visualizar los datos almacenados en la base de datos DynamoDB.

### Validaciones de las request
Las validaciones se implementan a través de los comandos utilizando las anotaciones @Valid y @NonNull, lo que permite mantener los controladores limpios y enfocados en la lógica de negocio. Esto asegura que las reglas de validación se apliquen de manera consistente y eficiente, sin mezclar la lógica de validación con la de control

### Excepciones del framework
Se implementó un GlobalException handler para capturar excepciones y enmascarar mensajes de error, asegurando respuestas más limpias y con mensajes claros.

## Mejoras futuras

A continuación se detallan algunas mejoras que quedaron fuera de scope por decisión, entre otras cosas debido a limitaciones de tiempo, pero que están mapeadas para su futura implementación

### Autenticación y Autorización
Se podría implementar un sistema para asegurar los endpoints y controlar el acceso de usuarios.

### Pipeline de CI/CD
Sería útil establecer un pipeline de integración y despliegue continuo para automatizar pruebas y despliegues.

### Métricas
Incluir métricas permitiría monitorear el rendimiento y el uso de la aplicación.

### Soporte Multi-Entorno
Expandir la infraestructura a entornos de desarrollo, staging y producción facilitaría el despliegue y las pruebas.
