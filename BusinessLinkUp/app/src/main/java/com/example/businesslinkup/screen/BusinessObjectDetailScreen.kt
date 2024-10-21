package com.example.businesslinkup.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.businesslinkup.entities.BusinessObject
import com.example.businesslinkup.entities.Comment
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.example.businesslinkup.viewModels.UserViewModel
import java.util.UUID
import androidx.compose.runtime.LaunchedEffect
import com.example.businesslinkup.entities.User
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessObjectDetailScreen(
    navController: NavController,
    businessViewModel: BusinessObjectViewModel,
    userViewModel: UserViewModel,
    objectId: String
) {
    val businessObjectUser by businessViewModel.businessObjectUser.collectAsState()

    LaunchedEffect(objectId) {
        businessViewModel.loadBusinessObjectAndComments(objectId)
    }

    LaunchedEffect(Unit) {
        userViewModel.refreshCurrentUser()
    }

    val businessObject by businessViewModel.businessObject.collectAsState()
    val comments by businessViewModel.comments.collectAsState()
    val commentAuthors by businessViewModel.commentAuthors.collectAsState()
    val newCommentText = remember { mutableStateOf("") }
    val currentUser by userViewModel.current.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = businessObject?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            item {
                businessObject?.let {
                    Image(
                        painter = rememberImagePainter(it.photoUrl),
                        contentDescription = "Business Object Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it.type.name ?: "", style = MaterialTheme.typography.titleSmall)
                    Text(text = it.title ?: "", style = MaterialTheme.typography.titleLarge)
                    Text(text = it.desc ?: "", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    businessObjectUser?.let { user ->
                        Text(text = "Added by: ${user.firstName} ${user.lastName}, points: ${user.points}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

            }

            item {
                Text(text = "Comments", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
            }


            items(comments.sortedBy{it.timestamp}) { comment ->
                val authorUser = commentAuthors[comment.authorId]
                authorUser?.let { author ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = rememberImagePainter(author.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "${author.firstName} ${author.lastName}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = comment.text, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BasicTextField(
                    value = newCommentText.value,
                    onValueChange = { newCommentText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                )
                Button(onClick = {
                    currentUser?.id?.let { id ->
                        val comment = Comment(
                            id = UUID.randomUUID().toString(),
                            objectId = objectId,
                            authorId = id,
                            targetUserId = businessObject?.userId ?: "",
                            text = newCommentText.value,
                            timestamp = Timestamp.now()
                        )
                        businessViewModel.addComment(comment)
                        newCommentText.value = ""
                        businessViewModel.getCommentsByObjectId(objectId) // Refresh comments
                    } ?: println("Current user is null")
                }) {
                    Text(text = "Add Comment")
                }
            }
        }
    }
}