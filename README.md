# Matrix
Matrix es un core/server manager para minecraft distribuido en forma de plugin para [Paper](https://github.com/PaperMC/Paper) y [BungeeCord](https://github.com/SpigotMC/BungeeCord)

## Modulos
El código está dividido en 4 módulos:

- **matrix-api** Este módulo es un conjunto de interfaces, clases abstractas y código no requiere implementación alguna.
- **matrix-common** Este módulo es la implementación de las partes de la API que no necesitan estar integradas con una plataforma de minecraft específica, como el manejo de bases de datos, ordenamiento de la información y utilidades varias relacionadas al servidor o a código.
- **matrix-bukkit** Implementación de la API y código común en Paper
- **matrix-bungee** Implementación de la API y código común en BungeeCord
- **matrix-external** Aplicación externa a minecraft utilizada para manejar aspectos del servidor de manera remota, no debe depender de ninguna plataforma de minecraft.