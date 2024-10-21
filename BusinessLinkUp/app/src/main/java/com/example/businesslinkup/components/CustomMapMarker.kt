package com.example.businesslinkup.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.platform.LocalContext


@Composable
fun CustomMapMarker(
    location: LatLng,
    title: String,
    type: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val bitmap = remember { createCustomMarkerBitmap(context, title, type, primaryColor) }

    Marker(
        state = MarkerState(position = location),
        icon = BitmapDescriptorFactory.fromBitmap(bitmap),
        onClick = {
            onClick()
            true
        }
    )
}

fun createCustomMarkerBitmap(
    context: android.content.Context,
    title: String,
    type: String,
    primaryColor: Int
): Bitmap {
    val width = 200
    val height = 80

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = primaryColor
        style = Paint.Style.FILL
    }

    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(rect, 20f, 20f, paint)

    paint.apply {
        color = Color.White.toArgb()
        textSize = 24f
        isAntiAlias = true
    }
    canvas.drawText(title, 20f, 30f, paint)

    paint.textSize = 18f
    canvas.drawText(type, 20f, 60f, paint)

    return bitmap
}
