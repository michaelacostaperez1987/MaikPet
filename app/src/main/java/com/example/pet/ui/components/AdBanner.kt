package com.example.pet.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-9690126773049877/7454361529"
) {
    val context = LocalContext.current
    
    Box(modifier = modifier.height(60.dp)) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            factory = { ctx ->
                AdView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                }
            }
        )
    }
}

@Composable
fun AdBannerSmall(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-9690126773049877/7454361529"
) {
    val context = LocalContext.current
    
    Box(modifier = modifier.height(60.dp)) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            factory = { ctx ->
                AdView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    
                    val adRequest = AdRequest.Builder().build()
                    loadAd(adRequest)
                }
            }
        )
    }
}
