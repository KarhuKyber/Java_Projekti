package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Visualisation2 extends Canvas implements IVisualisation {
	private final GraphicsContext gc;
	private int customerCount = 0;

	// live values
	private int inside = 0;
	private int barQ = 0, slotsQ = 0, bjQ = 0, rouletteQ = 0;

	public Visualisation2(int w, int h) {
		super(w, h);
		gc = this.getGraphicsContext2D();
		clearDisplay();
		redraw();
	}

	@Override
	public void clearDisplay() {
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void newCustomer() {
		customerCount++;
		// optional: we keep this, but main view is updateCasinoView
		redraw();
	}

	@Override
	public void updateCasinoView(int inside, int barQ, int slotsQ, int bjQ, int rouletteQ) {
		this.inside = inside;
		this.barQ = barQ;
		this.slotsQ = slotsQ;
		this.bjQ = bjQ;
		this.rouletteQ = rouletteQ;
		redraw();
	}

	private void redraw() {
		clearDisplay();

		double w = getWidth();
		double h = getHeight();

		// split canvas into 2x2
		double halfW = w / 2.0;
		double halfH = h / 2.0;

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);

		// rectangles
		gc.strokeRect(5, 5, halfW - 10, halfH - 10);                 // BAR
		gc.strokeRect(halfW + 5, 5, halfW - 10, halfH - 10);         // SLOTS
		gc.strokeRect(5, halfH + 5, halfW - 10, halfH - 10);         // BLACKJACK
		gc.strokeRect(halfW + 5, halfH + 5, halfW - 10, halfH - 10); // ROULETTE

		gc.setFill(Color.BLACK);
		gc.setFont(new Font(16));

		gc.fillText("BAR (queue: " + barQ + ")", 15, 30);
		gc.fillText("SLOTS (queue: " + slotsQ + ")", halfW + 15, 30);
		gc.fillText("BLACKJACK (queue: " + bjQ + ")", 15, halfH + 30);
		gc.fillText("ROULETTE (queue: " + rouletteQ + ")", halfW + 15, halfH + 30);

		gc.setFont(new Font(14));
		gc.fillText("Inside casino: " + inside, 15, h - 15);

		// optional: show customer count
		gc.setFill(Color.RED);
		gc.setFont(new Font(14));
		gc.fillText("Created customers: " + customerCount, halfW + 15, h - 15);
	}
}