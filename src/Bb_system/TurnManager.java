package Bb_system;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import Aa_main.Game;
import Cc_ui.DamageText;
import Ff_entities.Enemy;
import Ff_entities.Player;
import Gg_effects.AttackEffect;

public class TurnManager {

    // =========================================================
    // ENUM: Enemy Possible Actions
    // =========================================================
    public enum EnemyAction {
        ATTACK, HEAL, SHIELD
    }

    // =========================================================
    // TURN SYSTEM FIELDS
    // =========================================================
    private Queue<String> turnQueue;
    private String currentTurn;

    private boolean playerAttacked = false;
    private boolean enemyAttacked = false;

    private boolean waitingForNextTurn = false;
    private long nextTurnTime = 0;
    private final long TURN_DELAY = 1000; // 1 second delay between turns

    private boolean waitingForPlayer = false;

    // =========================================================
    // BATTLE STATE
    // =========================================================
    private boolean battleOver = false;
    private String winner = "";

    // Enemy turn-skipping flag
    private boolean enemySkipNextTurn = false;

    // =========================================================
    // REFERENCES
    // =========================================================
    private Player player;
    private Enemy enemy;
    private Game game;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public TurnManager(Player player, Enemy enemy, Game game) {
        this.player = player;
        this.enemy = enemy;
        this.game = game;

        // Setup turn order
        turnQueue = new LinkedList<>();
        turnQueue.add("PLAYER");
        turnQueue.add("ENEMY");

        currentTurn = turnQueue.peek();
    }

    // =========================================================
    // GETTERS & FLAGS
    // =========================================================
    public String getCurrentTurn() {
        return currentTurn;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public String getWinner() {
        return winner;
    }

    public boolean hasPlayerAttacked() {
        return playerAttacked;
    }

    public void setPlayerAttacked(boolean attacked) {
        this.playerAttacked = attacked;
    }

    public boolean isWaitingForPlayer() {
        return waitingForPlayer;
    }

    public void setWaitingForPlayer(boolean waiting) {
        this.waitingForPlayer = waiting;
    }

    public boolean isWaitingForNextTurn() {
        return waitingForNextTurn;
    }

    // =========================================================
    // TURN DELAY HANDLING
    // =========================================================
    public void startNextTurnWithDelay() {
        startNextTurnWithDelay(TURN_DELAY);
    }

    // Custom delay setter
    public void startNextTurnWithDelay(long delayMillis) {
        waitingForNextTurn = true;
        nextTurnTime = System.currentTimeMillis() + delayMillis;
    }

    // =========================================================
    // SKIP MECHANIC
    // =========================================================
    public void skipEnemyNextTurn() {
        enemySkipNextTurn = true;
    }

    // =========================================================
    // MAIN UPDATE LOOP
    // =========================================================
    public void update() {
        if (battleOver)
            return;

        // -----------------------------------------
        // CHECK WIN/LOSE CONDITIONS
        // -----------------------------------------
        if (player.isDead()) {
            battleOver = true;
            winner = "ENEMY";
            System.out.println("Player is defeated! GAME OVER.");

            game.getGamePanel().getSoundEffectsPlayer().stopBackgroundMusic();
            game.getGamePanel().getSoundEffectsPlayer().playSound("/soundfx/06_GameOver-1.wav");

            return;
        }

        if (enemy.getCurrentHealth() <= 0) {
            battleOver = true;
            winner = "PLAYER";
            System.out.println("Enemy defeated! YOU WIN!");

            game.getGamePanel().getSoundEffectsPlayer().stopBackgroundMusic();
            game.getGamePanel().getSoundEffectsPlayer().playSound("/soundfx/06_GameVictory.wav");

            return;
        }

        // -----------------------------------------
        // HANDLE DELAY BEFORE NEXT TURN
        // -----------------------------------------
        if (waitingForNextTurn) {
            if (System.currentTimeMillis() >= nextTurnTime) {
                nextTurn();
                waitingForNextTurn = false;
            } else {
                return; // still waiting
            }
        }

        // -----------------------------------------
        // TURN EXECUTION
        // -----------------------------------------
        if (currentTurn.equals("ENEMY") && !enemyAttacked) {

            // Enemy skips the turn
            if (enemySkipNextTurn) {
                System.out.println("Enemy loses this turn!");
                enemySkipNextTurn = false;
                enemyAttacked = true;
                startNextTurnWithDelay();
            } 
            else {
                handleEnemyTurn();
            }
        }

        // Player waits for input
        else if (currentTurn.equals("PLAYER") && !playerAttacked) {
            waitingForPlayer = true;
        }
    }

    // =========================================================
    // ENEMY TURN LOGIC
    // =========================================================
    private void handleEnemyTurn() {
        Random rand = new Random();

        // Build list of possible actions
        ArrayList<EnemyAction> possibleActions = new ArrayList<>();
        possibleActions.add(EnemyAction.ATTACK);

        if (enemy.getCurrentHealth() < enemy.getMaxHealth() * 0.5)
            possibleActions.add(EnemyAction.HEAL);

        if (enemy.getShield() <= 0)
            possibleActions.add(EnemyAction.SHIELD);


        // Randomly select an action
        EnemyAction chosen = possibleActions.get(rand.nextInt(possibleActions.size()));

        switch (chosen) {

            // -----------------------
            // ENEMY ATTACK
            // -----------------------
            case ATTACK:
                int damage = 10 + rand.nextInt(31);

                enemy.playAttackAnimation();

                float effectX = player.getX() + 64;
                float effectY = player.getY() + 64;

                AttackEffect effect = new AttackEffect(
                        effectX, effectY,
                        "/a_attackeffects/0_Croc_Dark1.png",
                        16, 5.0f, 40, -50
                );

                Game.getInstance().addEffect(effect);

                player.takeDamage(damage, game);

                Game.getInstance().addDamageText(
                        new DamageText(player.getX() + 64, player.getY() - 20, "-" + damage, Color.RED)
                );

                System.out.println("Enemy attacks! -" + damage + " HP");
                break;

            // -----------------------
            // ENEMY HEAL
            // -----------------------
            case HEAL:
                int heal = 20 + rand.nextInt(21);
                enemy.heal(heal);

                Game.getInstance().addDamageText(
                        new DamageText(enemy.getX() + 64, enemy.getY() - 20, "+" + heal, Color.GREEN)
                );

                System.out.println("Enemy heals! +" + heal + " HP");
                break;

            // -----------------------
            // ENEMY SHIELD
            // -----------------------
            case SHIELD:
                int shield = 30;
                enemy.addShield(shield);

                Game.getInstance().addDamageText(
                        new DamageText(enemy.getX() + 64, enemy.getY() - 40, "Shield +" + shield, Color.CYAN)
                );

                System.out.println("Enemy shields! +" + shield + " protection");
                break;
        }

        enemyAttacked = true;
        startNextTurnWithDelay();
    }

    // =========================================================
    // TURN ROTATION
    // =========================================================
    private void nextTurn() {
        String finished = turnQueue.poll();  // remove completed turn
        turnQueue.add(finished);             // move it to back of queue

        currentTurn = turnQueue.peek();      // next turn starts

        // Reset action flags
        playerAttacked = false;
        enemyAttacked = false;

        System.out.println("Now it’s " + currentTurn + "’s turn!");
    }
    
    public void endPlayerTurn() {
        if (currentTurn.equals("PLAYER") && !playerAttacked) {
            playerAttacked = true;   // mark player as done
            waitingForPlayer = false;
            startNextTurnWithDelay(); // proceed to enemy turn after a short delay
            System.out.println("Player ended their turn.");
        }
    }

}
