# Credibanco Backend API REST

Plataforma backend para la gestión de tarjetas y transacciones, desarrollada con Spring Boot 3, Java 21 y persistencia en PostgreSQL.

> **Guía visual (PDF, lenguaje no técnico):** [docs/guia-visual-credibanco.pdf](docs/guia-visual-credibanco.pdf) — también disponible en HTML: [docs/guia-visual-credibanco.html](docs/guia-visual-credibanco.html)

## 📦 Tecnologías y Dependencias

*   **Java 21**
*   **Spring Boot 3.2.5**
*   **Spring Web**: APIs REST, peticiones HTTP y servidor Tomcat embebido.
*   **Spring Data JPA**: ORM con Hibernate para interactuar con SQL usando objetos Java.
*   **Spring Boot Test**: Testing con JUnit, Mockito y AssertJ.
*   **PostgreSQL Driver**: Conexión eficiente a PostgreSQL vía JDBC.
*   **Swagger/OpenAPI**: Documentación automática de los endpoints.
*   **Lombok**: Anotacion para generan getters/setters y eliminar el código repetitivo.
*   **H2 Database**: Base de datos en memoria para correr pruebas unitarias automáticas.
*   **Mockito**: Mocking y simulaciones en los tests.
*   **JaCoCo**: Cobertura de pruebas en XML para SonarQube.
*   **Plugins de build**:
    *   `maven-compiler-plugin`
    *   `spring-boot-maven-plugin`
    *   `maven-resources-plugin`
    *   `jacoco-maven-plugin`

## ⚡ Instalación y Ejecución

**Prerrequisitos:**

*   Tener Java 21 y Maven instalados.
*   PostgreSQL configurado y disponible.

**Setup rápido PowerShell:**

```powershell
$env:JAVA_HOME_21="C:\Users\ASUS VivoBook\.jdks\ms-21.0.9"
$env:JAVA_HOME=$env:JAVA_HOME_21
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

**Compilación y ejecución:**

```bash
./mvnw clean install
./mvnw spring-boot:run
./mvnw clean test
```

## Cobertura de código hacia SonarQube

SonarQube no mide la cobertura por sí solo. JaCoCo instrumenta los tests, escribe `target/site/jacoco/jacoco.xml` y el scanner de Sonar importa ese XML.

Las propiedades viven en `pom.xml` y el scanner las lee al publicar el análisis. La URL del servidor y el token no van en el repositorio: se pasan por variable de entorno o por `-D` en el comando.

```xml
<properties>
    <java.version>21</java.version>
    <jacoco.version>0.8.12</jacoco.version>
    <sonar.projectKey>credibanco-backend</sonar.projectKey>
    <sonar.projectName>credibanco-backend</sonar.projectName>
    <sonar.java.source>${java.version}</sonar.java.source>
    <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

`jacoco-maven-plugin` tiene dos ejecuciones: `prepare-agent` (engancha el agente a Surefire) y `report` en la fase `verify` (genera el XML).

En PowerShell, desde la raíz del proyecto:

```powershell
$env:JAVA_HOME = "C:\Users\ASUS VivoBook\.jdks\ms-21.0.9"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SONAR_HOST_URL = "http://localhost:9000"
$env:SONAR_TOKEN = "<token-de-sonar>"

.\mvnw.cmd clean verify
.\mvnw.cmd org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar
```

`clean verify` corre los tests y deja el reporte en `target/site/jacoco/jacoco.xml`. El HTML local, para revisarlo sin abrir Sonar, está en `target/site/jacoco/index.html`.

El segundo comando publica el proyecto `credibanco-backend` en el servidor. En SonarQube el porcentaje queda en **Overall Code** / **Coverage** del proyecto. Si el servidor es SonarCloud, cambia el host y añade la organización:

```powershell
$env:SONAR_HOST_URL = "https://sonarcloud.io"
$env:SONAR_TOKEN = "<token-de-sonarcloud>"
.\mvnw.cmd org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar "-Dsonar.organization=<org>"
```

El token se crea en SonarQube en **My Account → Security → Generate Token** (tipo *Global Analysis Token* o *Project Analysis Token*).

## Auditoría de vulnerabilidades y security hotspots en Cursor

La extensión es **SonarQube for IDE** (antes SonarLint), publicador SonarSource. En Cursor: panel de extensiones, buscar `SonarQube for IDE` e instalarla. Al abrir esta carpeta analiza el código Java en caliente.

Dos modos:

*   **Independiente.** Marca bugs, vulnerabilidades y code smells con las reglas embebidas. Aparecen en el panel **Problems**, subrayados en el editor y en la vista **SonarQube**.
*   **Connected Mode.** Vincula el workspace al mismo `credibanco-backend` del servidor, así el IDE y el servidor usan el mismo quality profile, las mismas exclusiones y los hallazgos ya triageados.

Para Connected Mode: icono de SonarQube en la barra lateral → **Add SonarQube Connection** → URL (`http://localhost:9000` o `https://sonarcloud.io`) y un token de usuario (*User Token*). Después **Bind Project** y elige `credibanco-backend`.

Qué mirar en el IDE:

*   **Vulnerabilidades.** Filtro *Vulnerability* en Problems o en la vista SonarQube. Cada hallazgo abre la regla, el archivo y la línea.
*   **Security hotspots.** En Connected Mode, la vista SonarQube tiene la sección **Security Hotspots**. Ahí se revisa el código sensible (por ejemplo autenticación, criptografía o entrada de usuario) y se marca como *Safe* o *To Review*, igual que en el servidor. En modo independiente el IDE no trae el backlog de hotspots del servidor: ese listado sale del análisis publicado.

El análisis del IDE no sustituye `mvn sonar:sonar`. El IDE da feedback al escribir; el scanner publica cobertura, duplicación y el quality gate del pipeline.

## Swagger / OpenAPI

`springdoc-openapi-starter-webmvc-ui` 2.3.0 ya está en el `pom.xml`. `SecurityConfig` deja públicas `/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs/**`. El resto de `/api/**` exige JWT, salvo `POST /api/auth/login`.

El perfil activo por defecto es `dev` y usa PostgreSQL en `localhost:5432/credibanco`.

```powershell
$env:JAVA_HOME = "C:\Users\ASUS VivoBook\.jdks\ms-21.0.9"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

Cuando el log muestre que Tomcat escucha en el puerto **8083**:

| Qué | Ruta |
| --- | --- |
| Interfaz Swagger UI | http://localhost:8083/swagger-ui/index.html |
| Redirección clásica | http://localhost:8083/swagger-ui.html |
| Especificación OpenAPI (JSON) | http://localhost:8083/v3/api-docs |
| Especificación OpenAPI (YAML) | http://localhost:8083/v3/api-docs.yaml |

En Swagger UI, **Authorize** no aplica al login. Para probar un endpoint protegido: ejecuta `POST /api/auth/login` con el usuario de desarrollo (`admin` / `admin123`, definidos en `application-dev.properties`), copia el JWT de la respuesta y pégalo en **Authorize** como `Bearer <token>`.

Endpoints que expone la API:

*   `POST /api/auth/login`
*   `POST /api/tarjetas`
*   `PUT /api/tarjetas/{id}/recargar`
*   `GET /api/tarjetas`
*   `POST /api/transacciones/compra`
*   `POST /api/transacciones/recarga`
*   `PUT /api/transacciones/{id}/anular`
*   `GET /api/transacciones`

## 🏛️ Arquitectura en capas

*   **Controller**: Recibe solicitudes REST y expone API.
*   **Service**: Procesa la lógica del negocio.
*   **Repository**: Acceso y manejo de datos con JPA.
*   **Exception/DTO/Models**: Manejo de errores y estructura de datos.
*   **Test**: Pruebas unitarias e integración automatizadas.

## ⚙️ Configuración

*   Puerto por defecto **8083** (ajustable en `application.properties`).
*   Cadena JDBC editable para los entornos.

## 🚦 Flujo de la aplicación

1.  El cliente envía una solicitud HTTP (POST/GET/etc) a un endpoint.
2.  El **Controller** recibe la petición y la valida.
3.  El **Service** procesa la lógica requerida (por ejemplo, crear tarjeta, realizar transacción).
4.  El **Repository** interactúa con la base postulando/consultando datos SQL usando JPA.
5.  El **Controller** devuelve la respuesta HTTP estructurada (DTO o entidad).
6.  Si ocurre algún error, se maneja con clases `Exception` y se responde con códigos claros.
7.  Todos los endpoints están auto-documentados vía Swagger/OpenAPI.

## Endpoints y acciones para cumplir requisitos de cliente

### Tarjetas

*   **`POST /api/tarjetas`**

    El cliente puede crear una tarjeta nueva desde el panel de administración. Al crear la tarjeta, el sistema valida que:
    *   Tenga 16 dígitos, los primeros 6 corresponden al id del producto.
    *   Se registre nombre del titular (primer nombre y apellido).
    *   Fecha de vencimiento a 3 años posteriores a la creación.
    *   Se defina tipo de tarjeta (CRÉDITO o DÉBITO).
    *   El saldo inicial sea cero.

*   **`PUT /api/tarjetas/{id}/recargar`**

    El cliente puede recargar saldo en una tarjeta existente, agregando fondos únicamente en dólares.

*   **`GET /api/tarjetas`**

    El cliente puede consultar y listar todas las tarjetas creadas, visualizando las principales características y saldo actual.

### Transacciones

*   **`POST /transacciones/recarga`**

    Al recargar una tarjeta, se genera una transacción de tipo `recarga` para dejar registro de la operación.

*   **`POST /api/transacciones/compra`**

    Permite al cliente realizar una compra en el marketplace usando una tarjeta. El sistema valida los datos, verifica si el saldo es suficiente y registra la transacción como `EXITOSA` o `RECHAZADA`.

*   **`PUT /api/transacciones/{id}/anular`**

    El cliente puede anular una trasacción de compra (por ejemplo, devoluciones) si han pasado menos de 24 horas. El sistema actualiza el estado de la operación a `ANULADA` y reintegra el monto al saldo de la tarjeta.

*   **`GET /api/transacciones`**

    Permite consultar todas las transacciones (recargas, compras, anulaciones) asociadas a las tarjetas del cliente, mostrando detalles y estados.

## 🧪 Pruebas automatizadas
    
*   ** `ARCHIVO DE CONFIGURACION DE LA CONEXION BASE DE DATOS POSTGRESQL` **   

    CredibancoBackend\src\main\resources\application.properties