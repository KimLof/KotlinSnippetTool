import tornadofx.*
import java.io.Console
import java.sql.DriverManager
import java.util.*

class MainView : View("Snippet Tool") {
    // Tekstikentät koodinpätkän ja kategorian syöttämistä varten
    val snippetTextField = textfield()
    val categoryTextField = textfield()

    // Tietokantayhteys
    private lateinit var connection: java.sql.Connection

    init {
        // Ladataan tietokannan yhteystiedot properties-tiedostosta
        val properties = Properties()
        val propertiesStream = MainView::class.java.getResourceAsStream("/dbconfig.properties")
        properties.load(propertiesStream)

        val url = properties.getProperty("url")
        val username = properties.getProperty("username")
        val password = properties.getProperty("password")

        connection = DriverManager.getConnection(url, username, password)
    }

    override val root = borderpane {
        center {
            vbox(10) {
                paddingAll = 20

                label("Tervetuloa Snippet Tooliin!")

                hbox(10) {
                    label("Koodinpätkä:")
                    add(snippetTextField)
                }

                hbox(10) {
                    label("Kategoria:")
                    add(categoryTextField)
                }

                button("Tallenna") {
                    action {
                        val snippet = snippetTextField.text
                        val category = categoryTextField.text
                        // Tallennetaan koodinpätkä ja kategoria
                        val sql = "INSERT INTO snippet (content, category) VALUES (?, ?)"
                        val preparedStatement = connection.prepareStatement(sql)
                        preparedStatement.setString(1, snippet)
                        preparedStatement.setString(2, category)
                        preparedStatement.executeUpdate()
                        println("Koodinpätkä tallennettu: $snippet, kategoria: $category")
                        // Tyhjennetään tekstikentät tallennuksen jälkeen
                        snippetTextField.clear()
                        categoryTextField.clear()
                    }
                }

                button("Hae") {
                    action {
                        val searchKeyword = snippetTextField.text
                        // Haetaan koodinpätkää hakusanalla tietokannasta
                        val sql = "SELECT * FROM snippet WHERE content LIKE ?"
                        val preparedStatement = connection.prepareStatement(sql)
                        preparedStatement.setString(1, "%$searchKeyword%")
                        val resultSet = preparedStatement.executeQuery()
                        // Tulostetaan hakutulokset
                        while (resultSet.next()) {
                            println("Koodinpätkä: ${resultSet.getString("content")}, Kategoria: ${resultSet.getString("category")}")
                        }
                    }
                }

                button("Hallinnoi") {
                    action {
                        // Toteuta hallintatoiminnallisuus tässä
                        println("Hallinnoidaan koodinpätkiä")
                        // Avaa hallintanäkymä tai tee tarvittavat toimenpiteet koodinpätkien hallintaan
                    }
                }
            }
        }
    }

    override fun onUndock() {
        // Suljetaan tietokantayhteys sovelluksen sulkemisen yhteydessä
        connection.close()
    }
}
