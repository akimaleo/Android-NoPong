package com.letit0or1.kawa.nopong.pong

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.letit0or1.kawa.nopong.PongUnit
import com.letit0or1.kawa.nopong.SIZE
import com.letit0or1.kawa.nopong.log
import com.letit0or1.kawa.nopong.normalize
import com.letit0or1.kawa.nopong.originalState
import com.letit0or1.kawa.nopong.pxToDp
import kotlin.random.Random


@Composable
fun PongTable(
    state: List<List<PongUnit>> = originalState.map {
        it.map { PongUnit(side = it) }
    }
) {
    log("PongTable init")
    var tableSize by remember { mutableStateOf(IntSize.Zero) }
    val boxSize = (tableSize.width / SIZE).pxToDp()

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { tableSize = it }
    ) {
        Row {
            state.forEach {
                Column {
                    it.forEach { pongUnit ->
                        HitBlock(pongUnit, boxSize)
                    }
                }

            }
        }

//        LEFT
        Ball(
            color = Color.Gray,
            ballSide = 0,
            state = state
        )
//        RIGHT
        Ball(
            color = Color.DarkGray,
            ballSide = 1,
            state = state
        )

    }
}

@Composable
private fun Ball(
    color: Color = Color.Black,
    ballSize: Dp = 15.dp,
    ballSide: Int,
    state: List<List<PongUnit>>
) {
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    val parentSize = layoutCoordinates?.parentLayoutCoordinates?.size?.toSize() ?: Size.Zero
    var tableSize = parentSize.width

    var ballOffset by remember {
        mutableStateOf(
            Offset(
                x = if (ballSide == 0) 0f else 1000f,
                y = tableSize / 2
            ),
        )
    }
    var movementVector by remember {
        mutableStateOf(
            Offset(
                Random.nextFloat(),
                Random.nextFloat()
            ).normalize()
        )
    }
    LaunchedEffect(layoutCoordinates.toString() + ballOffset.toString()) {
        ballOffset += movementVector.times(10f)

        var newVector = movementVector.copy()

        state.forEachIndexed { x, it ->
            it.forEachIndexed { y, pongUnit ->
                pongUnit.rect?.takeIf { pongUnit.side.value != ballSide }?.let {
                    with(it.boundsInRoot()) {
                        val collision =
                            detectCollision(layoutCoordinates?.boundsInRoot()!!, this)
                        newVector = when (collision) {
                            CollisionDirection.TOP -> newVector.copy(y = -1f)
                            CollisionDirection.BOTTOM -> newVector.copy(y = 1f)
                            CollisionDirection.LEFT -> newVector.copy(x = -1f)
                            CollisionDirection.RIGHT -> newVector.copy(x = 1f)

                            CollisionDirection.NONE -> movementVector
                        }
                        if (collision != CollisionDirection.NONE) {
                            log("collision: $collision")
                            log("before = $movementVector; new= $newVector")
                            pongUnit.side.value = ballSide
                            movementVector = newVector
                            return@forEachIndexed
                        }
                    }
                }
            }

        }
        if (ballOffset.x < 0) {
            newVector = newVector.copy(x = 1f)
        }
        if (ballOffset.x > tableSize) {
            newVector = newVector.copy(x = -1f)
        }
        if (ballOffset.y > tableSize) {
            newVector = newVector.copy(y = -1f)
        }
        if (ballOffset.y < 0) {
            newVector = newVector.copy(y = 1f)
        }
        movementVector = newVector
    }
    Box(
        Modifier
            .graphicsLayer {
                translationX = ballOffset.x
                translationY = ballOffset.y
            }
            .size(ballSize)
            .background(color = color, shape = CircleShape)
            .onGloballyPositioned {
                layoutCoordinates = it
            }
    )
}

@Composable
private fun HitBlock(
    unit: PongUnit,
    size: Dp
) {
    val boxSide by remember { unit.side }
    val color by animateColorAsState(
        if (boxSide == 0) Color.DarkGray else Color.Gray,
        label = "side color"
    )
    val modifier = Modifier.background(color = color)

    Box(
        Modifier
            .then(modifier)
            .size(size)
            .onGloballyPositioned {
                unit.rect = it
            }
            .clickable { log("${unit.rect}") }
    )
}

enum class CollisionDirection {
    TOP, BOTTOM, LEFT, RIGHT, NONE
}

fun detectCollision(rect1: Rect, rect2: Rect): CollisionDirection {
    if (rect1.overlaps(rect2)) {
        // Compare positions to determine direction
        return if (Math.abs(rect1.center.x - rect2.center.x) > Math.abs(rect1.center.y - rect2.center.y)) {
            // Horizontal collision
            if (rect1.left < rect2.left) {
                CollisionDirection.LEFT
            } else {
                CollisionDirection.RIGHT
            }
        } else {
            // Vertical collision
            if (rect1.top < rect2.top) {
                CollisionDirection.TOP
            } else {
                CollisionDirection.BOTTOM
            }
        }
    }
    return CollisionDirection.NONE
}
