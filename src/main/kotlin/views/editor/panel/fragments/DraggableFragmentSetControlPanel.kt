package views.editor.panel.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import model.api.editor.clip.fragment.transformer.IdleTransformer
import model.api.editor.clip.fragment.transformer.SilenceTransformer
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import views.utils.OutlinedExposedDropDownMenu

@Composable
fun DraggableFragmentSetPanel(
    fragmentSetViewModel: FragmentSetViewModel<*, *>
) {

    Box {
        // to prevent view from changing size
//        OutlinedTextField("", {}, label = {Text("") }, modifier = Modifier.width(0.dp).padding(0.dp, 0.dp, 0.dp, 6.dp))
        Button({}, modifier = Modifier.width(0.dp).padding(top = 6.dp), enabled = false) {}

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
                    if (fragment == fragmentSetViewModel.selectedFragment) {
                        SelectedFragmentPanel(fragmentViewModel)
                    } else {
                        SimpleFragmentPanel(fragmentViewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedFragmentPanel(fragmentViewModel: FragmentViewModel<*>) {
    val density = LocalDensity.current

    Row {
        Button(
            enabled = fragmentViewModel.canPlayFragment,
            onClick = fragmentViewModel::onPlayClicked
        ) {
//            Icon(useResource("icons/play_arrow_black_24dp.svg") {
//                loadSvgPainter(it, density)
//            }, "play")
            Icon(Icons.Filled.PlayArrow, "play")
        }

        OutlinedExposedDropDownMenu(
            values = fragmentViewModel.transformerOptions,//FragmentTransformerType.values().map(FragmentTransformerType::name),
            selectedIndex = fragmentViewModel.selectedTransformerOptionIndex,//fragmentViewModel.transformer.type.ordinal,
            onChange = fragmentViewModel::onSelectTransformer,
            label = { Text("Transformer") },
            backgroundColor = Color.White
        )

//        when(fragmentViewModel.transformer) {
//            is FragmentTransformer.SilenceTransformer -> {
//                (fragmentState.fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs
//            }
//        }

        Button(
            enabled = fragmentViewModel.canStopFragment,
            onClick = fragmentViewModel::onStopClicked
        ) {
            Icon(useResource("icons/stop_black_24dp.svg") {
                loadSvgPainter(it, density)
            }, "stop")
        }
        Button(
            onClick = fragmentViewModel::onRemoveClicked
        ) {
            Icon(useResource("icons/delete_black_24dp.svg") {
                loadSvgPainter(it, density)
            }, "remove")
        }
    }
}

@Composable
private fun SimpleFragmentPanel(fragmentViewModel: FragmentViewModel<*>) {
    Row {
        when (val transformer = fragmentViewModel.transformer) {
            is IdleTransformer -> {
            }
            is SilenceTransformer -> {
                Text("${transformer.silenceDurationUs / 1e3} ms")
            }
        }
    }
}
