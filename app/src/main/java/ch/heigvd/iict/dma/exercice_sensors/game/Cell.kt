package ch.heigvd.iict.dma.exercice_sensors.game

class Cell(val col: Int, val row: Int) {
    var visited = false
    var topWall = true
    var bottomWall = true
    var leftWall = true
    var rightWall = true
}