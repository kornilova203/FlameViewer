package com.github.korniloval.flameviewer.converters

import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Generates random tree that contains given amount of nodes
 * @param nodesCount number of nodes in generated tree
 * @param numberOfMethodNames number of different method names in the tree
 */
class TreeGenerator(nodesCount: Int, numberOfMethodNames: Int = 100) {
    val root: Node

    init {
        if (numberOfMethodNames < 1) {
            throw IllegalArgumentException("Number of method names must be bigger that 1. It is $numberOfMethodNames")
        }
        val random = Random()
        val nodes = ArrayList<Node>()
        root = Node("")
        nodes.add(root)
        for (i in 0 until nodesCount) {
            val parent = nodes[random.nextInt(nodes.size)]
            var name = generateName(random, numberOfMethodNames)
            while (parent.children.any { it.name == name }) { // there should be no children with identical names
                name = generateName(random, numberOfMethodNames)
            }
            val newChild = Node(name)
            parent.children.add(newChild)
            nodes.add(newChild)
        }
        sortChildrenByNameRecursively(root)
    }

    private fun sortChildrenByNameRecursively(root: Node) {
        root.children.sortBy { it.name }
        for (child in root.children) {
            sortChildrenByNameRecursively(child)
        }
    }

    private fun generateName(random: Random, numberOfMethodNames: Int): String = "fun${random.nextInt(numberOfMethodNames)}()"

    fun outputFlamegraph(file: File) {
        file.writeText("")
        outputFlamegraphRecursively(root, file, ArrayList())
    }

    private fun outputFlamegraphRecursively(node: Node, file: File, currentStack: ArrayList<Node>) {
        currentStack.add(node)
        if (node.children.isEmpty()) { // if node is leaf then output current stack to file
            val stack = currentStack
                    .map { it.name }
                    .filter { !it.isEmpty() } // remove base node
                    .joinToString(";")
            file.appendText("$stack 1\n")
        } else {
            for (child in node.children) {
                outputFlamegraphRecursively(child, file, currentStack)
            }
        }
        currentStack.removeAt(currentStack.size - 1)
    }

    data class Node(val name: String, val children: MutableList<Node> = ArrayList())
}