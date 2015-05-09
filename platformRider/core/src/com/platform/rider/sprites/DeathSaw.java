package com.platform.rider.sprites;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.platform.rider.assets.Assets;
import com.platform.rider.utils.GameConstants;
import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

/**
 * Created by Gayan on 5/9/2015.
 */
public class DeathSaw extends AbstractGameObject{
    Vector2 speed;
    public DeathSaw(int xscale, int yscale, World world, String side, String userdata) {
        this.world = world;
        animatedSprite = new AnimatedSprite(Assets.instance.assetAnimations.deathSawAnimation);
        if (xscale == 0 && side.equals("R")) {
            speed = new Vector2(-10,0);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (xscale == 0 && side.equals("L")) {
            speed = new Vector2(10,0);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) - GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) * yscale + GameConstants.APP_HEIGHT / 2);
        } else if (yscale == 0 && side.equals("D")) {
            speed = new Vector2(0,10);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) - GameConstants.APP_HEIGHT / 2);
        } else if (yscale == 0 && side.equals("U")) {
            speed = new Vector2(0,-10);
            animatedSprite.setPosition((-animatedSprite.getWidth() / 2) * xscale + GameConstants.APP_WIDTH / 2, (-animatedSprite.getHeight() / 2) + GameConstants.APP_HEIGHT / 2);
        }

        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set((animatedSprite.getX() + animatedSprite.getWidth() / 2) /
                        GameConstants.PIXELS_TO_METERS,
                (animatedSprite.getY() + animatedSprite.getHeight() / 2) / GameConstants.PIXELS_TO_METERS
        );

        body = world.createBody(bodyDef);
        body.setLinearVelocity(speed);
        body.setUserData(userdata);
        body.setFixedRotation(false);
        shape.setRadius((animatedSprite.getWidth() / 2) /
                GameConstants.PIXELS_TO_METERS);
        /*shape.setAsBox(sprite.getWidth() / 2 / GameConstants.PIXELS_TO_METERS, sprite.getHeight()
                / 2 / GameConstants.PIXELS_TO_METERS);
        //shape.setRadius(10);*/

        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameConstants.SPRITE_3;
        fixtureDef.filter.maskBits = GameConstants.SPRITE_2|GameConstants.SPRITE_1;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public AnimatedSprite getAnimatedSprite(){
        return this.animatedSprite;
    }

    public void setAnimatedSprite(AnimatedSprite animatedSprite) {
        this.animatedSprite = animatedSprite;
    }

    @Override
    public void render(SpriteBatch batch) {
        animatedSprite.draw(batch);
    }
}
