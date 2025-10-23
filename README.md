# 🎬 Proyecto Integrado

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/) 
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose) 
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/) 
[![API](https://img.shields.io/badge/API-4CAF50?style=for-the-badge&logo=api&logoColor=white)](https://www.themoviedb.org/documentation/api)

---

## 📑 Índice
- [💡 Idea de la aplicación](#-idea-de-la-aplicación)  
- [🛠 Tecnologías empleadas](#-tecnologías-empleadas)  
- [✅ Check list de tareas](#-check-list-de-tareas)  
  - [📅 Quincena 1 (9 - 13 octubre)](#-quincena-1-9---13-octubre)  
  - [📅 Quincena 2 (14 - 27 octubre)](#-quincena-2-14---27-octubre)  
  - [📅 Quincena 3 (28 octubre - 10 noviembre)](#-quincena-3-28-octubre---10-noviembre)  
  - [📅 Quincena 4 (11 - 24 noviembre)](#-quincena-4-11---24-noviembre)  
  - [📅 Quincena 5 (25 noviembre - 5 diciembre)](#-quincena-5-25-noviembre---5-diciembre)
- [📑 Modificaciones en la documentación](#-modificaciones-en-la-documentación)  

---

## 💡 Idea de la aplicación
Esta aplicación surge ante la gran variedad de películas y series disponibles y el tiempo que invertimos en buscar contenido que guste a todos (amigos, pareja o familiares).  

**🎯 Objetivo:**  
Permitir que los usuarios se registren y tengan amigos con los que hacer rondas de “swipes” tipo Tinder, eligiendo series y películas en común. Cuando dos o más usuarios hacen **match**, ese contenido se guarda para consultas futuras.

---

## 🛠 Tecnologías empleadas
- **Kotlin**  
- **Jetpack Compose**  
- **Firebase Firestore**  
- **Firebase Auth**  
- **Room** (opcional)  
- **MVVM** con ViewModel y StateFlow  
- **API externa de películas**  
- **Material 3**  

---

## ✅ Check list de tareas

### 📅 Quincena 1 (9 - 13 octubre)
**🎯 Objetivo:** Configuración inicial y planificación  

**Tareas:**
- [x] Repositorio en GitHub activo y compartido  
- [x] Creación de la aplicación  
- [X] Esquema de base de datos (Modelo ER)  
- [X] Documentación v1 

**Entrega quincenal:**  
- ✅ Repositorio inicial
- ✅ Diagrama de casos de uso
- ✅ Diagrama ER 
- ✅ Diagrama de clases

---

### 📅 Quincena 2 (14 - 27 octubre)
**🎯 Objetivo:** Autenticación y primer flujo de contenido  

**Tareas:**
- [x] Configuración de Firebase (Auth + Firestore)
- [ ] Creación de pantallas de inicio de sesión y registro  
- [x] Guardado de datos de usuario en Firestore  
- [ ] Obtener contenido de la API  
- [ ] Swipe funcional  
- [ ] Guardado de likes/dislikes  
- [ ] Documentación v2  

**Entrega quincenal:**  
- ✅ Login/registro  
- ✅ API mostrando contenido  
- ✅ Swipe básico  
- ✅ Firestore guardando selecciones  

---

### 📅 Quincena 3 (28 octubre - 10 noviembre)
**🎯 Objetivo:** Amigos y detalles de contenido  

**Tareas:**
- [ ] Implementación de búsqueda de amigos  
- [ ] Creación de pantalla de amigos  
- [ ] Pantalla de detalles del contenido  
- [ ] Ver rondas  
- [ ] Documentación v3  

**Entrega quincenal:**  
- ✅ Amigos funcionales  
- ✅ Pantalla detalles  
- ✅ Rondas funcionales  

---

### 📅 Quincena 4 (11 - 24 noviembre)
**🎯 Objetivo:** Matches y diseño final  

**Tareas:**
- [ ] Pantalla de matches  
- [ ] Detectar matches  
- [ ] Documentación v4  
- [ ] Pulir diseño general  

**Entrega quincenal:**  
- ✅ Matches funcionales  
- ✅ Diseño pulido  

---

### 📅 Quincena 5 (25 noviembre - 5 diciembre)
**🎯 Objetivo:** Entrega final  

**Tareas:**
- [ ] Pruebas de funcionamiento  
- [ ] Corrección de errores  
- [ ] Documentación v5  

**Entrega final:**  
- ✅ App completa y funcional  
- ✅ Documentación final  

## 📑 Modificaciones en la documentación
- Modificaciones 16/Octubre
  - Modificado el diagrama de clases había clases vacías y su contenido en otras clases, debido a mal tabulación del UML y no me había dado cuenta. Además actualizado el tipo de datos, tanto en el diagrama  de clases como en las entidades en Kotlin, de Instant a Timestamp que funciona mejor en Firebase.
  - Modificado el modelo ER (para que sea una versión más "normalizada" de lo que sería un modelo ER si fuese SQL)
