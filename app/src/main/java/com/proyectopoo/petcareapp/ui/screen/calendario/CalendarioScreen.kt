package com.proyectopoo.petcareapp.ui.screen.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.proyectopoo.petcareapp.data.network.CalendarioServicioDto
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.util.statusColor
import com.proyectopoo.petcareapp.ui.util.statusLabel
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendario integrado (Bloque 9): vista de mes y semana de los servicios del usuario, con un
 * punto por servicio coloreado según su estado (mismo mapeo que el resto de la app, ver
 * `ui/util/StatusLabels.kt`). Los datos vienen de `GET /api/calendario`; `disponibilidad`
 * siempre llega vacía hoy (el backend no expone disponibilidad de cuidadores todavía), así que
 * esta pantalla solo renderiza `servicios`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    usuarioId: Int,
    onBack: () -> Unit,
    onOpenService: (Int) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(today) }
    var loadedYearMonth by remember { mutableStateOf<YearMonth?>(null) }
    var serviciosPorFecha by remember { mutableStateOf<Map<LocalDate, List<CalendarioServicioDto>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    suspend fun loadMonth(yearMonth: YearMonth) {
        isLoading = true
        loadError = null
        val response = runCatching {
            RetrofitClient.apiService.getCalendario(usuarioId, yearMonth.monthValue, yearMonth.year)
        }.getOrNull()
        isLoading = false
        val body = response?.takeIf { it.isSuccessful }?.body()
        loadedYearMonth = yearMonth
        if (body == null) {
            loadError = "No se pudo cargar el calendario. Intenta de nuevo."
            return
        }
        serviciosPorFecha = body.servicios
            .mapNotNull { dto -> runCatching { LocalDate.parse(dto.fecha) }.getOrNull()?.let { it to dto } }
            .groupBy({ it.first }, { it.second })
    }

    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val startMonth = remember { YearMonth.from(today).minusMonths(24) }
    val endMonth = remember { YearMonth.from(today).plusMonths(24) }

    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.from(today),
        firstDayOfWeek = firstDayOfWeek
    )
    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atDay(1),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = today,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(monthState) {
        snapshotFlow { monthState.firstVisibleMonth.yearMonth }
            .distinctUntilChanged()
            .collect { visibleMonth -> if (visibleMonth != loadedYearMonth) loadMonth(visibleMonth) }
    }
    LaunchedEffect(weekState) {
        snapshotFlow { weekState.firstVisibleWeek.days.firstOrNull()?.date }
            .distinctUntilChanged()
            .collect { date ->
                val visibleMonth = date?.let { YearMonth.from(it) } ?: return@collect
                if (selectedTab == 1 && visibleMonth != loadedYearMonth) loadMonth(visibleMonth)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Mes") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Semana") })
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            loadError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                WeekDayHeaderRow(firstDayOfWeek)
                Spacer(Modifier.height(4.dp))
                if (selectedTab == 0) {
                    HorizontalCalendar(
                        state = monthState,
                        dayContent = { day ->
                            CalendarDayCell(
                                date = day.date,
                                inMonth = day.position == DayPosition.MonthDate,
                                isSelected = day.date == selectedDate,
                                servicios = serviciosPorFecha[day.date].orEmpty(),
                                onClick = { selectedDate = day.date }
                            )
                        },
                        monthHeader = { month -> MonthTitle(month.yearMonth) }
                    )
                } else {
                    WeekCalendar(
                        state = weekState,
                        dayContent = { weekDay ->
                            CalendarDayCell(
                                date = weekDay.date,
                                inMonth = true,
                                isSelected = weekDay.date == selectedDate,
                                servicios = serviciosPorFecha[weekDay.date].orEmpty(),
                                onClick = { selectedDate = weekDay.date }
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            DayServicesList(
                date = selectedDate,
                servicios = serviciosPorFecha[selectedDate].orEmpty(),
                onOpenService = onOpenService,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthTitle(yearMonth: YearMonth) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES")) }
    Text(
        text = yearMonth.format(formatter).replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun WeekDayHeaderRow(firstDayOfWeek: DayOfWeek) {
    val days = remember(firstDayOfWeek) { (0..6).map { firstDayOfWeek.plus(it.toLong()) } }
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).replaceFirstChar { it.uppercase() },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    inMonth: Boolean,
    isSelected: Boolean,
    servicios: List<CalendarioServicioDto>,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
            if (servicios.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    servicios.take(3).forEach { servicio ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else statusColor(servicio.estado)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayServicesList(
    date: LocalDate,
    servicios: List<CalendarioServicioDto>,
    onOpenService: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES")) }
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = date.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        if (servicios.isEmpty()) {
            Text(
                "No hay servicios este día.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(servicios, key = { it.solicitudId }) { servicio ->
                    ServicioRow(servicio = servicio, onClick = { onOpenService(servicio.solicitudId) })
                }
            }
        }
    }
}

@Composable
private fun ServicioRow(servicio: CalendarioServicioDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor(servicio.estado))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(servicio.titulo, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (servicio.rol == "PROPIETARIO") "Como dueño" else "Como cuidador"} • ${statusLabel(servicio.estado)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
