package Cc_ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;

import Bb_system.TurnManager;

public class BattleUI {

    private Font font;
    private TurnManager turnManager;

    // Battle Start control
    private boolean showBattleStart = false;
    private long battleStartTime;
    private final long BATTLE_START_DURATION = 2000; // milliseconds

    private boolean showRetryOption = false;

    // End Turn button
    private JButton endTurnButton;

    public BattleUI(Font font, TurnManager turnManager, javax.swing.JPanel gamePanel) {
        this.font = font;
        this.turnManager = turnManager;

        initEndTurnButton(gamePanel);
    }

    // ======================================================================
    // End Turn Button Setup
    private void initEndTurnButton(javax.swing.JPanel gamePanel) {
        endTurnButton = new JButton("End Turn");
        endTurnButton.setBounds(1000, 700, 120, 40); // adjust as needed
        endTurnButton.addActionListener(e -> turnManager.endPlayerTurn());
        endTurnButton.setVisible(false); // hidden initially

        gamePanel.setLayout(null); // for manual positioning
        gamePanel.add(endTurnButton);
    }

    // Call this in your update loop
    public void update() {
        updateBattleStart();
        updateEndTurnButton();
    }

    private void updateEndTurnButton() {
        if (turnManager.getCurrentTurn().equals("PLAYER") && !turnManager.hasPlayerAttacked()) {
            endTurnButton.setVisible(true);
        } else {
            endTurnButton.setVisible(false);
        }
    }


    // ======================================================================
    // BATTLE START!
    public void triggerBattleStart() {
        showBattleStart = true;
        battleStartTime = System.currentTimeMillis();
    }

    public boolean isShowingBattleStart() {
        return showBattleStart;
    }

    public void updateBattleStart() {
        if (!showBattleStart) return;

        long elapsed = System.currentTimeMillis() - battleStartTime;
        if (elapsed >= BATTLE_START_DURATION) {
            showBattleStart = false;
        }
    }

    public void drawBattleStart(Graphics2D g2d, int screenWidth, int screenHeight) {
        if (!showBattleStart) return;

        g2d.setFont(font.deriveFont(80f));
        String text = "BATTLE START!";

        int textWidth = g2d.getFontMetrics().stringWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2;

        TextRenderer.drawOutlinedText(g2d, text, x, y, Color.WHITE, Color.BLACK, 6);
    }

    // ======================================================================
    public void drawTurnIndicator(Graphics g, int screenWidth) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font.deriveFont(50f));
        g2d.setColor(Color.WHITE);

        String text = turnManager.getCurrentTurn().equals("PLAYER") ? "YOUR TURN" : "ENEMY'S TURN";
        int x = (screenWidth - g2d.getFontMetrics().stringWidth(text)) / 2;
        int y = 150;

        TextRenderer.drawOutlinedText(g2d, text, x, y, Color.WHITE, Color.BLACK, 4);
    }

    public void drawBattleResult(Graphics g, int screenWidth, int screenHeight) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font.deriveFont(100f));

        String message = "";
        Color color;

        if (turnManager.getWinner().equals("PLAYER")) {
            message = "YOU WIN!";
            color = Color.YELLOW;
        } else {
            message = "GAME OVER";
            color = Color.RED;
        }

        int textWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight / 2) + g2d.getFontMetrics().getAscent() / 2;

        TextRenderer.drawOutlinedText(g2d, message, x, y, color, Color.BLACK, 6);

        // Draw retry option
        g2d.setFont(font.deriveFont(50f));
        String retryText = "Press R to Retry";
        int retryX = (screenWidth - g2d.getFontMetrics().stringWidth(retryText)) / 2;
        int retryY = y + 100;
        TextRenderer.drawOutlinedText(g2d, retryText, retryX, retryY, Color.WHITE, Color.BLACK, 4);

        showRetryOption = true;
    }

    // ======================================================================
    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isShowingRetry() {
        return showRetryOption;
    }

    public void setShowRetryOption(boolean show) {
        this.showRetryOption = show;
    }
}
