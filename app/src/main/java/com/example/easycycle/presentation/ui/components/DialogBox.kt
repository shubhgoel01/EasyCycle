package com.example.easycycle.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Component_tDialogBox(
    heading:String?=null,
    pointsList:List<String>?=null,
    body:String?=null,
    onDismissRequest: () -> Unit,
    composable: (@Composable () -> Unit)? = null
    ) {
    Log.d("Dialog Box","Called")
        Dialog(onDismissRequest = { onDismissRequest() }) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .verticalScroll(rememberScrollState()),  // Enable vertical scrolling
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                if(heading!=null)
                {
                    Text(
                        text = heading,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(top = 10.dp, start = 8.dp, end = 4.dp),  // Space between title and content
                        textAlign = TextAlign.Justify,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge  // Apply large title style
                    )
                }
                if(pointsList!=null)
                {
                    Column(modifier = Modifier.padding(start=16.dp,end=16.dp,top=3.dp)) {
                        pointsList.forEach{
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("• ", style = MaterialTheme.typography.bodyMedium,color = Color.Black)
                                Text(it, style = MaterialTheme.typography.bodyMedium,color = Color.Black,textAlign = TextAlign.Justify)
                            }
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                }
                if(body!=null)
                {
                    Text(
                        text = body,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth() // Ensure the text takes up the full width
                            .padding(start = 10.dp, end = 10.dp),
                        textAlign = TextAlign.Justify,  // Align text to the start
                        style = MaterialTheme.typography.bodyMedium,  // Apply body text style
                    )
                }
                if(composable!=null) {
                    Spacer(Modifier.height(3.dp))
                    composable()
                }
            }
        }
}
