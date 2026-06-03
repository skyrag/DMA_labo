package ch.heigvd.iict.dma.exercice_sensors

data class Cell(
    val col: Int, val row: Int,
    var topWall: Boolean = true,
    var bottomWall: Boolean = true,
    var leftWall: Boolean = true,
    var rightWall: Boolean = true,
    var visited: Boolean = false
)