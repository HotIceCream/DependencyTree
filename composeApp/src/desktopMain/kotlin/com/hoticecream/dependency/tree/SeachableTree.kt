package com.hoticecream.dependency.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun HighlightedTree(
    node: DependencyNode,
    searchQuery: String,
    initiallyExpanded: Boolean = false,
    level: Int = 0,
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    var isSelected by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.padding(start = (level * 24).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusable(
                    enabled = true,
                    interactionSource = interactionSource
                )
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown &&
                           keyEvent.key == Key.DirectionRight -> {
                            isExpanded = true
                            true
                        }
                        keyEvent.type == KeyEventType.KeyDown &&
                           keyEvent.key == Key.DirectionLeft -> {
                            isExpanded = false
                            true
                        }
                        else -> false
                    }
                }
                .clickable {
                    isExpanded = !isExpanded
                    isSelected = true
                }
                .padding(8.dp)
                .background(
                    if (isFocused || isSelected)
                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    else
                        Color.Transparent
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.children.isNotEmpty()) {
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowDown
                    else
                        Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Spacer(Modifier.width(24.dp))
            }

            Column {
                HighlightedText(
                    text = node.name,
                    searchQuery = searchQuery,
                    style = MaterialTheme.typography.subtitle1
                )
                HighlightedText(
                    text = node.version,
                    searchQuery = searchQuery,
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                )
            }
        }

        if (isExpanded) {
            node.children.forEach { child ->
                HighlightedTree(
                    node = child,
                    searchQuery = searchQuery,
                    initiallyExpanded = true,
                    level = level + 1
                )
            }
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    searchQuery: String,
    style: TextStyle,
    highlightColor: Color = MaterialTheme.colors.primary,
) {
    if (searchQuery.isEmpty()) {
        Text(text = text, style = style)
        return
    }

    val parts = text.split(
        searchQuery,
        ignoreCase = true
    )

    Row {
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                Text(
                    text = text.substring(
                        text.indexOf(searchQuery, ignoreCase = true),
                        text.indexOf(searchQuery, ignoreCase = true) + searchQuery.length
                    ),
                    style = style.copy(
                        background = highlightColor.copy(alpha = 0.3f),
                        color = highlightColor
                    )
                )
            }
            Text(text = part, style = style)
        }
    }
}

// Statistics panel
@Composable
fun SearchStatistics(
    originalCount: Int,
    filteredCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Total dependencies: $originalCount")
        Text("Matching dependencies: $filteredCount")
    }
}

// Complete implementation combining all features
@Composable
fun ComprehensiveSearchableTree(
    rootNodes: List<DependencyNode>,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var filteredNodes by remember { mutableStateOf(rootNodes) }

    LaunchedEffect(searchQuery) {
        filteredNodes = rootNodes.mapNotNull { it.filterBySearch(searchQuery) }
    }

    Column(modifier = modifier) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text("Search dependencies") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear search")
                    }
                }
            },
            singleLine = true
        )

        // Statistics
        SearchStatistics(
            originalCount = countTotalNodes(rootNodes),
            filteredCount = countTotalNodes(filteredNodes)
        )

        // Tree view
        ScrollableColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredNodes.isEmpty() && searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching dependencies found")
                }
            } else {
                filteredNodes.forEach { node ->
                    HighlightedTree(
                        node = node,
                        searchQuery = searchQuery,
                        initiallyExpanded = searchQuery.isNotEmpty()
                    )
                }
            }
        }
    }
}

// Updated search filter
fun DependencyNode.filterBySearch(query: String): DependencyNode? {
    if (query.isEmpty()) return this

    val matchesQuery = name.contains(query, ignoreCase = true) ||
       originalVersion.contains(query, ignoreCase = true) ||
       (targetVersion?.contains(query, ignoreCase = true) ?: false)

    val filteredChildren = children.mapNotNull { it.filterBySearch(query) }

    return when {
        matchesQuery || filteredChildren.isNotEmpty() -> DependencyNode(
            name = name,
            originalVersion = originalVersion,
            targetVersion = targetVersion,
            children = filteredChildren.toMutableList()
        )

        else -> null
    }
}

// Helper function to count total nodes in tree
fun countTotalNodes(nodes: List<DependencyNode>): Int {
    return nodes.sumOf { node ->
        1 + countTotalNodes(node.children)
    }
}
