package com.example.lly_004

//Default Imports
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lly_004.ui.theme.LLY_004Theme

/*Added Imports*/
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.CameraOptions



class MainActivity : ComponentActivity() {
    @OptIn(MapboxExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LLY_004Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }



            MapboxMap(
                Modifier.fillMaxSize(),
                mapInitOptionsFactory = { context ->
                    MapInitOptions(
                        context = context,
                        styleUri = "mapbox://styles/locallikeyou/clo61gomh003y01r70gk2784w",
                        cameraOptions = CameraOptions.Builder()
                            .center(Point.fromLngLat(47.6061, 122.3328))
                            .zoom(12.0)
                            .build()
                    )
                }
            )
            {
                //AddPointer(Point.fromLngLat(24.9384, 60.1699))
            }
        }
    }

    /*
    @OptIn(MapboxExperimental::class)
    @Composable
    fun AddPointer(point: Point) {
        val drawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.marker,
            null
        )
        val bitmap = drawable!!.toBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        PointAnnotation(
            iconImageBitmap = bitmap,
            iconSize = 0.5,
            point = point,
            onClick = {
                Toast.makeText(
                    this,
                    "Clicked on Circle Annotation: $it",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        )
    }
    */
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LLY_004Theme {
        Greeting("Android")
    }
}