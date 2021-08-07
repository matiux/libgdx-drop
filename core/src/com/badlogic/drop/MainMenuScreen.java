package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {

    final Drop game;
    OrthographicCamera camera;

    public MainMenuScreen(final Drop game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Drop.GAME_WIDTH, Drop.GAME_HEIGHT);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.draw(game.batch, "Welcome to Drop!!! ", 100, 250);
        game.font.draw(game.batch, "Tap anywhere to begin!", 100, 200);
        if(0 < game.dropsGathered) {
            game.font.draw(game.batch, "Last score: " + game.dropsGathered, 100, 150);
            game.font.draw(game.batch, "Free raindrops: " + game.finalFreeRaindrop, 100, 100);
            game.font.draw(game.batch, "Active raindrops: " + game.finalActiveRaindrop, 100, 50);
        }
        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            game.dropsGathered = 0;
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
