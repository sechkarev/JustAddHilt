import java.io.File

class ComputeBaseTestPath {

    operator fun invoke(): String {
        return File(testDataFolder).absolutePath
    }

    companion object {
        val testDataFolder = "testdata"
    }
}