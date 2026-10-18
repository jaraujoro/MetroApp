package com.metroapp.lima.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metroapp.lima.ui.components.SimpleBarChart
import com.metroapp.lima.ui.screens.dashboard.MetricBox
import com.metroapp.lima.ui.theme.MetroGreenContainer
import com.metroapp.lima.ui.theme.MetroGreenPrimary
import com.metroapp.lima.ui.theme.MetroRed
import com.metroapp.lima.ui.viewmodel.MetroViewModel
import com.metroapp.lima.util.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    viewModel: MetroViewModel
) {
    val reports by viewModel.reportsData.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Reportes y Estadísticas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MetroGreenPrimary
                )
                Text(
                    text = "Análisis de consumo, comparativas mensuales y exportación",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Summary KPIs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Total Consumido",
                        value = CurrencyUtils.formatCents(reports.totalSpentCents),
                        subtitle = "${reports.totalTripsCount} viajes efectivos",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Total Recargado",
                        value = CurrencyUtils.formatCents(reports.totalTopUpCents),
                        subtitle = "${reports.totalTopUpsCount} recargas",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Gasto Diario Promedio",
                        value = CurrencyUtils.formatCents(reports.averageDailySpentCents),
                        subtitle = "Por día de viaje activo",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Proyección Mensual",
                        value = CurrencyUtils.formatCents(reports.projectedMonthlySpentCents),
                        subtitle = "Estimación al fin de mes",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Daily expenses Chart
            item {
                SimpleBarChart(
                    title = "Gastos Diarios Recientes (Soles)",
                    dataPoints = reports.dailyExpenses,
                    barColor = MetroGreenPrimary
                )
            }

            // Monthly comparison Chart
            item {
                SimpleBarChart(
                    title = "Comparativa Mensual de Gastos (Soles)",
                    dataPoints = reports.monthlyComparison,
                    barColor = Color(0xFF1976D2)
                )
            }

            // Export Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Exportar Información",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Descarga o comparte tus registros contables en formatos estándar",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val file = viewModel.exportCsv()
                                        viewModel.exportUseCase.shareFile(file, "text/csv")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Archivo CSV generado exitosamente")
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error al exportar CSV: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MetroGreenPrimary)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exportar CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val file = viewModel.exportPdf()
                                        viewModel.exportUseCase.shareFile(file, "application/pdf")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Archivo PDF generado exitosamente")
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error al exportar PDF: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exportar PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
