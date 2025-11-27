package Aa_main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import Bb_system.SoundEffectsPlayer;
import Cc_ui.CardInfo;
import Dd_inputs.KeyboardInputs;
import Dd_inputs.MouseInputs;
import Ee_utilz.FontLoader;

//Inheritance
public class GamePanel extends JPanel {

	private MouseInputs mouseInputs;
	private Game game;
	private SoundEffectsPlayer getSoundFx; // sound
	private int lastHoveredCardIndex = -1; // track last hover
	private float hoverScale = 1.1f;  // scale when hovered


	public SoundEffectsPlayer getSoundEffectsPlayer() {
		return getSoundFx;
	}

	// Encapsulation
	public GamePanel(Game game) {
		this.game = game;
		// Make sure the panel can receive focus for input

		this.setFocusable(true);
		this.requestFocus();
		this.getSoundFx = new SoundEffectsPlayer(); // sound
		getSoundFx.playBackgroundMusic("/music/01_InGameBG_Funny_Bit_.wav", true); // loop = true
		// -------------------------
		// Mouse motion listener for dynamic cursor
		// -------------------------
		mouseInputs = new MouseInputs(this);

		addKeyListener(new KeyboardInputs(this));
		addMouseListener(mouseInputs);
		addMouseMotionListener(mouseInputs);

		// -------------------------------
		// Add MouseMotionListener for cursor changes
		// -------------------------------
		addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
		    @Override
		    public void mouseMoved(MouseEvent e) {
		        // Skip hover if dialogue is active, battle start is showing, or cards are entering
		    	// Skip hover if cards are still entering
		    	if (!game.isCardsActive() || game.getCardManager().isAnimating()) {
		    	    setHoveredCardIndex(-1);
		    	    setCursor(Cursor.getDefaultCursor());
		    	    lastHoveredCardIndex = -1;
		    	    return;
		    	}


		        int mouseX = e.getX();
		        int mouseY = e.getY();

		        int hoverIndex = game.getCardClickHandler().getHoveredCardIndex(mouseX, mouseY);
		        setHoveredCardIndex(hoverIndex);

		        // Play hover sound only on new hover
		        if (hoverIndex != -1 && hoverIndex != lastHoveredCardIndex) {
		            getSoundEffectsPlayer().playSound("/soundfx/01_Hover.wav");
		        }

		        lastHoveredCardIndex = hoverIndex;

		        // Cursor
		        if (hoverIndex != -1) {
		            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		        } else {
		            setCursor(Cursor.getDefaultCursor());
		        }
		    }
		});



		setPanelSize();
	}

	private void setPanelSize() {
		Dimension size = new Dimension(1280, 800);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);

	}

	public void updateGame() {

	}

	private int hoveredCardIndex = -1;

	public void setHoveredCardIndex(int index) {
		this.hoveredCardIndex = index;
	}

	public int getHoveredCardIndex() {
		return hoveredCardIndex;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// fill background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		// center area
		int gameWidth = 1280;
		int gameHeight = 800;
		int x = (getWidth() - gameWidth) / 2;
		int y = (getHeight() - gameHeight) / 2;
		Graphics g2 = g.create(x, y, gameWidth, gameHeight);

		// render game
		game.render(g2);

		// draw hover info
		int hover = getHoveredCardIndex();
		if (hover != -1) {
			CardInfo info = game.getCardClickHandler().getCardInfos()[hover];

			Point mousePos = getMousePosition();
			if (mousePos != null) {
				int tooltipX = mousePos.x + 20;
				int tooltipY = mousePos.y + 20;

//	            Font tooltipFont = new Font("Arial", Font.BOLD, 18);
//	            g.setFont(tooltipFont);

				// Load ByteBounce font at size 18
				Font tooltipFont = FontLoader.loadFont("/fonts/ByteBounce.ttf", 30f);
				g.setFont(tooltipFont);

				// Text
				String line1 = "Name: " + info.getName();
				String line2 = "Desc: " + info.getDescription();
				String line3 = "Damage: " + info.getDamage();

				// Auto-size background
				FontMetrics fm = g.getFontMetrics();
				int width = Math.max(Math.max(fm.stringWidth(line1), fm.stringWidth(line2)), fm.stringWidth(line3))
						+ 20;
				int height = 90;

				// Background (semi-transparent, round)
				g.setColor(new Color(20, 20, 20, 200));
				g.fillRoundRect(tooltipX, tooltipY, width, height, 15, 15);

				// Border
				g.setColor(Color.WHITE);
				g.drawRoundRect(tooltipX, tooltipY, width, height, 15, 15);

				// Draw text
				g.drawString(line1, tooltipX + 10, tooltipY + 25);
				g.drawString(line2, tooltipX + 10, tooltipY + 45);
				g.drawString(line3, tooltipX + 10, tooltipY + 65);
			}
		}

		g2.dispose();
	}

	public Game getGame() {
		return game;
	}

}
