package io.github.mobdev

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource

@Composable
fun ChatApp(viewModel: ChatViewModel) {
    val state = viewModel.state
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    BackHandler(enabled = state.openedImage != null || state.selectedChannel != null) {
        if (state.openedImage != null) {
            viewModel.closeImage()
        } else {
            viewModel.closeChannel()
        }
    }

    MaterialTheme(colorScheme = colors) {
        Scaffold { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
            ) {
                val openedImage = state.openedImage

                if (openedImage != null) {
                    ImageScreen(viewModel, openedImage)
                } else if (!state.isLoggedIn) {
                    LoginScreen(viewModel)
                } else if (isLandscape) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        ChannelsScreen(viewModel, Modifier.weight(0.35f).fillMaxHeight())
                        VerticalDivider()
                        if (state.selectedChannel == null) {
                            Box(
                                modifier = Modifier.weight(0.65f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.select_chat))
                            }
                        } else {
                            MessagesScreen(viewModel, Modifier.weight(0.65f).fillMaxHeight())
                        }
                    }
                } else if (state.selectedChannel == null) {
                    ChannelsScreen(viewModel, Modifier.fillMaxSize())
                } else {
                    MessagesScreen(viewModel, Modifier.fillMaxSize())
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        val error = state.error
        if (error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissError() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                text = { Text(stringResource(error)) }
            )
        }
    }
}
