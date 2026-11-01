package com.luycaslima.harpia.ui.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.compose.snippets.components.AppIcons
import com.luycaslima.harpia.ui.presentation.common.TopBar


@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), topBar = {
        TopBar("Scans")
    },
        floatingActionButton = {
        FloatingActionButton(onClick = {navController.navigate("Capture")}, Modifier.padding(16.dp) ){
            Icon(painter = rememberVectorPainter(AppIcons.Add),"Floating action button")
        }
    } ,
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Home Page")
                }
            }
    })

}