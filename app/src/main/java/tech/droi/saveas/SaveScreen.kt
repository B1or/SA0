package tech.droi.saveas

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tech.droi.saveas.MainActivity.Companion.TAG

@Composable
fun SaveScreen(
    modifier: Modifier = Modifier,
    viewModel: SaveViewModel = viewModel(),
    onSetFabAction: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val list by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        onSetFabAction {
            viewModel.add(context)
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                if (x > 15f || y > 15f || z > 15f) {
                    Log.d(TAG, "shake-up")
                    viewModel.sort()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }
    LazyColumn(
        modifier = modifier
    ) {
        Log.d(TAG, "change")
        items(list, key = { it.contactId!! }) { item ->
            Log.d(TAG, "item change")
            ContactRow(viewModel, item)
        }
    }
}

@Composable
fun ContactRow(
    viewModel: SaveViewModel,
    contactUi: ContactUi
) {
    val context = LocalContext.current
    Card(
        onClick = { viewModel.click(context, contactUi) },
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val pageCount = contactUi.names.size
            val list = contactUi.names.sortedBy { it.second.value }
            val page = list.indexOf(list.find { it.second == contactUi.saveAs })
            val pagerState = rememberPagerState(
                pageCount = { pageCount },
                initialPage = page
            )
            RoundThumbnail(imageUri = contactUi.photo, modifier = Modifier.padding(4.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(list[page].second.resource),
                        style = TextStyle(
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = list[page].first,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    viewModel.update(contactUi.contactId!!, list[page].second)
                    contactUi.saveAs = list[page].second
                }
            }
        }
    }
}

@Composable
fun RoundThumbnail(
    imageUri: android.net.Uri?,
    modifier: Modifier = Modifier
) {
    if (imageUri != null && !imageUri.path.isNullOrEmpty())
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Round thumbnail",
            modifier = modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    else
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "No photo",
            modifier = modifier
                .size(64.dp) // Example size
                .clip(CircleShape)
        )
}
