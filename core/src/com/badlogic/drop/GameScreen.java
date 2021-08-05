package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {

    final Drop game;
    private Texture dropImage;
    private Texture bucketImage;

    private TextureAtlas playPauseSprite;
    private Sprite pauseButton;
    private Sprite playButton;
    private Sprite controlButton;

    private Sound dropSound;
    private Music rainMusic;

    private OrthographicCamera camera;

    private ShapeRenderer shapeRenderer;
    private Rectangle bucket;

    private Array<Rectangle> raindrops;
    private long lastDropTime;

    public GameScreen(final Drop game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        // A Texture represents a loaded image that is stored in video ram.
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        playPauseSprite = new TextureAtlas("sprites.txt");
        pauseButton = playPauseSprite.createSprite("pause");
        pauseButton.setBounds(Drop.GAME_WIDTH - 60, Drop.GAME_HEIGHT - 60, 50, 50);
        playButton = playPauseSprite.createSprite("play");
        playButton.setBounds(Drop.GAME_WIDTH - 60, Drop.GAME_HEIGHT - 60, 50, 50);
        controlButton = pauseButton;

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Drop.GAME_WIDTH, Drop.GAME_HEIGHT);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setColor(Color.RED);

        createBucket();

        // create the raindrops array and spawn the first raindrop
        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }

    private void createBucket() {

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = Drop.GAME_WIDTH / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        bucket.width = 64;
        bucket.height = 64;
    }

    private void spawnRaindrop() {

        int raindropSize = MathUtils.random(16, 64);

        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, Drop.GAME_WIDTH - raindropSize);
        raindrop.y = Drop.GAME_HEIGHT;
        raindrop.width = raindropSize;
        raindrop.height = raindropSize;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        rainMusic.play();
    }

    @Override
    public void render(float delta) {

        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        ScreenUtils.clear(0, 0, 0.2f, 1);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // begin a new batch and draw the bucket and all drops
        game.batch.begin();
        game.font.draw(game.batch, "Drops Collected: " + game.dropsGathered, 20, Drop.GAME_HEIGHT - 10);
        controlButton.draw(game.batch);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y, raindrop.getWidth(), raindrop.getHeight());
        }
        game.batch.end();

        drawDebugRectangle();
        mouseAndKeyboardControl();

        // check if we need to create a new raindrop
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        if (GameStatus.RUN == game.State) {
            moveRaindrops();
        }
    }

    private void drawDebugRectangle() {
        if (Drop.DEBUG) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (Rectangle raindrop : raindrops) {
                shapeRenderer.rect(raindrop.getX(), raindrop.getY(), raindrop.getWidth(), raindrop.getHeight());
            }
            shapeRenderer.end();
        }
    }

    private void mouseAndKeyboardControl() {

        // process user input
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {

            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            checkIfControlButtonIsTouched(touchPos);

            bucket.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // make sure the bucket stays within the screen bounds
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > Drop.GAME_WIDTH - 64) bucket.x = Drop.GAME_WIDTH - 64;
    }

    private void moveRaindrops() {

        // move the raindrops, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the latter case we play back
        // a sound effect as well.
        for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

            if (raindrop.y + raindrop.height < 0) {
                iter.remove();
                debug("Rimossa raindrop dalla collezione");
            }

            if (raindrop.overlaps(bucket)) {
                game.dropsGathered++;
                dropSound.play();
                iter.remove();
            }

            if (0 > raindrop.y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }

    private void checkIfControlButtonIsTouched(Vector3 touchPos) {

        debug(String.format("Control button X: %f - Max X: %f - Y: %f - Max Y: %f", controlButton.getX(), controlButton.getX() + controlButton.getWidth(), controlButton.getY(), controlButton.getY() + controlButton.getHeight()));
        debug(String.format("Touch pos X: %f - Y: %f", touchPos.x, touchPos.y));

        if (touchPos.x > controlButton.getX() && touchPos.x < controlButton.getX() + controlButton.getWidth()) {

            if (touchPos.y > controlButton.getY() && touchPos.y < controlButton.getY() + controlButton.getHeight()) {
                debug(String.format("Control button touched. Status %s", this.game.State));

                switch (this.game.State) {
                    case RUN:
                        pause();
                        break;
                    case PAUSE:
                        resume();
                        break;
                }
            }
        }
    }

    private void debug(String message) {
        if (Drop.DEBUG) {
            System.out.println(message);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        controlButton = playButton;
        game.State = GameStatus.PAUSE;
    }

    @Override
    public void resume() {
        controlButton = pauseButton;
        game.State = GameStatus.RUN;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
        shapeRenderer.dispose();
        playPauseSprite.dispose();
    }
}
