package Aa_main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import Bb_system.*;
import Cc_ui.*;
import Dd_inputs.CardClickHandler;
import Ee_utilz.FontLoader;
import Ff_entities.*;
import Gg_effects.AttackEffect;

public class Game implements Runnable {

	// ---------------------------
	// Constants
	// ---------------------------
	private final int FPS_SET = 120;
	private final int UPS_SET = 200;

	// ---------------------------
	// Window & Panel
	// ---------------------------
	private GameWindow gameWindow;
	private GamePanel gamePanel;
	private Thread gameThread;

	// ---------------------------
	// Entities
	// ---------------------------
	private Player player;
	private Enemy enemy;

	// ---------------------------
	// Managers
	// ---------------------------
	private TurnManager turnManager;
	private DialogueManager dialogueManager;
	private BackgroundManager backgroundManager;
	private CardManager cardManager;
	private HUDRenderer hudRenderer;
	private DamageTextManager damageTextManager;
	private BattleUI battleUI;

	// ---------------------------
	// Fonts
	// ---------------------------
	private Font bytebounce;

	// ---------------------------
	// CardClickHandler
	// ---------------------------
	private CardClickHandler cardClickHandler;

	// ---------------------------
	// Attack Effects
	// ---------------------------
	private ArrayList<AttackEffect> activeEffects = new ArrayList<>();

	// ---------------------------
	// Singleton
	// ---------------------------
	private static Game instance;

	public static Game getInstance() {
		return instance;
	}

	// ---------------------------
	// Constructor
	// ---------------------------
	public Game() {
	    instance = this;

	    // Load font
	    bytebounce = FontLoader.loadFont("/fonts/ByteBounce.ttf", 60f);

	    //  Initialize managers and entities that don't need gamePanel
	    initClassesWithoutBattleUI();

	    // Create the panel and window
	    gamePanel = new GamePanel(this);
	    cardClickHandler = new CardClickHandler(gamePanel);
	    gameWindow = new GameWindow(gamePanel);
	    gamePanel.requestFocus();

	    // BattleUI (requires gamePanel for End Turn button)
	    battleUI = new BattleUI(bytebounce, turnManager, gamePanel);

	    // Start game loop
	    startGameLoop();
	}

	// ---------------------------
	// Initialization (without BattleUI yet)
	private void initClassesWithoutBattleUI() {
	    player = new Player(100, 450);
	    enemy = new Enemy(930, 400);

	    hudRenderer = new HUDRenderer(bytebounce, player, enemy);
	    turnManager = new TurnManager(player, enemy, this);
	    dialogueManager = new DialogueManager(bytebounce);
	    damageTextManager = new DamageTextManager(bytebounce);
	    backgroundManager = new BackgroundManager("/BG1.png");
	    cardManager = new CardManager();

	    try {
	        BufferedImage playerBubble = ImageIO.read(getClass().getResource("/ui/bubble_player.png"));
	        BufferedImage enemyBubble = ImageIO.read(getClass().getResource("/ui/bubble_enemy.png"));

	        dialogueManager.addDialogue("Juan", "I won't let you win!", 330, 630, playerBubble, 120, 380);
	        dialogueManager.addDialogue("Mang Gagantso", "Ha! You don't stand a chance!", 890, 630, enemyBubble, 680, 360);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    dialogueManager.start();

	}


	// ---------------------------
	// Start Game Loop
	// ---------------------------
	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	// ---------------------------
	// Update Logic
	// ---------------------------
	public void update() {

		// Dialogue updates
		if (dialogueManager != null && dialogueManager.isActive()) {
			dialogueManager.update();

			// Trigger Battle Start when dialogue finishes
			if (!dialogueManager.isActive() && !battleUI.isShowingBattleStart()) {
				battleUI.triggerBattleStart();
			}
			return; // Skip battle logic until dialogue finishes
		}

		// Battle Start updates
		if (battleUI.isShowingBattleStart()) {
			battleUI.updateBattleStart();

			// Trigger card entrance when Battle Start finishes
			if (!battleUI.isShowingBattleStart() && !cardManager.isAnimating()) {
				cardManager.startCardEntrance(gamePanel.getHeight());
			}

			return; // Skip other battle updates until Battle Start disappears
		}

		// Card animation updates
		cardManager.updateAnimation();

		// Skip battle logic until cards have finished sliding in
		if (cardManager.isAnimating())
			return;

		// Normal battle updates
		player.update();
		enemy.update();
		damageTextManager.update();
		turnManager.update();   // your existing turn logic
	    battleUI.update();      // dynamically show/hide the button
		updateEffects();
	}

	// ---------------------------
	// Render Game
	// ---------------------------
	public void render(Graphics g) {
		// Draw background & entities
		backgroundManager.draw(g);
		player.render(g);
		enemy.render(g);

		Graphics2D g2d = (Graphics2D) g;

		// Battle Start overlay
		if (battleUI.isShowingBattleStart()) {
			battleUI.drawBattleStart(g2d, gamePanel.getWidth(), gamePanel.getHeight());
			return;
		}

		// HUD & effects
		hudRenderer.draw(g2d);
		renderEffects(g2d);
		damageTextManager.draw(g2d);

		// Dialogue overlay
		if (dialogueManager != null && dialogueManager.isActive()) {
			dialogueManager.render(g2d);
		} else {
			// Draw cards
			cardManager.draw(g2d, gamePanel.getWidth(), gamePanel.getHeight());

			// Turn indicator or result
			if (!turnManager.isBattleOver()) {
				battleUI.drawTurnIndicator(g2d, gamePanel.getWidth());
			} else {
				battleUI.drawBattleResult(g2d, gamePanel.getWidth(), gamePanel.getHeight());
			}
		}
	}

	// ---------------------------
	// Attack Effects
	// ---------------------------
	public void addEffect(AttackEffect effect) {
		activeEffects.add(effect);
	}

	private void updateEffects() {
		for (int i = 0; i < activeEffects.size(); i++) {
			AttackEffect e = activeEffects.get(i);
			e.update();
			if (e.isFinished()) {
				activeEffects.remove(i);
				i--;
			}
		}
	}

	private void renderEffects(Graphics g) {
		for (AttackEffect e : activeEffects)
			e.render(g);
	}

	// ---------------------------
	// Game Loop
	// ---------------------------
	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long previousTime = System.nanoTime();
		double deltaU = 0, deltaF = 0;
		int frames = 0, updates = 0;
		long lastCheck = System.currentTimeMillis();

		while (true) {
			long currentTime = System.nanoTime();
			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;

			if (deltaU >= 1) {
				update();
				updates++;
				deltaU--;
			}

			if (deltaF >= 1) {
				gamePanel.repaint();
				frames++;
				deltaF--;
			}

			if (System.currentTimeMillis() - lastCheck >= 1000) {
			    lastCheck = System.currentTimeMillis();

			    System.out.println("FPS: " + frames + " | UPS: " + updates);

			    frames = 0;
			    updates = 0;
			}

		}
	}

	// ---------------------------
	// Accessors
	// ---------------------------
	public Player getPlayer() {
		return player;
	}

	public Enemy getEnemy() {
		return enemy;
	}

	public TurnManager getTurnManager() {
		return turnManager;
	}

	public DialogueManager getDialogueManager() {
		return dialogueManager;
	}

	public CardManager getCardManager() {
		return cardManager;
	}

	public BattleUI getBattleUI() {
		return battleUI;
	}

	public GamePanel getGamePanel() {
		return gamePanel;
	}

	public CardClickHandler getCardClickHandler() {
		return cardClickHandler;
	}

	// ---------------------------
	// Card Active Flag
	// ---------------------------
	public boolean isCardsActive() {
		return !(dialogueManager != null && dialogueManager.isActive()) && !battleUI.isShowingBattleStart()
				&& !cardManager.isAnimating();
	}

	// ---------------------------
	// Restart Battle
	// ---------------------------
	public void restartBattle() {
	    player.setX(100);
	    player.setY(450);
	    player.resetHealth();

	    enemy.resetForBattle();

	    // Reset cards
	    cardClickHandler.resetCards();

	    turnManager = new TurnManager(player, enemy, this);
	    hudRenderer = new HUDRenderer(bytebounce, player, enemy);
	    battleUI = new BattleUI(bytebounce, turnManager, gamePanel);
	    damageTextManager = new DamageTextManager(bytebounce);
	    activeEffects.clear();

	    gamePanel.getSoundEffectsPlayer().stopBackgroundMusic();
	    gamePanel.getSoundEffectsPlayer().playBackgroundMusic(
	        "/music/01_InGameBG_Funny_Bit_.wav", true
	    );

	    System.out.println("Battle restarted!");
	}


	public void addDamageText(DamageText text) {
		damageTextManager.addDamageText(text);
	}

	public void windowFocusLost() {
		player.resetDirectionBooleans();
	}
}
