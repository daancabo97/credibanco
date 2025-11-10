# Credibanco Backend API REST

Plataforma backend para la gestión de tarjetas y transacciones, desarrollada con Spring Boot 3, Java 21 y persistencia en PostgreSQL.

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
*   **Plugins de build**:
    *   `maven-compiler-plugin`
    *   `spring-boot-maven-plugin`
    *   `maven-resources-plugin`

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

## 📃 Documentación interactiva

Disponible en:
http://localhost:8083/swagger-ui/index.html#/

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

*   **`POST /api/transacciones/recarga`**

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