package com.luycaslima.harpia.ui.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.snippets.components.AppIcons
import com.luycaslima.harpia.data.local.ReconstructionProject
import com.luycaslima.harpia.ui.presentation.common.Dialog
import com.luycaslima.harpia.ui.presentation.common.TopBar


/*
* Button Plus Create a new Project
* This project will keep camera intrinscs
* Name
* Date taken
* The Photos order of taken (and renaming it when one is inserted there)
* Depth camera photo (future)
* Resulting model (if possible)
* */


//TODO address performance when creating the project and counting them when needed to put a default name

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current // What is this?

    val  toBeDeletedId = remember {mutableStateOf("")}
    val  openAlertDialog = remember { mutableStateOf(false) }

    // Re-sync project list whenever HomeScreen resumes (e.g., returning from CaptureScreen)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadProjects()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), topBar = {
        TopBar("Scans")
    },
        floatingActionButton = {
        FloatingActionButton(onClick = {navController.navigate("Capture")}, Modifier.padding(16.dp) ){
            Icon(painter = rememberVectorPainter(AppIcons.Add),"Floating action button")
        }
    } ,
        content = { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues)

            ){
                when (val state = uiState) {
                    is HomeUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is HomeUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = state.message)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.loadProjects() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    is HomeUiState.Success -> {
                        if (state.projects.isEmpty()) {
                            // Empty state: loaded cleanly, no projects saved yet

                            Text(modifier =  Modifier.align(Alignment.Center),text ="No Projects loaded")
                             //EmptyProjectsPlaceholder(onStartScan = onNavigateToCapture)
                        } else {
                            when {
                                openAlertDialog.value -> {
                                    Dialog(
                                        onDismissRequest = {
                                            openAlertDialog.value = false
                                            toBeDeletedId.value = ""
                                        },
                                        onConfirmation = {
                                            //have a try catch here and catch error
                                            viewModel.deleteProject(toBeDeletedId.value)
                                            openAlertDialog.value = false
                                            toBeDeletedId.value = ""
                                        },
                                        dialogTitle = "Delete Project?",
                                        dialogText = "The project will be permanently deleted from you device."
                                    )
                                }
                            }
                            // Populated state: render project cards
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = state.projects,
                                    key = { it.id }
                                ) { project ->
                                    ProjectCard(
                                        project = project,
                                        onClick = {
                                            openAlertDialog.value = true
                                            toBeDeletedId.value = project.id

                                            //viewModel.deleteProject(project.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
    })

}

//ONLY FOR TESTING
@Composable
fun ProjectCard(project: ReconstructionProject,modifier: Modifier = Modifier, onClick : () -> Unit) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
             modifier = Modifier.fillMaxSize()
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = project.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${project.photos.size} photos • ${project.intrinsics.width}x${project.intrinsics.height}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
            Button(modifier= Modifier.align(Alignment.BottomEnd) ,onClick= onClick) {
                Icon(painter = rememberVectorPainter(AppIcons.Delete),"Delete project button")
            }
        }

    }
}