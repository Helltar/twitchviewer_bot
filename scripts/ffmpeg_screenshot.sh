#!/bin/bash

yt_dlp_out=twitch_$2.mp4

mkdir -p temp
cd temp || exit

timeout -k 10 -s SIGINT 25 yt-dlp https://www.twitch.tv/"$1" -q -o "$yt_dlp_out"
timeout -k 5 15 ffmpeg -ss 00:00:17 -i "$yt_dlp_out" -vframes 1 out_"$2".jpg

rm "$yt_dlp_out"
