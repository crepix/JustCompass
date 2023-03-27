package crepix.java_conf.gr.jp.compass.ui

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import crepix.java_conf.gr.jp.compass.R
import crepix.java_conf.gr.jp.compass.viewmodel.CompassViewModel
import crepix.java_conf.gr.jp.uibase.theme.JustCompassTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun CompassScreenRoot(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: CompassViewModel = hiltViewModel()
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startMeasure()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopMeasure()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView({ View(it).apply { keepScreenOn = true } })
    Compass(
        viewModel.uiState.collectAsState(),
        { viewModel.fixNeedlePosition(it) },
        { viewModel.releaseNeedlePosition() }
    )
}

@Composable
fun Compass(
    uiState: State<CompassViewModel.UiState>,
    onDrag: (degree: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    JustCompassTheme {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = onDragEnd,
                            onDrag = { change, _ ->
                                change.consume()
                                onDrag.invoke(0f)
                            }
                        )
                    }
            )
            CompassPlane(
                uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            )
            Image(
                painter = painterResource(id = R.drawable.needle),
                contentDescription = "needle",
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .rotate(uiState.value.needleDegree)
            )
        }
    }
}

@Composable
fun CompassPlane(uiState: State<CompassViewModel.UiState>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.rotate(uiState.value.planeDegree)) {
        Box(
            modifier = modifier
                .background(color = MaterialTheme.colors.background)
                .padding(2.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.primary)
                .padding(2.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.background)
        )
        Text(
            text = "N",
            color = MaterialTheme.colors.primary,
            fontSize = 32.sp,
            modifier = Modifier.align(alignment = Alignment.TopCenter)
        )
        Text(
            text = "E",
            color = MaterialTheme.colors.primary,
            fontSize = 32.sp,
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .rotate(90f)
                .padding(12.dp)
        )
        Text(
            text = "S",
            color = MaterialTheme.colors.primary,
            fontSize = 32.sp,
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .rotate(180f)
        )
        Text(
            text = "W",
            color = MaterialTheme.colors.primary,
            fontSize = 32.sp,
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .rotate(270f)
                .padding(8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(45f)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .width(2.dp)
                    .height(32.dp)
                    .padding(top = 8.dp)
                    .background(color = MaterialTheme.colors.primary)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(135f)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .width(2.dp)
                    .height(32.dp)
                    .padding(top = 8.dp)
                    .background(color = MaterialTheme.colors.primary)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(225f)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .width(2.dp)
                    .height(32.dp)
                    .padding(top = 8.dp)
                    .background(color = MaterialTheme.colors.primary)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(315f)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .width(2.dp)
                    .height(32.dp)
                    .padding(top = 8.dp)
                    .background(color = MaterialTheme.colors.primary)
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    Compass(MutableStateFlow(CompassViewModel.UiState(0f, 0f)).collectAsState(), {}, {})
}
