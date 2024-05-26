<p align="center">
    <img src="https://helltar.com/projects/twitchviewer_bot/img/botpic-circle.png" alt="botpic" width="12%"/>
    <br><br>
    <a href="https://t.me/twitchviewer_bot"><img src="https://helltar.com/projects/twitchviewer_bot/img/qr.png" alt="qr_code" width="50%"/></a>
</p>

```bash
docker run --rm -d \
  --name twitchviewerbot \
  -e CREATOR_ID=12345 \
  -e BOT_TOKEN=123:xxx \
  -e BOT_USERNAME=name_bot \
  -e TWITCH_TOKEN=qwerty \
  -v twitchviewerbot_data:/app/data \
  ghcr.io/helltar/twitchviewerbot:latest
```
