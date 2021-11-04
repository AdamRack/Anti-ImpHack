package mod.imphack.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SongManager {

	private final List<ISound> songs = Collections.singletonList(ICopeBrickZebra.sound);

	private final ISound menuSong;
	private ISound currentSong;

	public SongManager() {
		this.menuSong = this.getRandomSong();
		this.currentSong = this.getRandomSong();
	}

	public ISound getMenuSong() {
		return this.menuSong;
	}

	public void skip() {
		boolean flag = isCurrentSongPlaying();
		if (flag) {
			this.stop();
		}
		this.currentSong = songs.get((songs.indexOf(currentSong) + 1) % songs.size());
		if (flag) {
			this.play();
		}
	}

	public void play() {
		if (!this.isCurrentSongPlaying()) {
			Minecraft.getMinecraft().soundHandler.playSound(currentSong);
		}
	}

	public void stop() {
		if (this.isCurrentSongPlaying()) {
			Minecraft.getMinecraft().soundHandler.stopSound(currentSong);
		}
	}

	private boolean isCurrentSongPlaying() {
		return Minecraft.getMinecraft().soundHandler.isSoundPlaying(currentSong);
	}

	public void shuffle() {
		this.stop();
		Collections.shuffle(this.songs);
	}

	private ISound getRandomSong() {
		Random random = new Random();
		return songs.get(random.nextInt(songs.size()));
	}
}