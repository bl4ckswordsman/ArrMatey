package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.state.IndexersState
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel

@Composable
fun ProwlarrIndexersContent(
    modifier: Modifier = Modifier,
    viewModel: ProwlarrIndexersViewModel
) {
    val indexersState by viewModel.indexers.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        when (val state = indexersState) {
            is IndexersState.Initial,
            is IndexersState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is IndexersState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is IndexersState.Success -> {
                if (state.items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No indexers configured",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item { Spacer(modifier = Modifier.size(4.dp)) }
                        items(items = state.items, key = { it.id }) { indexer ->
                            IndexerCard(indexer = indexer)
                        }
                        item { Spacer(modifier = Modifier.size(4.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexerCard(indexer: ProwlarrIndexer) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = indexer.name ?: indexer.implementationName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val protocol = indexer.protocol?.lowercase() ?: "unknown"
                val protocolColor = when (protocol) {
                    "torrent" -> MaterialTheme.colorScheme.primary
                    "usenet" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                }
                Text(
                    text = protocol.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = protocolColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (indexer.enable) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (indexer.enable)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (indexer.supportsRss) {
                    Icon(
                        imageVector = Icons.Default.RssFeed,
                        contentDescription = "RSS",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (indexer.supportsSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = mokoString(MR.strings.search),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!indexer.message.isNullOrBlank()) {
                Text(
                    text = indexer.message!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
