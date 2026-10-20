# Auditoría Técnica — CredibancoBackend
**Proyecto:** Spring Boot 3.2.5 · Java 21 · PostgreSQL  
**Fecha:** Junio 2026  
**Revisado por:** Claude (Anthropic)

---

## Índice
1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Lo Bueno — Fortalezas](#2-lo-bueno--fortalezas)
3. [Lo Malo — Bugs y Problemas Críticos](#3-lo-malo--bugs-y-problemas-críticos)
4. [Lo Mejorable — Deuda Técnica](#4-lo-mejorable--deuda-técnica)
5. [Qué Implementaría — Roadmap](#5-qué-implementaría--roadmap)
6. [Tabla de Prioridades](#6-tabla-de-prioridades)

---

## 1. Resumen Ejecutivo

El proyecto es una pasarela bancaria simulada (Credibanco) con operaciones de tarjetas de crédito/débito, compras, recargas y anulaciones. La estructura base es sólida y sigue buenas prácticas de Spring Boot, pero tiene **bugs críticos de consistencia**, ausencia de seguridad, y varios puntos de deuda técnica que deben resolverse antes de considerar el proyecto como "producción-ready".

---

## 2. Lo Bueno — Fortalezas

### Arquitectura y Estructura
- **Separación de capas limpia:** `controller → service → repository` bien delimitada. Cada capa tiene una responsabilidad clara.
- **Inyección por constructor** (no `@Autowired` en campo): mejor práctica, facilita los tests y hace las dependencias explícitas.
- **`@Transactional` en operaciones críticas:** `registrarCompra`, `recargar`, `anularTransaccion` están correctamente envueltas en transacciones. Si algo falla a mitad de operación, el rollback es automático.
- **`BigDecimal` para montos monetarios:** correcto. Evita los errores de punto flotante de `double`/`float` que son inaceptables en sistemas financieros.

### Reglas de Negocio Implementadas
- Validación de saldo insuficiente para tarjetas débito.
- Regla de anulación: solo compras exitosas y dentro de las últimas 24 horas.
- Validación de número de tarjeta de 16 dígitos.
- Validación de fecha de vencimiento mínima (3 años desde creación).

### Testing
- Tests unitarios de controller con MockMvc y Mockito.
- Tests de integración con `@SpringBootTest`.
- Tests de repositorio con `@DataJpaTest` + H2 en memoria.
- El test de integración `testCompraYAnulacion` prueba un flujo end-to-end realista.

### Documentación y Observabilidad
- **Swagger/OpenAPI** integrado con `springdoc-openapi`. Cada endpoint tiene `@Operation` y `@ApiResponse`.
- Anotaciones `@Tag` en el controlador de tarjetas.
- `ApiExceptionHandler` con `@ControllerAdvice` para centralizar el manejo de errores.

### Stack Tecnológico Moderno
- Spring Boot 3.2.5 + Java 21 (LTS): versiones actuales y con soporte.
- Lombok para reducir boilerplate.
- H2 en scope `test` para tests sin dependencia de PostgreSQL real.
- Índices en base de datos para `numero_tarjeta`, `tarjeta_id` y `estado`.

---

## 3. Lo Malo — Bugs y Problemas Críticos

### BUG #1 — Inconsistencia de estados entre SQL y código
**Severidad: ALTA**

El SQL define los estados válidos como `'pendiente'`, `'realizada'`, `'anulada'`, pero el código Java usa `'exitosa'` al crear transacciones.

```java
// TransaccionServiceImpl.java — línea 35 y 52
tx.setEstado("exitosa");  // ❌ El SQL define 'realizada', no 'exitosa'
```
```sql
-- Credibanco.sql — línea 19
estado varchar(10) not null  -- 'pendiente', 'realizada', 'anulada'
```

**Impacto:** El estado de la BD nunca coincide con lo documentado. Si alguien consulta por `estado = 'realizada'` no encontrará nada. El método `findByEstado("realizada")` del repositorio devolverá siempre vacío.

**Solución:**
```java
// Crear un Enum
public enum EstadoTransaccion {
    PENDIENTE, REALIZADA, ANULADA
}
// Usar en el modelo con @Enumerated(EnumType.STRING)
```

---

### BUG #2 — `IllegalArgumentException` nunca es capturada como error HTTP 400
**Severidad: ALTA**

El `ApiExceptionHandler` solo maneja `BusinessException`, pero toda la lógica de negocio lanza `IllegalArgumentException`.

```java
// TransaccionServiceImpl.java
throw new IllegalArgumentException("Saldo insuficiente");  // ❌

// ApiExceptionHandler.java
@ExceptionHandler(BusinessException.class)  // Solo captura BusinessException
public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) { ... }
```

**Impacto:** Cuando falla la validación de negocio (saldo insuficiente, tarjeta no encontrada, etc.), Spring retorna un **HTTP 500** en lugar de **400 Bad Request**. El cliente recibe un error interno del servidor cuando en realidad el error es del cliente.

**Solución:**
```java
// Opción A: cambiar las excepciones en el service
throw new BusinessException("Saldo insuficiente");

// Opción B: agregar handler para IllegalArgumentException
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage(), "Validacion"));
}
```

---

### BUG #3 — Los controllers retornan entidades directamente, no DTOs
**Severidad: MEDIA-ALTA**

`TarjetaController` y `TransaccionController` retornan `Tarjeta` y `Transaccion` (entidades JPA) directamente. `TarjetaResponse` y `TransaccionResponse` existen pero **nunca se usan**.

```java
// TarjetaController.java
public Tarjeta crearTarjeta(@RequestBody TarjetaRequest request)  // ❌ expone entidad

// TarjetaResponse.java existe pero no se usa en ningún controller
```

**Impacto:**
- Se expone la relación `@OneToMany transacciones` de `Tarjeta`, que puede causar lazy-loading inesperado o serialización infinita.
- En producción bancaria esto podría exponer el PAN (número de tarjeta completo) o datos internos de la entidad.
- Cualquier cambio en la entidad JPA rompe la API pública.

**Solución:**
```java
// En el controller, mapear a DTO
public TarjetaResponse crearTarjeta(@RequestBody TarjetaRequest request) {
    Tarjeta tarjeta = tarjetaService.crearTarjeta(request);
    return mapToResponse(tarjeta);  // o usar MapStruct
}
```

---

### BUG #4 — Duplicación de la lógica de recarga
**Severidad: MEDIA**

La recarga está implementada en **dos lugares distintos** con lógica diferente:

```java
// TarjetaServiceImpl.recargarSaldo() — actualiza saldo + crea transacción
// TransaccionService.recargar()      — crea transacción + actualiza saldo (orden diferente)
```

**Impacto:** Dos fuentes de verdad. Si se corrige un bug en una, la otra queda desactualizada. Viola el principio DRY.

---

### BUG #5 — CORS abierto (`allowedOrigins("*")`)
**Severidad: MEDIA en desarrollo, CRÍTICA en producción**

```java
// CorsConfig.java
.allowedOrigins("*")  // ❌ Cualquier origen puede hacer requests
```

En una pasarela bancaria esto es inaceptable. Cualquier sitio web malicioso puede hacer requests autenticados a la API desde el browser de un usuario.

**Solución:**
```java
.allowedOrigins("https://mi-frontend.credibanco.com")
// O configurar por perfil en application.properties
```

---

### BUG #6 — Validación de fecha de vencimiento incompleta
**Severidad: MEDIA**

```java
// TarjetaServiceImpl.java — línea 42
if (tarjeta.getFechaVencimiento().getYear() < LocalDate.now().getYear() + 3)
```

Solo compara el **año**, ignora el mes. Una tarjeta creada en diciembre 2025 con vencimiento enero 2028 pasaría la validación (2028 >= 2028) aunque no hayan pasado 3 años completos.

**Solución:**
```java
if (tarjeta.getFechaVencimiento().isBefore(LocalDate.now().plusYears(3))) {
    throw new BusinessException("...");
}
```

---

## 4. Lo Mejorable — Deuda Técnica

### 4.1 Sin autenticación ni autorización
El endpoint `GET /api/tarjetas` lista **todas las tarjetas** del sistema sin ningún control. Cualquiera con acceso a la URL puede ver el saldo y datos de todos los usuarios.

**Qué agregar:** Spring Security + JWT.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

### 4.2 Sin validación de inputs con Bean Validation
Los DTOs no tienen ninguna anotación de validación:

```java
// TarjetaRequest.java — sin validaciones
public class TarjetaRequest {
    private String numeroTarjeta;  // ❌ sin @NotNull, @Size
    private String nombreTitular;  // ❌ sin @NotBlank
    private LocalDate fechaVencimiento; // ❌ sin @Future
    private String tipoTarjeta;    // ❌ acepta cualquier string
}
```

**Qué agregar:**
```java
public class TarjetaRequest {
    @NotBlank @Size(min = 16, max = 16)
    private String numeroTarjeta;

    @NotBlank @Size(max = 100)
    private String nombreTitular;

    @Future
    private LocalDate fechaVencimiento;

    @Pattern(regexp = "credito|debito")
    private String tipoTarjeta;
}
// Y en el controller: @Valid @RequestBody TarjetaRequest request
```

---

### 4.3 Strings mágicos en lugar de Enums
El código usa strings literales como `"compra"`, `"recarga"`, `"exitosa"`, `"debito"`, `"credito"` en múltiples lugares.

```java
if ("debito".equalsIgnoreCase(tarjeta.getTipoTarjeta()))  // ❌ string mágico
tx.setEstado("exitosa");                                    // ❌ string mágico
```

Un typo en cualquiera de estos es un bug silencioso. **Solución: enums.**

```java
public enum TipoTarjeta { CREDITO, DEBITO }
public enum TipoMovimiento { COMPRA, RECARGA }
public enum EstadoTransaccion { PENDIENTE, REALIZADA, ANULADA }
```

---

### 4.4 Sin paginación en los listados
```java
public List<Tarjeta> listarTarjetas()  // ❌ devuelve TODO
public List<Transaccion> listarTransaccionesPorTarjeta(Long tarjetaId)  // ❌ sin límite
```

Con 10,000 tarjetas o 1,000,000 de transacciones esto colapsa la memoria.

**Solución:**
```java
Page<Transaccion> listarTransacciones(Long tarjetaId, Pageable pageable);

// Controller
@GetMapping
public Page<TransaccionResponse> listar(
    @RequestParam Long tarjetaId,
    @PageableDefault(size = 20) Pageable pageable) { ... }
```

---

### 4.5 Sin logging
No hay ninguna llamada a `Logger` en todo el proyecto. No hay trazabilidad de operaciones.

```java
// Agregar en cada service:
private static final Logger log = LoggerFactory.getLogger(TransaccionServiceImpl.class);

log.info("Compra registrada: tarjeta={}, monto={}, txId={}", tarjetaId, monto, tx.getId());
log.warn("Intento de anulación fuera de plazo: txId={}", transaccionId);
log.error("Error inesperado al procesar compra", ex);
```

---

### 4.6 El número de tarjeta lo define el cliente
En una pasarela real, el número de tarjeta se **genera internamente** con el algoritmo de Luhn y el BIN del producto. El cliente no debe poder elegir su propio número.

```java
// TarjetaServiceImpl.crearTarjeta() — debería generar el número:
private String generarNumeroTarjeta(String productId) {
    // 6 dígitos BIN + 9 aleatorios + 1 dígito Luhn
    // Verificar con algoritmo de Luhn
}
```

---

### 4.7 Sin migraciones de base de datos (Flyway/Liquibase)
El `Credibanco.sql` se ejecuta manualmente. En un equipo o en CI/CD esto es inmanejable.

```xml
<!-- Agregar al pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```
```
src/main/resources/db/migration/
  V1__create_tables.sql
  V2__insert_test_data.sql
```

---

### 4.8 Sin perfiles de Spring (dev/prod/test)
No hay `application-dev.properties`, `application-prod.properties`. Toda la configuración va en un solo archivo. En producción no deberías tener H2, ni Swagger expuesto, ni CORS abierto.

---

### 4.9 Tests de integración requieren BD real
`TransaccionServiceIntegrationTest` usa `@SpringBootTest` que levanta el contexto completo e intenta conectarse a PostgreSQL. No puede correr en CI sin una BD.

**Solución:** usar `@SpringBootTest` con H2 para tests de integración, PostgreSQL solo para tests end-to-end en un entorno dedicado.

---

## 5. Qué Implementaría — Roadmap

### Fase 1: Correcciones urgentes (1-2 días)
1. **Unificar estados con Enum** (`PENDIENTE`, `REALIZADA`, `ANULADA`)
2. **Cambiar `IllegalArgumentException` → `BusinessException`** en todos los services
3. **Mapear a DTOs** en los controllers (usar `TarjetaResponse` y `TransaccionResponse`)
4. **Agregar `@Valid`** en los request bodies con Bean Validation

### Fase 2: Seguridad básica (3-5 días)
```
Spring Security + JWT
├── POST /auth/login → retorna JWT
├── Todos los endpoints requieren Bearer token
└── Un usuario solo puede ver sus propias tarjetas
```

### Fase 3: Calidad del dominio (1 semana)
- Enums para todos los strings mágicos
- Generación automática de número de tarjeta con Luhn
- Flyway para migraciones
- Paginación en listados
- Logging estructurado (SLF4J + Logback)
- Perfiles `dev` / `prod` / `test`

### Fase 4: Features bancarios avanzados (2-3 semanas)
- **Idempotency key:** evitar doble cobro si el cliente reintenta un request
- **Flujo de autorización en 2 pasos:** `autorizar` → `capturar` (como Visa/Mastercard real)
- **Webhooks:** notificar al comercio cuando cambia el estado de una transacción
- **Auditoría:** `@CreatedBy`, `@LastModifiedBy` con Spring Data Envers
- **Rate limiting:** máximo N transacciones por minuto por tarjeta
- **Cifrado del PAN:** el número de tarjeta no debe guardarse en claro (tokenización)

### Fase 5: DevOps y observabilidad
- **Docker + docker-compose** (app + PostgreSQL)
- **Spring Actuator** para health checks y métricas
- **Micrometer + Prometheus** para métricas
- **OpenTelemetry** para distributed tracing
- **Pipeline CI/CD** con tests automáticos en cada PR

---

## 6. Tabla de Prioridades

| # | Problema / Mejora | Tipo | Prioridad | Esfuerzo |
|---|---|---|---|---|
| 1 | Bug: estados inconsistentes SQL vs código | Bug | 🔴 Crítica | Bajo |
| 2 | Bug: `IllegalArgumentException` retorna HTTP 500 | Bug | 🔴 Crítica | Bajo |
| 3 | Bug: controllers exponen entidades JPA | Bug | 🔴 Crítica | Medio |
| 4 | Sin autenticación (cualquiera ve todas las tarjetas) | Seguridad | 🔴 Crítica | Alto |
| 5 | CORS abierto `allowedOrigins("*")` | Seguridad | 🔴 Crítica | Bajo |
| 6 | Sin `@Valid` en DTOs de entrada | Validación | 🟠 Alta | Bajo |
| 7 | Strings mágicos → Enums | Calidad | 🟠 Alta | Bajo |
| 8 | Duplicación de lógica de recarga | Diseño | 🟠 Alta | Bajo |
| 9 | Validación de fecha de vencimiento incompleta | Bug | 🟠 Alta | Bajo |
| 10 | Sin paginación en listados | Performance | 🟡 Media | Medio |
| 11 | Sin logging | Observabilidad | 🟡 Media | Bajo |
| 12 | Sin Flyway/Liquibase | DevOps | 🟡 Media | Medio |
| 13 | Sin perfiles dev/prod/test | Configuración | 🟡 Media | Bajo |
| 14 | Número de tarjeta generado por cliente | Dominio | 🟡 Media | Medio |
| 15 | Tests de integración sin BD embebida | Testing | 🟡 Media | Medio |
| 16 | Idempotency key | Bancario | 🟢 Baja | Alto |
| 17 | Flujo autorización → captura | Bancario | 🟢 Baja | Alto |
| 18 | Tokenización del PAN | Seguridad PCI | 🟢 Baja | Alto |
| 19 | Docker + CI/CD | DevOps | 🟢 Baja | Alto |
| 20 | Métricas y tracing | Observabilidad | 🟢 Baja | Alto |

---

*Documento generado automáticamente mediante análisis estático del código fuente.*
