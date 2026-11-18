package com.arkanoid.systems;

import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Explosion;
import com.arkanoid.core.entities.FloatingText;
import com.arkanoid.core.entities.LineEffect;
import com.arkanoid.core.entities.Particle;
import com.arkanoid.core.entities.TrailEffect;
import com.arkanoid.utils.ColorCache;
import com.arkanoid.utils.ObjectPool;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Owns visual effect instances and their bounded object pools for a game session.
 */
public class VisualEffectsManager {
    private final ObjectPool<Particle> particlePool;
    private final ObjectPool<TrailEffect> trailPool;
    private final ObjectPool<FloatingText> floatingTextPool;

    private final List<Explosion> explosions = new ArrayList<>();
    private final List<LineEffect> lineEffects = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<TrailEffect> trailEffects = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private double trailSpawnTimer = 0.0;

    public VisualEffectsManager() {
        this.particlePool = new ObjectPool<>(
                () -> new Particle(0, 0, Color.WHITE),
                particle -> particle.setActive(false),
                GameConstants.MAX_PARTICLES
        );
        this.trailPool = new ObjectPool<>(
                () -> new TrailEffect(0, 0, 5, 0.5, Color.CYAN),
                trail -> trail.setActive(false),
                GameConstants.MAX_TRAIL_EFFECTS
        );
        this.floatingTextPool = new ObjectPool<>(
                () -> new FloatingText("", 0, 0, 1.0, 50, Color.WHITE),
                text -> text.setActive(false),
                GameConstants.MAX_FLOATING_TEXTS
        );

        particlePool.prewarm(50);
        trailPool.prewarm(30);
    }

    public void spawnBrickParticles(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            if (particles.size() >= GameConstants.MAX_PARTICLES) {
                particlePool.release(particles.remove(0));
            }
            Particle particle = particlePool.acquire();
            particle.reset(x, y, color);
            particles.add(particle);
        }
    }

    public void spawnFloatingText(String text, double x, double y, double lifeDuration, double speed, Color color) {
        if (floatingTexts.size() >= GameConstants.MAX_FLOATING_TEXTS) {
            floatingTextPool.release(floatingTexts.remove(0));
        }
        FloatingText floatingText = floatingTextPool.acquire();
        floatingText.reset(text, x, y, lifeDuration, speed, color);
        floatingTexts.add(floatingText);
    }

    public void spawnExplosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        explosions.add(new Explosion(x, y, frameWidth, frameHeight, duration));
    }

    public void spawnLineEffect(double x1, double y1, double x2, double y2, double duration) {
        lineEffects.add(new LineEffect(x1, y1, x2, y2, duration));
    }

    public void spawnBallTrails(List<Ball> balls, double deltaTime) {
        trailSpawnTimer += deltaTime;
        if (trailSpawnTimer < GameConstants.TRAIL_SPAWN_INTERVAL) {
            return;
        }
        trailSpawnTimer = 0.0;

        for (Ball ball : balls) {
            if (ball.isAttachedToPaddle()) {
                continue;
            }
            double vx = ball.getVelocityX();
            double vy = ball.getVelocityY();
            if (Math.abs(vx) <= 10 && Math.abs(vy) <= 10) {
                continue;
            }

            if (trailEffects.size() >= GameConstants.MAX_TRAIL_EFFECTS) {
                trailPool.release(trailEffects.remove(0));
            }

            double speed = Math.sqrt(vx * vx + vy * vy);
            double trailLife = 0.3 + (speed / 1000.0);
            double trailRadius = ball.getRadius() * 0.8;
            double speedFactor = Math.min(speed / 500.0, 1.0);
            Color trailColor = ColorCache.getColor(speedFactor * 0.3, 1.0, 1.0, 1.0);

            TrailEffect trail = trailPool.acquire();
            trail.reset(ball.getCenterX(), ball.getCenterY(), trailRadius, trailLife, trailColor);
            trailEffects.add(trail);
        }
    }

    public void update(double deltaTime) {
        Iterator<Explosion> explosionIt = explosions.iterator();
        while (explosionIt.hasNext()) {
            Explosion explosion = explosionIt.next();
            explosion.update(deltaTime);
            if (!explosion.isActive()) {
                explosionIt.remove();
            }
        }

        Iterator<LineEffect> lineIt = lineEffects.iterator();
        while (lineIt.hasNext()) {
            LineEffect lineEffect = lineIt.next();
            lineEffect.update(deltaTime);
            if (!lineEffect.isActive()) {
                lineIt.remove();
            }
        }

        Iterator<Particle> particleIt = particles.iterator();
        while (particleIt.hasNext()) {
            Particle particle = particleIt.next();
            particle.update(deltaTime);
            if (!particle.isActive()) {
                particleIt.remove();
                particlePool.release(particle);
            }
        }

        Iterator<TrailEffect> trailIt = trailEffects.iterator();
        while (trailIt.hasNext()) {
            TrailEffect trail = trailIt.next();
            trail.update(deltaTime);
            if (!trail.isActive()) {
                trailIt.remove();
                trailPool.release(trail);
            }
        }

        Iterator<FloatingText> textIt = floatingTexts.iterator();
        while (textIt.hasNext()) {
            FloatingText text = textIt.next();
            text.update(deltaTime);
            if (!text.isActive()) {
                textIt.remove();
                floatingTextPool.release(text);
            }
        }
    }

    public void render(GraphicsContext gc) {
        for (Explosion explosion : explosions) {
            explosion.render(gc);
        }
        for (Particle particle : particles) {
            particle.render(gc);
        }
        for (LineEffect lineEffect : lineEffects) {
            lineEffect.render(gc);
        }
        for (TrailEffect trail : trailEffects) {
            trail.render(gc);
        }
        for (FloatingText floatingText : floatingTexts) {
            floatingText.render(gc);
        }
    }

    /**
     * Releases all active pooled effects back to their pools and clears all effect lists.
     */
    public void clearAndReleaseAll() {
        particlePool.releaseAll(particles);
        particles.clear();

        trailPool.releaseAll(trailEffects);
        trailEffects.clear();

        floatingTextPool.releaseAll(floatingTexts);
        floatingTexts.clear();

        explosions.clear();
        lineEffects.clear();
        trailSpawnTimer = 0.0;
    }

    /**
     * Releases active effects and clears pool references on session teardown.
     */
    public void cleanup() {
        clearAndReleaseAll();
        particlePool.clear();
        trailPool.clear();
        floatingTextPool.clear();
    }

    public List<Explosion> getExplosions() {
        return explosions;
    }

    public List<LineEffect> getLineEffects() {
        return lineEffects;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<TrailEffect> getTrailEffects() {
        return trailEffects;
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }

    public ObjectPool<Particle> getParticlePool() {
        return particlePool;
    }

    public ObjectPool<TrailEffect> getTrailPool() {
        return trailPool;
    }

    public ObjectPool<FloatingText> getFloatingTextPool() {
        return floatingTextPool;
    }
}
