package Ff_entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import Ee_utilz.Constants.EnemyConstants;

public class Enemy extends Entity {

    // =========================================================
    // SPRITES
    // =========================================================
    private BufferedImage[] idleFrames;
    private BufferedImage[] attackFrames;
    private BufferedImage[] hurtFrames;
    private BufferedImage[] walkFrames;

    // =========================================================
    // POSITION + ATTACK MOVEMENT
    // =========================================================
    private float originalX, originalY;
    private float attackOffsetX = -100;
    private float attackOffsetY = 0;

    private boolean isAttacking = false;

    // Reference to target player
    private Player target;

    // =========================================================
    // ANIMATION
    // =========================================================
    private int aniTick = 0, aniIndex = 0;
    private int aniSpeed = 30;
    private int enemyAction = EnemyConstants.IDLE;

    // =========================================================
    // ENEMY STATS
    // =========================================================
    private int currentHealth = 500;
    private int maxHealth = 500;
    private int shield = 0;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public Enemy(float x, float y) {
        super(x, y);
        originalX = x;
        originalY = y;
        loadAnimations();
    }

    public void setTarget(Player player) {
        this.target = player;
    }

    // =========================================================
    // UPDATE + RENDER
    // =========================================================
    public void update() {
        updateAnimationTick();

        if (enemyAction == EnemyConstants.ATTACK && target != null) {
            isAttacking = true;
            x = target.getX() + attackOffsetX;
            y = target.getY() + attackOffsetY;
        } else {
            isAttacking = false;
            x = originalX;
            y = originalY;
        }
    }

    public void render(Graphics g) {
        BufferedImage frame = getCurrentFrame();
        g.drawImage(frame, (int) x, (int) y, 64 * 4, 64 * 4, null);
    }

    // =========================================================
    // ANIMATION LOGIC
    // =========================================================
    private void updateAnimationTick() {

        aniTick++;

        if (aniTick >= aniSpeed) {
            aniTick = 0;
            aniIndex++;

            int frames = EnemyConstants.GetSpriteAmount(enemyAction);

            if (aniIndex >= frames) {

                // DEAD → hold last frame
                if (enemyAction == EnemyConstants.HURT && isDead()) {
                    aniIndex = frames - 1;
                    enemyAction = EnemyConstants.DEATH;
                    return;
                }

                aniIndex = 0;

                if (enemyAction == EnemyConstants.ATTACK || enemyAction == EnemyConstants.HURT) {
                    enemyAction = EnemyConstants.IDLE;
                }
            }
        }
    }

    private BufferedImage getCurrentFrame() {
        switch (enemyAction) {
            case EnemyConstants.ATTACK: return attackFrames[aniIndex];
            case EnemyConstants.HURT:   return hurtFrames[aniIndex];
            case EnemyConstants.DEATH:  return hurtFrames[hurtFrames.length - 1];
            default: return idleFrames[aniIndex];
        }
    }

    // =========================================================
    // ACTIONS
    // =========================================================
    public void playAttackAnimation() {
        enemyAction = EnemyConstants.ATTACK;
        aniIndex = 0;
    }

    public void playHurtAnimation() {
        enemyAction = EnemyConstants.HURT;
        aniIndex = 0;
        aniTick = 0;
        aniSpeed = 30;
    }

    // =========================================================
    // HEALTH SYSTEM
    // =========================================================
    public void takeDamage(int damage) {

        if (shield > 0) {
            int blocked = Math.min(shield, damage);
            damage -= blocked;
            shield -= blocked;
        }

        currentHealth = Math.max(0, currentHealth - damage);

        if (isDead()) {
            playHurtAnimation();
        }
    }

    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }

    public void addShield(int amount) {
        shield += amount;
    }

    // =========================================================
    // RESET (FOR RETRY BUTTON)
    // =========================================================
    public void resetForBattle() {
        currentHealth = maxHealth;
        enemyAction = EnemyConstants.IDLE;
        aniIndex = 0;
        x = originalX;
        y = originalY;
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================
    public boolean isDead() { return currentHealth <= 0; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getShield() { return shield; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    @Override public int getX() { return (int) x; }
    @Override public int getY() { return (int) y; } 

    // =========================================================
    // SPRITE LOADING
    // =========================================================
    private void loadAnimations() {
        idleFrames   = loadAnimationRow("/characters/01_pig-idle-combat.png", 2, 1);
        attackFrames = loadAnimationRow("/characters/02-pig-thrust.png", 8, 1);
        hurtFrames   = loadAnimationRow("/characters/03-pig-hurt.png", 6, 0);
        walkFrames   = loadAnimationRow("/characters/01_pig-walk.png", 9, 1);
        
    }

    private BufferedImage[] loadAnimationRow(String path, int cols, int row) {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResourceAsStream(path));
            BufferedImage[] frames = new BufferedImage[cols];

            for (int i = 0; i < cols; i++) {
                frames[i] = sheet.getSubimage(i * 64, row * 64, 64, 64);
            }

            return frames;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getWidth() {
        return 100; // or sprite width
    }

    public int getHeight() {
        return 120; // or sprite height
    }
}
