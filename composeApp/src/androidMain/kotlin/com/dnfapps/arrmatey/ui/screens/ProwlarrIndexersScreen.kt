package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersState
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

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
            is ProwlarrIndexersState.Initial,
            is ProwlarrIndexersState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProwlarrIndexersState.Error -> {
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

            is ProwlarrIndexersState.Success -> {
                if (state.items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mokoString(MR.strings.no_indexers_configured),
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
                    text = indexer.name ?: indexer.implementationName ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val protocol = indexer.protocol?.lowercase() ?: mokoString(MR.strings.unknown).lowercase()
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
                val dotColor = if (indexer.enable)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (indexer.enable) mokoString(MR.strings.enabled) else mokoString(MR.strings.disabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = dotColor
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

            indexer.message?.message?.let { msg ->
                if (msg.isNotBlank()) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (indexer.message?.type?.lowercase()) {
                            "warning" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}
