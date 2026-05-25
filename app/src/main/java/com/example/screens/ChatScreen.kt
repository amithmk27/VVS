package com.example.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.TextDark
import com.example.viewmodel.ChatViewModel
import com.example.widgets.BrahminAvatar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val activeRecipientId by chatViewModel.activeRecipientId.collectAsState()
    val chatSessions by chatViewModel.chatSessions.collectAsState()
    val activeMessages by chatViewModel.activeMessages.collectAsState()

    if (activeRecipientId != null) {
        // Mode 2: Specific Private Conversation Thread Box
        val selectedPartnerId = activeRecipientId!!
        val sessionInfo = chatSessions.firstOrNull { it.recipientId == selectedPartnerId }
        val partnerName = sessionInfo?.recipientName ?: "Brahmin Match"
        val partnerAvatarUrl = sessionInfo?.recipientAvatarUrl

        PrivateChatThreadView(
            partnerId = selectedPartnerId,
            partnerName = partnerName,
            partnerAvatarUrl = partnerAvatarUrl,
            messages = activeMessages,
            onSendMessage = { chatViewModel.sendMessage(it) },
            onBack = { chatViewModel.setActiveChatRecipient(null) },
            onViewProfile = { onNavigateToDetails(selectedPartnerId) }
        )
    } else {
        // Mode 1: Active Conversations List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("chat_list_view_root")
        ) {
            // Soft Green general banner top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.SoftGreen)
                    .padding(vertical = 18.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Vedic Chats Inbox",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            }

            if (chatSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Forum,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active chats yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Navigate to the 'Matches' tab, accept connection proposals, or send details to ignite text conversations!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(chatSessions, key = { it.recipientId }) { session ->
                        ChatSessionItemRow(
                            session = session,
                            onClick = {
                                chatViewModel.setActiveChatRecipient(session.recipientId)
                            }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSessionItemRow(
    session: ChatSession,
    onClick: () -> Unit
) {
    val relativeTime = getFormattedTime(session.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp)
            .testTag("chat_session_item_${session.recipientId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrahminAvatar(
            avatarUrl = session.recipientAvatarUrl,
            fullName = session.recipientName,
            size = 52.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.recipientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Text(
                    text = relativeTime,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.lastMessage,
                    fontSize = 13.sp,
                    color = if (session.unreadCount > 0) TextDark else Color.Gray,
                    fontWeight = if (session.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (session.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = session.unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatThreadView(
    partnerId: String,
    partnerName: String,
    partnerAvatarUrl: String?,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onViewProfile: () -> Unit
) {
    var textMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Automatic scrolling to latest messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onViewProfile)
                    ) {
                        BrahminAvatar(avatarUrl = partnerAvatarUrl, fullName = partnerName, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(partnerName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Online  •  Click to view bio", fontSize = 11.sp, color = GoldAccent)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PurplePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textMessage,
                        onValueChange = { textMessage = it },
                        placeholder = { Text("Write auspicious message...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .testTag("chat_input_textfield")
                    )

                    FloatingActionButton(
                        onClick = {
                            if (textMessage.isNotBlank()) {
                                onSendMessage(textMessage)
                                textMessage = ""
                            }
                        },
                        containerColor = PurplePrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAF5))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId != partnerId
                    ChatBubble(message = msg, isMe = isMe)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMe) PurplePrimary else Color.White
    val contentColor = if (isMe) Color.White else TextDark
    val corners = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                contentColor = contentColor,
                shape = corners,
                shadowElevation = if (isMe) 2.dp else 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = message.messageText,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun getFormattedTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
