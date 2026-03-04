package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.state.ProwlarrSearchState
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrSearchViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProwlarrSearchContent(
    modifier: Modifier = Modifier,
    viewModel: ProwlarrSearchViewModel
) {
    val searchState by viewModel.searchResults.collectAsStateWithLifecycle()
    val grabStatus by viewModel.grabStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var queryText by remember { mutableStateOf("") }
    var grabTarget by remember { mutableStateOf<ProwlarrSearchResult?>(null) }

    // Show snackbar on grab result then reset
    LaunchedEffect(grabStatus) {
        when (grabStatus) {
            is OperationStatus.Success -> {
                snackbarHostState.showSnackbar("Release sent to download client")
                viewModel.resetGrabStatus()
            }
            is OperationStatus.Error -> {
                val msg = (grabStatus as OperationStatus.Error).message ?: "Failed to grab release"
                snackbarHostState.showSnackbar(msg)
                viewModel.resetGrabStatus()
            }
            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for releases...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (queryText.isNotEmpty()) {
                        IconButton(onClick = {
                            queryText = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (queryText.isNotBlank()) viewModel.performSearch(queryText)
                    }
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = searchState) {
                is ProwlarrSearchState.Initial -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Search for releases across your indexers",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ProwlarrSearchState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProwlarrSearchState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = state.message, modifier = Modifier.padding(16.dp))
                    }
                }

                is ProwlarrSearchState.Success -> {
                    if (state.items.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.items,
                                key = { it.guid ?: it.title ?: it.hashCode() }
                            ) { result ->
                                SearchResultCard(
                                    result = result,
                                    isGrabbing = grabStatus is OperationStatus.InProgress &&
                                            grabTarget?.guid == result.guid,
                                    onGrab = { grabTarget = result }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Grab confirmation dialog
    grabTarget?.let { result ->
        AlertDialog(
            onDismissRequest = { if (grabStatus !is OperationStatus.InProgress) grabTarget = null },
            title = { Text(mokoString(MR.strings.grab_release_title)) },
            text = {
                Text(
                    text = stringResource(MR.strings.confirm_grab_release, result.title ?: "this release"),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.grabRelease(result)
                        grabTarget = null
                    },
                    enabled = grabStatus !is OperationStatus.InProgress
                ) {
                    if (grabStatus is OperationStatus.InProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(mokoString(MR.strings.grab))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { grabTarget = null },
                    enabled = grabStatus !is OperationStatus.InProgress
                ) {
                    Text(mokoString(MR.strings.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultCard(
    result: ProwlarrSearchResult,
    isGrabbing: Boolean,
    onGrab: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = result.title ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onGrab, enabled = !isGrabbing) {
                    if (isGrabbing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = mokoString(MR.strings.grab)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = result.indexer ?: "Unknown indexer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = result.protocol?.name ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (result.protocol) {
                        ReleaseProtocol.Torrent -> MaterialTheme.colorScheme.primary
                        ReleaseProtocol.Usenet -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )

                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text(
                    text = result.size.bytesAsFileSizeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text(
                    text = "${result.age}d",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (result.protocol == ReleaseProtocol.Torrent) {
                Row {
                    Text(
                        text = "↑${result.seeders ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "↓${result.leechers ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (result.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    result.categories.take(3).forEach { category ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(category.name ?: "Category ${category.id}") }
                        )
                    }
                }
            }
        }
    }
}
