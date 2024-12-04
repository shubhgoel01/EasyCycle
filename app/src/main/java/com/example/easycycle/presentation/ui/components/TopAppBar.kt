package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTopAppBar(title:String, scope: CoroutineScope, scaffoldState: ScaffoldState) {
    val context= LocalContext.current
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
            .height(50.dp)
    )
}