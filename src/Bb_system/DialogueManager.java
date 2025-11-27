package Bb_system;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class DialogueManager {

	private static class DialogueLine {
	    String speaker;
	    String text;
	    float targetX;
	    float targetY;
	    BufferedImage bubbleImage;
	    int textX; // hardcoded text X inside bubble
	    int textY; // hardcoded text Y inside bubble

	    DialogueLine(String speaker, String text, float targetX, float targetY, BufferedImage bubbleImage, int textX, int textY) {
	        this.speaker = speaker;
	        this.text = text;
	        this.targetX = targetX;
	        this.targetY = targetY;
	        this.bubbleImage = bubbleImage;
	        this.textX = textX;
	        this.textY = textY;
	    }
	}


    private Queue<DialogueLine> dialogueQueue = new LinkedList<>();
    private DialogueLine currentLine = null;
    private boolean active = false;

    // Typewriter effect
    private String visibleText = "";
    private int charIndex = 0;
    private long lastCharTime = 0;
    private int charDelay = 35;
    private boolean lineComplete = false;
    private long lineCompleteTime = 0;
    private long nextLineDelay = 2000;

    private Font dialogueFont;

    public DialogueManager(Font baseFont) {
        dialogueFont = baseFont.deriveFont(Font.PLAIN, 50f);
    }

    // -----------------------
    // Add dialogue with bubble image
    // -----------------------
    public void addDialogue(String speaker, String text, float targetX, float targetY, BufferedImage bubbleImage, int textX, int textY) {
        dialogueQueue.add(new DialogueLine(speaker, text, targetX, targetY, bubbleImage, textX, textY));
    }


    public void start() {
        if (!dialogueQueue.isEmpty()) {
            currentLine = dialogueQueue.poll();
            active = true;
            resetTypewriter();
        }
    }

    public boolean isActive() {
        return active;
    }

    
    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();

        String line = "";
        int drawY = y;

        for (String word : text.split(" ")) {
            String testLine = line + word + " ";

            if (fm.stringWidth(testLine) > maxWidth) {
                g.drawString(line, x, drawY);
                line = word + " ";
                drawY += lineHeight;
            } else {
                line = testLine;
            }
        }

        g.drawString(line, x, drawY); // draw last line
    }

    
    
    public void update() {
        if (!active || currentLine == null) return;

        if (!lineComplete && System.currentTimeMillis() - lastCharTime >= charDelay) {
            if (charIndex < currentLine.text.length()) {
                visibleText += currentLine.text.charAt(charIndex);
                charIndex++;
                lastCharTime = System.currentTimeMillis();
            } else {
                lineComplete = true;
                lineCompleteTime = System.currentTimeMillis();
            }
        }

        if (lineComplete && System.currentTimeMillis() - lineCompleteTime >= nextLineDelay) {
            if (!dialogueQueue.isEmpty()) {
                currentLine = dialogueQueue.poll();
                resetTypewriter();
            } else {
                active = false;
            }
        }
    }

    public void render(Graphics2D g) {
        if (!active || currentLine == null) return;

        int bubbleWidth = 512;
        int bubbleHeight = 512;

        int bubbleX = (int) currentLine.targetX - bubbleWidth / 2;
        int bubbleY = (int) currentLine.targetY - bubbleHeight;

        // Draw bubble
        if (currentLine.bubbleImage != null) {
            g.drawImage(currentLine.bubbleImage, bubbleX, bubbleY, bubbleWidth, bubbleHeight, null);
        }

        // Draw text (hardcoded inside bubble)
        g.setFont(dialogueFont);
        g.setColor(Color.WHITE);
        drawWrappedText(g, visibleText, currentLine.textX, currentLine.textY, 390);

    }


    private void resetTypewriter() {
        visibleText = "";
        charIndex = 0;
        lineComplete = false;
        lastCharTime = System.currentTimeMillis();
    }
}
