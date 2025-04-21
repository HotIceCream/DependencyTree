package com.hoticecream.dependency.tree

data class DependencyNode(
    val name: String,
    val originalVersion: String,
    val targetVersion: String? = null, // Version after "->"
    val children: MutableList<DependencyNode> = mutableListOf(),
) {

    val version: String get() = originalVersion + (targetVersion?.let { " -> $it" } ?: "")
}

fun parseDependencyTree(input: String): List<DependencyNode> {
    val lines = input.lines()
    val rootNodes = mutableListOf<DependencyNode>()
    val nodeStack = mutableListOf<Pair<Int, DependencyNode>>()  // (depth, node)

    fun getDepth(line: String): Int {
        return line.takeWhile { it == ' ' || it == '|' }.count { it == ' ' } / 3
    }

    fun parseDependencyLine(line: String): DependencyNode? {
        if (line.isBlank()) return null

        // Updated regex to capture both versions
        val regex = """[+\\]--- (.*?):(.*?):(.*?)(?:\s+->\s+(.*?))?(?:\s+\(\*\))?$""".toRegex()
        val match = regex.find(line.trim()) ?: return null

        val (group, artifact, originalVersion, targetVersion) = match.destructured

        return DependencyNode(
            name = "$group:$artifact",
            originalVersion = originalVersion,
            targetVersion = targetVersion.takeIf { it.isNotEmpty() }
        )
    }

    lines.forEach { line ->
        if (line.isBlank()) return@forEach

        val depth = getDepth(line)
        val node = parseDependencyLine(line) ?: return@forEach

        while (nodeStack.isNotEmpty() && nodeStack.last().first >= depth) {
            nodeStack.removeLast()
        }

        if (depth == 0) {
            rootNodes.add(node)
            nodeStack.clear()
            nodeStack.add(depth to node)
        } else {
            val parent = nodeStack.lastOrNull()?.second
            parent?.children?.add(node)
            nodeStack.add(depth to node)
        }
    }

    return rootNodes
}

// Helper function to print the tree (for debugging)
fun printDependencyTree(node: DependencyNode, level: Int = 0) {
    val indent = "  ".repeat(level)
    val versionText = if (node.targetVersion != null) {
        "${node.originalVersion} -> ${node.targetVersion}"
    } else {
        node.originalVersion
    }
    println("$indent${node.name}:$versionText")
    node.children.forEach { child ->
        printDependencyTree(child, level + 1)
    }
}

// Usage example
fun main() {
    val input = """
        +--- androidx.databinding:viewbinding:8.0.2 -> 8.5.2
        |    \--- androidx.annotation:annotation:1.0.0 -> 1.9.1
        |         \--- androidx.annotation:annotation-jvm:1.9.1
    """.trimIndent()

    val dependencies = parseDependencyTree(input)

    // Print the parsed tree
    dependencies.forEach { root ->
        printDependencyTree(root)
    }
}



