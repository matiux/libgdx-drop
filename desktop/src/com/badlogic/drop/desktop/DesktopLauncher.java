package com.badlogic.drop.desktop;

import com.badlogic.drop.Drop;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Drop");
		config.setWindowedMode(Drop.GAME_WIDTH, Drop.GAME_HEIGHT);
		new Lwjgl3Application(new Drop(), config);
	}
}
