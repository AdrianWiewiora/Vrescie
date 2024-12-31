package com.example.vresciecompose.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TicTacToeGame(
    modifier: Modifier = Modifier,
    board: MutableState<Array<Array<String>>>,
    onCellClick: (Int, Int) -> Unit,
    listenForMoves: () -> Unit
) {
    // Rozpocznij nasłuchiwanie na ruchy, kiedy komponent jest aktywny
    LaunchedEffect(Unit) {
        listenForMoves()
    }

    Column(modifier = modifier) {
        board.value.forEachIndexed { rowIndex, row -> // Użyj board.value
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIndex, cell -> // Użyj board.value
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(1.dp, Color.Gray)
                            .clickable { onCellClick(rowIndex, colIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        when (cell) {
                            "X" -> Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Player X",
                                tint = Color.Blue,
                                modifier = Modifier.size(48.dp)
                            )
                            "O" -> Icon(
                                imageVector = Icons.Outlined.Circle,
                                contentDescription = "Player O",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}