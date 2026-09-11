package io.nekohasekai.sagernet.database

data class SubscriptionInfo(
    val usedBytes: Long?,
    val remainingBytes: Long?,
    val expireAtSeconds: Long?,
)

fun SubscriptionBean.parseInfo(): SubscriptionInfo {
    val values = subscriptionUserinfo.orEmpty()
        .split(';', '\n')
        .mapNotNull { part ->
            val pieces = part.trim().split('=', limit = 2)
            if (pieces.size != 2) null
            else pieces[0].trim().lowercase() to pieces[1].trim().toLongOrNull()
        }
        .filter { it.second != null }
        .associate { it.first to it.second!! }

    val upload = values["upload"]
    val download = values["download"]
    val used = when {
        upload == null && download == null -> null
        else -> runCatching { Math.addExact(upload ?: 0L, download ?: 0L) }.getOrNull()
    }
    val total = values["total"]
    val remaining = if (total == null) null else (total - (used ?: 0L)).coerceAtLeast(0L)
    val expiry = values["expire"]?.takeIf { it > 0L }
    return SubscriptionInfo(used, remaining, expiry)
}
