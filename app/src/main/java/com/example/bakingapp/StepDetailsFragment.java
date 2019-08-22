package com.example.bakingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakingapp.databinding.FragmentStepDetailsBinding;
import com.example.bakingapp.data.StepData;
import com.example.bakingapp.utils.RecipeDataUtils;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class StepDetailsFragment extends Fragment implements ExoPlayer.EventListener {

    public static final String TAG = StepDetailsFragment.class.getSimpleName();

    private FragmentStepDetailsBinding mBinder;
    private Context mContext;

    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private OnNextOrPreviousStepClickListener mOnNextOrPreviousStepClicked;

    public interface OnNextOrPreviousStepClickListener {
        void onClick(boolean nextStep);
    }

    public StepDetailsFragment(){}

    public StepDetailsFragment(OnNextOrPreviousStepClickListener listener) {
        mOnNextOrPreviousStepClicked = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();

        mBinder = FragmentStepDetailsBinding.inflate(inflater, container, false);

        StepData step = RecipeDataUtils.getInstance().getCurrentStepData();

        if(step == null){
            throw new NullPointerException("Current step data is null");
        }

        mBinder.stepDescriptionTv.setText(step.getDescription());
        mBinder.stepVideoPlayer.setDefaultArtwork(BitmapFactory.decodeResource
                (getResources(), R.drawable.movie_loading_image));

        mPlayerView = mBinder.stepVideoPlayer;

        // show next/previous arrows
        if(RecipeDataUtils.getInstance().hasNextStep()){
            mBinder.nextStepIv.setVisibility(View.VISIBLE);
            mBinder.nextStepIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnNextOrPreviousStepClicked != null)
                    {
                        boolean nextStepBtnClicked = getContext().getResources().getBoolean(R.bool.next_step_arrow_clicked);
                        mOnNextOrPreviousStepClicked.onClick(nextStepBtnClicked);
                    }
                }
            });
        }else{
            mBinder.nextStepIv.setVisibility(View.INVISIBLE);
        }

        if(RecipeDataUtils.getInstance().hasPreviousStep()){
            mBinder.previusStepIv.setVisibility(View.VISIBLE);
            mBinder.previusStepIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnNextOrPreviousStepClicked != null)
                    {
                        boolean previousStepBtnClicked = getContext().getResources().getBoolean(R.bool.previous_step_arrow_clicked);
                        mOnNextOrPreviousStepClicked.onClick(previousStepBtnClicked);
                    }
                }
            });
        }else{
            mBinder.previusStepIv.setVisibility(View.INVISIBLE);
        }

        initializeMediaSession();
        initializePlayer(Uri.parse(step.getVideo_url()));

        return mBinder.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();

        if(mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        releasePlayer();
        if(mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        releasePlayer();
        if(mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if((playbackState == ExoPlayer.STATE_READY)){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }

        if(mMediaSession != null && mStateBuilder != null) {
            mMediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(mContext, TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }

    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(mContext, "ClassicalMusicQuiz");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    mContext, userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        if(mExoPlayer == null) return;

        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    }

    public static class MediaReceiver extends BroadcastReceiver {
        public MediaReceiver(){

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }
}
