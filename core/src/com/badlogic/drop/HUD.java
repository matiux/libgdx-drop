package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class HUD implements Disposable {

    private final Label scoreLabel;
    private final Label freeRaindropsLabel;
    private final Label activeRaindropsLabel;
    public Stage stage;
    private Integer dropsGathered;
    private Integer freeRaindrops;
    private Integer activeRaindrops;

    public HUD(SpriteBatch sb) {

        // We must create order by creating a table in our stage
        stage = new Stage(
                new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera()),
                sb
        );

        BitmapFont white = new BitmapFont(Gdx.files.internal("font/white32.fnt"), false);
        Label.LabelStyle labelStyle = new Label.LabelStyle(white, Color.WHITE);

        scoreLabel = new Label(String.format("Collected drops: %06d", dropsGathered), labelStyle);
        freeRaindropsLabel = new Label(String.format("Free raindrops: %d", freeRaindrops), labelStyle);
        activeRaindropsLabel = new Label(String.format("Active raindrops: %d", activeRaindrops), labelStyle);

        initCounters();

        Sprite spriteCloseButton = (new TextureAtlas("sprites.txt")).createSprite("close");
        spriteCloseButton.setSize(50, 50);

        Image closeButton = new Image(new SpriteDrawable(spriteCloseButton));
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

//        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
//        style.font = new BitmapFont();
//        style.font.setColor(Color.WHITE);
//        TextButton continueButton = new TextButton("continue", style);

        Table table = new Table();

        if (Drop.DEBUG) {
            table.debug();
        }

        table.align(Align.topLeft);
        table.setFillParent(true);

        table.add(freeRaindropsLabel).align(Align.left);
        table.add(activeRaindropsLabel).align(Align.left);
        table.add(closeButton).align(Align.right).expandX().pad(10, 0, 0, 10);
        table.row();
        table.add(scoreLabel).align(Align.left);

        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }

    public void incrementScore(Integer increment) {

        dropsGathered += increment;

        scoreLabel.setText(String.format("Collected drops: %06d", dropsGathered));
    }

    public void incrementScore() {
        incrementScore(1);
    }

    public void setFreeRaindrops(Integer freeRaindrops) {

        this.freeRaindrops = freeRaindrops;

        freeRaindropsLabel.setText(String.format("Free raindrops: %d", this.freeRaindrops));
    }

    public void setActiveRaindrops(Integer activeRaindrops) {

        this.activeRaindrops = activeRaindrops;

        activeRaindropsLabel.setText(String.format("Active raindrops: %d", this.activeRaindrops));
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void initCounters() {

        dropsGathered = 0;

        incrementScore(0);
        setFreeRaindrops(0);
        setActiveRaindrops(0);
    }
}
