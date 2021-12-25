package views.editor.panel.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import model.api.editor.clip.fragment.transformer.FragmentTransformer
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import views.utils.ExposedDropDownMenu

@ExperimentalComposeUiApi
@Composable
fun DraggableFragmentSetPanel(
    fragmentSetViewModel: FragmentSetViewModel<*, *>,
) {

    Box {
        // to prevent view from changing size
        Column {
            OutlinedTextField("", {}, label = {Text("") }, modifier = Modifier.width(0.dp).padding(0.dp, 0.dp, 0.dp, 6.dp))
            Button({}, modifier = Modifier.width(0.dp).padding(top = 6.dp), enabled = false) {}
        }

        for ((fragment, fragmentViewModel) in fragmentSetViewModel.fragmentViewModels) {
            Box(modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeWithLayer(0, 0) {
                            fragmentViewModel.onControlPanelPlaced(placeable.width.toFloat())
                            translationX = fragmentViewModel.computeControlPanelXPositionWinPx
                        }
                    }
                }) {

                if (!fragmentViewModel.isError) {
                    /*
                    if (fragment == fragmentSetViewModel.selectedFragment) {
                        SelectedFragmentPanel(fragmentViewModel)
                    } else {
                        SelectedFragmentPanel(fragmentViewModel)
                    }
                     */
                    SelectedFragmentPanel(fragmentViewModel)
                }
            }
        }
    }
}

/*
@Composable
private fun UnselectedFragmentPanel(fragmentViewModel: FragmentViewModel<*>) {
    Row {
        when (fragmentViewModel.transformerType) {
            FragmentTransformer.Type.SILENCE -> {
                UnselectedSilenceTransformerPanel(fragmentViewModel)
            }
            FragmentTransformer.Type.DELETE -> {}
            FragmentTransformer.Type.IDLE -> {}
        }
    }
}

@Composable
private fun UnselectedSilenceTransformerPanel(fragmentViewModel: FragmentViewModel<*>) {
    Text("${fragmentViewModel.silenceTransformerSilenceDurationMs} ms")
}
 */

@ExperimentalComposeUiApi
@Composable
private fun SelectedFragmentPanel(fragmentViewModel: FragmentViewModel<*>) {
    val density = LocalDensity.current

    Card(
        modifier = Modifier
            .padding(top = 4.dp)
            .pointerInput(fragmentViewModel) {
                detectTapGestures(onPress = {
                    fragmentViewModel.onControlPanelPress()
                })
            },
        elevation = 4.dp
    ) {IntrinsicSize.Min
        Column(modifier = Modifier.padding(4.dp).width(240.dp)) {
            Row {
                ExposedDropDownMenu(
                    values = fragmentViewModel.transformerTypeOptions,
                    selectedIndex = fragmentViewModel.selectedTransformerTypeOptionIndex,
                    onChange = fragmentViewModel::onSelectTransformer,
                    label = { Text("Transformer") },
                    modifier = Modifier.width(132.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                when(fragmentViewModel.transformerType) {
                    FragmentTransformer.Type.SILENCE -> {
                        SelectedSilenceTransformerPanel(fragmentViewModel)
                    }
                    FragmentTransformer.Type.DELETE -> {}
                    FragmentTransformer.Type.IDLE -> {}
                }
            }

            Row(modifier = Modifier) {
                Button(
                    enabled = fragmentViewModel.canPlayFragment,
                    onClick = fragmentViewModel::onPlayClicked
                ) {
                    Icon(useResource("icons/play_arrow_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "play")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    enabled = fragmentViewModel.canStopFragment,
                    onClick = fragmentViewModel::onStopClicked
                ) {
                    Icon(useResource("icons/stop_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "stop")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = fragmentViewModel::onRemoveClicked,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Icon(useResource("icons/delete_black_24dp.svg") {
                        loadSvgPainter(it, density)
                    }, "remove")
                }
            }
        }
    }
}

@Composable
private fun SelectedSilenceTransformerPanel(fragmentViewModel: FragmentViewModel<*>) {
    val density = LocalDensity.current

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        TextField(
            modifier = Modifier
                .width(
                    (9.5.dp * fragmentViewModel.silenceTransformerSilenceDurationMs.length + 34.5.dp)
                        .coerceAtLeast(64.dp)
                )
                .onFocusChanged {
                    if (!it.hasFocus) {
                        fragmentViewModel.onRefreshSilenceDurationMs()
                    }
                }
            ,
            shape = MaterialTheme.shapes.small.copy(topEnd = ZeroCornerSize),
            value = fragmentViewModel.silenceTransformerSilenceDurationMs,
            onValueChange = fragmentViewModel::onInputSilenceDurationMs,
            label = {
                Text("ms")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(29.dp)
//                .align(Alignment.CenterEnd)
                .clip(MaterialTheme.shapes.small.let {
                    it.copy(all = ZeroCornerSize).copy(topEnd = it.topEnd)
                })
                .background(MaterialTheme.colors.primary),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = useResource("icons/add_black_24dp.svg") {
                    loadSvgPainter(it, density)
                },
                contentDescription = "increment",
                modifier = Modifier.clickable(onClick = fragmentViewModel::onIncreaseSilenceDurationMs),
                tint = MaterialTheme.colors.surface
            )
            Divider(
                modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(),
                color = MaterialTheme.colors.surface
            )
            Icon(
                painter = useResource("icons/remove_black_24dp.svg") {
                    loadSvgPainter(it, density)
                },
                contentDescription = "decrement",
                modifier = Modifier.clickable(onClick = fragmentViewModel::onDecreaseSilenceDurationMs),
                tint = MaterialTheme.colors.surface
            )
        }
    }
}