import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.effect.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.util.Duration
import tornadofx.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.random.Random

class DotController(
    private val background: AnchorPane,
    private val dotCount: Int,
    private val cmCount: Int
) : View() {
    private val dots = ArrayList<NormalDot>()
    private val cm = ArrayList<CMDot>()
    private var run = true
    private var pause = false
    private var colorChangePercentage = 90.0
    private var dotGeneratePercentage = 40.0
    private var cmColorChangePercentage = 95.0
    private var tick = 100.0
    private var dotRadius = 5.0
    private var cmRadius = 15.0
    private var debug = true

    override val root: AnchorPane by fxml("ui/ControlPanel.fxml")
    private val dotsPercentage: Label by fxid("dots_percentage_text")
    private val cmColorChange: Button by fxid("dot_cm_change")
    private val debugCheckBox: CheckBox by fxid("debug")
    private val pauseCheckBoc: CheckBox by fxid("pause")
    private val dotsProgress: ProgressBar by fxid("dots_percentage_progress")
    private val dotsCounter: Label by fxid("dots_count")
    private val fullScreen: Button by fxid("full_screen")
    private val add100: Button by fxid("dot_add_100")
    private val add10: Button by fxid("dot_add_10")
    private val add1: Button by fxid("dot_add_1")
    private val rem1: Button by fxid("dot_rem_1")
    private val rem10: Button by fxid("dot_rem_10")
    private val rem100: Button by fxid("dot_rem_100")
    private val tickSlider: Slider by fxid("tick_slide")
    private val tickCounter: Label by fxid("tick")

    private val dotsLimit = 10000

    init {
        val dotsMin = cmCount
        add100.setOnAction { addDots(100, dotsLimit) }
        add10.setOnAction { addDots(10, dotsLimit) }
        add1.setOnAction { addDots(1, dotsLimit) }
        rem100.setOnAction { remDots(100, dotsMin) }
        rem10.setOnAction { remDots(10, dotsMin) }
        rem1.setOnAction { remDots(1, dotsMin) }
        cmColorChange.setOnAction {
            cm.forEach { cmDot ->
                changeGradient(cmDot)
            }
        }
        debugCheckBox.setOnAction {
            debug = debugCheckBox.isSelected
        }
        pauseCheckBoc.setOnAction {
            pause = pauseCheckBoc.isSelected
        }
        fullScreen.setOnAction {
            primaryStage.isFullScreen = !primaryStage.isFullScreen
            relocateDots()
        }
        tickCounter.text = tick.roundToInt().toString()
        tickSlider.value = tick
        tickSlider.setOnMouseReleased {
            tick = tickSlider.value
        }
        tickSlider.setOnMouseClicked {
            tickCounter.text = tick.roundToInt().toString()
        }

    }

    private fun addDots(count: Int, limit: Int) {
        for (i in 0 until if (dots.size + count > limit)
            limit - dots.size
        else
            count
        )
            dotCreate()
    }

    private fun remDots(count: Int, limit: Int) {
        for (i in 0 until if (dots.size - count > limit)
            (limit - count).absoluteValue
        else
            count
        )
            dots[i].kill()
    }

    private fun changeGradient(cmDot: Dot) {
        cmDot.fill = getGradientColor()
    }

    private fun build() {

        for (i in 0 until dotCount) {
            dots.add(NormalDot(dotRadius, background.width / 2.0, background.height / 2.0))
            background.add(dots.last())
            //dots.last().effect = MotionBlur()
        }

        for (i in 0 until cmCount) {
            cm.add(CMDot(cmRadius, background.width / 2.0, background.height / 2.0))
            cm.last().stroke = Color.BLACK
            background.add(cm.last())
        }

        cm.forEach { cmDot ->
            cmDot.fill = getGradientColor()
            /*Color.rgb(
                Random.nextInt(0, 255),
                Random.nextInt(0, 255),
                Random.nextInt(0, 255)
            )*/
            //cmDot.effect = Lighting()
        }
    }

    fun relocateDots() {
        dots.forEach {
            if (it.translateX > background.width || it.translateX < 0 || it.translateY > background.height || it.translateY < 0)
                it.move(
                    Duration(tick),
                    Point2D(
                        when {
                            it.translateX > background.width -> background.width - 50.0
                            it.translateX < 0 -> 50.0
                            else -> 0.0
                        },
                        when {
                            it.translateY > background.height -> background.height - 50.0
                            it.translateY < 0 -> 50.0
                            else -> 0.0
                        }
                    )
                )
        }
        cm.forEach {
            if (it.translateX > background.width || it.translateX < 0 || it.translateY > background.height || it.translateY < 0)
                it.move(
                    Duration(tick),
                    Point2D(
                        when {
                            it.translateX > background.width -> background.width - 50.0
                            it.translateX < 0 -> 50.0
                            else -> 0.0
                        },
                        when {
                            it.translateY > background.height -> background.height - 50.0
                            it.translateY < 0 -> 50.0
                            else -> 0.0
                        }
                    )
                )
        }
    }

    override fun onUndock() {
        super.onUndock()
        run = false
        System.exit(0)
    }

    fun dest() {
        run = false
    }

    fun start() {
        openWindow()
        if (cm.isEmpty() || dots.isEmpty())
            build()
        thread {
            val deadDots = ArrayList<Int>()
            while (run) {
                Thread.sleep(tick.roundToLong() + 1)

                runLater {
                    randomDotCreate()

                    // move cm's
                    cm.forEach {
                        if (Random.nextDouble(100.0) > cmColorChangePercentage)
                            it.fill = getGradientColor()
                        generateMove(it, tick)//, cmRngX, cmRngY)
                        //randomDotCreate()
                    }

                    // move dots and set dot color to closest cm's color
                    for (i in 0 until dots.size) {
                        generateMove(dots[i], tick)//, dotRngX, dotRngY)
                        if (dots[i].isDead()) {
                            deadDots.add(i)
                            if (debug)
                                dots[i].fill = Color.RED
                        } else
                            dots[i].fill = cm[closestTo(dots[i])].fill

                        //randomDotCreate()
                    }

                    // kill and remove dots
                    deadDots.sort()
                    for (i in deadDots.size - 1 downTo 0) {
                        background.children.remove(dots[i])
                        dots.remove(dots[i])
                    }
                    deadDots.clear()
                    runLater {
                        dotsCounter.text = dots.size.toString()
                        dotsPercentage.text = "${((dots.size / dotsLimit.toDouble()) * 100.0).roundToInt()}%"
                        dotsProgress.progress = (dots.size / dotsLimit.toDouble())

                        //dCount.text = "Balls: ${dots.size}"
                    }
                }
                while (pause)
                    sleep(1)
            }
        }
    }

    private fun getGradientColor(): LinearGradient = LinearGradient(
        0.0,
        0.0,
        1.0,
        0.0,
        true,
        CycleMethod.NO_CYCLE,
        Stop(
            0.0,
            Color.color(
                Random.nextDouble(0.0, 1.0),
                Random.nextDouble(0.0, 1.0),
                Random.nextDouble(0.0, 1.0)
            )
        ),
        Stop(
            1.0,
            Color.color(
                Random.nextDouble(0.0, 1.0),
                Random.nextDouble(0.0, 1.0),
                Random.nextDouble(0.0, 1.0)
            )
        )

    )

    private fun randomDotCreate() {
        if (Random.nextDouble(100.0) > dotGeneratePercentage)
            dotCreate()
    }

    private fun dotCreate() {
        val dotto =
            NormalDot(dotRadius, Random.nextDouble(50.0, background.width - 50.0), Random.nextDouble(50.0, background.height - 50.0))
        if (debug)
            dotto.fill = Color.GREEN
        //println("NEW at %.2f %.2f".format(dots.last().translateX, dots.last().translateY))
        runLater {
            val cCount = background.children.count()
            background.addChildIfPossible(dotto, 0)
            if (background.children.count() > cCount) {
                dots.add(dotto)
                //dots.last().effect = MotionBlur()
            }
            //background.add(dots.last())
        }
    }

    private fun generateMove(dot: Dot, duration: Double) {//, rngFrom: Double, rngTo: Double) {
        //val rngX = Random.nextDouble(rngFrom, rngTo)
        //val rngY = Random.nextDouble(rngFrom, rngTo)
        dot.move(background, duration)//, rngX, rngY)
    }

    private fun closestTo(dot: Dot): Int {
        //println("Calc closest")
        var index = Random.nextInt(cm.size)
        var closestValue = Double.MAX_VALUE
        if (Random.nextDouble(100.0) < colorChangePercentage)
            cm.forEach {
                val tmpX = (dot.translateX - it.translateX).pow(2)
                val tmpY = (dot.translateY - it.translateY).pow(2)
                if (sqrt(tmpX + tmpY) < closestValue) {
                    closestValue = tmpX + tmpY
                    index = cm.indexOf(it)
                }
            }
        return index
    }
}
