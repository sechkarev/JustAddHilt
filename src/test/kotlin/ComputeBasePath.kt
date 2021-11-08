import java.io.File

class ComputeBasePath {

    operator fun invoke(): String {
        return File(testDataFolder).absolutePath
    }

    companion object {
        val testDataFolder = "testdata"
    }
}