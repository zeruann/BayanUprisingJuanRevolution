package Dd_inputs;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static Cc_ui.CardLayout.*;

import Aa_main.Game;
import Aa_main.GamePanel;
import Cc_ui.CardInfo;
import Cc_ui.DamageText;
import Ff_entities.Enemy;
import Ff_entities.Player;
import Gg_effects.AttackEffect;

public class CardClickHandler {

	private GamePanel gamePanel;

	// Drag state
	private int draggingIndex = -1;
	private boolean dragging = false;
	private int dragOffsetX, dragOffsetY;
	private int dragX, dragY;

	// Threshold to activate play
	private static final int PLAY_LINE_Y = 500;

	
	
	
	
	
	private CardInfo[] cardInfos = new CardInfo[] {
			new CardInfo("Expose the Scandal", "Reveal the corrupt official's shady dealings.", 25),
			new CardInfo("Whistleblower", "Leak documents to weaken the enemy.", 20),
			new CardInfo("People's Protest", "Rally citizens to pressure the enemy.", 30),
			new CardInfo("Truth Campaign", "Spread awareness, dealing ongoing damage.", 15),
			new CardInfo("Legal Action", "High-damage court action.", 40), new CardInfo("Heal", "Restore your HP.", 0),
			new CardInfo("Shield", "Protect yourself.", 0),
			new CardInfo("Special Attack", "Powerful special attack.", 50) };

	public CardClickHandler(GamePanel gamePanel) {
		this.gamePanel = gamePanel;

		MouseAdapter adapter = new MouseAdapter() {

		    @Override
		    public void mousePressed(MouseEvent e) {

		        if (!Game.getInstance().isCardsActive())
		            return;

		        draggingIndex = getClickedCardIndex(e.getX(), e.getY());

		        if (draggingIndex != -1) {
		            dragging = true;

		            int cardX = getCardX(draggingIndex);
		            int cardY = getCardY();

		            dragOffsetX = e.getX() - cardX;
		            dragOffsetY = e.getY() - cardY;
		            dragX = e.getX();
		            dragY = e.getY();
		        }
		    }

		    @Override
		    public void mouseDragged(MouseEvent e) {

		        if (!dragging) return;

		        dragX = e.getX() - dragOffsetX;
		        dragY = e.getY() - dragOffsetY;

		        gamePanel.repaint();
		        Game.getInstance().getCardManager().setDragging(draggingIndex, dragX, dragY);

		    }

		    @Override
		    public void mouseReleased(MouseEvent e) {

		        if (!dragging) return;

		        dragging = false;

		        // If card is released upward = play card
		        if (dragY < PLAY_LINE_Y) {
		            handlePlayedCard(draggingIndex);
		        }

		        draggingIndex = -1;
		        gamePanel.repaint();
		        Game.getInstance().getCardManager().clearDragging();

		    }
		};

		gamePanel.addMouseListener(adapter);
		gamePanel.addMouseMotionListener(adapter);

	}
	
	private int getCardX(int index) {

	    int cardWidth = WIDTH;
	    int spacing = SPACING;

	    int totalWidth = cardInfos.length * cardWidth
	                   + (cardInfos.length - 1) * spacing;

	    int startX = (gamePanel.getWidth() - totalWidth) / 2;

	    return startX + index * (cardWidth + spacing);
	}
	private int getCardY() {
	    return gamePanel.getHeight() - HEIGHT - BOTTOM_OFFSET;
	}


	public CardInfo[] getCardInfos() {
		return cardInfos;
	}

	// =========================================================
	// Get hovered card index (for mouse-over effects)
	// =========================================================
	public int getHoveredCardIndex(int mouseX, int mouseY) {

	    int cardWidth = WIDTH, cardHeight = HEIGHT;
	    int spacing = SPACING;

	    int totalWidth = cardInfos.length * cardWidth
	                   + (cardInfos.length - 1) * spacing;

	    int startX = (gamePanel.getWidth() - totalWidth) / 2;
	    int y = gamePanel.getHeight() - HEIGHT - BOTTOM_OFFSET;

	    for (int i = cardInfos.length - 1; i >= 0; i--) {
	        if (!cardInfos[i].isActive()) continue;

	        int x = startX + i * (cardWidth + spacing);

	        if (mouseX >= x && mouseX <= x + cardWidth &&
	            mouseY >= y && mouseY <= y + cardHeight) {
	            return i;
	        }
	    }
	    return -1;
	}


	private int getClickedCardIndex(int mouseX, int mouseY) {

	    int cardWidth = WIDTH, cardHeight = HEIGHT;
	    int spacing = SPACING;

	    int totalWidth = cardInfos.length * cardWidth
	                   + (cardInfos.length - 1) * spacing;

	    int startX = (gamePanel.getWidth() - totalWidth) / 2;
	    int y = gamePanel.getHeight() - HEIGHT - BOTTOM_OFFSET;

	    for (int i = cardInfos.length - 1; i >= 0; i--) {

	        if (!cardInfos[i].isActive()) continue;

	        int x = startX + i * (cardWidth + spacing);

	        if (mouseX >= x && mouseX <= x + cardWidth &&
	            mouseY >= y && mouseY <= y + cardHeight) {
	            return i;
	        }
	    }
	    return -1;
	}

	
	

	// Handle card click
	public void handleCardClick(int mouseX, int mouseY) {
		Game game = gamePanel.getGame();
		Player player = game.getPlayer();

		if (game.getTurnManager().isBattleOver())
			return;
		if (!game.getTurnManager().getCurrentTurn().equals("PLAYER") || game.getTurnManager().hasPlayerAttacked())
			return;

		int cardIndex = getClickedCardIndex(mouseX, mouseY);
		if (cardIndex == -1)
			return;


		switch (cardIndex) {
		case 0, 1, 2, 3, 4 -> performAttack(game, cardIndex);
		case 5 -> performHeal(game, player);
		case 6 -> performShield(game, player);
		case 7 -> performSpecialAttack(game);
		}

		// Mark the card as used
		cardInfos[cardIndex].setActive(false);
	}
	
	private void handlePlayedCard(int cardIndex) {

	    Game game = gamePanel.getGame();
	    Player player = game.getPlayer();

	    if (game.getTurnManager().isBattleOver()) return;
	    if (!game.getTurnManager().getCurrentTurn().equals("PLAYER")) return;
	    if (!cardInfos[cardIndex].isActive()) return;

	    switch (cardIndex) {
	        case 0,1,2,3,4 -> performAttack(game, cardIndex);
	        case 5 -> performHeal(game, player);
	        case 6 -> performShield(game, player);
	        case 7 -> performSpecialAttack(game);
	    }

	    cardInfos[cardIndex].setActive(false);
	}


	// Reset all cards back to active
	public void resetCards() {
		for (CardInfo card : cardInfos) {
			card.setActive(true);
		}
	}

	private void performAttack(Game game, int cardIndex) {
		Player player = game.getPlayer();
		Enemy enemy = game.getEnemy();
		player.setAttacking(true);

		int damage;
		String effectPath;
		float scale;
		int frames;
		int[] xOffsets;
		int yOffset;

		switch (cardIndex) {
		case 0:
			damage = 25;
			effectPath = "/a_attackeffects/1_Dark-Bolt.png";
			scale = 4f;
			frames = 11;
			yOffset = -20;
			xOffsets = new int[] { 0 };
			break;

		case 1:
			damage = 20;
			effectPath = "/a_attackeffects/2_Lightning.png";
			scale = 5f;
			frames = 10;
			yOffset = -180;
			xOffsets = new int[] { -30, 0, 30 };
			break;
		case 2:
			damage = 30;
			effectPath = "/a_attackeffects/3_spark.png";
			scale = 6f;
			frames = 7;
			yOffset = 0;
			xOffsets = new int[] { 0 };
			break;
		case 3:
			damage = 80;
			effectPath = "/a_attackeffects/4_Thunderstrike_wblur.png";
			scale = 7.5f;
			frames = 13;
			yOffset = -80;
			xOffsets = new int[] { 0 };
			break;
		case 4:
			damage = 100;
			effectPath = "/a_attackeffects/5_HolyVFX02.png";
			scale = 8.5f;
			frames = 16;
			yOffset = -50;
			xOffsets = new int[] { 0 };
			break;
		default:
			damage = 50;
			effectPath = "/a_attackeffects/Default.png";
			scale = 5f;
			frames = 10;
			yOffset = 0;
			xOffsets = new int[] { 0 };

		}

		enemy.takeDamage(damage);
		enemy.playHurtAnimation();

		float effectX = enemy.getX() + 128;
		float effectY = enemy.getY() + 128;
		for (int xOffset : xOffsets) {
			game.addEffect(new AttackEffect(effectX, effectY, effectPath, frames, scale, xOffset, yOffset));
		}

		game.addDamageText(new DamageText(enemy.getX() + 64, enemy.getY() - 20, "-" + damage, Color.RED));
		gamePanel.getSoundEffectsPlayer().playSound("/soundfx/07_AttackExplosion.wav");

		// game.getTurnManager().setPlayerAttacked(true);
		// game.getTurnManager().startNextTurnWithDelay(1500);
		// game.getTurnManager().setWaitingForPlayer(false);
	}

	private void performHeal(Game game, Player player) {
		int heal = 30;
		player.heal(heal);
		game.addDamageText(new DamageText(player.getX() + 64, player.getY() - 20, "+" + heal, Color.GREEN));
		game.addEffect(new AttackEffect(player.getX() + 64, player.getY() + 64,
				"/a_healingeffects/00_HEAL_Paladin Cross.png", 10, 2f, 40, 30));

	}

	private void performShield(Game game, Player player) {
		int shieldAmount = 50;
		player.addShield(shieldAmount);
		game.addDamageText(
				new DamageText(player.getX() + 64, player.getY() - 40, "Shield +" + shieldAmount, Color.CYAN));
		game.addEffect(new AttackEffect(player.getX() + 64, player.getY() + 64,
				"/a_shieldeffects/00_SHIELD_Shield2.png", 20, 0.5f, 40, 30));

		// game.getTurnManager().setPlayerAttacked(true);
		// game.getTurnManager().startNextTurnWithDelay(1500);
		// game.getTurnManager().setWaitingForPlayer(false);
	}

	private void performSpecialAttack(Game game) {
		Player player = game.getPlayer();
		Enemy enemy = game.getEnemy();
		player.setAttacking(true);

		int damage = 50 + (int) (Math.random() * 21);
		enemy.takeDamage(damage);
		enemy.playHurtAnimation();

		float effectX = enemy.getX() + 128;
		float effectY = enemy.getY() + 128;
		game.addEffect(new AttackEffect(effectX, effectY, "/a_specialeffects/00_SPECIAL_Paladin2.png", 12, 4f, 0, -60));
		game.addEffect(new AttackEffect(effectX, effectY, "/a_attackeffects/Fire-bomb.png", 14, 11f, 0, -60));

		game.addDamageText(new DamageText(enemy.getX() + 64, enemy.getY() - 20, "-" + damage, Color.RED));

		// game.getTurnManager().setPlayerAttacked(true);
		// game.getTurnManager().startNextTurnWithDelay(1500);
		// game.getTurnManager().setWaitingForPlayer(false);
	}
}
