package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.mygdx.game.Constants.*;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
    private static final int SPRITE_SIZE = 256;
    private Texture dropImage;
    private Texture bucketImage;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Set<MySprite> sprites;
    float dt;
    long currentTime;
    float accumulator;
    MySprite selected;

    @Override
    public void create() {
        // load the images for the droplet and the bucket, 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("tree.png"));
        bucketImage = new Texture(Gdx.files.internal("man.png"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        batch = new SpriteBatch();
        sprites = new HashSet<>();

        MySprite bucket = new MySprite(bucketImage);
        sprites.add(bucket);
        bucket.setSize(SPRITE_SIZE, SPRITE_SIZE);
        bucket.setPosition(0, 0);

        int i = 0;
        while (i < 20) {
            spawnRaindrop();
            i++;
        }

        accumulator = 0f;
        dt = 1f / 60f;
        currentTime = TimeUtils.millis();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {

        long newTime = TimeUtils.millis();
        float frameTime = (newTime - currentTime) / 1000f;
//		if ( frameTime > 0.25f ) {
//			frameTime = 0.25f;
//		}
        currentTime = newTime;

        accumulator += frameTime;

        while (accumulator >= dt) {
            update(dt);
            accumulator -= dt;
        }

        float alpha = Math.max(accumulator - dt, 0f);
        update(alpha);

        ScreenUtils.clear(1, 0, 1, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Sprite sprite : sprites) {
            sprite.draw(batch);
        }
        batch.end();
    }

    void update(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-1000*deltaTime, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(1000*deltaTime, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 1000*deltaTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -1000*deltaTime);
        }

        camera.position.x = MathUtils.clamp(camera.position.x, SCREEN_WIDTH / 2, WORLD_WIDTH - SCREEN_WIDTH / 2);
        camera.position.y = MathUtils.clamp(camera.position.y, SCREEN_HEIGHT / 2, WORLD_HEIGHT - SCREEN_HEIGHT / 2);

        sprites.forEach(sprite -> {
            if (Objects.nonNull(sprite.target)) {
                Vector2 dir = sprite.target.sub(new Vector2(sprite.getX(), sprite.getY())).setLength(1);
                sprite.translate(50* dir.x * deltaTime, 50 * dir.y * deltaTime);
            }
        });
    }

    private void spawnRaindrop() {
        MySprite raindrop = new MySprite(dropImage);
        raindrop.setPosition(MathUtils.random(0, WORLD_WIDTH - SPRITE_SIZE), MathUtils.random(0, WORLD_HEIGHT - SPRITE_SIZE));
        raindrop.setSize(SPRITE_SIZE, SPRITE_SIZE);
        sprites.add(raindrop);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 pos = new Vector2(Gdx.input.getX() + camera.position.x - SCREEN_WIDTH / 2, SCREEN_HEIGHT - Gdx.input.getY() + camera.position.y - SCREEN_HEIGHT / 2);
        if (button == 0) {
            System.out.println(camera.position);
            System.out.println(pos);
            System.out.println("-----");
            Optional<MySprite> optional = sprites.stream()
                    .filter(sprite -> sprite.getBoundingRectangle().contains(pos))
                    .findFirst();
            if (Objects.nonNull(selected)) {
                selected.setAlpha(1f);
                selected.target = null;
                selected = null;
            }
            optional.ifPresent(
                    sprite -> {
                        selected = sprite;
                        selected.setAlpha(0.1f);
                    });
        }
        if (button == 1) {
            if (Objects.nonNull(selected)) {
                selected.target = pos;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
