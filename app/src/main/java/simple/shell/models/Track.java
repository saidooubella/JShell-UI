package simple.shell.models;

import android.net.*;

public final class Track {

	public final String name;
	public final String artist;
	public final Uri uri;

	public Track(String name, String artist, Uri uri) {
		this.name = name;
		this.artist = artist;
		this.uri = uri;
	}
}
