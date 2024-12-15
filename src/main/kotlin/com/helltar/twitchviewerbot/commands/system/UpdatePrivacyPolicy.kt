package com.helltar.twitchviewerbot.commands.system

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.twitchviewerbot.Config
import com.helltar.twitchviewerbot.Strings
import com.helltar.twitchviewerbot.commands.BotCommand
import com.helltar.twitchviewerbot.db.dao.privacyPoliciesDao

class UpdatePrivacyPolicy(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (userId != Config.creatorId)
            return

        if (argumentsString.isBlank()) {
            replyToMessage(localizedString(Strings.UPDATE_PRIVACY_POLICY_EXAMPLE))
            return
        }

        privacyPoliciesDao.update(argumentsString)

        replyToMessage(localizedString(Strings.PRIVACY_POLICY_SUCCESFULLY_UPDATED))
    }
}
