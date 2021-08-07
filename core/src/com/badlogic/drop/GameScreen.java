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
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {

    final Drop game;
    private final Texture dropImage;
    private final Texture bucketImage;

    private final TextureAtlas playPauseSprite;
    private final Sprite pauseButton;
    private final Sprite playButton;
    private Sprite controlButton;

    private final Sound dropSound;
    private final Music rainMusic;

    private final OrthographicCamera camera;

    private final ShapeRenderer shapeRenderer;
    private Rectangle bucket;

    //private Array<Rectangle> raindrops;

    // array containing the active bullets.
    private final Array<Raindrop> activeRaindrop = new Array<>();
    private final Pool<Raindrop> raindropPool = new Pool<Raindrop>() {
        @Override
        protected Raindrop newObject() {
            return new Raindrop();
        }
    };

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

        Raindrop raindrop = raindropPool.obtain();
        raindrop.init(new Integer[]{100,150,200}[MathUtils.random(0, 2)] , bucket);
        activeRaindrop.add(raindrop);
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
        game.font.draw(game.batch, String.format("Free raindrops: %d - Active raindrops: %d", raindropPool.getFree(), activeRaindrop.size), 20, Drop.GAME_HEIGHT - 40);

        controlButton.draw(game.batch);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Raindrop raindrop : activeRaindrop) {
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
            for (Raindrop raindrop : activeRaindrop) {
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
        int len = activeRaindrop.size;
        for (int i = len; --i >= 0; ) {
            Raindrop raindrop = activeRaindrop.get(i);
            raindrop.update(Gdx.graphics.getDeltaTime());

            if (!raindrop.alive) {
                game.dropsGathered++;
                dropSound.play();
                activeRaindrop.removeIndex(i);
                raindropPool.free(raindrop);
            }

            if (raindrop.y + raindrop.height < 0) {
                game.finalFreeRaindrop = raindropPool.getFree();
                game.finalActiveRaindrop = activeRaindrop.size;
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        }
    }

    private void checkIfControlButtonIsTouched(Vector3 touchPos) {

        Drop.debug(String.format("Control button X: %f - Max X: %f - Y: %f - Max Y: %f", controlButton.getX(), controlButton.getX() + controlButton.getWidth(), controlButton.getY(), controlButton.getY() + controlButton.getHeight()));
        Drop.debug(String.format("Touch pos X: %f - Y: %f", touchPos.x, touchPos.y));

        if (touchPos.x > controlButton.getX() && touchPos.x < controlButton.getX() + controlButton.getWidth()) {

            if (touchPos.y > controlButton.getY() && touchPos.y < controlButton.getY() + controlButton.getHeight()) {
                Drop.debug(String.format("Control button touched. Status %s", this.game.State));

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
