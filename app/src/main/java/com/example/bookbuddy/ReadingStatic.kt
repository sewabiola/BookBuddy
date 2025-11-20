package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStatsScreen(navController: NavHostController) {
    var stats by remember { mutableStateOf(BookBuddyDatabase.getReadingStatistics()) }

    LaunchedEffect(Unit) {
        stats = BookBuddyDatabase.getReadingStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Books: ${stats.totalBooks}")
            Text("Not Started: ${stats.notStarted}")
            Text("Reading: ${stats.reading}")
            Text("Read: ${stats.read}")
        }
    }
}
