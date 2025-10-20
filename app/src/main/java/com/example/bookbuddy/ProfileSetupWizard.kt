package com.example.bookbuddy

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Task 42: 1-1-2-1 Design profile setup wizard
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileSetupWizard(
    onComplete: (UserProfile) -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var userProfile by remember { mutableStateOf(UserProfile()) }

    val steps = listOf(
        "Basic Info",
        "Reading Preferences",
        "Favorite Genres",
        "Reading Goals"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Profile") },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = (currentStep + 1).toFloat() / steps.size,
                modifier = Modifier.fillMaxWidth()
            )

            // Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (index <= currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (index < currentStep) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (index <= currentStep)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index <= currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() with fadeOut() + slideOutHorizontally()
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> BasicInfoStep(
                        profile = userProfile,
                        onProfileUpdate = { userProfile = it }
                    )
                    1 -> ReadingPreferencesStep(
                        profile = userProfile,
                        onProfileUpdate = { userProfile = it }
                    )
                    2 -> FavoriteGenresStep(
                        profile = userProfile,
                        onProfileUpdate = { userProfile = it }
                    )
                    3 -> ReadingGoalsStep(
                        profile = userProfile,
                        onProfileUpdate = { userProfile = it }
                    )
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            onComplete(userProfile)
                        }
                    }
                ) {
                    Text(if (currentStep < steps.size - 1) "Next" else "Complete")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        if (currentStep < steps.size - 1) Icons.Default.ArrowForward else Icons.Default.Done,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
fun BasicInfoStep(
    profile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Let's start with the basics",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell us a bit about yourself",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = profile.displayName,
            onValueChange = { onProfileUpdate(profile.copy(displayName = it)) },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.bio,
            onValueChange = { onProfileUpdate(profile.copy(bio = it)) },
            label = { Text("Bio") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            placeholder = { Text("Tell us about your reading journey...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.location,
            onValueChange = { onProfileUpdate(profile.copy(location = it)) },
            label = { Text("Location (Optional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = profile.website,
            onValueChange = { onProfileUpdate(profile.copy(website = it)) },
            label = { Text("Website (Optional)") },
            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ReadingPreferencesStep(
    profile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Reading Preferences",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Help us understand your reading habits",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = profile.readingPreferences.notificationsEnabled,
                onCheckedChange = {
                    onProfileUpdate(
                        profile.copy(
                            readingPreferences = profile.readingPreferences.copy(
                                notificationsEnabled = it
                            )
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Get notified about new recommendations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = profile.readingPreferences.shareReadingActivity,
                onCheckedChange = {
                    onProfileUpdate(
                        profile.copy(
                            readingPreferences = profile.readingPreferences.copy(
                                shareReadingActivity = it
                            )
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Share Reading Activity", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Let others see what you're reading",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = profile.isPublicProfile,
                onCheckedChange = {
                    onProfileUpdate(profile.copy(isPublicProfile = it))
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Public Profile", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Make your profile visible to everyone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FavoriteGenresStep(
    profile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
    val allGenres = listOf(
        "Fiction", "Non-Fiction", "Mystery", "Thriller", "Romance",
        "Science Fiction", "Fantasy", "Biography", "History",
        "Self-Help", "Business", "Poetry", "Horror", "Adventure",
        "Young Adult", "Classics", "Graphic Novel", "Memoir"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Favorite Genres",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select your favorite book genres",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        allGenres.chunked(2).forEach { rowGenres ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowGenres.forEach { genre ->
                    val isSelected = profile.favoriteGenres.contains(genre)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) {
                                profile.favoriteGenres - genre
                            } else {
                                profile.favoriteGenres + genre
                            }
                            onProfileUpdate(profile.copy(favoriteGenres = updated))
                        },
                        label = { Text(genre) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowGenres.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingGoalsStep(
    profile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit
) {
    var goalTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Reading Goals",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Set a reading goal to stay motivated",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = if (profile.readingPreferences.readingGoal.targetBooksPerYear > 0)
                profile.readingPreferences.readingGoal.targetBooksPerYear.toString()
            else "",
            onValueChange = {
                val target = it.toIntOrNull() ?: 0
                onProfileUpdate(
                    profile.copy(
                        readingPreferences = profile.readingPreferences.copy(
                            readingGoal = profile.readingPreferences.readingGoal.copy(
                                targetBooksPerYear = target
                            )
                        )
                    )
                )
            },
            label = { Text("Number of Books") },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
            placeholder = { Text("e.g., 12") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = goalTypeExpanded,
            onExpandedChange = { goalTypeExpanded = !goalTypeExpanded }
        ) {
            OutlinedTextField(
                value = profile.readingPreferences.readingGoal.goalType.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Goal Period") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalTypeExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = goalTypeExpanded,
                onDismissRequest = { goalTypeExpanded = false }
            ) {
                GoalType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onProfileUpdate(
                                profile.copy(
                                    readingPreferences = profile.readingPreferences.copy(
                                        readingGoal = profile.readingPreferences.readingGoal.copy(
                                            goalType = type
                                        )
                                    )
                                )
                            )
                            goalTypeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "You can always adjust your reading goals later in settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
