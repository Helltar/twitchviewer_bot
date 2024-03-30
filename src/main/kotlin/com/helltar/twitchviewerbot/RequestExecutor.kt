package com.helltar.twitchviewerbot

import com.annimon.tgbotsmodule.commands.context.MessageContext
import kotlinx.coroutines.*

object RequestExecutor {

    private val requestsList = hashMapOf<String, Job>()

    fun addRequest(requestKey: String, ctx: MessageContext, block: () -> Unit) {
        if (requestsList.containsKey(requestKey))
            if (requestsList[requestKey]?.isCompleted == false) {
                ctx.replyToMessage()
                    .setText(Strings.localizedString(Strings.MANY_REQUEST, ctx.user().id))
                    .callAsync(ctx.sender)

                return
            }

        requestsList[requestKey] =
            CoroutineScope(Dispatchers.IO)
                .launch(CoroutineName(requestKey)) {
                    block()
                }
    }
}