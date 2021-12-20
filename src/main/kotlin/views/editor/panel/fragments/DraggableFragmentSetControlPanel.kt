package views.editor.panel.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.svgResource
import androidx.compose.ui.unit.dp
import viewmodels.api.editor.panel.fragments.base.FragmentSetViewModel
import viewmodels.api.editor.panel.fragments.base.FragmentViewModel
import viewmodels.api.editor.panel.fragments.draggable.DraggableFragmentSetViewModel

@Composable
fun DraggableFragmentSetPanel(
    fragmentSetViewModel: FragmentSetViewModel<*>
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

                if (fragment == fragmentSetViewModel.selectedFragment) {
                    SelectedFragmentPanel(fragmentViewModel)
                } else {
                    SimpleFragmentPanel(fragmentViewModel)
                }
            }
        }
    }
}

@Composable
private fun SelectedFragmentPanel(fragmentViewModel: FragmentViewModel) {
    Row {
        Button(
            enabled = fragmentViewModel.canPlayFragment,
            onClick = fragmentViewModel::onPlayClicked
        ) {
            Icon(svgResource("icons/play_arrow_black_24dp.svg"), "play")
        }

//        when(fragmentViewModel.transformer) {
//            is FragmentTransformer.SilenceTransformer -> {
//                (fragmentState.fragment.transformer as FragmentTransformer.SilenceTransformer).silenceDurationUs
//            }
//        }

        Button(
            enabled = fragmentViewModel.canStopFragment,
            onClick = fragmentViewModel::onStopClicked
        ) {
            Icon(svgResource("icons/stop_black_24dp.svg"), "stop")
        }
        Button(
            onClick = fragmentViewModel::onRemoveClicked
        ) {
            Icon(svgResource("icons/delete_black_24dp.svg"), "delete")
        }
    }
}

@Composable
private fun SimpleFragmentPanel(fragmentViewModel: FragmentViewModel) {
    Row {
        Text("Simple fragment")
    }
}