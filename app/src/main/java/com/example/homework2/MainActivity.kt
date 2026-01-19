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
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import java.util.*
import kotlin.math.ceil

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

//класс

data class Calculation(
    val id: String = UUID.randomUUID().toString(),
    val total: Double,
    val people: Int,
    val tipPercent: Double = 10.0
) {
    val tipAmount: Double get() = total * tipPercent / 100
    val totalWithTip: Double get() = total + tipAmount
    val perPerson: Double get() = ceil(totalWithTip / people)
}

// VIEWMODEL

class SplitMateViewModel : ViewModel() {

    private val calculations = mutableListOf<Calculation>()

    fun addCalculation(calculation: Calculation) {
        calculations.add(calculation)
    }

    fun getCalculationById(id: String): Calculation? {
        return calculations.find { it.id == id }
    }

    fun reset() {
        calculations.clear()
    }
}

//сэкраны

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
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Делим счёт легко!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = { navController.navigate("input") },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Начать", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun InputScreen(
    navController: NavHostController,
    viewModel: SplitMateViewModel
) {
    var total by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("2") }
    var tipPercent by remember { mutableStateOf("0") }

    val isEnabled = total.toDoubleOrNull() != null &&
            people.toIntOrNull() != null &&
            total.toDouble() > 0 &&
            people.toInt() > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Введите данные",
            style = MaterialTheme.typography.headlineLarge
        )

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

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = isEnabled,
            onClick = {
                val calculation = Calculation(
                    total = total.toDouble(),
                    people = people.toInt(),
                    tipPercent = tipPercent.toDoubleOrNull() ?: 0.0
                )
                viewModel.addCalculation(calculation)
                navController.navigate("result/${calculation.id}")
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
fun ResultScreen(
    navController: NavHostController,
    calculationId: String,
    viewModel: SplitMateViewModel
) {
    val calculation = viewModel.getCalculationById(calculationId)

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

//Навигация
@Composable
fun SplitMateApp() {
    val navController = rememberNavController()
    val viewModel: SplitMateViewModel = viewModel() // 👈 ВАЖНО

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("input") {
            InputScreen(navController, viewModel)
        }
        composable(
            "result/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            ResultScreen(
                navController,
                it.arguments?.getString("id") ?: "",
                viewModel
            )
        }
    }
}
