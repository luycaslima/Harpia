package com.luycaslima.harpia.ui.presentation.feature.capture.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun CapturedThumbnail (
    bitmap: Bitmap,
    index : Int,
    modifier: Modifier = Modifier,
    size : Dp = 64.dp,
    cornerRadius : Dp = 8.dp
){
    /*
    * TODO Scale down or decode the bitmap at a smaller sample size (BitmapFactory.Options.inSampleSize or ThumbnailUtils.extractThumbnail)
    *  on a background thread before passing it to . Rotate also the image
    * */
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap()  }
    val index = remember(index) {  index } // Am I passing this state correctly? It need to change when i insert a new image
    val shape = RoundedCornerShape(cornerRadius)

    Image(
        bitmap = imageBitmap,
        contentDescription = index.toString(),
        contentScale = ContentScale.Crop,
        modifier =  modifier.size(size)
            .clip(shape)
            .border(BorderStroke(1.dp,Color.LightGray),shape)
    )
}