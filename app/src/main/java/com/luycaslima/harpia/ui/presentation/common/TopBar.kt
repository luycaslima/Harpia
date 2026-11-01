package com.luycaslima.harpia.ui.presentation.common

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavController
import com.example.compose.snippets.components.AppIcons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title : String) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        title = {
            Text(title)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopCenterBar(title : String , onClick : () -> Unit){
    CenterAlignedTopAppBar(title = {
        Text("Capture")
    }, navigationIcon = {
        IconButton(onClick = onClick) {
            Icon(painter = rememberVectorPainter(AppIcons.ArrowBack), contentDescription = "Go back button")
        }
    })
}