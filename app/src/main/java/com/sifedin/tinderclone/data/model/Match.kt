package com.sifedin.tinderclone.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Match(
    val id: String = "",
    val users: List<String> = emptyList(),
    @get:PropertyName("lastMessage") @set:PropertyName("lastMessage")
    var lastMessage: String? = null,
    @get:PropertyName("lastMessageTimestamp") @set:PropertyName("lastMessageTimestamp")
    var lastMessageTimestamp: Date? = null,
    @get:PropertyName("lastMessageSenderId") @set:PropertyName("lastMessageSenderId")
    var lastMessageSenderId: String? = null,
    @get:PropertyName("seenBy") @set:PropertyName("seenBy")
    var seenBy: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null
)