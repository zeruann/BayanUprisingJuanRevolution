package Dd_inputs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Aa_main.Game;
import Aa_main.GamePanel;


public class KeyboardInputs implements KeyListener{
	private Game game;
	private GamePanel gamePanel;
	public KeyboardInputs(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		 this.game = gamePanel.getGame(); // <-- add this
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	} 
	
	@Override
	public void keyPressed(KeyEvent e) {

		int key = e.getKeyCode();


		// Retry option
		if (key == KeyEvent.VK_R) {
		    if (game.getBattleUI().isShowingRetry()) {
		        game.restartBattle(); 
		    }
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}


	
}
