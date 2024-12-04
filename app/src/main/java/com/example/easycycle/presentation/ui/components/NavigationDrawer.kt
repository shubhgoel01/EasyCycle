package com.example.easycycle.presentation.ui.components

import android.view.MenuItem
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun drawerContent(scope:CoroutineScope, scaffoldState: ScaffoldState, navController: NavController) {
    val context= LocalContext.current
    var menuList by remember { mutableStateOf(listOf<menuItems>()) }
    Box(modifier= Modifier.fillMaxSize())
    {
        Column {
            Spacer(modifier = Modifier.padding(20.dp))
            Text(
                text = "MENU", modifier = Modifier
                    .padding(start = 20.dp)
                    .clickable {
                        scope.launch {
                            scaffoldState.drawerState.close()
                        }
                    }, color = Color.Red
            )

            menuList = listOf(
                menuItems(title = "Home", icon = Icons.Filled.Home, isOpen = true),
                menuItems(title = "History", icon = Icons.Filled.CheckCircle, isOpen = false),
                menuItems(title = "Premium", icon = Icons.Filled.Star, isOpen = false),
                menuItems(title = "Log Out", icon = Icons.Filled.AccountCircle, isOpen = false)
            )
            menuList.forEach { item ->
                ViewContent(item = item) { clickedItem ->//this is parameter
                    menuList = menuList.map { currentItem ->
                        currentItem.copy(isOpen = currentItem == clickedItem)
                    }
                    when (clickedItem.title) {
                        //Navigate according to the title
                    }
                    //close the drawer
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                }
            }
        }
    }
}

@Composable
fun ViewContent(item:menuItems , onClick:(item:menuItems)->Unit) {
    val title=item.title
    val icon=item.icon
    //val status by remember{ mutableStateOf(item.isOpen) }
    Surface(
        modifier= Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp)
            .border(
                width = 2.dp,
                color = if (item.isOpen) Color.Gray else Color.White,
                shape = RoundedCornerShape(20.dp)
            ) ,
        //color= if (status) Color.Gray else Color.White,
        shape = RoundedCornerShape(20.dp)
    )
    {
        Row(modifier= Modifier.fillMaxSize(),verticalAlignment= Alignment.CenterVertically)
        {
            Icon(imageVector = icon, contentDescription =null, Modifier.padding(start=if (!item.isOpen) 20.dp else 50.dp))
            Text(text = title,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .clickable {
                        if (item.isOpen)
                            return@clickable
                        onClick(item)
                        //then navigate to screen
                    },
                fontSize = 20.sp,
            )
        }
    }
}

data class menuItems(
    val title: String,
    val icon: ImageVector,
    var isOpen:Boolean=false
)