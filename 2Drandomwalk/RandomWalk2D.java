/*
 * This is a 2-D random walk simulation
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * 
 * @author Bilbo
 */
public class RandomWalk2D extends JPanel implements ActionListener{

	// particle info
	int N;
	int[][] xposition;
	int[][] yposition;
	double[][] rposition;
	double[] aver, aver2, steps;

	// model info
	double rprob;
	int currentT, maxT;

	// visual info
	int xrange; //the visible range
	Color pcolor;
	boolean drawstuff;

	// GUI components
	JFrame frame;
	JPanel controlPanel, infoPanel;
	Picture picPanel;
	JTextField NField, TField, pField, RField;
	JLabel NLabel, TLabel, pLabel, RLabel;
	JButton startButton, resetButton, plotsButton;

	Plot2D rplot, r2plot;

    /**
     * The constructor
     */
    public RandomWalk2D() {
        N = 1;
		maxT = 21;
		
		xposition = new int[N][maxT];
		yposition = new int[N][maxT];
		xposition[0][0] = 0;
		yposition[0][0] = 0;

		rprob = 0.25;

		xrange = 20;

		pcolor = new Color(255, 0, 0, 255);
		drawstuff = true;

		rplot = new Plot2D(500,400);
		rplot.setTitle("<r>");
		rplot.setLabels("steps", "<r>");
		rplot.showAxes(true);
		rplot.boxed(true);
		rplot.lines(true);
		r2plot = new Plot2D(500,400);
		r2plot.setTitle("<r^2>");
		r2plot.setLabels("steps", "<r^2>");
		r2plot.showAxes(true);
		r2plot.boxed(true);
		r2plot.lines(true);

		controlPanel = new JPanel();
		infoPanel = new JPanel();
		picPanel = new Picture();

		NField = new JTextField(String.valueOf(N), 8);
		TField = new JTextField(String.valueOf(maxT-1), 8);
		pField = new JTextField(String.valueOf(rprob), 8);
		RField = new JTextField(String.valueOf(xrange), 8);
		NLabel = new JLabel("N = ");
		TLabel = new JLabel("maxT = ");
		pLabel = new JLabel("right prob. = ");
		RLabel = new JLabel("visible size= ");

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		plotsButton = new JButton("Plots");
		plotsButton.addActionListener(this);

		controlPanel.setLayout(new GridLayout(6,2));
		controlPanel.add(NLabel);
		controlPanel.add(NField);
		controlPanel.add(TLabel);
		controlPanel.add(TField);
		controlPanel.add(pLabel);
		controlPanel.add(pField);
		controlPanel.add(RLabel);
		controlPanel.add(RField);
		controlPanel.add(startButton);
		controlPanel.add(resetButton);
		controlPanel.add(plotsButton);

		setLayout(new BorderLayout());
		add(picPanel, BorderLayout.CENTER);
		add(controlPanel, BorderLayout.EAST);
		
		frame = new JFrame("2-D random walk");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container pane = frame.getContentPane();
		pane.add(this);
		
		pane.validate();
		//Display the window.
        frame.pack();
        frame.setVisible(true);
		
    }
	
	public static void main(String[] args){
		RandomWalk2D walker = new RandomWalk2D();
	}

    public void actionPerformed(ActionEvent e){
		if(e.getSource() == startButton){
			startSim();
		}else if(e.getSource() == resetButton){
			for(int i=0; i<xposition.length; i++){
				xposition[i][0] = 0;
				yposition[i][0] = 0;
			}
			currentT = 0;
			picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
		}else if(e.getSource() == plotsButton){
			if(currentT ==0){
				drawstuff = false;
				startSim();
				drawstuff = true;
				picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
			}
			drawPlots();
		}
	}

	public void startSim(){
		// first set up the system using the given parameters
		try{
			xrange = Integer.valueOf(RField.getText());
			int tmpN = Integer.valueOf(NField.getText());
			int tmpT = Integer.valueOf(TField.getText());
			if(tmpN <= 0){
				tmpN = 1;
				NField.setText("1");
			}
			if(tmpN != N || tmpT != maxT){
				N = tmpN;
				maxT = tmpT + 1;
				xposition = new int[N][maxT];
				yposition = new int[N][maxT];
				//pcolor = new Color((float)1.0, 0, 0, (float)(1.0/Math.min(10,N)));
				for(int i=0; i<N; i++){
					xposition[i][0] = 0;
					yposition[i][0] = 0;
				}
			}
			currentT = 0;
			
			double tmpprob = Double.valueOf(pField.getText());
			if(tmpprob > 1.0 || tmpprob < 0.0){
				tmpprob = 0.25;
				pField.setText("0.25");
			}
			rprob = tmpprob;
		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
		
		// Then perform the time steps
		Random rand = new Random();
		
		/////////// If prob = 0.25, use a slightly more efficient method /////////
		if(rprob == 0.25){
			int val;
			for(int i=0; i<maxT-1; i++){
				currentT++;
				for(int j=0; j<xposition.length; j++){
					val = rand.nextInt(4);
					if(val == 0){
						xposition[j][i+1] = xposition[j][i]+1;
						yposition[j][i+1] = yposition[j][i];
					}else if(val == 1){
						xposition[j][i+1] = xposition[j][i];
						yposition[j][i+1] = yposition[j][i]+1;
					}else if(val == 2){
						xposition[j][i+1] = xposition[j][i]-1;
						yposition[j][i+1] = yposition[j][i];
					}else{
						xposition[j][i+1] = xposition[j][i];
						yposition[j][i+1] = yposition[j][i]-1;
					}
				}
				if(drawstuff){
					if(N < 100 && maxT < 500){
						picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
						try{
							Thread.sleep(10);
						}catch(InterruptedException ignore){}
					}else if(currentT%4 == 0){
						picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
					}
				}
			}
		}else{
			double val;
			for(int i=0; i<maxT-1; i++){
				currentT++;
				for(int j=0; j<xposition.length; j++){
					val = rand.nextDouble();
					if(val < rprob){
						xposition[j][i+1] = xposition[j][i]+1;
						yposition[j][i+1] = yposition[j][i];
					}else if(val < rprob + (1-rprob)/3){
						xposition[j][i+1] = xposition[j][i];
						yposition[j][i+1] = yposition[j][i]+1;
					}else if(val < rprob + (1-rprob)*2/3){
						xposition[j][i+1] = xposition[j][i]-1;
						yposition[j][i+1] = yposition[j][i];
					}else {
						xposition[j][i+1] = xposition[j][i];
						yposition[j][i+1] = yposition[j][i]-1;
					}
				}
				if(drawstuff){
					if(N < 100 && maxT < 500){
						picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
						try{
							Thread.sleep(10);
						}catch(InterruptedException ignore){}
					}else if(currentT%4 == 0){
						picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
					}
				}
			}
		}
		picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
	}

	public void drawPlots(){
		// first compute the data
		rposition = new double[N][maxT];
		aver = new double[maxT];
		aver2 = new double[maxT];
		steps = new double[maxT];
		double tmpavex = 0;
		double tmpavey = 0;
		for(int i=0; i<maxT; i++){
			aver[i] = 0;
			aver2[i] = 0;
			tmpavex = 0;
			tmpavey = 0;
			for(int j=0; j<N; j++){
				rposition[j][i] = Math.sqrt(xposition[j][i]*xposition[j][i] + yposition[j][i]*yposition[j][i]);
				tmpavex += xposition[j][i];
				tmpavey += yposition[j][i];
				aver2[i] += rposition[j][i]*rposition[j][i];
			}
			tmpavex = tmpavex/N;
			tmpavey = tmpavey/N;
			aver[i] = Math.sqrt(tmpavex*tmpavex + tmpavey*tmpavey);
			aver2[i] = aver2[i]/N;
			steps[i] = (double)i;
		}

		// now draw it
		rplot.clearData();
		rplot.addX(steps);
		rplot.addY(aver);
		rplot.paint();

		r2plot.clearData();
		r2plot.addX(steps);
		r2plot.addY(aver2);
		r2plot.paint();

		// make a window and display
		PlotWindow rwind = new PlotWindow(rplot);
		PlotWindow r2wind = new PlotWindow(r2plot);
	}

////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	public class Picture extends JPanel{
		public int width, height;
		public Picture(){
			super();
			width = 700;
			height = 700;
			setPreferredSize(new Dimension(width, height));
			setBackground(Color.WHITE);
		}

		// ye olde painting method
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int seglength = width/xrange/2;
			if (seglength<1) seglength = 1;
			int partsize = Math.max(2, seglength/2);

			//paint the particles
			g.setColor(pcolor);
			int xp = 0;
			int yp = 0;
			for(int i=0; i<xposition.length; i++){
				xp = (int)(width/2 + xposition[i][currentT]*seglength);
				yp = (int)(height/2 - yposition[i][currentT]*seglength);
				g.fillOval(xp-partsize/2, yp-partsize/2, partsize, partsize);
			}
			
			// if there is only one particle, draw the path
			if(N == 1){
				g.setColor(Color.BLACK);
				int xp2, yp2;
				for(int i=0; i<currentT; i++){ //remember that MaxT is always > currentT
					xp = (int)(width/2 + xposition[0][i]*seglength);
					yp = (int)(height/2 - yposition[0][i]*seglength);
					xp2 = (int)(width/2 + xposition[0][i+1]*seglength);
					yp2 = (int)(height/2 - yposition[0][i+1]*seglength);
					g.drawLine(xp, yp, xp2, yp2);
				}
			}

			//draw the time
			g.setColor(Color.BLACK);
			g.drawString("t = "+String.valueOf(currentT), 10, 10);
		}

	}
}
