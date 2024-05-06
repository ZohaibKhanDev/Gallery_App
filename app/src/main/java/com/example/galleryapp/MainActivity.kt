package com.example.galleryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.galleryapp.ui.theme.GalleryAppTheme
import java.util.Calendar
import java.util.Date

class MainActivity : ComponentActivity() {
    private var photosItems: List<String> by mutableStateOf(emptyList())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryAppTheme {
                val calendar = Calendar.getInstance()
                calendar.time = Date()
                val toDate = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val fromDate = calendar.timeInMillis
                NavEntry()

            }
        }
        requestPermission()
    }

    fun requestPermission() {
        permissionGaranted(this, Manifest.permission.READ_MEDIA_IMAGES) {
            if (it) {

                photosItems = getPhotosList(applicationContext)
            } else {
                registerActivityResult.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    val registerActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { garanted ->
            if (garanted) {
                photosItems = getPhotosList(applicationContext)
            }
        }


    fun getPhotosList(context: Context): List<String> {
        val photosList = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID)

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
        )

        cursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val photoId = cursor.getLong(idColumn)
                val photoUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photoId
                )
                photosList.add(photoUri.toString())
            }
        }

        return photosList
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun Photos(photos: List<String>, navController: NavController) {
        var textFieldState by remember { mutableStateOf("") }
        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(value = textFieldState, onValueChange = {
                        textFieldState = it
                    }, colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.34F),
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.34F),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White
                    ), modifier = Modifier.height(50.dp), placeholder = {
                        Text(text = "Search", fontSize = 15.sp)
                    }, textStyle = TextStyle(
                        fontSize = 15.sp
                    ), shape = RoundedCornerShape(12.dp), leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "",
                            modifier = Modifier.size(23.dp)
                        )
                    })

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "",
                        modifier = Modifier

                            .align(Alignment.CenterVertically)
                            .padding(start = 22.dp)
                            .size(30.dp)
                    )
                }

            })
        }) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = it.calculateTopPadding(), bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(photos) { photo ->
                    AsyncImage(
                        model = photo,
                        contentDescription = "",
                        modifier = Modifier
                            .clickable {
                                navController.navigate(
                                    Screen.PicDetail.route + "/${
                                        Uri.encode(
                                            photo
                                        )
                                    }"
                                )
                            }
                            .aspectRatio(1f)
                            .size(120.dp),
                        contentScale = ContentScale.Crop,
                    )
                }

            }
        }
    }


    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController = navController, startDestination = Screen.Photos.route) {
            composable(Screen.Photos.route) {
                Photos(photos = photosItems, navController)
            }
            composable(Screen.Albums.route) {
                Albums(navController = navController)
            }
            composable(Screen.Explore.route) {
                Explore(navController = navController)
            }

            composable(Screen.PicDetail.route + "/{pic}",
                arguments = listOf(
                    navArgument("pic") {
                        type = NavType.StringType
                    }
                )
            ) {
                val pic = it.arguments?.getString("pic")
                DetailScreen(navController, pic)
            }

        }

    }

    fun performDeletion(context: Context, imageUri: Uri) {
        try {
            val contentResolver = context.contentResolver
            // Delete the image from the content provider
            val deletedRows = contentResolver.delete(imageUri, null, null)
            if (deletedRows > 0) {
                Log.d("DeletionSuccess", "Image deleted successfully")
            } else {
                Log.d("DeletionFailure", "No image deleted")
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during deletion
            Log.e("DeletionError", "Error deleting image: ${e.message}")
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun DetailScreen(navController: NavController, pic: String?) {
        val context = LocalContext.current
        Scaffold(topBar = {
            TopAppBar(title = {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { navController.popBackStack() })
            },
                colors = TopAppBarDefaults.topAppBarColors(Color.Black), actions = {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "",
                        modifier = Modifier.padding(end = 10.dp), tint = Color.White
                    )
                }
            )
        }) {
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = pic,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.aspectRatio(1f)
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(text = "Share", fontSize = 9.sp, color = Color.White)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(text = "Favourite", fontSize = 9.sp, color = Color.White)
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(text = "Edit", fontSize = 9.sp, color = Color.White)
                    }

                    Column(
                        modifier = Modifier.clickable {
                            performDeletion(context, Uri.parse(pic))
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(text = "Delete", fontSize = 9.sp, color = Color.White)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(text = "More", fontSize = 9.sp, color = Color.White)
                    }
                }


            }

        }
    }


    @Composable
    fun BottomNavigation(navController: NavController) {
        val items = listOf(
            Screen.Photos,
            Screen.Albums,
            Screen.Explore
        )

        NavigationBar {
            val navStack by navController.currentBackStackEntryAsState()
            val current = navStack?.destination?.route
            items.forEach {
                NavigationBarItem(selected = current == it.route, onClick = {
                    navController.navigate(it.route) {
                        navController.graph.let {
                            it.route?.let { it1 -> popUpTo(it1) }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }, icon = {
                    if (current == it.route) {
                        Icon(imageVector = it.selectedIcon, contentDescription = "")
                    } else {
                        Icon(imageVector = it.unSelectedIcon, contentDescription = "")
                    }
                }, label = {
                    if (current == it.route) {
                        Text(text = it.title, color = Color.Black)
                    } else {
                        Text(text = it.title, color = Color.Gray)
                    }
                })
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun NavEntry() {
        val navController = rememberNavController()
        Scaffold(bottomBar = { BottomNavigation(navController = navController) }) {
            Navigation(navController = navController)
        }
    }


    @Composable
    fun Albums(navController: NavController) {

    }

    @Composable
    fun Explore(navController: NavController) {

    }

}


inline fun permissionGaranted(context: Context, permission: String, call: (Boolean) -> Unit) {
    if (ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        call.invoke(true)
    } else {
        call.invoke(false)
    }
}

