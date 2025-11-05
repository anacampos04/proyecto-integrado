# TMDb API Integration

Integración completa con The Movie Database (TMDb) API para obtener películas y series.

## Configuración

### 1. Obtener API Key

1. Crea una cuenta en [TMDb](https://www.themoviedb.org/)
2. Ve a Settings → API
3. Solicita una API key (tipo Developer)
4. Copia tu API key

### 2. Configurar API Key localmente

Edita el archivo `local.properties` en la raíz del proyecto y reemplaza `YOUR_API_KEY_HERE`:

```properties
tmdb.api.key=tu_api_key_aqui
```

## Uso

### Crear instancia del repositorio

```kotlin
val tmdbRepository = TmdbClient.createRepository(BuildConfig.TMDB_API_KEY)
```

### Obtener contenido popular

```kotlin
viewModelScope.launch {
    val result = tmdbRepository.getPopularContent(
        page = 1,
        includeMovies = true,
        includeTv = true
    )

    result.onSuccess { contenido ->
        // contenido es List<ContenidoLite>
        println("Obtenidos ${contenido.size} elementos")
    }

    result.onFailure { error ->
        println("Error: ${error.message}")
    }
}
```

### Descubrir contenido con filtros

```kotlin
viewModelScope.launch {
    // Buscar películas en Netflix y Prime Video
    val result = tmdbRepository.discoverContent(
        tipo = TipoContenido.PELICULA,
        plataformas = listOf("8", "119"), // Netflix y Prime
        minRating = 7.0,
        page = 1
    )

    result.onSuccess { contenido ->
        // contenido filtrado por plataformas y rating
    }
}
```

### Buscar por texto

```kotlin
viewModelScope.launch {
    val result = tmdbRepository.searchContent(
        query = "Breaking Bad",
        tipo = TipoContenido.SERIE,
        page = 1
    )

    result.onSuccess { resultados ->
        // resultados de búsqueda
    }
}
```

## Estructura

- **TmdbApiService**: Interfaz Retrofit con todos los endpoints
- **TmdbRepository**: Repositorio que maneja la lógica de negocio
- **TmdbClient**: Cliente singleton para crear instancias configuradas
- **models/**: Modelos de respuesta de TMDb y mappers a ContenidoLite

## IDs de Plataformas (región ES)

Ya configurados en `PlataformasCatalogo`:

- Netflix: `8`
- Amazon Prime Video: `119`
- Disney+: `337`
- HBO Max: `384`
- Apple TV+: `350`
- Movistar Plus+: `149`
- Filmin: `63`
- Rakuten TV: `35`
- SkyShowtime: `1773`
