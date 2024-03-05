import javafx.scene.control.PopupControl.USE_COMPUTED_SIZE
import javafx.scene.layout.Priority
import tornadofx.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.*



// TÄLLÄ HETKELLÄ EI KÄYTÖSSÄ EIKÄ VÄLTTÄMÄTTÄ TULE OLLENKAAN
class AdminView : View("Admin Panel") {
    // Tekstikenttä hakua varten
    val searchTextField = textfield()

    val categoryTextField = textfield()

    // Lista koodinpätkistä
    val snippets = mutableListOf<String>().asObservable()

    // Tietokantayhteys
    private lateinit var connection: Connection

    init {
        // Ladataan tietokannan yhteystiedot properties-tiedostosta
        val properties = Properties()
        val propertiesStream = AdminView::class.java.getResourceAsStream("/dbconfig.properties")
        properties.load(propertiesStream)

        val url = properties.getProperty("url")
        val username = properties.getProperty("username")
        val password = properties.getProperty("password")

        connection = DriverManager.getConnection(url, username, password)

        // Haetaan aluksi kaikki koodinpätkät ja päivitetään lista
        refreshSnippetList()

        // Päivitetään lista myös hakukentän tekstin muuttuessa
        searchTextField.textProperty().addListener { _, _, newValue ->
            searchSnippets(newValue)
        }
    }

    private fun refreshSnippetList() {
        val sql = "SELECT content FROM snippet"
        val preparedStatement = connection.prepareStatement(sql)
        val resultSet = preparedStatement.executeQuery()
        snippets.clear()
        while (resultSet.next()) {
            snippets.add(resultSet.getString("content"))
        }
    }

    private fun searchSnippets(keyword: String) {
        if (keyword.isBlank()) {
            refreshSnippetList()
            return
        }

        val sql = "SELECT content FROM snippet WHERE content LIKE ?"
        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setString(1, "%$keyword%")
        val resultSet = preparedStatement.executeQuery()
        snippets.clear()
        while (resultSet.next()) {
            snippets.add(resultSet.getString("content"))
        }
    }

    override val root = borderpane {
        center {
            vbox(10) {
                paddingAll = 20

                label("Admin Panel")

                hbox(10) {
                    label("Haku:")
                    add(searchTextField)
                }

                listview(snippets) {
                    vgrow = Priority.ALWAYS
                    prefHeight = USE_COMPUTED_SIZE
                }

                button("Poista koodinpätkä") {
                    action {
                        val selectedItem = snippets.firstOrNull { it == searchTextField.text }
                        selectedItem?.let { snippet ->
                            // Poistetaan koodinpätkä tietokannasta
                            val sql = "DELETE FROM snippet WHERE content = ?"
                            val preparedStatement = connection.prepareStatement(sql)
                            preparedStatement.setString(1, snippet)
                            preparedStatement.executeUpdate()
                            println("Koodinpätkä poistettu: $snippet")
                            // Päivitetään lista poiston jälkeen
                            refreshSnippetList()
                        }
                    }
                }

                button("Päivitä koodinpätkä") {
                    action {
                        val selectedItem = snippets.firstOrNull { it == searchTextField.text }
                        selectedItem?.let { snippet ->
                            val category = categoryTextField.text
                            // Päivitetään koodinpätkän kategoria tietokannassa
                            val sql = "UPDATE snippet SET category = ? WHERE content = ?"
                            val preparedStatement = connection.prepareStatement(sql)
                            preparedStatement.setString(1, category)
                            preparedStatement.setString(2, snippet)
                            preparedStatement.executeUpdate()
                            println("Koodinpätkän kategoria päivitetty: $snippet, uusi kategoria: $category")
                            // Tyhjennetään tekstikentät päivityksen jälkeen
                            searchTextField.clear()
                            categoryTextField.clear()
                            // Päivitetään lista päivityksen jälkeen
                            refreshSnippetList()
                        }
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
