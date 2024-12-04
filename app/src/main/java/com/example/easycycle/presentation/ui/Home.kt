package com.example.easycycle.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.easycycle.data.model.Student
import com.example.easycycle.presentation.viewmodel.UserViewModel
import com.example.easycycle.R

@Composable
fun homeScreen(student : Student) {
    Log.d("Home","Entered Home Screen")
    val imageUrl = student.imageURL.ifEmpty { "default" }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(text = "Profile", fontWeight = FontWeight.Bold)
            profile(student,imageUrl)
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "Schedules", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun profile(student: Student,imageUrl:String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(10.dp),
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (imageUrl == "default") R.drawable.person else imageUrl)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .build(),
                contentDescription = "Student Image",
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 10.dp)
            )

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = student.name,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(
                    text = student.registrationNumber,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Text(text = student.branch, fontSize = 20.sp)
            }
        }
    }
}

