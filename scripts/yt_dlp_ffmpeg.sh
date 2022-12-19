#!/bin/bash

yt_dlp_out=twitch_$2.mp4
ffmpeg_out=out_$2.mp4

mkdir -p temp
cd temp || exit

timeout -k 10 -s SIGINT 35 yt-dlp https://www.twitch.tv/"$1" -q -o "$yt_dlp_out"
timeout -k 5 -s SIGINT 60 ffmpeg -ss 00:00:16 -i "$yt_dlp_out" -c copy -loglevel quiet "$ffmpeg_out"

rm $yt_dlp_out

#mkdir -p archive
#cp "$ffmpeg_out" archive/"$1_$(date +%d-%m-%Y_%H-%M-%S).mp4"
