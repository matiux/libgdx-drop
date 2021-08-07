package com.badlogic.drop;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class Raindrop extends Rectangle implements Pool.Poolable {

    public boolean alive;
    private final int raindropSize;
    private int velocity;
    private Rectangle bucket;

    /**
     * Bullet constructor. Just initialize variables.
     */
    public Raindrop() {

        Drop.debug("New raindrop created.");

        raindropSize = MathUtils.random(16, 64);
        x = MathUtils.random(0, Drop.GAME_WIDTH - raindropSize);
        y = Drop.GAME_HEIGHT;
        width = raindropSize;
        height = raindropSize;
        alive = false;
    }

    /**
     * Initialize the bullet. Call this method after getting a bullet from the pool.
     */
    public void init(int velocity, Rectangle bucket) {
        this.velocity = velocity;
        this.bucket = bucket;
        alive = true;
    }

    /**
     * Callback method when the object is freed. It is automatically called by Pool.free()
     * Must reset every meaningful field of this bullet.
     */
    @Override
    public void reset() {

        Drop.debug("Raindrop freed and reset.");

        x = MathUtils.random(0, Drop.GAME_WIDTH - raindropSize);
        y = Drop.GAME_HEIGHT;
    }

    /**
     * Method called each frame, which updates the bullet.
     */
    public void update(float delta) {

        y -= velocity * delta;

        if (this.overlaps(bucket)) {
            alive = false;
        }
    }
}
