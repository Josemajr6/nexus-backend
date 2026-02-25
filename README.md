# Nexus API

API REST desarrollada con **Spring Boot** para la gestión de la plataforma **Nexus**. Este sistema permite la administración de actores, empresas, contratos, ofertas y comentarios, siguiendo una arquitectura escalable basada en el diagrama de dominio del proyecto.


## 📋 Descripción

Nexus API es el backend encargado de gestionar la lógica de negocio y la persistencia de datos del ecosistema Nexus. El sistema implementa una jerarquía de entidades donde `Actor` sirve como clase base para los distintos roles del sistema (como Empresas), y gestiona relaciones complejas como la publicación de ofertas y la formalización de contratos publicitarios.

## 🛠️ Tecnologías Utilizadas

* **Java:** 17 (JDK 17)
* **Framework:** Spring Boot
* **Base de Datos:** PostgreSQL
* **ORM:** Spring Data JPA (Hibernate)
* **Documentación API:** SpringDoc OpenAPI (Swagger UI)
* **Gestor de Dependencias:** Maven
* **Validación:** Hibernate Validator (Jakarta Validation)

## 🗂️ Modelo de Datos (Dominio)

Basado en el código implementado y el diseño UML, la estructura principal incluye:

### Jerarquía de Actores
* **DomainEntity:** Entidad base que gestiona `ID` y `Version` para todas las entidades.
* **Actor:** Clase abstracta (Tabla única) que contiene credenciales (`user`, `email`, `password`) y fecha de registro. Es la base para todos los usuarios del sistema.
* **Empresa:** Extiende de `Actor`. Incluye atributos específicos como el **CIF** y gestiona una lista de contratos.

### Negocio
* **Contrato:** Vincula a una `Empresa` con un tipo de servicio.
    * *Tipos de contrato:* `BANNER`, `PUBLICACION`.
* **Oferta:** Publicaciones realizadas por un `Actor`. Contiene detalles como URL, tienda, precios (oferta vs original) y fecha de expiración.
* **Comentario:** Interacciones de los usuarios sobre las ofertas, con capacidad de ser reportados.

## 🚀 Instalación y Configuración

### Prerrequisitos
1.  Tener instalado **Java 17**.
2.  Tener instalado **PostgreSQL**.

### 1. Clonar el repositorio
```bash
git clone <url-del-repositorio>
cd nexus-api
