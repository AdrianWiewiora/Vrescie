package com.example.vresciecompose.data

data class Stats(
    var games: Int = 0,
    var winsIdPlayer1: Int = 0,
    var winsIdPlayer2: Int = 0
)

data class MoveData(
    val playerId: String = "",
    val positionX: Int = 0,
    val positionY: Int = 0
)