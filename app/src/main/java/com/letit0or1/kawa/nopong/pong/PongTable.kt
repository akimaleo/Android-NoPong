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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
    },
    onCountChange: (Pair<Int, Int>) -> (Unit)
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
        val calculateSide: (Int) -> Int =
            { side -> state.sumOf { it.filter { it.side.value == side }.size } }
        if (tableSize.width > 0) {
//        LEFT
            Ball(
                color = Color.Gray,
                ballSide = 0,
                tableSize = tableSize.width,
                state = state,
                onHit = {
                    onCountChange(calculateSide(0) to calculateSide(1))
                }
            )
//        RIGHT
            Ball(
                color = Color.DarkGray,
                ballSide = 1,
                tableSize = tableSize.width,
                state = state,
                onHit = {
                    onCountChange(calculateSide(0) to calculateSide(1))
                }
            )
        }

    }
}

@Composable
private fun Ball(
    color: Color = Color.Black,
    tableSize: Int,
    ballSize: Dp = 15.dp,
    ballSide: Int,
    state: List<List<PongUnit>>,
    onHit: () -> Unit
) {
    var ballLayout: LayoutCoordinates? by remember { mutableStateOf(null) }

    var ballOffset by remember {
        mutableStateOf(
            Offset(
                x = if (ballSide == 0) 0f else 1000f,
                y = (tableSize / 2).toFloat()
            ),
        )
    }
    var movementVector by remember {
        mutableStateOf(
            Offset(
                Random.nextInt(-50, 50).toFloat(),
                Random.nextInt(-50, 50).toFloat(),
            ).normalize()
        )
    }
    LaunchedEffect(ballOffset) {
        ballOffset += movementVector.times(20f)
        val ballRect = ballLayout?.boundsInRoot()!!
        val parentRect = ballLayout?.parentLayoutCoordinates?.boundsInRoot()!!
        var newVector = movementVector.copy().normalize()

        state.forEachIndexed { x, it ->
            it.forEachIndexed { y, pongUnit ->
                pongUnit.rect().takeIf { pongUnit.side.value != ballSide }?.let { pongUnitRect ->
                    // Detect collision of the BAll and PontUnit
                    val collision = detectCollision(ballRect, pongUnitRect)
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
                        onHit()
                        movementVector = newVector
                        return@forEachIndexed
                    }
                }
            }

        }
// Refactored Collision Detection with Parent Boundaries
        if (ballRect.left <= parentRect.left) {
            newVector = newVector.copy(x = 1f) // Bounce right if hitting the left boundary
        }
        if (ballRect.right >= parentRect.right) {
            newVector = newVector.copy(x = -1f) // Bounce left if hitting the right boundary
        }
        if (ballRect.bottom >= parentRect.bottom) {
            newVector = newVector.copy(y = -1f) // Bounce up if hitting the bottom boundary
        }
        if (ballRect.top <= parentRect.top) {
            newVector = newVector.copy(y = 1f) // Bounce down if hitting the top boundary
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
                ballLayout = it
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
