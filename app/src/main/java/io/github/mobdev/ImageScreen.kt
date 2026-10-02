package io.github.mobdev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage

@Composable
fun ImageScreen(viewModel: ChatViewModel, link: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = BASE_URL + "img/" + link,
            contentDescription = stringResource(R.string.image_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        TextButton(
            onClick = { viewModel.closeImage() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(text = stringResource(R.string.close), color = Color.White)
        }
    }
}
