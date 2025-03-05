# Proyecto de Prueba de la API de Reconocimiento de Voz de Android

Este proyecto es una aplicación de Android diseñada para probar la funcionalidad de la API de Reconocimiento de Voz (Speech-to-Text) de Android. La aplicación permite a los usuarios convertir su habla en texto y rellenar campos de entrada en la interfaz de usuario.

## Características

- **Reconocimiento de Voz**: Convierte el habla del usuario en texto utilizando la API de Reconocimiento de Voz de Android.
- **Interfaz de Usuario con Jetpack Compose**: Utiliza Jetpack Compose para una interfaz de usuario moderna y reactiva.
- **Gestión de Permisos**: Solicita permisos de micrófono en tiempo de ejecución para garantizar la privacidad del usuario.

## Requisitos

- Android Studio Arctic Fox o superior
- Dispositivo o emulador con Android 5.0 (Lollipop) o superior
- Conexión a Internet para el reconocimiento de voz

## Instalación

1. Clona este repositorio en tu máquina local:
   ```bash
   git clone https://github.com/tu-usuario/tu-repositorio.git
   ```

2. Abre el proyecto en Android Studio.

3. Sincroniza el proyecto con Gradle.

4. Ejecuta la aplicación en un dispositivo o emulador.

## Uso

1. Al abrir la aplicación, verás un saludo y tres campos de entrada.

2. Haz clic en el icono de micrófono junto a cualquier campo para iniciar el reconocimiento de voz.

3. Habla claramente al dispositivo. El texto reconocido se mostrará en el campo correspondiente.

## Estructura del Proyecto

- **MainActivity.kt**: Contiene la lógica principal para el reconocimiento de voz y la interfaz de usuario.
- **AndroidManifest.xml**: Define los permisos necesarios para el uso del micrófono.
- **Tema de la Aplicación**: Configurado en `TestTheme` para un diseño consistente.
