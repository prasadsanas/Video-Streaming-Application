package com.example.videostreamingapp;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;


import java.util.Locale;

public class ViewHolder extends RecyclerView.ViewHolder {

    SimpleExoPlayer exoPlayer;
    SimpleExoPlayerView playerView;

    public ViewHolder(@NonNull View itemView) {
        super (itemView);
    }

    public void setExoplayer(Application application, String name, String Videourl){

        TextView textView = itemView.findViewById (R.id.tv_item_name);
        playerView = itemView.findViewById (R.id.exoplayer_item);

        textView.setText (name);

        try {
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector (new AdaptiveTrackSelection.Factory (bandwidthMeter));
            exoPlayer = (SimpleExoPlayer) ExoPlayerFactory.newSimpleInstance (application,trackSelector);
            Uri video = Uri.parse (Videourl);
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory ("video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory ();
            MediaSource mediaSource = new ExtractorMediaSource (video,dataSourceFactory,extractorsFactory,null,null);
            playerView.setPlayer (exoPlayer);
            exoPlayer.prepare (mediaSource);
            exoPlayer.setPlayWhenReady (false);
        }catch (Exception e){
            Log.e ("ViewHolder","exoplayer error"+ e.toString ());
        }
    }
}
