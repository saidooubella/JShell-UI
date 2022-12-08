package simple.shell.media;

import android.os.*;
import android.support.v4.media.*;
import android.support.v4.media.MediaBrowserCompat.*;
import android.support.v4.media.session.*;
import java.util.*;

public final class MediaPlaybackService extends MediaBrowserServiceCompat {

	private static final String MEDIA_ROOT_ID = "simple.shell.MEDIA_ROOT_ID";

	private PlaybackStateCompat.Builder stateBuilder;
	private MediaSessionCompat mediaSession;

	@Override
	public void onCreate() {
		super.onCreate();

        mediaSession = new MediaSessionCompat(this, "Shell");

        mediaSession.setFlags(
			MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
			MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder();
		stateBuilder.setActions(
			PlaybackStateCompat.ACTION_PLAY |
			PlaybackStateCompat.ACTION_PLAY_PAUSE |
			PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
			PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
			PlaybackStateCompat.ACTION_STOP);

        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new SessionCallback());

        setSessionToken(mediaSession.getSessionToken());
	}

	@Override
	public BrowserRoot onGetRoot(String p1, int p2, Bundle p3) {
		return new BrowserRoot(MEDIA_ROOT_ID, null);
	}

	@Override
	public void onLoadChildren(String p1, Result<List<MediaItem>> p2) {
		
	}

	private final class SessionCallback extends MediaSessionCompat.Callback {

		@Override
		public void onPlay() {

		}

		@Override
		public void onPause() {

		}

		@Override
		public void onStop() {

		}

		@Override
		public void onSkipToNext() {

		}

		@Override
		public void onSkipToPrevious() {

		}
	}
}
