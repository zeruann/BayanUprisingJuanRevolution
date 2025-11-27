package Cc_ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import Ff_entities.Enemy;
import Ff_entities.Player;

public class HUDRenderer {

    /*** === CONSTANTS === ***/
    private static final int BAR_WIDTH = 300;
    private static final int BAR_HEIGHT = 20;
    private static final int OUTLINE_THICKNESS = 2;
    private static final int OUTLINE_THICKNESS_NAME = 3;

    private static final int PLAYER_NAME_X = 20;
    private static final int PLAYER_NAME_Y = 60;

    private static final int ENEMY_NAME_Y = 60;
    private static final int SCREEN_WIDTH = 1280;

    private static final int SHIELD_ICON_SIZE = 32;
    private static final int SHIELD_ICON_Y_OFFSET = 110;

    /*** === FIELDS === ***/
    private Player player;
    private Enemy enemy;
    private Font baseFont;
    private BufferedImage shieldIcon;
    
    private BufferedImage playerProfile;
    private BufferedImage enemyProfile;

    private static final int PROFILE_SIZE = 150;
    private static final int PROFILE_PADDING = 10;


    /*** === CONSTRUCTOR === ***/
    public HUDRenderer(Font font, Player player, Enemy enemy) {
        this.baseFont = font;
        this.player = player;
        this.enemy = enemy;

        try {
        	playerProfile = ImageIO.read(getClass().getResourceAsStream("/enemyprofile/Level 1_Normal.png"));
            enemyProfile = ImageIO.read(getClass().getResourceAsStream("/enemyprofile/Level 1_Normal.png"));
            shieldIcon = ImageIO.read(getClass().getResourceAsStream("/ui/tile089.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /*** === MAIN DRAW === ***/
    public void draw(Graphics2D g2d) {
        g2d.setFont(baseFont);

        drawPlayerHUD(g2d);
        drawEnemyHUD(g2d);
    }


    /*** =====================================================
     *  PLAYER HUD
     * =====================================================*/
    private void drawPlayerHUD(Graphics2D g2d) {

        int profileX = PLAYER_NAME_X;
        int profileY = PLAYER_NAME_Y - 40;

        // --- Draw Profile ---
        g2d.drawImage(playerProfile, profileX, profileY, PROFILE_SIZE, PROFILE_SIZE, null);

        // --- Name Position (right of image) ---
        int nameX = profileX + PROFILE_SIZE + PROFILE_PADDING;
        int nameY = PLAYER_NAME_Y;

        TextRenderer.drawOutlinedText(
            g2d, "Juan",
            nameX, nameY,
            Color.WHITE, Color.BLACK, OUTLINE_THICKNESS_NAME
        );

        // --- Health Bar ---
        int barX = nameX;
        int barY = nameY + 20;

        drawHealthBar(
            g2d,
            barX, barY,
            player.getCurrentHealth(),
            player.getMaxHealth(),
            false
        );

        drawHPLabels(
            g2d,
            barX, barY,
            player.getCurrentHealth(),
            player.getMaxHealth()
        );

        // --- Shield under HP ---
        drawShieldIcon(g2d, player.getShield(), barX);
    }



    /*** =====================================================
     *  ENEMY HUD
     * =====================================================*/
    private void drawEnemyHUD(Graphics2D g2d) {

        String enemyName = "Mang Gagantso";

        int profileX = SCREEN_WIDTH - PROFILE_SIZE - 20;
        int profileY = ENEMY_NAME_Y - 40;

        // --- Draw Profile ---
        g2d.drawImage(enemyProfile, profileX, profileY, PROFILE_SIZE, PROFILE_SIZE, null);

        // --- Name (left of image) ---
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(enemyName);

        int nameX = profileX - PROFILE_PADDING - nameWidth;
        int nameY = ENEMY_NAME_Y;

        TextRenderer.drawOutlinedText(
            g2d, enemyName,
            nameX, nameY,
            Color.WHITE, Color.BLACK, OUTLINE_THICKNESS_NAME
        );

        // --- Health Bar ---
        int barX = profileX - PROFILE_PADDING - BAR_WIDTH;
        int barY = nameY + 20;

        drawHealthBar(
            g2d,
            barX, barY,
            enemy.getCurrentHealth(),
            enemy.getMaxHealth(),
            true
        );

        drawHPLabels(
            g2d,
            barX, barY,
            enemy.getCurrentHealth(),
            enemy.getMaxHealth()
        );
    }



    /*** =====================================================
     *  SHIELD ICON BELOW PLAYER HP
     * =====================================================*/
    private void drawShieldIcon(Graphics2D g2d, int shield, int baseX) {
        if (shield <= 0) return;

        int iconX = baseX + 10;
        int iconY = SHIELD_ICON_Y_OFFSET;

        // draw icon
        g2d.drawImage(shieldIcon, iconX, iconY, SHIELD_ICON_SIZE, SHIELD_ICON_SIZE, null);

        // draw shield text like other labels
        String shieldText = "+" + shield;

        // optional: increase font size like HP label
        Font original = g2d.getFont();
        Font bigFont = original.deriveFont(30f); // same as HP labels
        g2d.setFont(bigFont);

        TextRenderer.drawOutlinedText(
            g2d,
            shieldText,
            iconX + 40,
            iconY + 24,
            Color.CYAN,
            Color.BLACK,
            OUTLINE_THICKNESS
        );

        g2d.setFont(original); // restore
    }




    /*** =====================================================
     *  GENERIC HEALTH BAR DRAWER
     * =====================================================*/
    private void drawHealthBar(Graphics2D g2d, int x, int y,
                               int currentHP, int maxHP, boolean reverseFill) {

        double hpPercent = currentHP / (double) maxHP;
        int fillWidth = (int) (hpPercent * BAR_WIDTH);

        // background
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT);

        // filled green part
        g2d.setColor(Color.GREEN);

        if (reverseFill) {
            g2d.fillRect(x + (BAR_WIDTH - fillWidth), y, fillWidth, BAR_HEIGHT); // right → left
        } else {
            g2d.fillRect(x, y, fillWidth, BAR_HEIGHT); // left → right
        }

        // border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, BAR_WIDTH, BAR_HEIGHT);
    }


    /*** =====================================================
     *  HP LABELS: "HP:" + "current / max"
     * =====================================================*/
    private void drawHPLabels(Graphics2D g2d, int barX, int barY, int currentHP, int maxHP) {

        Font original = g2d.getFont();
        Font bigFont = original.deriveFont(25f);
        g2d.setFont(bigFont);

        String hpLabel = "HP:";
        TextRenderer.drawOutlinedText(
            g2d, hpLabel,
            barX, barY - 2,
            Color.WHITE, Color.BLACK, OUTLINE_THICKNESS
        );

        String hpText = currentHP + " / " + maxHP;

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(hpText);

        TextRenderer.drawOutlinedText(
            g2d, hpText,
            barX + BAR_WIDTH - textWidth, barY - 2,
            Color.WHITE, Color.BLACK, OUTLINE_THICKNESS
        );

        g2d.setFont(original);
    }
}
