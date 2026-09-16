# PetCare v2.0 — Release Notes (Android)

Resumen del lado móvil de los Bloques 4 al 12. Excluye explícitamente monetización,
membresías, planes premium y organizaciones — no se tocó nada de eso.

## Bloque 4 — UX / Play Store ready
- Toast → Snackbar en toda la app (con acción "Deshacer" donde aplica).
- `PetCareApplication` implementa `ImageLoaderFactory` (caché de memoria 25%, disco 50MB).
- `RetryInterceptor` con backoff exponencial 1s/2s/4s (solo GET, no duplica escrituras).
- `values-en/strings.xml` para onboarding, login, registro, navegación y acciones comunes.

## Bloque 5 — Offline y accesibilidad
- `MensajeLocalEntity`/`MensajeLocalDao` (Room): caché offline del chat y cola de reintento
  (ids locales negativos para mensajes pendientes, reemplazados por el id real del servidor).
- Fix de contraste WCAG AA, `contentDescription` agregado donde faltaba.

## Bloque 6.2 — Tests de red
- `ApiServiceMockWebServerTest.kt`: casos 200/400/500/timeout-con-reintento contra un
  `MockWebServer` real, verificando que la capa de red se comporta como esperan las pantallas.

## Bloque 7 — Badges
- Badge mostrado en perfil, tarjetas de ofertas y tarjetas de solicitudes.

## Bloque 8 — Foto antes/después
- Prompt de evidencia (ANTES al aceptar, DESPUÉS al finalizar) que nunca bloquea la acción
  subyacente — sube la foto, la encola offline (`EvidenciaLocalEntity`, mismo patrón que el
  chat) o se omite, y el cambio de estado sigue adelante en los tres casos.
- Miniaturas ANTES/DESPUÉS en el detalle del servicio (dueño y cuidador).

## Bloque 9 — Calendario
- `CalendarioScreen.kt` con vistas de mes y semana (`com.kizitonwose.calendar:compose` 2.6.2,
  fijado por compatibilidad de metadata de Kotlin — ver comentario en `app/build.gradle.kts`).
- Días con puntos de color por estado del servicio; tocar un día abre el detalle existente.

## Bloque 10 — Llamadas telefónicas
- Botón de llamada (`Intent.ACTION_DIAL`) visible solo cuando el servicio está confirmado.

## Bloque 11 — Expediente médico
- Tab "Expediente Médico" en el perfil de la mascota (`DogInfoScreen.kt`): entradas agrupadas
  por tipo, alerta visual cuando `fecha_proxima` cae en los próximos 15 días, foto del carnet
  de vacunas. Solo el dueño puede agregar/eliminar entradas.
- Un cuidador puede consultarlo en modo lectura (sin agregar/editar/eliminar) antes de ofertar,
  durante y después de un servicio — ver "Expediente médico para cuidadores" más abajo.

## Bloque 12 — Alerta de mascota perdida
- Botón "🚨 Mi mascota se perdió" y "¡La encontré!" en el perfil de la mascota.
- `AlertasPerdidasScreen.kt` (feed de alertas cercanas) y `AlertaPerdidaDetalleScreen.kt`
  (mapa, avistamientos, reportar avistamiento).
- Alcance: sin adjuntar foto al avistamiento (el endpoint espera una URL, no upload directo) ni
  geocodificación inversa automática de la dirección — se puede agregar en una iteración futura.

## Expediente médico para cuidadores
- Nueva pantalla de solo lectura (`ExpedienteMedicoScreen.kt`, reutiliza `ExpedienteMedicoSection`
  con `isOwner = false`) accesible desde la tarjeta de una solicitud PENDIENTE en el feed del
  cuidador (antes de ofertar), desde el detalle de un servicio ACCEPTED o COMPLETED, y desde la
  barra superior del chat.
- El backend (`petcare-services`) ahora valida permisos reales en
  `GET /api/pets/{id}/expediente` en vez de estar completamente abierto — ver el commit
  correspondiente en ese repositorio para el detalle de la regla de negocio.

## Mejoras de UX (comparación, resumen, notificaciones, estadísticas, búsqueda, ayuda, configuración)
- `CompararOfertasScreen.kt`: compara hasta 3 postulaciones de cuidadores lado a lado (con
  calificación promedio en vivo) para la misma solicitud, con botón "Aceptar" por columna.
- `ResumenServicioScreen.kt`: fotos antes/después y duración del servicio (calculada desde las
  evidencias), accesible desde el historial de servicios completados.
- `NotificacionesScreen.kt`: centro de notificaciones sobre la infraestructura de Room que ya
  existía (`NotificationDao`/`AppNotifier`) pero no tenía una pantalla propia.
- `EstadisticasCuidadorScreen.kt` / `EstadisticasPropietarioScreen.kt`: métricas calculadas desde
  endpoints ya existentes (historial, calificaciones, favoritos) — sin agregados nuevos en el
  backend. La "tasa de aceptación" y el "gasto estimado" son cálculos de mejor esfuerzo (el
  gasto solo se cuenta cuando la solicitud incluye una línea "Precio:" en su descripción).
- `BusquedaGlobalScreen.kt`: búsqueda local (sin llamadas de red nuevas) por nombre de cuidador,
  perro o tipo de servicio, sobre los datos ya cargados en la pantalla de inicio.
- `AyudaScreen.kt` (guías rápidas + FAQ + contacto) y `ConfiguracionScreen.kt` (modo oscuro
  persistido localmente, idioma vía los ajustes del sistema — Android 13+ tiene selector nativo
  por app, notificaciones "no molestar", privacidad, acerca de).
- Indicador de conexión global (`ConnectivityBanner`) y botón flotante de emergencia visible
  mientras hay un servicio activo (antes solo estaba dentro del diálogo de detalle).
- Animación de check verde al aceptar una oferta, publicar una solicitud, completar un servicio
  y enviar una calificación.
- Todos los accesos nuevos viven en el perfil de propietario/cuidador (menú de accesos rápidos)
  y en los puntos de entrada específicos mencionados arriba.

## Pendiente
- **API Key real de Google Maps**: sigue sin configurar. Ver `GOOGLE_MAPS_SETUP.md` para los
  pasos — el mapa funciona pero muestra el watermark "for development purposes only" hasta que
  se configure una key real en `secrets.properties`.
- Eliminación de cuenta desde la app: no implementada todavía (no hay endpoint de autoservicio
  wireado); por ahora `ConfiguracionScreen` remite a contactar soporte.
- Badge del cuidador (NUEVO/CONFIABLE/etc.): existe en el backend (`usuarios.badge`) pero no está
  wireado en ningún DTO/pantalla de la app todavía — pendiente de una iteración futura.
