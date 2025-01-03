package com.example.easycycle.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.easycycle.R
import com.example.easycycle.presentation.ui.components.ComponentDropdown
import com.example.easycycle.presentation.ui.components.Component_Button
import com.example.easycycle.presentation.ui.components.Component_tDialogBox
import com.example.easycycle.presentation.ui.components.Component_textField
import com.example.easycycle.presentation.ui.components.component_image
import com.example.easycycle.presentation.viewmodel.SharedViewModel

@Composable
fun SignInScreen(sharedViewModel: SharedViewModel, updateUserType : (String)->Unit) {
    val context= LocalContext.current
    var visible by remember{ mutableStateOf(false) }

    var password by remember{ mutableStateOf("") }
    var expanded2 by remember { mutableStateOf(false) }
    var textBox1 by remember{ mutableStateOf( "")}
    val passwordChange = { value : String ->
        password=value
    }

    var selectedOption1 by remember { mutableStateOf("") }
    var selectedOption2 by remember { mutableStateOf("") }
    val inputChange1 = { value : String ->
        updateUserType(value)
        selectedOption1 = value
        selectedOption2=""
    }
    var expanded1 by remember { mutableStateOf(false) }
    val onExpandChange1={
        expanded1=!expanded1
        textBox1=""
        password=""
    }

    val inputChange2 = { value : String ->
        selectedOption2 = value
    }

    val onExpandChange2={
        expanded2=!expanded2
        textBox1=""
        password=""
    }

    val onChange1 = { value : String ->
        textBox1=value
    }

    LaunchedEffect(Unit){
        // When Logging-in first clear any data available in LocalData
        sharedViewModel.clearProfile()
    }


    if(visible)
        Component_tDialogBox(text1,textItems, onDismissRequest = {
            visible=false
        })

    Box(
        modifier = login_box_modifier
    )
    {
        Column(
            modifier = log_in_column_modifier,  // Inner padding for the content
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            component_image(R.drawable.icon,img_modifier,"Logo Image")
            Spacer(modifier = Modifier.height(32.dp))
            val users = listOf("User","Admin")
            ComponentDropdown(selectedOption1,users,inputChange1, onExpandChange1,expanded1,label="Select User Type")
            Spacer(modifier = Modifier.height(10.dp))

            val loginMethod =if(selectedOption1 == "")listOf("Select Login Method") else if(selectedOption1 == "User") listOf("Reg.No.","Email") else  listOf("Admin Id.","Email")
            ComponentDropdown(selectedOption2,loginMethod,inputChange2, onExpandChange2,expanded2,label = "Select Login Method")
            Spacer(modifier = Modifier.height(24.dp))

            Component_textField(textBox1,onChange1,label= if(selectedOption2=="Reg.No.") "Registration Number" else if(selectedOption2=="Admin Id.") "Admin Id." else "Email", readOnly = (selectedOption2==""))
            Spacer(modifier = Modifier.height(10.dp))

            Component_textField(password,passwordChange, visualTransformation = PasswordVisualTransformation(),label="Password", readOnly = (textBox1==""))
            Spacer(modifier = Modifier.height(24.dp))

            Component_Button(title = "Press", onClick = {
                Log.d(":Login","Button Pressed")
                sharedViewModel.login(selectedOption1,selectedOption2,textBox1,password,context)},
                enabled = password != ""
            )
            Text(text = "How It Works?",modifier=Modifier.clickable { visible= true }, color=Color.Red)
        }
    }
}


val img_modifier = Modifier
    .size(width = 200.dp, height = 100.dp)

    .background(Color.Gray)

val login_box_modifier= Modifier
    .fillMaxSize()
    .background(color = Color.White)  // Light background color
    .padding(16.dp)

val log_in_column_modifier = Modifier
    .fillMaxSize()
    .padding(24.dp)  // Padding for entire content
    .background(Color.White, shape = RoundedCornerShape(16.dp))  // Card-like background
    .padding(30.dp)

const val text1:String="How Sign-In Works"
val textItems = listOf(
    "When the user enters their registration number and password, the system verifies if they are a student of NITK.",
    "If not, an 'ACCESS DENIED' message is displayed.",
    "Otherwise, the user is signed in with their registered email and password.",
    "No registration is required, as student information is preloaded into the system.",
    "If an authorized user signs in for the first time, their account is automatically created without the need for registration."
)