package com.luycaslima.harpia.ui.presentation.feature.capture.components.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luycaslima.harpia.data.CapturedFrame

@Composable
fun DebugInfoCard(statusMessage : String, captureCount : Int ,lastCapturedFrame: CapturedFrame?){
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth(0.85f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Total Captures: $captureCount", color = Color.Green, fontSize = 16.sp)
            Text("Status: $statusMessage", color = Color.White, fontSize = 14.sp)

            lastCapturedFrame?.let { info ->
                Spacer(modifier = Modifier.height(6.dp))
                Text("File: ${info.filename}", color = Color.Yellow, fontSize = 12.sp)
                Text(
                    "Pos (XYZ): %.3f, %.3f, %.3f".format(
                        info.positionX,
                        info.positionY,
                        info.positionZ
                    ),
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
                Text(
                    "Rot (Q): %.2f, %.2f, %.2f, %.2f".format(
                        info.rotationQx,
                        info.rotationQy,
                        info.rotationQz,
                        info.rotationQw
                    ),
                    color = Color.Cyan,
                    fontSize = 12.sp
                )
                Text(
                    "Focal (fx, fy): %.1f, %.1f".format(info.focalLengthX, info.focalLengthY),
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}