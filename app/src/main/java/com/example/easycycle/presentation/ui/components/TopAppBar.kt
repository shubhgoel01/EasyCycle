package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.easycycle.presentation.viewmodel.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTopAppBar(
    title:String,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    sharedViewModel: SharedViewModel,
    fun1:(()->Unit)?= null,
    fun2:(()->Unit)?=null
){
    val context= LocalContext.current
    val remainingTime = sharedViewModel.remainingTime.collectAsState()
    val showLiveButton = sharedViewModel.showLiveIcon.collectAsState()
    val showLiveButtonMessage = sharedViewModel.showLiveIconMessage.collectAsState()
    TopAppBar(
        title ={
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White), // Customize style
                textAlign = TextAlign.Center,
                modifier= Modifier.padding(top=15.dp,start=105.dp)
            )
        },
        colors= TopAppBarDefaults.topAppBarColors(
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Gray
        ),
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null,modifier= Modifier.padding(top=7.dp))
            }
        },
        modifier= Modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
            .height(50.dp),
        actions = {
            if(remainingTime.value!=null){
                Text(text = remainingTime.value!!,color = Color.Red,style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
            }
            //Show Live Blinking
            if (showLiveButton.value) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Align dot and text in the center
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp) // Adjust size of the dot
                            .clip(CircleShape)
                            .background(Color.Red) // Red dot
                    )

                    Spacer(modifier = Modifier.width(8.dp)) // Add some space between the dot and text

                    Text(
                        text = showLiveButtonMessage.value,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

        }
    )
}