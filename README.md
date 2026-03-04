# Versión Español: API REST de Gestión de Biblioteca

Una API REST de Gestión de Biblioteca para gestionar usuarios, libros y préstamos de forma moderna,
escalable y segura. _Hecha con Spring Boot 3.5.7, Java 21, Docker, Flyway y Maven. Utiliza PostgreSQL para la base
de datos y Redis para la caché._

### Características Principales

- **Autenticación y Seguridad:** Spring Security con implementación de JWT stateless. Los tokens expiran después de 1
  hora. Control de acceso basado en roles (USER vs ADMIN).
- **Arquitectura:** Patrón estándar Controller-Service-Repository.
- **Mapeo de Datos:** Utiliza MapStruct y DTOs para asegurar un manejo estructurado, seguro y de alto rendimiento en las requests
  y responses.
- **Manejo de Errores:** `GlobalExceptionHandler` que mapea excepciones de negocio personalizadas a respuestas HTTP estándar.
- **Caché y Rendimiento:** Integración con Redis para cachear todos los endpoints posibles para mejor velocidad.
- **Auditoría de Base de Datos:** Diseño de entidades con trazabilidad a través de propiedades `created_at` y `last_modified_at`.
- **Observabilidad:** Spring Boot Actuator habilitado para verificaciones de estado (health checks) en tiempo real, monitoreo de
  métricas y nivelación dinámica de logs.
- **Documentación:** Swagger UI autogenerado para la exploración y prueba de endpoints.
- **CI/CD:** Pipeline de pruebas automatizadas utilizando GitHub Actions.

### Reglas de Negocio

1. **Disponibilidad:** Los usuarios no pueden pedir prestado un libro si no hay copias disponibles. Prestar un libro reduce las
   copias en 1; devolverlo aumenta las copias en 1.
2. **Límites de Préstamo:** Un usuario puede tener un máximo de 3 préstamos activos en cualquier momento.
3. **Préstamos Duplicados:** Un usuario no puede pedir prestadas múltiples copias del mismo libro simultáneamente.
4. **Roles y Acceso:** Los usuarios pueden tener el rol `USER` o `ADMIN`. Ciertos endpoints administrativos están restringidos a
   los Administradores.
5. **Gestión de Perfil:** Los usuarios pueden actualizar su información personal y contraseñas. Pueden recuperar su propia
   información a través de `/users/me` y `/loans/me`.

### Cómo Ejecutar Localmente

Crea y configura tu archivo `.env` en el directorio raíz:

``` Properties
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=
DB_HOST=
DB_PORT=5432
REDIS_HOST=
REDIS_PORT=
JWT_SECRET=
JWT_EXPIRATION=
APP_PORT=8080
ACTUATOR_USERNAME=
ACTUATOR_PASSWORD=
```

Ejecuta una instancia de PostgreSQL y Redis. El archivo `docker-compose.yml` provisto tiene todo lo necesario para levantarlos
usando: `docker-compose up -d`. Luego, ejecuta la aplicación Spring Boot localmente a través de tu IDE o Maven.
Alternativamente, puedes ejecutar todo el stack (Base de Datos, Redis y Aplicación) en contenedores con:
`docker-compose --profile prod up --build`.

### Despliegue en Producción

Esta aplicación está configurada para alojarse en la nube:

- Base de datos: Supabase (PostgreSQL con Transaction Pooler para soporte IPv4).
- Caché: Upstash (Redis Serverless).
- Host: Render, usando el perfil de Spring `prod`.

_Nota sobre Swagger en Producción_: Para poder visualizar la documentación de la API en el entorno de producción, las propiedades
que deshabilitan Swagger en `application-prod.properties` están temporalmente comentadas.

---

# English Version: Library Management REST API

A Library Management REST API built with Spring Boot to manage users, books, and loans in a modern, scalable, and secure way.
_Built with Spring Boot 3.5.7, Java 21, Docker, Flyway, and Maven. Uses PostgreSQL (Supabase) for the database and Redis (Upstash)
for caching._

### Core Features

* **Authentication & Security:** Spring Security with stateless JWT implementation. Tokens expire after 1 hour. Role-based access
  control (USER vs ADMIN).
* **Architecture:** Standard Controller-Service-Repository pattern.
* **Data Mapping:** Uses MapStruct and DTOs to ensure requests and responses are structured, type-safe, and high-performance.
* **Error Handling:** GlobalExceptionHandler mapping custom business exceptions to standard HTTP responses.
* **Caching & Performance:** Redis integration to cache heavily accessed endpoints.
* **Database Auditing:** Base entities automatically track `created_at` and `last_modified_at` timestamps.
* **Observability:** Spring Boot Actuator enabled for real-time health checks, metric monitoring, and dynamic log leveling (
  integrated with Better Stack).
* **Documentation:** Auto-generated Swagger UI for endpoint exploration and testing.
* **CI/CD:** Automated testing pipeline using GitHub Actions.

### Business Rules

1. **Availability:** Users cannot borrow a book if there are no available copies. Borrowing reduces copies by 1; returning
   increases copies by 1.
2. **Loan Limits:** A user can have a maximum of 3 active loans at any given time.
3. **Duplicate Loans:** A user cannot borrow multiple copies of the exact same book simultaneously.
4. **Roles & Access:** Users can have either a `USER` or `ADMIN` role. Certain administrative endpoints are restricted to Admins.
5. **Profile Management:** Users can update their personal information and passwords. They can retrieve their own info via
   `/users/me` and `/loans/me`.

### How to Run Locally

Create and configure your `.env` file in the root directory:

```properties
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=
DB_HOST=
DB_PORT=5432
REDIS_HOST=
REDIS_PORT=
JWT_SECRET=
JWT_EXPIRATION=
APP_PORT=8080
ACTUATOR_USERNAME=
ACTUATOR_PASSWORD=
```

Run a PostgreSQL and Redis instance. The provided docker-compose.yml file has everything you need to build them using:
`docker-compose up -d`. Then, run the Spring Boot application locally via your IDE or Maven.

Alternatively, you can run the entire stack (Database, Redis, and Application) in containers with:
`docker-compose --profile prod up --build`.

### Production Deployment

This application is configured for cloud hosting:
-Database: Supabase (PostgreSQL with Transaction Pooler for IPv4 support).

- Cache: Upstash (Serverless Redis).
- Host: Render, using the prod Spring profile.

_Note on Swagger in Production_: To be able to view the API documentation in the production environment, the properties disabling
Swagger in application-prod.properties are temporarily commented out.