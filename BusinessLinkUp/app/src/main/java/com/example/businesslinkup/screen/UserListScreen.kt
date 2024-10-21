package com.example.businesslinkup.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import com.example.businesslinkup.R
import com.example.businesslinkup.entities.User
import com.example.businesslinkup.viewModels.UserViewModel

@Composable
fun UserListScreen(viewModel: UserViewModel) {
    val users by viewModel.usersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(users ?: emptyList()) { user ->
            UserItem(user)
        }
    }
}

@Composable
fun UserItem(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter: Painter = if (user.profilePictureUrl.isNotEmpty()) {
                rememberImagePainter(user.profilePictureUrl)
            } else {
                painterResource(id = R.drawable.icon)
            }
            Image(
                painter = painter,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${user.points}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserItem() {
    UserItem(
        User(
            id = "1",
            username = "john_doe",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            profilePictureUrl = "",
            points = 100
        )
    )
}
