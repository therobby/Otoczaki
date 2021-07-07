import tornadofx.*

fun main() {
    launch<Mniam>()
}

class Mniam : App(MainView::class)

class MainView : View() {
    //private val ballos = arrayListOf<Circle>()
    override val root = anchorpane{
        setMinSize(100.0,100.0)
        title = "Otoczaki"
        style {
            backgroundColor += c("black")
        }
        setPrefSize(800.0, 600.0)
    }
    private val dotController = DotController(root,500,3)

    override fun onDock() {
        super.onDock()
        //currentStage?.isResizable = false
        root.widthProperty().addListener { observable, oldValue, newValue ->
            onResize()
        }
        root.heightProperty().addListener { observable, oldValue, newValue ->
            onResize()
        }
        dotController.start()
    }

    override fun onUndock() {
        super.onUndock()
        dotController.dest()
    }

    private fun onResize(){
        dotController.relocateDots()
    }
}