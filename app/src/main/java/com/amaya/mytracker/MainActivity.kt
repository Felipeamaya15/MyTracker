package com.amaya.mytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

data class TrackItem(
    val id: String,
    val title: String,
    val chapter: Int,
    val status: String = "Reading",
    val lastUpdated: Long = 0L,
    val imageUrl: String = "",
    val totalChapters: Int = 0,
    val genres: List<String> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TrackerScreen(
                onNavigateToDetail = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }
        composable(
            route = "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            DetailScreen(itemId = itemId, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val lista by viewModel.lista.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchApiQuery by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = lista
        .filter { it.title.contains(searchQuery, ignoreCase = true) }
        .sortedByDescending { it.lastUpdated }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyTracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Search in my list
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en mi lista...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search for new manga in the API
            Box {
                OutlinedTextField(
                    value = searchApiQuery,
                    onValueChange = { 
                        searchApiQuery = it
                        viewModel.searchMangaApi(it)
                    },
                    label = { Text("Añadir nuevo (MyAnimeList)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (searchApiQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchApiQuery = ""
                                viewModel.searchMangaApi("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    }
                )

                if (searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .heightIn(max = 250.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        LazyColumn {
                            items(searchResults) { manga ->
                                ListItem(
                                    headlineContent = { Text(manga.title) },
                                    supportingContent = { 
                                        Column {
                                            Text(if (manga.chapters != null) "${manga.chapters} capítulos" else "Capítulos desconocidos")
                                            manga.genres?.let { genres ->
                                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                                    genres.take(3).forEach { genre ->
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                                            shape = MaterialTheme.shapes.extraSmall,
                                                            modifier = Modifier.padding(end = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = genre.name,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    leadingContent = {
                                        AsyncImage(
                                            model = manga.images.jpg.image_url,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.addItemFromApi(manga)
                                        searchApiQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredList, key = { it.id }) { item ->
                    TrackItemRow(
                        item = item,
                        onUpdate = { updates -> viewModel.updateItem(item.id, updates) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onClick = { onNavigateToDetail(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackItemRow(
    item: TrackItem, 
    onUpdate: (Map<String, Any>) -> Unit, 
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(item.chapter.toString()) }
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.status == "Completed") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUrl.isNotEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.size(width = 50.dp, height = 75.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title, 
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.status == "Completed") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${if(item.lastUpdated > 0) sdf.format(Date(item.lastUpdated)) else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editValue,
                            onValueChange = { 
                                val filtered = it.filter { c -> c.isDigit() }
                                val newValue = filtered.toIntOrNull() ?: 0
                                if (item.totalChapters == 0 || newValue <= item.totalChapters) {
                                    editValue = filtered
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(48.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        IconButton(onClick = {
                            val newValue = editValue.toIntOrNull() ?: item.chapter
                            onUpdate(mapOf("chapter" to newValue))
                            if (item.totalChapters > 0 && newValue == item.totalChapters) {
                                onUpdate(mapOf("status" to "Completed"))
                            }
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar", modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Text(
                            text = "Cap. ${item.chapter}${if(item.totalChapters > 0) " / ${item.totalChapters}" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { isEditing = true; editValue = item.chapter.toString() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                        }
                        FilledTonalButton(
                            onClick = { 
                                val nextChapter = item.chapter + 1
                                if (item.totalChapters == 0 || nextChapter <= item.totalChapters) {
                                    onUpdate(mapOf("chapter" to nextChapter))
                                    if (item.totalChapters > 0 && nextChapter == item.totalChapters) {
                                        onUpdate(mapOf("status" to "Completed"))
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp),
                            enabled = item.totalChapters == 0 || item.chapter < item.totalChapters
                        ) {
                            Text("+1", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(itemId: String, viewModel: TrackerViewModel = viewModel(), onBack: () -> Unit) {
    val items by viewModel.lista.collectAsState()
    val item = items.find { it.id == itemId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.size(width = 180.dp, height = 260.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(item.genres) { genre ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(genre) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(item.status) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = if (item.status == "Completed") Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Capítulos: ${item.chapter} / ${if(item.totalChapters > 0) item.totalChapters else "?"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { viewModel.deleteItem(item.id); onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar de mi lista")
                }
            }
        }
    }
}
