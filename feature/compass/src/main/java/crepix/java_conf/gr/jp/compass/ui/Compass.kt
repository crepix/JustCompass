package crepix.java_conf.gr.jp.compass.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun CompassScreenRoot(viewModel: CompassViewModel = hiltViewModel()) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
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

    Compass(
        viewModel.uiState.collectAsState(),
        { viewModel.fixNeedlePosition(it) },
        { viewModel.releaseNeedlePosition(it) }
    )
}

@Composable
fun Compass(
    uiState: State<CompassViewModel.UiState>,
    onDrag: (degree: Float) -> Unit,
    onDragEnd: (velocity: Int) -> Unit
) {
    var lastPosition: Offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var lastMovement: Offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isPlaneFixed by remember { mutableStateOf(false) }

    JustCompassTheme {
        Box {
            BoxWithConstraints {
                val maxWidth = maxWidth
                val maxHeight = maxHeight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    val area = lastPosition.y * lastMovement.x - lastPosition.x * lastMovement.y
                                    val h = sqrt(lastPosition.x * lastPosition.x + lastPosition.y * lastPosition.y)
                                    onDragEnd.invoke((area / h).toInt())
                                },
                                onDrag = { change, offset ->
                                    val radius =
                                        atan2(change.position.y - maxHeight.value, change.position.x - maxWidth.value)
                                    onDrag.invoke(-90 - 180 * radius / PI.toFloat())
                                    lastMovement = Offset(
                                        change.previousPosition.x + offset.x - maxWidth.value,
                                        change.previousPosition.y + offset.y - maxHeight.value
                                    )
                                    lastPosition = Offset(
                                        change.previousPosition.x - maxWidth.value,
                                        change.previousPosition.y - maxHeight.value
                                    )
                                    change.consume()
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown().also {
                                    val radius = atan2(it.position.y - maxHeight.value, it.position.x - maxWidth.value)
                                    onDrag.invoke(-90 - 180 * radius / PI.toFloat())
                                    it.consume()
                                }
                                val up = waitForUpOrCancellation()
                                if (up != null) {
                                    up.consume()
                                    onDragEnd.invoke(0)
                                }
                            }
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .background(color = MaterialTheme.colors.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.primary)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.primaryVariant)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colors.background)
            )

            CompassPlane(
                uiState,
                isPlaneFixed,
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
                    .rotate(-uiState.value.needleDegree)
                    .alpha(if (uiState.value.isError) 0.25f else 1f)
            )

            if (!isPlaneFixed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopCenter)
                            .width(4.dp)
                            .height(32.dp)
                            .padding(top = 16.dp)
                            .clip(shape)
                            .alpha(if (uiState.value.isError) 0.25f else 1f)
                            .background(color = MaterialTheme.colors.primary)
                    )
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)) {
                Box(modifier = Modifier.aspectRatio(1.8f)) {
                    Text(
                        text = " ${uiState.value.planeDegree.toInt()}°",
                        color = MaterialTheme.colors.primary,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(if (uiState.value.isError) 0.25f else 1f)
                            .clickable { isPlaneFixed = !isPlaneFixed }
                    )

                    Text(
                        text = if (isPlaneFixed) "fix" else "roll",
                        color = MaterialTheme.colors.primary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 30.dp)
                            .alpha(if (uiState.value.isError) 0.25f else 1f)
                            .clickable { isPlaneFixed = !isPlaneFixed }
                    )
                }
            }

            if (uiState.value.isError) {
                Text(
                    text = stringResource(id = R.string.error),
                    color = Color.White,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 48.dp)
                )
            }
        }
    }
}

@Composable
fun CompassPlane(uiState: State<CompassViewModel.UiState>, isPlaneFixed: Boolean, modifier: Modifier = Modifier) {
    val degree = if (isPlaneFixed) 0f else -uiState.value.planeDegree
    Box(modifier = modifier.rotate(degree)) {
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

        for (i in 1..4) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(90f * i - 45f)
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

        for (i in 1..8) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(45f * i - 22.5f)
            ) {
                Box(
                    modifier = Modifier
                        .align(alignment = Alignment.TopCenter)
                        .width(2.dp)
                        .height(32.dp)
                        .padding(top = 16.dp)
                        .background(color = MaterialTheme.colors.primaryVariant)
                )
            }
        }
    }
}

private val shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    Compass(MutableStateFlow(CompassViewModel.UiState(330f, 218f, false)).collectAsState(), {}, {})
}
