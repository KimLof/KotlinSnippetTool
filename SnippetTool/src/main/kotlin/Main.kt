import javafx.application.Application
import tornadofx.App

class Main : App(MainView::class)

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}