package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TicTacToeGame(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TicTacToeGame(modifier: Modifier = Modifier) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var result by remember { mutableStateOf<String?>(null) }
    var isPlayerTurn by remember { mutableStateOf(true) }

    fun checkWinner(b: List<String>): String? {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Cols
            listOf(0, 4, 8), listOf(2, 4, 6)             // Diagonals
        )
        for (p in winPatterns) {
            if (b[p[0]].isNotEmpty() && b[p[0]] == b[p[1]] && b[p[0]] == b[p[2]]) {
                return b[p[0]]
            }
        }
        if (b.all { it.isNotEmpty() }) return "Draw"
        return null
    }

    fun minimax(b: MutableList<String>, depth: Int, isMaximizing: Boolean): Int {
        val winner = checkWinner(b)
        if (winner == "O") return 10 - depth
        if (winner == "X") return depth - 10
        if (winner == "Draw") return 0

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0 until 9) {
                if (b[i].isEmpty()) {
                    b[i] = "O"
                    val score = minimax(b, depth + 1, false)
                    b[i] = ""
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0 until 9) {
                if (b[i].isEmpty()) {
                    b[i] = "X"
                    val score = minimax(b, depth + 1, true)
                    b[i] = ""
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    LaunchedEffect(isPlayerTurn) {
        if (!isPlayerTurn && result == null) {
            delay(600) // Small delay to make it feel like AI is "thinking"
            val bestMove = withContext(Dispatchers.Default) {
                var bestScore = Int.MIN_VALUE
                var move = -1
                val b = board.toMutableList()
                for (i in 0 until 9) {
                    if (b[i].isEmpty()) {
                        b[i] = "O"
                        val score = minimax(b, 0, false)
                        b[i] = ""
                        if (score > bestScore) {
                            bestScore = score
                            move = i
                        }
                    }
                }
                move
            }
            
            if (bestMove != -1) {
                val newBoard = board.toMutableList()
                newBoard[bestMove] = "O"
                board = newBoard
                result = checkWinner(board)
                isPlayerTurn = true
            }
        }
    }

    fun onCellClick(index: Int) {
        if (board[index].isEmpty() && result == null && isPlayerTurn) {
            val newBoard = board.toMutableList()
            newBoard[index] = "X"
            board = newBoard
            result = checkWinner(board)
            if (result == null) {
                isPlayerTurn = false
            }
        }
    }

    fun resetGame() {
        board = List(9) { "" }
        result = null
        isPlayerTurn = true
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tic Tac Toe",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = when {
                result == "X" -> "You Win! (Wait, that shouldn't happen)"
                result == "O" -> "AI Wins!"
                result == "Draw" -> "It's a Draw!"
                else -> "Your Turn (X)"
            },
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            Column {
                for (i in 0 until 3) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (j in 0 until 3) {
                            val index = i * 3 + j
                            Cell(
                                value = board[index],
                                onClick = { onCellClick(index) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { resetGame() }) {
            Text("Restart Game")
        }
    }
}

@Composable
fun Cell(value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Color.Gray),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = value,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (value == "X") Color.Blue else Color.Red
            )
        }
    }
}
