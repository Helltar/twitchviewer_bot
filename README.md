<p align="center">
    <img src="https://helltar.com/projects/twitchviewer_bot/img/botpic-circle.png" alt="botpic" width="12%"/>
    <br><br>
    <a href="https://t.me/twitchviewer_bot"><img src="https://helltar.com/projects/twitchviewer_bot/img/qr.png" alt="qr_code" width="50%"/></a>
</p>

Edit the **.env** file by specifying the tokens for the bot, and also provide the address and auth. data for **PostgreSQL**:

- CREATOR_ID: your Telegram user-ID
- BOT_TOKEN & BOT_USERNAME: [BotFather](https://t.me/BotFather)
- TWITCH_CLIENT_ID & TWITCH_CLIENT_SECRET: [Twitch Developer Console](https://dev.twitch.tv/console/apps/create)

```bash
docker run --rm -d --name twitchviewerbot --env-file .env ghcr.io/helltar/twitchbot:latest
```

### Commands

- **/list** - Show your favorite channels
- **/live** - Check who is online from your list
- **/clip** - Get short clips from all channels on your list
- **/screen** - Get a screenshot from a channel
- **/add** - Add a channel to your list
- **/updateprivacy** - Update bot privacy policy (_/privacy_)
