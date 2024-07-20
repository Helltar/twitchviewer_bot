package com.helltar.twitchviewerbot.commands.simple

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.commands.BotCommand
import com.helltar.twitchviewerbot.dao.PrivacyPoliciesDAO

class PrivacyCommand(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(PrivacyPoliciesDAO().getPolicyText())
    }
}