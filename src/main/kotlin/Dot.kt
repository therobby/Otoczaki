import javafx.geometry.Point2D
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.Circle
import javafx.util.Duration
import tornadofx.move
import tornadofx.runLater
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.random.Random

open class Dot(
    radius: Double,
    startX: Double = 0.0,
    startY: Double = 0.0
) : Circle(radius) {
    protected var alive = 0
    private var vector = newVector()

    init {
        //println("$startX  $startY")
        layoutX = 0.0
        layoutY = 0.0
        translateX = startX
        translateY = startY
        /*fill = Color.rgb(
            Random.nextInt(0, 255),
            Random.nextInt(0, 255),
            Random.nextInt(0, 255)
        )*/
    }

    fun kill() {
        alive = Int.MAX_VALUE
    }

    open fun isDead(): Boolean = false

    private fun newVector() = Point2D(Random.nextDouble(-25.0, 25.0), Random.nextDouble(-25.0, 25.0))


    private fun checkVector(on: AnchorPane, duration: Double) {
        if (positionCheck(on, duration)) {
            vector = newVector()
            if(translateX + vector.x > on.width || translateY + vector.y > on.height ||
                translateX + vector.x < 0 || translateY + vector.y < 0
            )
                println("$translateX $translateY")
        }
    }

    private fun positionCheck(on: AnchorPane, duration: Double) : Boolean {
        return if(translateX + vector.x > on.width || translateY + vector.y > on.height ||
            translateX + vector.x < 0 || translateY + vector.y < 0
        ) {
            val movY = when {
                translateY < 0 -> 50.0
                translateY > on.height -> on.height - 50.0
                else -> translateY
            }

            val movX = when {
                translateX < 0 -> 50.0
                translateX > on.width -> on.width - 50.0
                else -> translateX
            }
            move(Duration(0.0), Point2D(movX, movY))
            true
        }
        else
            false
    }

    fun move(on: AnchorPane, duration: Double) {
        alive++
        //positionCheck(on.height, on.width)
        checkVector(on, duration)
        move(Duration(duration), Point2D(translateX + vector.x, translateY + vector.y))
    }
}