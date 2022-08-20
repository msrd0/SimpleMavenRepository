package io.puharesource.simplemavenrepo

import com.google.gson.*
import java.io.File
import spark.Spark.*
import spark.resource.AbstractFileResolvingResource
import spark.staticfiles.MimeType

internal val gson : Gson = GsonBuilder().setPrettyPrinting().create()

private class FileName(private val name: String): AbstractFileResolvingResource() {
    override fun getFilename() = name
    
    override fun getInputStream() = throw UnsupportedOperationException()
    override fun getDescription() = throw UnsupportedOperationException()
}

internal fun mimeType(filename: String) = MimeType.fromResource(FileName(filename))

fun main() {
    // Load config file
    val config = Config.loadConfig(File("config.json"))

    // Create directories / files
    val repositoryDirectory = File(config.storagePath)
    val tmpDirectory = File("tmp")
    val usersFile = File("users.json")
    val defaultPomFile = File("default_pom.xml")

    if (!repositoryDirectory.exists()) {
        repositoryDirectory.mkdirs()
    }

    if (!tmpDirectory.exists()) {
        repositoryDirectory.mkdirs()
    }

    if (!defaultPomFile.exists()) {
        defaultPomFile.createNewFile()

        defaultPomFile.writeText(Authentication::class.java.getResourceAsStream("/default_pom.xml")!!.bufferedReader().readText())
    }

    port(config.port)

    Routes.registerRoutes(
            config = config,
            authentication = Authentication(usersFile),
            tmpDirectory = tmpDirectory,
            repositoryDirectory = repositoryDirectory,
            adminCss = Authentication::class.java.getResourceAsStream("/admin.css")!!.bufferedReader().readText(),
            defaultPom = defaultPomFile.readText()
    )
}
