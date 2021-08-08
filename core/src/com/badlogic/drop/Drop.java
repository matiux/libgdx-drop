package com.badlogic.drop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

enum GameStatus {
    PAUSE,
    RUN,
}

public class Drop extends Game {

    public GameStatus State = GameStatus.RUN;
    public static final boolean DEBUG = true;
    public static final int GAME_WIDTH = 1024;
    public static final int GAME_HEIGHT = 768;

    // The SpriteBatch object is used to render objects onto the screen, such as textures.
    public SpriteBatch batch;

    // The BitmapFont object is used, along with a SpriteBatch, to render text onto the screen.
    public BitmapFont font;

    public int dropsGathered;
    public int finalFreeRaindrop;
    public int finalActiveRaindrop;
    public OrthographicCamera camera;

    @Override
    public void create() {

        batch = new SpriteBatch();
        font = new BitmapFont(); // use libGDX's default Arial font
        font.getData().setScale(1.5f, 1.5f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Drop.GAME_WIDTH, Drop.GAME_HEIGHT);

        this.setScreen(new MainMenuScreen(this));
    }

    public void render() {
        super.render(); // important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    public static void debug(String message) {
        if (Drop.DEBUG) {
            System.out.println(message);
        }
    }
}
