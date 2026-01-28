package com.example.homework2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlin.math.roundToInt



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplitMateApp()
                }
            }
        }
    }
}



data class Calculation(
    val total: Double,
    val people: Int,
    val tipPercent: Double = 0.0
) {
    val tipAmount: Double get() = (total * tipPercent / 100 * 100).roundToInt() / 100.0
    val totalWithTip: Double get() = total + tipAmount
    val perPerson: Double get() = (totalWithTip / people * 100).roundToInt() / 100.0 //две цифры после запятой
}


class SplitMateViewModel : ViewModel() {
    var calculation by mutableStateOf<Calculation?>(null)  //
        private set // без него не работае

    fun calculate(total: Double, people: Int, tipPercent: Double) {
        calculation = Calculation(total, people, tipPercent)
    }

    fun reset() {
        calculation = null
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SplitMate",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Делим счёт легко!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(64.dp))
        Button(
            onClick = { navController.navigate("input") },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Начать", style = MaterialTheme.typography.titleLarge)
        }
    }
}


@Composable
fun InputScreen(navController: NavHostController, viewModel: SplitMateViewModel) {
    var total by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("2") }
    var tipPercent by remember { mutableStateOf("0") }

    val isEnabled = total.toDoubleOrNull()?.let { it > 0 } == true &&
            people.toIntOrNull()?.let { it > 0 } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Введите данные", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(
            value = total,
            onValueChange = { total = it },
            label = { Text("Сумма счёта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = people,
            onValueChange = { people = it },
            label = { Text("Количество людей") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tipPercent,
            onValueChange = { tipPercent = it },
            label = { Text("Чаевые (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            enabled = isEnabled,
            onClick = {
                viewModel.calculate(
                    total = total.toDouble(),
                    people = people.toInt(),
                    tipPercent = tipPercent.toDoubleOrNull() ?: 0.0
                )
                navController.navigate("result")
            },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("РАССЧИТАТЬ")
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("НАЗАД")
        }
    }
}



@Composable
fun ResultScreen(navController: NavHostController, viewModel: SplitMateViewModel) {
    val calculation = viewModel.calculation

    if (calculation == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Расчёт не найден")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Результат", style = MaterialTheme.typography.headlineLarge)

        Card {
            Column(Modifier.padding(16.dp)) {
                ResultRow("Сумма:", "${calculation.total} ₽")
                ResultRow("Людей:", "${calculation.people}")
                ResultRow("Чаевые:", "${calculation.tipAmount} ₽")
                Divider()
                ResultRow("Итого:", "${calculation.totalWithTip} ₽", true)
                ResultRow("С каждого:", "${calculation.perPerson} ₽", true)
            }
        }

        Button(
            onClick = { navController.navigate("input") },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("ИЗМЕНИТЬ")
        }

        Button(
            onClick = {
                viewModel.reset()
                navController.navigate("home") {
                    popUpTo(0)
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("НОВЫЙ РАСЧЁТ")
        }
    }
}


@Composable
fun ResultRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            value,
            color = if (bold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun SplitMateApp() {
    val navController = rememberNavController()
    val viewModel: SplitMateViewModel = viewModel()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("input") { InputScreen(navController, viewModel) }
        composable("result") { ResultScreen(navController, viewModel) }
    }
}
