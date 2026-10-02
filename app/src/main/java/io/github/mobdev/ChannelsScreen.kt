package io.github.mobdev

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ChannelsScreen(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.channels_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { viewModel.logout() }) {
                Text(stringResource(R.string.logout))
            }
        }
        HorizontalDivider()

        LazyColumn {
            items(state.channels) { channel ->
                val isSelected = channel == state.selectedChannel
                val background = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }

                Text(
                    text = channel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(background)
                        .clickable { viewModel.openChannel(channel) }
                        .padding(16.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
