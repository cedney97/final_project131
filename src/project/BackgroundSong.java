package project;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import edu.princeton.cs.introcs.StdAudio;
import edu.princeton.cs.introcs.StdDraw;

public class BackgroundSong {
	private boolean stop = false;
	private AudioInputStream audioStream;
	private Clip clip;

	public BackgroundSong(File song) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		audioStream = AudioSystem.getAudioInputStream(song);
		clip = AudioSystem.getClip();
		clip.open(audioStream);
	}

	/**
	 * Starts playing the song in a new thread so that the caller can proceed
	 * without waiting for the song to end
	 */
	void play() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public void stop() {
		clip.stop();
	}

}
