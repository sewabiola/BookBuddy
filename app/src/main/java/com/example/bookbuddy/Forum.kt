// File: Forum.kt
package com.example.bookbuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import java.util.*
import androidx.compose.material.icons.filled.Warning



data class ForumPost(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val comments: MutableList<Comment> = mutableListOf(),
    var isDeleted: Boolean = false,
    var flags: Int = 0
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val replies: MutableList<Comment> = mutableListOf(),
    var isDeleted: Boolean = false,
    var flags: Int = 0
)

data class ModerationAction(
    val moderatorId: String,
    val targetId: String,
    val targetType: String, // "post" or "comment"
    val action: String, // "delete", "restore", "flag", "note"
    val note: String?,
    val timestamp: Long = System.currentTimeMillis()
)

/* ---------------------------
   Database extensions (add only)
   --------------------------- */

fun BookBuddyDatabase.addForumStorage() {
    // noop placeholder so file references BookBuddyDatabase in compile-time,
    // real storage below is in companion object extension via top-level vals/functions
}

/* We'll create a private set of storage variables and functions using file-level scope.
   These do NOT overwrite BookBuddyDatabase; they just call it and add forum storage. */

private val forumPostsStorage = linkedMapOf<String, ForumPost>() // ordered
private val moderationLog = mutableListOf<ModerationAction>()

// Create a post
fun BookBuddyDatabase.createPost(title: String, body: String): ForumPost? {
    val user = getCurrentUser() ?: return null
    val post = ForumPost(
        authorId = user.userId.ifBlank { user.email },
        authorName = user.displayName.ifBlank { user.username.ifBlank { user.email } },
        title = title,
        body = body
    )
    forumPostsStorage[post.id] = post
    return post
}

// Reply to a post (top-level comment)
fun BookBuddyDatabase.replyToPost(postId: String, commentBody: String): Comment? {
    val user = getCurrentUser() ?: return null
    val post = forumPostsStorage[postId] ?: return null
    val comment = Comment(
        authorId = user.userId.ifBlank { user.email },
        authorName = user.displayName.ifBlank { user.username.ifBlank { user.email } },
        body = commentBody
    )
    post.comments.add(comment)
    return comment
}

// Reply to a comment (threaded)
fun BookBuddyDatabase.replyToComment(postId: String, parentCommentId: String, replyBody: String): Comment? {
    val user = getCurrentUser() ?: return null
    val post = forumPostsStorage[postId] ?: return null
    // find recursively
    fun findAndAdd(list: MutableList<Comment>): Comment? {
        for (c in list) {
            if (c.id == parentCommentId) {
                val reply = Comment(
                    authorId = user.userId.ifBlank { user.email },
                    authorName = user.displayName.ifBlank { user.username.ifBlank { user.email } },
                    body = replyBody
                )
                c.replies.add(reply)
                return reply
            } else {
                val r = findAndAdd(c.replies)
                if (r != null) return r
            }
        }
        return null
    }
    return findAndAdd(post.comments)
}

// Get all posts (latest first)
fun BookBuddyDatabase.getAllPosts(): List<ForumPost> {
    return forumPostsStorage.values.reversed().toList()
}

// Get single post by id
fun BookBuddyDatabase.getPostById(postId: String): ForumPost? {
    return forumPostsStorage[postId]
}

// Moderation: delete post or comment (soft delete)
fun BookBuddyDatabase.moderateDeletePost(postId: String, moderatorId: String, note: String? = null): Boolean {
    val post = forumPostsStorage[postId] ?: return false
    post.isDeleted = true
    moderationLog.add(ModerationAction(moderatorId, postId, "post", "delete", note))
    return true
}

fun BookBuddyDatabase.moderateRestorePost(postId: String, moderatorId: String, note: String? = null): Boolean {
    val post = forumPostsStorage[postId] ?: return false
    post.isDeleted = false
    moderationLog.add(ModerationAction(moderatorId, postId, "post", "restore", note))
    return true
}

fun BookBuddyDatabase.flagPost(postId: String, moderatorId: String? = null): Boolean {
    val post = forumPostsStorage[postId] ?: return false
    post.flags += 1
    moderationLog.add(ModerationAction(moderatorId ?: "system", postId, "post", "flag", null))
    return true
}

// Delete comment (soft)
fun BookBuddyDatabase.moderateDeleteComment(postId: String, commentId: String, moderatorId: String, note: String? = null): Boolean {
    val post = forumPostsStorage[postId] ?: return false
    fun findAndMark(comments: MutableList<Comment>): Boolean {
        for (c in comments) {
            if (c.id == commentId) {
                c.isDeleted = true
                moderationLog.add(ModerationAction(moderatorId, commentId, "comment", "delete", note))
                return true
            }
            if (findAndMark(c.replies)) return true
        }
        return false
    }
    return findAndMark(post.comments)
}

fun BookBuddyDatabase.getModerationLog(): List<ModerationAction> = moderationLog.toList()

/* ---------------------------
   UI Components (Compose)
   --------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(navController: NavHostController) {
    var showNewPost by remember { mutableStateOf(false) }
    val posts = remember { mutableStateListOf<ForumPost>() }

    LaunchedEffect(Unit) {
        posts.clear()
        posts.addAll(BookBuddyDatabase.getAllPosts())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Discussion Forum") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row {
                        TextButton(onClick = { showNewPost = true }) { Text("New Post") }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxWidth()
            .padding(12.dp)
        ) {
            if (posts.isEmpty()) {
                Text("No posts yet. Be the first to start a discussion!", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn {
                    items(posts) { post ->
                        PostCard(post = post, onOpenThread = {
                            // show thread screen: push a dialog-like bottom sheet or navigate to thread screen
                            navController.navigate("forum_thread/${post.id}")
                        })
                    }
                }
            }
        }
    }

    if (showNewPost) {
        NewPostDialog(
            onCreate = { title, body ->
                BookBuddyDatabase.createPost(title, body)
                // refresh list
                posts.clear()
                posts.addAll(BookBuddyDatabase.getAllPosts())
                showNewPost = false
            },
            onDismiss = { showNewPost = false }
        )
    }
}

@Composable
fun PostCard(post: ForumPost, onOpenThread: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (post.flags > 0) {
                    Text("⚑ ${post.flags}", color = Color.Red, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "by ${post.authorName} • ${Date(post.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (post.body.length > 240) post.body.take(240) + "..." else post.body,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onOpenThread) { Text("Open") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumThreadScreen(postId: String, navController: NavHostController) {
    val post = remember { mutableStateOf<ForumPost?>(BookBuddyDatabase.getPostById(postId)) }
    var newCommentBody by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var showModeration by remember { mutableStateOf(false) }
    val currentUser = BookBuddyDatabase.getCurrentUser()

    // Refresh on open
    LaunchedEffect(postId) {
        post.value = BookBuddyDatabase.getPostById(postId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(post.value?.title ?: "Thread") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row {
                        TextButton(onClick = { showModeration = true }) { Text("Moderation") }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(12.dp)
            .fillMaxWidth()
        ) {
            post.value?.let { p ->
                if (p.isDeleted) {
                    Text("This post has been removed by a moderator.", color = Color.Red)
                    return@Column
                }

                Text("by ${p.authorName} • ${Date(p.createdAt)}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(p.body, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Divider()

                // Comments
                Text("Comments", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ThreadedComments(
                    comments = p.comments,
                    onReply = { commentId ->
                        replyingToCommentId = commentId
                    },
                    onFlag = { commentId ->
                        // simple flag: find comment and increment flags
                        // we don't have direct API; we can reuse moderateDeleteComment with no deletion
                        BookBuddyDatabase.flagPost(postId) // flag post as proxy (quick approach)
                    },
                    onDeleteComment = { commentId ->
                        val moderatorId = currentUser?.userId ?: "system"
                        BookBuddyDatabase.moderateDeleteComment(postId, commentId, moderatorId, "Deleted by moderator")
                        post.value = BookBuddyDatabase.getPostById(postId)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reply box
                OutlinedTextField(
                    value = newCommentBody,
                    onValueChange = { newCommentBody = it },
                    label = { Text(if (replyingToCommentId == null) "Add a comment" else "Replying") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        replyingToCommentId = null
                        newCommentBody = ""
                    }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newCommentBody.isNotBlank()) {
                            if (replyingToCommentId == null) {
                                BookBuddyDatabase.replyToPost(postId, newCommentBody)
                            } else {
                                BookBuddyDatabase.replyToComment(postId, replyingToCommentId!!, newCommentBody)
                                replyingToCommentId = null
                            }
                            newCommentBody = ""
                            post.value = BookBuddyDatabase.getPostById(postId)
                        }
                    }) { Text("Post") }
                }
            }
        }
    }

    if (showModeration) {
        ModerationPanel(
            postId = postId,
            onDismiss = { showModeration = false },
            onAction = { action, note ->
                val moderatorId = BookBuddyDatabase.getCurrentUser()?.userId ?: "system"
                when (action) {
                    "delete_post" -> BookBuddyDatabase.moderateDeletePost(postId, moderatorId, note)
                    "restore_post" -> BookBuddyDatabase.moderateRestorePost(postId, moderatorId, note)
                }
                showModeration = false
            }
        )
    }
}

@Composable
fun ThreadedComments(
    comments: List<Comment>,
    onReply: (String) -> Unit,
    onFlag: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    depth: Int = 0
) {
    Column {
        comments.forEach { c ->
            if (c.isDeleted) {
                Text("(deleted)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${c.authorName} • ${Date(c.createdAt)}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(c.body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            TextButton(onClick = { onReply(c.id) }) { Text("Reply") }
                            TextButton(onClick = { onFlag(c.id) }) {
                                Icon(Icons.Default.Warning, contentDescription = "Flag Comment") }
                            TextButton(onClick = { onDeleteComment(c.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                        }
                    }
                }
            }
            if (c.replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.padding(start = (depth + 1) * 12.dp)) {
                    ThreadedComments(
                        comments = c.replies,
                        onReply = onReply,
                        onFlag = onFlag,
                        onDeleteComment = onDeleteComment,
                        depth = depth + 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NewPostDialog(onCreate: (String, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Post") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Body") }, modifier = Modifier.height(120.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && body.isNotBlank()) {
                    onCreate(title.trim(), body.trim())
                }
            }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ModerationPanel(postId: String, onDismiss: () -> Unit, onAction: (String, String?) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moderation") },
        text = {
            Column {
                Text("Take action on this thread:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") })
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onAction("delete_post", note) }) { Text("Delete Post") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onAction("restore_post", note) }) { Text("Restore Post") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
