# Documentación del Frontend - Credibanco

Este documento proporciona una descripción general de la arquitectura, estructura y componentes del proyecto frontend de la aplicación Credibanco.

## 1. Tecnologías Utilizadas

- **Angular**: Framework principal para el desarrollo de la aplicación.
- **TypeScript**: Lenguaje de programación utilizado.
- **HTML & CSS/SCSS**: Para la estructura y el estilo de los componentes.
- **Angular Router**: Para la gestión de la navegación y las rutas de la aplicación.

## 2. Estructura del Proyecto

La estructura principal del código fuente se encuentra en la carpeta `src/app`.


src/
└── app/
    ├── components/
    │   ├── tarjeta-form/       # Componente para crear nuevas tarjetas
    │   ├── tarjetas-list/      # Componente para listar las tarjetas existentes
    │   ├── transaccion-form/   # Componente para registrar nuevas transacciones
    │   └── transacciones-list/ # Componente para listar las transacciones
    │
    ├── app-routing.module.ts   # Módulo de enrutamiento principal
    ├── app.component.ts        # Componente raíz de la aplicación
    └── app.module.ts           # Módulo raíz de la aplicación


### Descripción de Carpetas Clave

- **`src/app/components`**: Contiene todos los componentes reutilizables y específicos de las vistas de la aplicación. Cada componente tiene su propia carpeta con su archivo TypeScript, HTML, CSS y de pruebas.

## 3. Módulo de Enrutamiento (`app-routing.module.ts`)

Este archivo define las rutas de navegación de la aplicación. Cada ruta está asociada a un componente que se renderiza cuando la ruta es activada.

### Rutas Definidas:

- **`/tarjetas`**:
  - **Componente**: `TarjetasListComponent`
  - **Descripción**: Muestra la lista de todas las tarjetas de crédito/débito registradas en el sistema. Es la página principal de la aplicación.

- **`/tarjetas/nuevo`**:
  - **Componente**: `TarjetaFormComponent`
  - **Descripción**: Muestra un formulario para crear y registrar una nueva tarjeta.

- **`/transacciones`**:
  - **Componente**: `TransaccionesListComponent`
  - **Descripción**: Muestra una lista de todas las transacciones realizadas.

- **`/transacciones/nuevo`**:
  - **Componente**: `TransaccionFormComponent`
  - **Descripción**: Muestra un formulario para registrar una nueva transacción (por ejemplo, una compra o un retiro).

- **`/` (Ruta Raíz)**:
  - **Redirige a**: `/tarjetas`
  - **Descripción**: Si un usuario navega a la raíz del sitio, será redirigido automáticamente a la lista de tarjetas.

## 4. Cómo Empezar

Sigue estos pasos para configurar y ejecutar el proyecto en un entorno de desarrollo local.

### Prerrequisitos

- **Node.js y npm**: Asegúrase de tener instalada una versión LTS de Node.js. Puedes descargarlo desde nodejs.org.
- **Angular CLI**: Instalar la CLI de Angular globalmente con el siguiente comando:
  
  npm install -g @angular/cli
  

### Instalación y Ejecución

1. **Clonar el repositorio** (si aplica) y navegar a la carpeta del proyecto.
2. **Instalar las dependencias**:

   
3. **Inicia el servidor de desarrollo**:
   
   ng serve -o
   
   El comando `-o` abrirá automáticamente tu navegador en `http://localhost:4200/`. La aplicación se recargará automáticamente si cambias alguno de los archivos fuente.
