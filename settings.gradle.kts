import java.nio.file.Files
import java.nio.file.Paths

rootProject.name = "daycold"

val root = Paths.get(rootProject.projectDir.absolutePath)!!
Files.walk(root)
    .filter { it.parent != root }
    .filter { it.fileName.endsWith("build.gradle.kts") }
    .forEach {
        val currentName = it.parent.fileName.toString()
        val name = if (it.parent.parent == root) {
            currentName
        } else {
            "${it.parent.parent.fileName}:$currentName"
        }
        include(name)
    }
