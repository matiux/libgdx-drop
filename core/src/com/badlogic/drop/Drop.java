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

    public static final boolean DEBUG = true;
    public static final int GAME_WIDTH = 1400;
    public static final int GAME_HEIGHT = 1080;
    public GameStatus State = GameStatus.RUN;
    public HUD hud;

    // The SpriteBatch object is used to render objects onto the screen, such as textures.
    public SpriteBatch batch;

    // The BitmapFont object is used, along with a SpriteBatch, to render text onto the screen.
    public BitmapFont font;

    public OrthographicCamera camera;

    public static void debug(String message) {
        if (Drop.DEBUG) {
            System.out.println(message);
        }
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        font = new BitmapFont(); // use libGDX's default Arial font
        font.getData().setScale(1.5f, 1.5f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Drop.GAME_WIDTH, Drop.GAME_HEIGHT);

        hud = new HUD(batch);

        this.setScreen(new MainMenuScreen(this));
    }

    public void render() {
        super.render(); // important!
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        hud.dispose();
    }
}
