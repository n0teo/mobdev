package io.github.mobdev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun MessagesScreen(viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.lastOrNull()?.id) {
        if (state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.closeChannel() }) {
                Text(stringResource(R.string.back))
            }
            Text(
                text = state.selectedChannel ?: "",
                style = MaterialTheme.typography.titleMedium
            )
        }
        HorizontalDivider()

        if (state.canLoadMore && state.messages.isNotEmpty()) {
            TextButton(
                onClick = { viewModel.loadOlderMessages() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.load_more))
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(state.messages) { message ->
                MessageItem(message = message, viewModel = viewModel)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.messageText,
                onValueChange = { viewModel.onMessageTextChange(it) },
                placeholder = { Text(stringResource(R.string.message_hint)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.sendMessage() },
                enabled = state.messageText.isNotBlank() && state.isOnline
            ) {
                Text(stringResource(R.string.send))
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, viewModel: ChatViewModel) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = message.from,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )

        val text = message.data.text
        val image = message.data.image?.link

        if (text != null) {
            Text(text = text.text)
        } else if (image != null) {
            AsyncImage(
                model = BASE_URL + "thumb/" + image,
                contentDescription = stringResource(R.string.image_description),
                modifier = Modifier
                    .size(150.dp)
                    .clickable { viewModel.openImage(image) }
            )
        }
    }
}
