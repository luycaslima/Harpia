package com.luycaslima.harpia.ui.presentation.feature.capture.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luycaslima.harpia.data.local.CapturedFrame



@Composable
fun FrameCarousel( frames : List<CapturedFrame>, modifier: Modifier ) {

    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items( frames.count()) { i ->
                val frame = frames[i]
                CapturedThumbnail(
                    bitmap = frame.thumbnailBitmap,
                    index = frame.index
                )
            }
    }
}