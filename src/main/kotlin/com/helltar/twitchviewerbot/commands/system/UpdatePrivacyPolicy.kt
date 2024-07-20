package com.helltar.twitchviewerbot.commands.system

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.EnvConfig
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.BotCommand
import com.helltar.twitchviewerbot.dao.PrivacyPoliciesDAO

class UpdatePrivacyPolicy(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (userId != EnvConfig.creatorId)
            return

        if (argumentsAsString.isBlank()) {
            replyToMessage(localizedString(Strings.UPDATE_PRIVACY_POLICY_EXAMPLE))
            return
        }

        PrivacyPoliciesDAO().update(argumentsAsString)

        replyToMessage(localizedString(Strings.PRIVACY_POLICY_SUCCESFULLY_UPDATED))
    }
}