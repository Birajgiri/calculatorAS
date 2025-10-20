package com.example.simplecalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplecalculator.ui.theme.SimpleCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleCalculatorTheme {
                CalculatorScreen(context = this)
            }
        }
    }
}

@Composable
fun CalculatorScreen(context: Context) {
    // SharedPreferences for state persistence
    val prefs = context.getSharedPreferences("calcPrefs", Context.MODE_PRIVATE)

    var display by remember {
        mutableStateOf(prefs.getString("lastValue", "0") ?: "0")
    }
    var firstNumber by remember { mutableStateOf(0.0) }
    var operation by remember { mutableStateOf("") }
    var newInput by remember { mutableStateOf(true) }

    fun saveDisplay() {
        prefs.edit().putString("lastValue", display).apply()
    }

    fun clearAll() {
        display = "0"
        firstNumber = 0.0
        operation = ""
        newInput = true
        saveDisplay()
    }

    fun calculate() {
        val secondNumber = display.toDoubleOrNull() ?: 0.0
        val result = when (operation) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "*" -> firstNumber * secondNumber
            "/" -> if (secondNumber == 0.0) Double.NaN else firstNumber / secondNumber
            else -> return
        }

        display = when {
            result.isNaN() -> "ERROR"
            result.toString().length > 8 -> "OVERFLOW"
            else -> {
                if (result == result.toLong().toDouble())
                    result.toLong().toString()
                else
                    String.format("%.4f", result).trimEnd('0').trimEnd('.')
            }
        }

        newInput = true
        operation = ""
        saveDisplay()
    }

    fun onButtonClick(label: String) {
        when (label) {
            "C" -> clearAll()
            "CE" -> {
                display = "0"
                newInput = true
                saveDisplay()
            }
            "+", "-", "*", "/" -> {
                firstNumber = display.toDoubleOrNull() ?: 0.0
                operation = label
                newInput = true
            }
            "=" -> {
                if (operation.isNotEmpty()) calculate()
            }
            else -> { // digits
                if (newInput || display == "0" || display == "ERROR" || display == "OVERFLOW") {
                    display = label
                    newInput = false
                } else if (display.length < 8) {
                    display += label
                }
                saveDisplay()
            }
        }
    }

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = display,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buttons Grid
        val buttons = listOf(
            listOf("1", "2", "3", "+"),
            listOf("4", "5", "6", "-"),
            listOf("7", "8", "9", "*"),
            listOf("CE", "0", "C", "/"),
            listOf("=")
        )

        for (row in buttons) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (label in row) {
                    Button(
                        onClick = { onButtonClick(label) },
                        modifier = if (label == "=")
                            Modifier
                                .weight(4f)
                                .height(80.dp)
                        else
                            Modifier
                                .weight(1f)
                                .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (label) {
                                "=", "+", "-", "*", "/" -> Color(0xFFFFCC00)
                                "C", "CE" -> Color(0xFFFF9999)
                                else -> Color(0xFFE0E0E0)
                            },
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
