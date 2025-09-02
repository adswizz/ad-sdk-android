package com.adswizz.universalplayer.models

import android.net.Uri

object DB {
    private val podcastList = listOf(
        "RAD_1, 2 prerolls, http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_1.mp3",
        "Podcast RAD_QueenBohemian, 1 prerol and 1 postroll, http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_QueenBohemian.mp3",
        "RAD_muchosEvents, many rad events, http://sdk.mobile.streaming.adswizz.com:8501/podcast_RAD_muchosEvents.mp3",
        "RAD_brokenTS, large file, http://sdk.mobile.streaming.adswizz.com:8501/podcast_LARGE_large.mp3",
    )

    private val serverSideList = listOf(
        "shakeDemo, HLS stream, https://streaming.sdk.eks.devel.adswizz.com/shakeDemo/playlist.m3u8",
        "shakeCoupon, ICY stream, https://streaming.sdk.eks.devel.adswizz.com/shakeCoupon",
        "shakeCoupon, HLS stream, https://streaming.sdk.eks.devel.adswizz.com/shakeDemo/playlist.m3u8",
        "voice ads, Shake calendar voice navigation, https://streaming.sdk.eks.devel.adswizz.com/voiceNavigation_shakeCalendar"
    )

    /*
          ad zones ?
     */
    private val clientSideList = mapOf<String, String>(
        "17873" to "shake me, open link",
        "17880" to "shake me, send email",
        "17866" to "voice ads, get in calendar",
        "17867" to "voice ads, place a call"
    )

    fun getPodcastList(): List<MediaData>{
        return podcastList.map{
            val (title, subtitle, uri) = it.split(",")
//            val title = uri.lastPathSegment?.split(".")?.firstOrNull()?.trim() ?: ""
            MediaData(title.trim() , title.trim(), subtitle.trim(), uri.trim(), Uri.parse(uri.trim()), false)
        }

    }

    fun getServerSideList():  List<MediaData>{
        return serverSideList.map{
            val (title, subtitle, uri) = it.split(",")
            MediaData(title.trim() , title.trim(), subtitle.trim(), uri.trim(), Uri.parse(uri.trim()), false)
        }
    }

    fun getClientSideList(): List<MediaData>{
        return clientSideList.map{ entry ->
            val (title, subtitle) = entry.value.split(",")
            MediaData(entry.key , title.trim(), subtitle.trim(), "", Uri.parse(""), false)
        }
    }
}