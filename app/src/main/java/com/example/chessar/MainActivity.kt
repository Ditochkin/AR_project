package com.example.chessar

import android.graphics.fonts.FontFamily
import android.graphics.fonts.FontStyle
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chessar.ui.theme.ChessArTheme
import com.example.chessar.ui.theme.Translucent
import com.google.ar.core.Config
import com.xperiencelabs.astronaut.SpeechToTextManager
import com.xperiencelabs.astronaut.SpeechToTextWrapper
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.PlacementMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SpeechToTextManager.initialise(this)

        setContent {
            ChessArTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()){
                        var currentModel = remember {
                            mutableStateOf("circle")
                        }

                        var listenEnable by remember {
                            mutableStateOf(false)
                        }

                        var questionButtonEnable by remember {
                            mutableStateOf(false)
                        }

                        var animation by remember {
                            mutableStateOf("Victory")
                        }

//                        ARScreen(currentModel.value)
                        val nodes = remember {
                            mutableListOf<ArNode>()
                        }

                        val modelNode = remember{
                            mutableStateOf<ArModelNode?>(null)
                        }

                        val placeModelButton = remember {
                            mutableStateOf(false)
                        }

                        Box(modifier = Modifier.fillMaxSize()){
                            ARScene(
                                modifier = Modifier.fillMaxSize(),
                                nodes = nodes,
                                planeRenderer = true,
                                onCreate = {arSceneView ->
                                    arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
                                    arSceneView.planeRenderer.isShadowReceiver = false
                                    modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
                                        loadModelGlbAsync(
                                            glbFileLocation = "models/${currentModel.value}.glb", scaleToUnits = 1f
                                        )
                                        {
                                            onAnchorChanged = {
                                                placeModelButton.value = !isAnchored
                                            }
                                            onHitResult = {node, hitResult ->
                                                placeModelButton.value = node.isTracking
                                            }
                                            playAnimation(animation)
                                        }
                                    }
                                    nodes.add(modelNode.value!!)
                                },
                                onSessionCreate = {
                                    planeRenderer.isVisible = false
                                }
                            )
                        }

                        LaunchedEffect(key1 = currentModel.value){
                            modelNode.value?.loadModelGlbAsync(
                                glbFileLocation = "models/${currentModel.value}.glb", scaleToUnits = 1f
                            )
                            modelNode.value?.playAnimation(animation)
                            Log.e("errorloading", "ERROR LOADING MODEL")
                        }

                        Box(modifier = Modifier)
                        {
                            Button(
                                modifier = Modifier.align(Alignment.TopCenter),
                                onClick = {
                                    questionButtonEnable = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Translucent,
                                    contentColor = Color.White)) {
                                Text(text = "?", fontSize = 20.sp)
                            }
                        }

                        if (questionButtonEnable)
                        {
                            AlertDialog(
                                onDismissRequest = {
                                    questionButtonEnable = false
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { questionButtonEnable = false }
                                    ) {
                                        Text("OK", fontSize = 18.sp)
                                    }
                                },
                                title = { Text(text = "Animation list") },
                                text = { Text("Walk\nVictory\nRun\nIdle\nDefeat") },
                            )
                        }

                        Box(modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.BottomEnd)
                            .padding(start = 80.dp),
                            contentAlignment = Alignment.Center,
                        ){
                            Button(
                                modifier = Modifier.align(Alignment.TopCenter),
                                onClick = {
                                    listenEnable = !listenEnable
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Translucent,
                                    contentColor = Color.White)) {
                                Text(text = "Speak")
                            }
                        }

                        Box(modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.Center
                        )
                        {
                            Box(modifier = Modifier.align(Alignment.TopCenter))
                            {
                                Text(text = animation,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        background = Translucent,
                                    )
                                    )
                            }
                        }

                        Box(modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.BottomStart)
                        .padding(end = 80.dp),
                        contentAlignment = Alignment.Center
                        ){
                            Button(
                                modifier = Modifier.align(Alignment.TopCenter),
                                onClick = {
                                    modelNode.value?.anchor()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Translucent,
                                    contentColor = Color.White)) {
                                Text(text = "Place it")
                            }
                        }

                        Menu(modifier = Modifier.align(Alignment.BottomCenter))
                        {
                            currentModel.value = it
                        }

                        SpeechToTextWrapper(
                            listenEnable = listenEnable,
                            onSpeechStarted = {  },
                            onSpeechStopped = { listenEnable= false },
                            onSpeechError = {
                                Log.e("speecherror", it)
                            },
                            onSpeechResult ={
                                animation = it.joinToString { "\n" }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        SpeechToTextManager.destroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun Menu(modifier: Modifier, onClick: (String)->Unit)
{
    var currentIndex by remember {
        mutableStateOf(0)
    }
    val itemList = listOf(
        Food("circle", R.drawable.triangle),
        Food("square", R.drawable.square),
        Food("triangle", R.drawable.circle),
    )

    fun updateIndex(offset:Int)
    {
        currentIndex = (currentIndex + itemList.size + offset) % itemList.size
        onClick(itemList[currentIndex].name)
    }

    Row(modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround) {
        IconButton(onClick = {
            updateIndex(-1)
        }) {
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24), contentDescription = "previous")
        }

        CircularImage(imageId = itemList[currentIndex].imageId)

        IconButton(onClick = { updateIndex(1) }) {
            Icon(painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), contentDescription = "next")
        }
    }

}

data class Food(var name:String, var imageId:Int)

@Composable
fun CircularImage(
    modifier: Modifier = Modifier,
    imageId:Int)
{
    Box(modifier = modifier
        .size(150.dp)
        .clip(RoundedCornerShape(0))
        .border(width = 0.dp, Translucent))
    {
        Image(painter = painterResource(id = imageId), contentDescription = null, modifier = Modifier.size(150.dp), contentScale = ContentScale.FillBounds)
    }
}

//@Composable
//fun ARScreen(model: String)
//{
//    val nodes = remember("run") {
//        mutableListOf<ArNode>()
//    }
//
//    val modelNode = remember{
//        mutableStateOf<ArModelNode?>(null)
//    }
//
//    val placeModelButton = remember {
//        mutableStateOf(false)
//    }
//
//    Box(modifier = Modifier.fillMaxSize()){
//        var animation by remember {
//            mutableStateOf("run")
//        }
//
//        ARScene(
//            modifier = Modifier.fillMaxSize(),
//            nodes = nodes,
//            planeRenderer = true,
//            onCreate = {arSceneView ->
//                arSceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED
//                arSceneView.planeRenderer.isShadowReceiver = false
//                modelNode.value = ArModelNode(arSceneView.engine, PlacementMode.INSTANT).apply {
//                    loadModelGlbAsync(
//                        glbFileLocation = "models/${model}.glb", scaleToUnits = 1f
//                    )
//                    {
//                        onAnchorChanged = {
//                            placeModelButton.value = !isAnchored
//                        }
//                        onHitResult = {node, hitResult ->
//                            placeModelButton.value = node.isTracking
//                        }
//                        playAnimation("Run")
//                    }
//                }
//                nodes.add(modelNode.value!!)
//            },
//            onSessionCreate = {
//                planeRenderer.isVisible = false
//            }
//        )
//    }
//
//    Box(modifier = Modifier
//        .size(200.dp)
////        .align(Alignment.BottomStart)
//        .padding(end = 80.dp),
//        contentAlignment = Alignment.Center
//    ){
//        Button(
//            modifier = Modifier.align(Alignment.TopCenter),
//            onClick = {
//                modelNode.value?.anchor()
//            },
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Translucent,
//                contentColor = Color.White)) {
//            Text(text = "Place it")
//        }
//    }
//
//    LaunchedEffect(key1 = model){
//        modelNode.value?.loadModelGlbAsync(
//            glbFileLocation = "models/${model}.glb", scaleToUnits = 1f
//        )
////        modelNode.value?.stopAnimation("Defeat")
//        modelNode.value?.playAnimation("Run")
//        Log.e("errorloading", "ERROR LOADING MODEL")
//    }
//}