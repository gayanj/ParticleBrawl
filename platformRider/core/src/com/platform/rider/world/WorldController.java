package com.platform.rider.world;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.platform.rider.sprites.Hero;
import com.platform.rider.sprites.Particle;
import com.platform.rider.sprites.Saw;
import com.platform.rider.utils.GameConstants;
import com.platform.rider.utils.TouchPadHelper;

import java.util.*;

/**
 * Created by Gayan on 3/23/2015.
 */
public class WorldController {
    private static final String TAG = WorldController.class.getName();
    private Game game;
    public OrthographicCamera camera;
    public World world;
    public Hero hero;
    public HashMap<String, Particle> particleHashMap = new HashMap<String, Particle>();
    public List<String> normalParticlesForRemoval = new ArrayList<String>();
    public List<String> splitParticlesForRemoval = new ArrayList<String>();
    public List<Saw> saws = new ArrayList<Saw>();
    public TouchPadHelper touchPadHelper;
    int totalParticlesCreated = 0;
    int totalParticlesDestroyed = 0;
    int splitParticlesAlive = 0;
    int totalParticlesAlive = 0;
    int stage = 1;
    Array<Vector2> splitParticlePosition = new Array<Vector2>();

    public WorldController (Game game) {
        this.game = game;
        init();
    }

    private void init() {
        initTouchpad();
        Gdx.input.setInputProcessor(touchPadHelper.getStage());
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.
                getHeight());
        initPhysics();
        world.setContactListener(new reactorContactListener());
    }

    private void initPhysics() {
        world = new World(new Vector2(0, 0), true);
        createHero();
        createParticles();
        createSpikes();
    }

    private void initTouchpad() {
        touchPadHelper = new TouchPadHelper();
    }

    private void createHero() {
        hero = new Hero(new Vector2(0, 0), world);
    }

    private void createParticles() {
        for (int i = 0; i < 4; i++) {
            createNewParticle(GameConstants.NORMAL_PARTICLE);
        }
    }

    private void createNewParticle(String type) {
        Random r = new Random();
        int xLow = -(Gdx.graphics.getWidth() / 2 - 100);
        int xHigh = Gdx.graphics.getWidth() / 2 - 100;
        int xR = r.nextInt(xHigh - xLow) + xLow;

        int yLow = -(Gdx.graphics.getWidth() / 2 - 100);
        int yHigh = Gdx.graphics.getWidth() / 2 - 100;
        int yR = r.nextInt(yHigh - yLow) + yLow;
        Vector2 position = new Vector2(xR, yR);
        Particle particle = new Particle(position, world, totalParticlesCreated, type);
        //increment the number of particles created count
        particleHashMap.put(String.valueOf(totalParticlesCreated++), particle);
        totalParticlesAlive++;
    }

    private void createSplitParticle(Vector2 position) {
        Particle particle = new Particle(position, world, totalParticlesCreated, GameConstants.SPLIT_PARTICLE);
        //increment the number of particles created count
        particleHashMap.put(String.valueOf(totalParticlesCreated++), particle);
        totalParticlesAlive++;
    }

    private void createSpikes() {
        for (int i = 0; i < (Gdx.graphics.getHeight() / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "R");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getHeight() / 130) + 1; i++) {
            int yscale = 2 * (i + 1);
            Saw saw = new Saw(0, yscale, world, "L");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getWidth() / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "U");
            saws.add(saw);
        }
        for (int i = 0; i < (Gdx.graphics.getWidth() / 130) + 1; i++) {
            int xscale = 2 * (i + 1);
            Saw saw = new Saw(xscale, 0, world, "D");
            saws.add(saw);
        }
    }

    public void update(float deltaTime) {
        world.step(deltaTime, 8, 3);
        destroyParticles();
        destroySplitParticles();
        checkStage();
        splitParticles();
        camera.update();
        //Touch pad readings
        Vector2 touchPadVec = new Vector2(touchPadHelper.getTouchpad().getKnobPercentX() * 10f, touchPadHelper.getTouchpad().getKnobPercentY() * 10f);
        //If touchpad readings are zero dont alter velocity of hero
        if (touchPadVec.x != 0 && touchPadVec.y != 0) {
            hero.getBody().setLinearVelocity(touchPadVec);
        }
        updateHero();
        updateParticles();
        updateSpikes();
    }

    private void updateHero() {
        hero.getSprite().setPosition((hero.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - hero.getSprite().
                        getWidth() / 2,
                (hero.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - hero.getSprite().getHeight() / 2
        );
    }

    private void destroyParticles() {
        for (String particleKey : normalParticlesForRemoval) {
            particleHashMap.get(particleKey).setSprite(null);
            final Array<JointEdge> list = particleHashMap.get(particleKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(particleHashMap.get(particleKey).getBody());
            particleHashMap.remove(particleKey);
            //increment the number of destroyed particles
            totalParticlesDestroyed++;
            totalParticlesAlive--;
            //create a new particle
            createNewParticle(GameConstants.NORMAL_PARTICLE);
        }
        normalParticlesForRemoval.clear();
    }

    private void destroySplitParticles() {
        for (String particleKey : splitParticlesForRemoval) {
            particleHashMap.get(particleKey).setSprite(null);
            final Array<JointEdge> list = particleHashMap.get(particleKey).getBody().getJointList();
            while (list.size > 0) {
                world.destroyJoint(list.get(0).joint);
            }
            world.destroyBody(particleHashMap.get(particleKey).getBody());
            particleHashMap.remove(particleKey);
            //increment the number of destroyed particles
            totalParticlesDestroyed++;
            splitParticlesAlive--;
            totalParticlesAlive--;
        }
        splitParticlesForRemoval.clear();
    }

    private void checkStage() {
        if (totalParticlesDestroyed > 10 && stage == 1) {
            stage++;
        }
    }

    private void splitParticles() {
        if (totalParticlesDestroyed > 0 && totalParticlesDestroyed % 20 == 0 && splitParticlesAlive == 0 && totalParticlesAlive < 20) {
            createNewParticle(GameConstants.SPLIT_PARTICLE);
            splitParticlesAlive++;
        }
        for (Vector2 plarticlePosition : splitParticlePosition) {
            //increment the number of particles created count
            createSplitParticle(plarticlePosition);
            splitParticlesAlive++;
        }
        splitParticlePosition.clear();
    }

    private void updateParticles() {
        for (Map.Entry<String, Particle> entry : particleHashMap.entrySet()) {
            Particle particle = entry.getValue();
            Vector2 heroPos = hero.getBody().getPosition();
            Vector2 particlePos = particle.getBody().getPosition();
            Vector2 distance = new Vector2(0, 0);
            distance.add(heroPos).sub(particlePos);

            particle.setNormaliseVector(distance.nor());

            if (!particle.isColliding()) {
                particle.getBody().setLinearVelocity(particle.getNormaliseVector().x * particle.getSpeed(), particle.getNormaliseVector().y * particle.getSpeed());
            }

            if (particle.isColliding()) {
                int count = particle.getCounter();
                count++;
                particle.setCounter(count);
            }

            if (particle.getCounter() > 5) {
                particle.setColliding(false);
                particle.setCounter(0);
            }

            if (GameConstants.SPLIT_PARTICLE.equals(particle.getType())) {
                updateSplitParticleCount(particle);
            }

            particle.getSprite().setPosition((particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - particle.getSprite().
                            getWidth() / 2,
                    (particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - particle.getSprite().getHeight() / 2
            );
        }
    }

    private void updateSpikes() {
        for (Saw saw : saws) {
            saw.getSprite().setPosition((saw.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS) - saw.getSprite().
                            getWidth() / 2,
                    (saw.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS) - saw.getSprite().getHeight() / 2
            );
        }
    }

    private void updateSplitParticleCount(Particle particle) {
        if (stage == 2 && particle.getSplitParticleCount() > 200) {
            //set split particle position
            Vector2 particlePosition = new Vector2(particle.getBody().getPosition().x * GameConstants.PIXELS_TO_METERS - particle.getSprite().
                    getWidth()/2,particle.getBody().getPosition().y * GameConstants.PIXELS_TO_METERS - particle.getSprite().
                    getHeight()/2);
            splitParticlePosition.add(particlePosition);
            particle.setSplitParticleCount(0);
        } else {
            int count = particle.getSplitParticleCount();
            count++;
            particle.setSplitParticleCount(count);
        }
    }

    class reactorContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {

        }

        @Override
        public void endContact(Contact contact) {
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                Particle particle = particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * GameConstants.COLLISION_SPEED, -particle.getNormaliseVector().y * GameConstants.COLLISION_SPEED);
                contact.getFixtureB().getBody().setLinearVelocity(inverseNormal);
                particle.setColliding(true);
            }
            /*if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                Particle particle = particleHashMap.get(contact.getFixtureB().getBody().getUserData().toString());
                particle.setColliding(true);
                if (particle.getCollisionCount() > 3) {
                    Vector2 inverseNormal = new Vector2(-particle.getNormaliseVector().x * GameConstants.COLLISION_SPEED, -particle.getNormaliseVector().y * GameConstants.COLLISION_SPEED);
                    contact.getFixtureA().getBody().setLinearVelocity(inverseNormal);
                    particle.setCollisionCount(0);
                } else {
                    int count = particle.getCollisionCount();
                    count++;
                    particle.setCollisionCount(count);
                }
            }*/
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_1 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //remove particles
                if (!normalParticlesForRemoval.contains(contact.getFixtureA().getBody().getUserData().toString())) {
                    normalParticlesForRemoval.add(contact.getFixtureA().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_3 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_1) {
                //remove particles
                if (!splitParticlesForRemoval.contains(contact.getFixtureB().getBody().getUserData().toString())) {
                    splitParticlesForRemoval.add(contact.getFixtureB().getBody().getUserData().toString());
                }
            }
            if (contact.getFixtureA().getFilterData().categoryBits == GameConstants.SPRITE_2 && contact.getFixtureB().getFilterData().categoryBits == GameConstants.SPRITE_3) {
                //game over
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }
}
