package views.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Code borrowed from StackOverflow answer
 * https://stackoverflow.com/questions/67111020/exposed-drop-down-menu-for-jetpack-compose
 * by Salomon BRYS
 */

@Composable
fun ExposedDropDownMenu(
    values: List<String>,
    selectedIndex: Int,
    onChange: (Int) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity),
    shape: Shape = MaterialTheme.shapes.small.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize)
) {
    ExposedDropDownMenuImpl(
        values = values,
        selectedIndex = selectedIndex,
        onChange = onChange,
        label = label,
        modifier = modifier,
        backgroundColor = backgroundColor,
        shape = shape,
        decorator = { color, width, content ->
            Box(Modifier.drawBehind {
                val strokeWidth = width.value * density
                val y = size.height - strokeWidth / 2
                drawLine(
                    color,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
            }) {
                content()
            }
        }
    )
}

@Composable
fun OutlinedExposedDropDownMenu(
    values: List<String>,
    selectedIndex: Int,
    onChange: (Int) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity),
    shape: Shape = MaterialTheme.shapes.small
) {
    ExposedDropDownMenuImpl(
        values = values,
        selectedIndex = selectedIndex,
        onChange = onChange,
        label = label,
        modifier = modifier,
        backgroundColor = backgroundColor,
        shape = shape,
        decorator = { color, width, content ->
            Box(Modifier.border(width, color, shape)) {
                content()
            }
        }
    )
}

@Composable
private fun ExposedDropDownMenuImpl(
    values: List<String>,
    selectedIndex: Int,
    onChange: (Int) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier,
    backgroundColor: Color,
    shape: Shape,
    decorator: @Composable (Color, Dp, @Composable () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val indicatorColor = //MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        if (expanded) MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        else MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.UnfocusedIndicatorLineOpacity)
    val indicatorWidth = (if (expanded) 2 else 1).dp
    val labelColor = //MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        if (expanded) MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        else MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
    val trailingIconColor = //MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity)
        if (expanded) MaterialTheme.colors.primary.copy(alpha = ContentAlpha.high)
        else MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity)
    val rotation: Float by animateFloatAsState(if (expanded) 180f else 0f)

    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier.width(IntrinsicSize.Min)) {
        decorator(indicatorColor, indicatorWidth) {
            Box(Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = shape)
                .onGloballyPositioned { textfieldSize = it.size.toSize() }
                .clip(shape)
                .focusRequester(focusRequester)
                .clickable {
                    if (!dismissed) {
                        expanded = true
                    }
                    focusRequester.requestFocus()
                }
                .padding(start = 16.dp, end = 12.dp, top = 9.dp, bottom = 13.5.dp)
            ) {
                Column(Modifier.padding(end = 32.dp)) {
                    ProvideTextStyle(value = MaterialTheme.typography.caption.copy(color = labelColor)) {
                        label()
                    }
                    Text(
                        text = values[selectedIndex],
                        modifier = Modifier.padding(top = 1.5.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Change",
                    tint = trailingIconColor,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 4.dp)
                        .rotate(rotation)
                )
            }
        }

        val scope = rememberCoroutineScope()

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                dismissed = true
                expanded = false
                scope.launch {
                    delay(300)
                    dismissed = false
                }
            },
            modifier = Modifier.width(with(LocalDensity.current) { textfieldSize.width.toDp() })
        ) {
            values.forEachIndexed { i, v ->
                DropdownMenuItem(
                    onClick = {
                        onChange(i)
                        scope.launch {
                            delay(150)
                            expanded = false
                        }
                    }
                ) {
                    Text(v)
                }
            }
        }
    }
}