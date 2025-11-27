package Bb_system;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static Cc_ui.CardLayout.*;

import Aa_main.Game;

public class CardManager {

	private int draggingIndex = -1;
	private int dragX, dragY;
    private BufferedImage[] cardImages;

    public void setDragging(int index, int x, int y) {
        this.draggingIndex = index;
        this.dragX = x;
        this.dragY = y;
    }

    public void clearDragging() {
        draggingIndex = -1;
    }

    
    // Animation
    private float[] cardY;
    private float targetY;
    private boolean cardsEntering = false;
    private float speed = 8f;       // pixels per frame
    private int[] delays;            // staggered animation
    private int frameCount = 0;

    public CardManager() {
        loadCards();
    }

    private void loadCards() {
        cardImages = new BufferedImage[8];
        try {
            cardImages[0] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/Attack1.png"));
            cardImages[1] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/Attack2.png"));
            cardImages[2] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/Attack3.png"));
            cardImages[3] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/Attack4.png"));
            cardImages[4] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/Attack5.png"));
            cardImages[5] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/HealCard.png"));
            cardImages[6] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/ShieldCard.png"));
            cardImages[7] = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/cards/JusticeCard2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Trigger the slide-in animation
    public void startCardEntrance(int screenHeight) {
    	targetY = screenHeight - HEIGHT - BOTTOM_OFFSET;

        cardY = new float[cardImages.length];
        delays = new int[cardImages.length];

        for (int i = 0; i < cardY.length; i++) {
            cardY[i] = screenHeight + 50; // start offscreen
            delays[i] = i * 10;           // stagger animation
        }

        cardsEntering = true;
        frameCount = 0;
    }

    // Update animation (call each frame in Game.update())
    public void updateAnimation() {
        if (!cardsEntering) return;

        boolean done = true;

        for (int i = 0; i < cardY.length; i++) {
            if (frameCount >= delays[i]) {
                if (cardY[i] > targetY) {
                    cardY[i] -= speed;
                    if (cardY[i] < targetY) cardY[i] = targetY;
                    done = false;
                }
            } else {
                done = false; // still waiting for delay
            }
        }

        frameCount++;
        if (done) cardsEntering = false;
    }

    public boolean isAnimating() {
        return cardsEntering;
    }

    public void draw(Graphics g, int screenWidth, int screenHeight) {
        if (cardImages == null) return;

        int cardWidth = WIDTH;
        int cardHeight = HEIGHT;
        int spacing = SPACING;


        int totalWidth = cardImages.length * (cardWidth + spacing);

        int startX = (screenWidth - totalWidth) / 2;

        int cardYPos = (cardY != null) ? (int) cardY[0] : screenHeight - HEIGHT - BOTTOM_OFFSET;

        // ===== BACKGROUND PANEL =====
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bgPadding = 25;
        int bgX = startX - bgPadding;
        int bgY = cardYPos - bgPadding;
        int bgWidth = totalWidth + bgPadding * 2;
        int bgHeight = cardHeight + bgPadding * 2;

        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 30, 30);

        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(bgX, bgY, bgWidth, bgHeight, 30, 30);

        // ===== DRAW CARDS =====
        int hoveredIndex = -1;
        if (Game.getInstance().isCardsActive() && !isAnimating()) {
            hoveredIndex = Game.getInstance().getGamePanel().getHoveredCardIndex();
        }

     // Draw all non-dragging cards
        for (int i = 0; i < cardImages.length; i++) {

            if (i == draggingIndex) continue;

            if (i != hoveredIndex) {
                drawCard(g, i, startX, cardWidth, cardHeight, spacing, screenHeight);
            }
        }

        // Draw hovered card on top (if not dragging it)
        if (hoveredIndex != -1 && hoveredIndex != draggingIndex) {
            drawCard(g, hoveredIndex, startX, cardWidth, cardHeight, spacing, screenHeight, true);
        }

        // ✅ Draw dragged card LAST so it is always on top
        if (draggingIndex != -1) {
            g.drawImage(cardImages[draggingIndex],
                        dragX - WIDTH / 2,
                        dragY - HEIGHT / 2,
                        WIDTH, HEIGHT,
                        null);
        }


    }

    // Helper method to draw a card
    private void drawCard(Graphics g, int i, int startX, int cardWidth, int cardHeight, int spacing, int screenHeight) {
        drawCard(g, i, startX, cardWidth, cardHeight, spacing, screenHeight, false);
    }

    private void drawCard(Graphics g, int i, int startX, int cardWidth, int cardHeight, int spacing,
                          int screenHeight, boolean isHovered) {
        int x = startX + i * (cardWidth + spacing);
        int y = (cardY != null) ? (int) cardY[i] : screenHeight - HEIGHT - BOTTOM_OFFSET;

        Graphics2D g2d = (Graphics2D) g.create();

        if (isHovered) {
            float scale = 1.1f;
            int centerX = x + cardWidth / 2;
            int centerY = y + cardHeight / 2;
            g2d.translate(centerX, centerY);
            g2d.scale(scale, scale);
            g2d.translate(-centerX, -centerY);
        }

        g2d.drawImage(cardImages[i], x, y, cardWidth, cardHeight, null);
        g2d.dispose();
    }

    public BufferedImage[] getCardImages() {
        return cardImages;
    }

    public boolean isMouseOverCard(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (cardImages == null) return false;

        int cardWidth = WIDTH;
        int cardHeight = HEIGHT;
        int spacing = SPACING;
        int totalWidth = cardImages.length * cardWidth 
                + (cardImages.length - 1) * spacing;

        int startX = (screenWidth - totalWidth) / 2;

        int y = (cardY != null) ? (int) cardY[0] : screenHeight - HEIGHT - BOTTOM_OFFSET;

        for (int i = cardImages.length - 1; i >= 0; i--) {  // Right to left for overlap
            int x = startX + i * (cardWidth + spacing);
            if (mouseX >= x && mouseX <= x + cardWidth && mouseY >= y && mouseY <= y + cardHeight) {
                return true;
            }
        }

        return false;
    }
}
