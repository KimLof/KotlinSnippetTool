import javafx.application.Platform
import tornadofx.*
import java.sql.DriverManager
import java.util.*

class MainView : View("Snippet Tool") {

    private val statusLabel = label() {
        paddingAll = 10
    }

    // Tekstikentät koodinpätkän ja kategorian syöttämistä varten
    val snippetTextField = textfield {
        promptText = "Koodinpätkä"
        textProperty().addListener { _, _, newValue ->
            // Suorita haku uudelleen jokaisen kirjoitetun merkin jälkeen
            if (newValue != null) {
                Platform.runLater {
                    searchDatabase(newValue)
                }
            }
        }
    }
    val categoryTextField = textfield {
        promptText = "Kategoria"
        textProperty().addListener { _, _, newValue ->
            // Suorita haku uudelleen jokaisen kirjoitetun merkin jälkeen
            if (newValue != null) {
                Platform.runLater {
                    searchDatabase(newValue)
                }
            }
        }
    }



    // Taulukko hakutuloksille
    val searchResults = mutableListOf<Pair<String, String>>().observable()

    // Tietokantayhteys
    private lateinit var connection: java.sql.Connection

    // Valittu koodinpätkä muokkausta varten
    private var selectedSnippet: Pair<String, String>? = null

    // Muokkaus tila
    private var editMode = false

    // Boolean property editMode käyttämiseksi tallennusnapin näkyvyyden säätämiseen
    private val editModeProperty = booleanProperty()

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

    fun searchDatabase(searchKeyword: String) {
        // Haetaan koodinpätkiä hakusanalla tietokannasta
        val sql = "SELECT * FROM snippet WHERE content LIKE ? OR category LIKE ?"
        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setString(1, "%$searchKeyword%")
        preparedStatement.setString(2, "%$searchKeyword%")
        val resultSet = preparedStatement.executeQuery()

        // Tyhjennetään hakutulokset
        searchResults.clear()

        // Lisätään hakutulokset listaan
        while (resultSet.next()) {
            val content = resultSet.getString("content")
            val category = resultSet.getString("category")
            searchResults.add(Pair(content, category))
        }
    }

    override val root = borderpane {
        center {
            hbox(10) {
                vbox {
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

                    button("Lisää") {
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
                }

                listview(searchResults) {
                    cellFormat {
                        text = it.first
                    }


                    // Asetetaan tapahtumankäsittelijä, joka päivittää koodinpätkän tiedot
                    // kun käyttäjä valitsee koodinpätkän listasta
                    selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                        if (searchResults.isNotEmpty() && newValue != null) {
                            snippetTextField.text = newValue.first
                            categoryTextField.text = newValue.second
                            selectedSnippet = newValue
                            editMode = true
                        }
                    }
                }



                searchDatabase("");

                button("Muokkaa") {
                    action {
                        if (editModeProperty.value) {
                            println("Muokkaus pois päältä")
                            statusLabel.text = ""
                            editModeProperty.value = false // Poista muokkaustila päältä
                        } else {
                            if (selectedSnippet != null) {
                                statusLabel.text = "Muokkaus päällä"
                                editModeProperty.value = true
                            } else {
                                println("Valitse muokattava koodinpätkä")
                            }
                        }
                    }
                }

                button("Tallenna") {
                    action {
                        if (editMode) {
                            val snippet = snippetTextField.text
                            val category = categoryTextField.text
                            // Päivitetään koodinpätkän tiedot tietokantaan
                            val sql = "UPDATE snippet SET content = ?, category = ? WHERE content = ?"
                            val preparedStatement = connection.prepareStatement(sql)
                            preparedStatement.setString(1, snippet)
                            preparedStatement.setString(2, category)
                            preparedStatement.setString(3, selectedSnippet!!.first)
                            preparedStatement.executeUpdate()
                            println("Koodinpätkän tiedot päivitetty: $snippet, kategoria: $category")
                            // Tyhjennetään tekstikentät tallennuksen jälkeen
                            snippetTextField.clear()
                            categoryTextField.clear()
                            selectedSnippet = null
                            editMode = false
                            editModeProperty.value = false // Poista muokkaustila päältä
                        } else {
                            println("Valitse ensin koodinpätkä ja paina Muokkaa ennen Tallenna-painiketta")
                        }
                    }
                    visibleWhen(editModeProperty)
                }

            }
        }

        top {
            // Lisätään statusLabel yläreunaan
            add(statusLabel)
        }

    }

    override fun onUndock() {
        // Suljetaan tietokantayhteys sovelluksen sulkemisen yhteydessä
        connection.close()
    }
}
