package com.example.bakingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.squareup.picasso.Picasso;

public class StepDetailsFragment extends Fragment implements ExoPlayer.EventListener {

    public static final String TAG = StepDetailsFragment.class.getSimpleName();
    public static final String KEY_PLAYER_POSITION = "player-position";
    public static final String KEY_PLAYER_STATE = "player-state";

    private FragmentStepDetailsBinding mBinder;
    private Context mContext;

    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private OnNextOrPreviousStepClickListener mOnNextOrPreviousStepClicked;

    private long mPlayerPosition;
    private boolean mPlayerState;
    private StepData mStepData;

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

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_PLAYER_POSITION)) {
                mPlayerPosition = savedInstanceState.getLong(KEY_PLAYER_POSITION);
            }
            if (savedInstanceState.containsKey(KEY_PLAYER_STATE)) {
                mPlayerState = savedInstanceState.getBoolean(KEY_PLAYER_STATE);
            }
        }else{
            mPlayerPosition = 0;
            mPlayerState = true;
        }

        mBinder = FragmentStepDetailsBinding.inflate(inflater, container, false);

        mStepData = RecipeDataUtils.getInstance().getCurrentStepData();

        if (mStepData == null) {
            throw new NullPointerException("Current step data is null");
        }

        mBinder.stepDescriptionTv.setText(mStepData.getDescription());
        mBinder.stepVideoPlayer.setDefaultArtwork(BitmapFactory.decodeResource
                (getResources(), R.drawable.movie_loading_image));

        // Show thumbnail image if exists
        if (mStepData.getThumbnail_url() != null && !mStepData.getThumbnail_url().equals("")) {
            mBinder.stepThumbnailIv.setVisibility(View.VISIBLE);
            Picasso.get().load(mStepData.getThumbnail_url()).into(mBinder.stepThumbnailIv);
        } else {
            mBinder.stepThumbnailIv.setVisibility(View.INVISIBLE);
        }

        mPlayerView = mBinder.stepVideoPlayer;

        // show next/previous arrows
        if (RecipeDataUtils.getInstance().hasNextStep()) {
            mBinder.nextStepIv.setVisibility(View.VISIBLE);
            mBinder.nextStepIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnNextOrPreviousStepClicked != null) {
                        boolean nextStepBtnClicked = getContext().getResources().getBoolean(R.bool.next_step_arrow_clicked);
                        mOnNextOrPreviousStepClicked.onClick(nextStepBtnClicked);
                    }
                }
            });
        } else {
            mBinder.nextStepIv.setVisibility(View.INVISIBLE);
        }

        if (RecipeDataUtils.getInstance().hasPreviousStep()) {
            mBinder.previusStepIv.setVisibility(View.VISIBLE);
            mBinder.previusStepIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnNextOrPreviousStepClicked != null) {
                        boolean previousStepBtnClicked = getContext().getResources().getBoolean(R.bool.previous_step_arrow_clicked);
                        mOnNextOrPreviousStepClicked.onClick(previousStepBtnClicked);
                    }
                }
            });
        } else {
            mBinder.previusStepIv.setVisibility(View.INVISIBLE);
        }

        return mBinder.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(KEY_PLAYER_POSITION, mPlayerPosition);
        outState.putBoolean(KEY_PLAYER_STATE, mPlayerState);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            // Show Media Player if no video url is set
            if (mStepData.getVideo_url() != null && !mStepData.getVideo_url().equals("")){
                mBinder.stepVideoPlayer.setVisibility(View.VISIBLE);
                initializeMediaSession();
                initializePlayer(Uri.parse(mStepData.getVideo_url()));
            }else{
                mBinder.stepVideoPlayer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || mExoPlayer == null)) {

            // Show Media Player if no video url is set
            if (mStepData.getVideo_url() != null && !mStepData.getVideo_url().equals("")){
                mBinder.stepVideoPlayer.setVisibility(View.VISIBLE);
                initializeMediaSession();
                initializePlayer(Uri.parse(mStepData.getVideo_url()));
            }else{
                mBinder.stepVideoPlayer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            releasePlayer();
            if (mMediaSession != null) {
                mMediaSession.setActive(false);
                mMediaSession.release();
                mMediaSession = null;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Util.SDK_INT > 23) {
            releasePlayer();
            if (mMediaSession != null) {
                mMediaSession.setActive(false);
                mMediaSession.release();
                mMediaSession = null;
            }
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
            String userAgent = Util.getUserAgent(mContext, "BakingApp");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    mContext, userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(mPlayerState);
            mExoPlayer.seekTo(mPlayerPosition);
        }
    }

    private void releasePlayer() {
        if(mExoPlayer == null) return;

        updatePlayerTrackers();
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    private void updatePlayerTrackers(){
        if(mExoPlayer == null) return;

        mPlayerState = mExoPlayer.getPlayWhenReady();
        mPlayerPosition = mExoPlayer.getCurrentPosition();
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
