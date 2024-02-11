package com.letit0or1.kawa.nopong

import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.util.Vector
import kotlin.math.sqrt

fun log(message: String) = Log.e("PongTable", message)
const val SIZE = 30
val originalState: Array<Array<MutableState<Int>>>
    get() {
        val newArray = Array(SIZE) {
            if (it < (SIZE / 2)) {
                Array(SIZE) { mutableStateOf(0) }
            } else {
                Array(SIZE) { mutableStateOf(1) }
            }
        }
        return newArray
    }

data class PongUnit(
    var rect: LayoutCoordinates? = null,
    val side: MutableState<Int>
)

val Int.toDp get() = (this / Resources.getSystem().displayMetrics.density).toInt()

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Offset.normalize(): Offset {
    val magnitude = sqrt(this.x * this.x + this.y * this.y)
    return Offset(this.x / magnitude, this.y / magnitude)
}
