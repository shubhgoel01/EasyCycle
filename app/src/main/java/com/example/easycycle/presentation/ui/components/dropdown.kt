package com.example.easycycle.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
@Composable
fun ComponentDropdown(
    selectedOption: String,
    list: List<String>,
    onOptionChange: (String) -> Unit,
    onExpandedChange:()->Unit,
    expanded:Boolean,
    modifier: Modifier = Modifier,
    label:String
) {
    Box(modifier = modifier) {
        // Row containing the selected option and dropdown button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Black))
                .clickable { onExpandedChange() }
                .padding(8.dp) // Optional padding for content
        ) {
            Text(text = if(selectedOption=="") label else selectedOption, modifier = Modifier.weight(1f),color= if(selectedOption!="") Color.Black else Color.Gray)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.padding(start = 2.dp).clickable { onExpandedChange() })
        }

        // The DropdownMenu showing the list of options
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
        ) {
            list.forEach { option ->
                DropdownMenuItem(text = { Text(text = option) }, onClick = {
                    onOptionChange(option)
                    onExpandedChange()
                })
            }
        }
    }
}