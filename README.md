# challenge-lemon

## Descripción

Este proyecto utiliza el framework **Micronaut** para construir una aplicación basada en Java. Está configurado para usar PostgreSQL como base de datos y Flyway para la gestión de migraciones. Además, incluye soporte para Kafka y validación con Jakarta.

## Requisitos previos

Antes de comenzar, asegúrate de tener instalados los siguientes requisitos:

- **Java 21**
- **Gradle** (opcional, ya que el proyecto incluye el wrapper de Gradle)
- **Docker** y **Docker Compose** (para levantar servicios como la base de datos)

## Configuración inicial

1. Clona este repositorio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd challenge-lemon
   ```

2. Configura las variables necesarias en el archivo `application.yml` ubicado en `src/main/resources/`.

3. Asegúrate de que el archivo `docker-compose.yml` esté configurado correctamente para tu entorno.

## Levantar el proyecto

### Usando Docker Compose para levantar la base de datos y otros servicios:

1. Construye los contenedores:
   ```bash
   docker-compose build
   ```

2. Levanta los servicios:
   ```bash
   docker-compose up
   ```

### Build y ejecución de la aplicación:

1. Compila el proyecto:
   ```bash
   ./gradlew build
   ```

2. Ejecuta la aplicación:
   ```bash
   ./gradlew run
   ```

## Migraciones de base de datos

Este proyecto utiliza Flyway para gestionar las migraciones de base de datos. Las migraciones se encuentran en `src/main/resources/db/migration/`.

## Pruebas

Para ejecutar las pruebas, utiliza el siguiente comando:
```bash
./gradlew test
```

## Estructura del proyecto

- **src/main/java**: Código fuente principal.
- **src/test/java**: Pruebas unitarias.
- **src/main/resources**: Archivos de configuración y recursos estáticos.
- **build.gradle**: Archivo de configuración de Gradle.
- **docker-compose.yml**: Configuración de Docker Compose.

### Arquitectura Hexagonal

Este proyecto sigue los principios de la **Arquitectura Hexagonal** (Puertos y Adaptadores), lo que permite mantener la lógica de negocio aislada de las tecnologías externas (Micronaut, PostgreSQL, Kafka).

### Estructura de Capas

La jerarquía de paquetes en `me.lemon.challenge.lopez` se divide de la siguiente manera:

#### 1. Dominio (`domain`)
Es el núcleo de la aplicación. Contiene los **Dominios** de negocio.
* **Independencia:** No tiene dependencias de frameworks ni de la base de datos.

#### 2. Aplicación (`application`)
Orquesta el flujo de datos hacia y desde el dominio.
* **Usecase:** Contiene la implementación de los casos de uso.
* **Inbound/Outbound:** Define los puertos de entrada (quién llama a la app) y salida (a quién llama la app).

#### 3. Infraestructura (`infrastructure`)
Contiene las implementaciones técnicas y el detalle de las herramientas externas.
* **Adapters:** Implementaciones concretas de los puertos de dominio (ej. JPA/Hibernate para repositorios).
* **Controllers:** Puntos de entrada REST (Micronaut).
* **Decorators:** Decoradores, en este caso utilizamos un decorador para mantener una transaccion atomica en el caso de uso `PaymentAuthorizationDecorator`.
* **Messaging (Kafka):** Listeners y Producers para la comunicación por mensajes.
* **Config:** Configuración específica del framework y beans.

## Dependencias principales

- **Micronaut**: Framework principal.
- **PostgreSQL**: Base de datos.
- **Flyway**: Gestión de migraciones.
- **Kafka**: Sistema de mensajería.
- **Lombok**: Generación de código boilerplate.
