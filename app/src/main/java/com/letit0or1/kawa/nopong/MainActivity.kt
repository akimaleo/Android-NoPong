package com.letit0or1.kawa.nopong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.letit0or1.kawa.nopong.pong.PongTable
import com.letit0or1.kawa.nopong.ui.theme.NoPongTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoPongTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var start by remember { mutableStateOf(true) }
    var boxesCount: Pair<Int, Int>? by remember { mutableStateOf(null) }

    Box(Modifier.fillMaxWidth()) {
        boxesCount?.let { boxesCount ->
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .weight(boxesCount.first.toFloat())
                        .background(color = Color.DarkGray)
                )
                Box(
                    Modifier
                        .background(color = Color.Gray)
                        .weight(boxesCount.first.toFloat())
                )
            }
            Row(Modifier.align(Alignment.TopCenter)) {
                Text(text = "${boxesCount.first}", style = MaterialTheme.typography.headlineLarge)
                Text(modifier = Modifier.padding(horizontal = 16.dp), text = "vs")
                Text(text = "${boxesCount.second}", style = MaterialTheme.typography.headlineLarge)
            }
        }

        AnimatedVisibility(modifier = Modifier.align(Alignment.Center), visible = start) {
            PongTable {
                boxesCount = it
            }
        }

//        OutlinedButton(
//            modifier = Modifier.align(Alignment.BottomCenter),
//            onClick = { start = !start }) {
//            Text(text = if (start) "Stop" else "Start")
//        }
    }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NoPongTheme {
        MainScreen()
    }
}