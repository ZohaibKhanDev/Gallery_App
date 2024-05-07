package com.example.galleryapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {
    private var photosItems: List<String> by mutableStateOf(emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryAppTheme {

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
        val videosList = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val videosProjection = arrayOf(MediaStore.Video.Media._ID)

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
        )

        for (item in photosItems) {
            val pair = item.split(",")
            val uri = pair[0].substring(1) // Removing the leading "("
            val type = pair[1].substring(0, pair[1].length - 1) // Removing the trailing ")"
            if (type == "video") {
                videosList.add(uri)
            } else {
                photosList.add(uri)
            }
        }
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

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videosProjection, null, null, null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (cursor.moveToNext()) {
                val videoId = cursor.getLong(idColumn)
                val videoUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId
                )
                videosList.add(Pair(videoUri.toString(), "video").toString())
            }
        }


        return photosList
    }


    @RequiresApi(Build.VERSION_CODES.O)
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


    @RequiresApi(Build.VERSION_CODES.O)
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

            composable(Screen.PicDetail.route + "/{pic}", arguments = listOf(navArgument("pic") {
                type = NavType.StringType
            })) {
                val pic = it.arguments?.getString("pic")
                DetailScreen(navController, pic)
            }

            composable(Screen.VideoDetail.route) {
                VideoDetail(navController = navController, videos = photosItems)
            }

        }

    }

    fun performDeletion(context: Context, imageUri: Uri?) {
        imageUri?.let {
            try {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        context, "Permission denied. Cannot delete image.", Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val contentResolver = context.contentResolver
                val deletedRows = contentResolver.delete(imageUri, null, null)
                if (deletedRows > 0) {
                    Log.d("DeletionSuccess", "Image deleted successfully")
                } else {
                    Log.e("DeletionError", "No image deleted")
                }
            } catch (e: Exception) {
                Log.e("DeletionError", "Error deleting image: ${e.message}")
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun DetailScreen(navController: NavController, pic: String?) {
        val context = LocalContext.current
        var more by remember {
            mutableStateOf(false)
        }
        val wallpaperManager = WallpaperManager.getInstance(context)
        val coroutineScope = rememberCoroutineScope()

        Scaffold(topBar = {
            TopAppBar(title = {
                Icon(imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { navController.popBackStack() })
            }, colors = TopAppBarDefaults.topAppBarColors(Color.Black), actions = {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "",
                    modifier = Modifier.padding(end = 10.dp),
                    tint = Color.White
                )
            })
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
                        .padding(top = it.calculateTopPadding())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.clickable {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$pic")
                                type = "image/*"
                            }

                            val shareIntentActivities: List<ResolveInfo> =
                                context.packageManager.queryIntentActivities(
                                    sendIntent, PackageManager.MATCH_DEFAULT_ONLY
                                )

                            if (shareIntentActivities.isNotEmpty()) {
                                context.startActivity(Intent.createChooser(sendIntent, "Share"))
                            } else {
                                Toast.makeText(
                                    context,
                                    "No app available to handle share action",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(
                            text = "Share",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
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

                        Text(
                            text = "Favourite",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
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

                        Text(
                            text = "Edit",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(
                        modifier = Modifier.clickable {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                performDeletion(context, Uri.parse(pic))
                            } else {
                                registerActivityResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "",
                            tint = Color.White
                        )

                        Text(
                            text = "Delete",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
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

                        Text(text = "More",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { more = !more })

                        DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                            DropdownMenuItem(text = {
                                Text(text = "Move")
                            }, onClick = {

                            })

                            DropdownMenuItem(text = {
                                Text(text = "Rename")
                            }, onClick = { })

                            DropdownMenuItem(text = {
                                Text(text = "Set as Wallpaper")
                            }, onClick = {

                                coroutineScope.launch {
                                    val wallpaperManager = WallpaperManager.getInstance(context)
                                    try {
                                        val bitmap = loadBitmapFromUri(context, Uri.parse(pic))
                                        if (bitmap != null) {
                                            setWallpaper(wallpaperManager, bitmap)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }

                            })

                            DropdownMenuItem(text = {
                                Text(text = "set as contact avatar")
                            }, onClick = { })

                            DropdownMenuItem(text = {
                                Text(text = "hide")
                            }, onClick = { })

                            DropdownMenuItem(text = {
                                Text(text = "Details")
                            }, onClick = { })

                        }
                    }
                }


            }

        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        return inputStream?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    private fun setWallpaper(wallpaperManager: WallpaperManager, bitmap: Bitmap) {
        wallpaperManager.setBitmap(bitmap)
    }

    @Composable
    fun BottomNavigation(navController: NavController) {
        val items = listOf(
            Screen.Photos, Screen.Albums, Screen.Explore
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun NavEntry() {
        val navController = rememberNavController()
        Scaffold(bottomBar = { BottomNavigation(navController = navController) }) {
            Navigation(navController = navController)
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun Albums(navController: NavController) {
        var search by remember {
            mutableStateOf("")
        }
        Scaffold(topBar = {
            CenterAlignedTopAppBar(title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = search,
                        onValueChange = {
                            search = it
                        },
                        placeholder = {
                            Text(text = "Search", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        },
                        textStyle = TextStyle(
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedContainerColor = Color.LightGray.copy(alpha = 0.50f),
                            unfocusedContainerColor = Color.LightGray.copy(alpha = 0.50f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "",
                        modifier = Modifier.size(30.dp)
                    )
                }
            })
        }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = it.calculateTopPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.clickable { navController.navigate(Screen.Photos.route) },
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .background(Color.White)
                                .clip(RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(text = "All", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Column(
                        modifier = Modifier.clickable { navController.navigate(Screen.VideoDetail.route) },
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .background(Color.White)
                                .clip(RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )


                        }

                        Text(text = "Videos", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                                .background(Color.White)
                                .clip(RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.folder),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize(), contentScale = ContentScale.Crop
                            )
                        }

                        Text(text = "Favourite", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)

                    }


                }
            }
        }
    }

    @Composable
    fun VideoDetail(navController: NavController, videos: List<String>) {
        val context = LocalContext.current
        val exoPlayer = remember { ExoPlayer.Builder(context).build() }

        DisposableEffect(Unit) {
            val mediaItem = if (videos.isNotEmpty()) {
                MediaItem.fromUri(videos[0])
            } else {
                MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            onDispose {
                exoPlayer.release()
            }
        }

        val playerView = remember { StyledPlayerView(context) }
        playerView.player = exoPlayer

        AndroidView(factory = { playerView })
    }


    /*    @Composable
    fun VideoItem(videoUri: String) {
        var isPlaying by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isPlaying = !isPlaying
                }
        ) {
            val uri = videoUri.toUri()
            val thumbnailUri = getThumbnailUri(context, uri)
            Image(
                painter = painterResource(id = com.google.android.exoplayer2.R.drawable.exo_icon_previous),
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp).size(20.dp),
                contentScale = ContentScale.Crop
            )
            if (isPlaying) {
                val exoPlayer = SimpleExoPlayer.Builder(context).build()
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)

                val playerView = StyledPlayerView(context)
                playerView.player = exoPlayer

                DisposableEffect(AndroidView(factory = { playerView })) {

                    exoPlayer.prepare()
                    exoPlayer.play()
                    onDispose {
                        exoPlayer.release()
                    }

                }
            }
        }
    }*/

    fun getThumbnailUri(context: Context, videoUri: Uri): Uri? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)

            val bitmap = retriever.getFrameAtTime()

            val thumbnailFile = File(context.cacheDir, "thumbnail.jpg")
            val outputStream = FileOutputStream(thumbnailFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            return Uri.fromFile(thumbnailFile)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null
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

