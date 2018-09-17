/*
 * This is a 2-D random walk with memory simulation
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * 
 * @author Bilbo
 */
public class MemoryWalk extends JPanel implements ActionListener{

	// particle info
	int N;
	int[][] xposition;
	int[] laststep;
	double[] avex, avex2, steps;

	// model info
	double rprob;
	int currentT, maxT;

	// visual info
	int xrange; //the visible range
	Color pcolor;
	int[] hist;
	boolean drawstuff;

	// GUI components
	JFrame frame;
	JPanel controlPanel, infoPanel;
	Picture picPanel;
	JTextField NField, TField, pField, RField;
	JLabel NLabel, TLabel, pLabel, RLabel;
	JButton startButton, resetButton, plotsButton, dataButton;

	Plot2D rplot, r2plot;

    /**
     * The constructor
     */
    public MemoryWalk() {
        N = 1;
		maxT = 21;
		
		xposition = new int[N][maxT];
		xposition[0][0] = 1;
		laststep = new int[N];
		for(int i=0; i<N; i++){
			laststep[i] = 1;
		}

		rprob = 2.0/3;

		xrange = 20;
		hist = new int[xrange*2];

		pcolor = new Color(255, 0, 0, 255);
		drawstuff = true;

		rplot = new Plot2D(500,400);
		rplot.setTitle("<x>");
		rplot.setLabels("steps", "<x>");
		rplot.showAxes(true);
		rplot.boxed(true);
		rplot.lines(true);
		r2plot = new Plot2D(500,400);
		r2plot.setTitle("<x^2>");
		r2plot.setLabels("steps", "<x^2>");
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
		pLabel = new JLabel("same prob. = ");
		RLabel = new JLabel("visible size= ");

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		plotsButton = new JButton("Plots");
		plotsButton.addActionListener(this);
		dataButton = new JButton("Write Data");
		dataButton.addActionListener(this);

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
		controlPanel.add(dataButton);

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
		MemoryWalk walker = new MemoryWalk();
	}

    public void actionPerformed(ActionEvent e){
		if(e.getSource() == startButton){
			startSim();
		}else if(e.getSource() == resetButton){
			for(int i=0; i<xposition.length; i++){
				xposition[i][0] = 1;
				laststep[i] = 1;
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
		}else if(e.getSource() == dataButton){
			if(currentT ==0){
				drawstuff = false;
				startSim();
				drawstuff = true;
				picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
			}
			writeData();
		}
	}

	public void startSim(){
		// first set up the system using the given parameters
		try{
			xrange = Integer.valueOf(RField.getText());
			hist = new int[xrange*2];
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
				laststep = new int[N];
				for(int i=0; i<N; i++){
					laststep[i] = 1;
				}
				if(N<1000){
					pcolor = new Color((float)1.0, 0, 0, (float)(1.0/Math.min(10,N)));
				}else{
					pcolor = Color.RED;
				}
				
				for(int i=0; i<N; i++){
					xposition[i][0] = 1;
				}
			}
			currentT = 0;
			
			double tmpprob = Double.valueOf(pField.getText());
			if(tmpprob > 1.0 || tmpprob < 0.0){
				tmpprob = 0.5;
				pField.setText("0.5");
			}
			rprob = tmpprob;
		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
		
		// Then perform the time steps
		Random rand = new Random();
		
		/////////// If prob = 0.5, use a slightly more efficient method /////////
		if(rprob == 0.5){
			for(int i=0; i<maxT-1; i++){
				currentT++;
				for(int j=0; j<xposition.length; j++){
					if(rand.nextBoolean()){
						xposition[j][i+1] = xposition[j][i]+1;
					}else{
						xposition[j][i+1] = xposition[j][i]-1;
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
						xposition[j][i+1] = xposition[j][i]+laststep[j];
					}else{
						xposition[j][i+1] = xposition[j][i]-laststep[j];
						laststep[j] = -1*laststep[j];
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
		avex = new double[maxT-1];
		avex2 = new double[maxT-1];
		steps = new double[maxT-1];
		for(int i=0; i<maxT-1; i++){
			avex[i] = 0;
			avex2[i] = 0;
			for(int j=0; j<N; j++){
				avex[i] += xposition[j][i];
				avex2[i] += xposition[j][i]*xposition[j][i];
			}
			avex[i] = avex[i]/N;
			avex2[i] = avex2[i]/N;
			steps[i] = (double)i+1;
		}

		// now draw it
		rplot.clearData();
		rplot.addX(steps);
		rplot.addY(avex);
		rplot.paint();

		r2plot.clearData();
		r2plot.addX(steps);
		r2plot.addY(avex2);
		r2plot.paint();

		// make a window and display
		PlotWindow rwind = new PlotWindow(rplot);
		PlotWindow r2wind = new PlotWindow(r2plot);
	}
	
	public void writeData(){
		// first compute the data
		avex = new double[maxT-1];
		avex2 = new double[maxT-1];
		steps = new double[maxT-1];
		for(int i=0; i<maxT-1; i++){
			avex[i] = 0;
			avex2[i] = 0;
			for(int j=0; j<N; j++){
				avex[i] += xposition[j][i];
				avex2[i] += xposition[j][i]*xposition[j][i];
			}
			avex[i] = avex[i]/N;
			avex2[i] = avex2[i]/N;
			steps[i] = (double)i+1;
		}
		
		// now write it to a file
		if(N > 0){
			File dataFile = new File("Data");
			if(!dataFile.exists()){
				dataFile.mkdir();
			}
			dataFile = new File("Data/data"+String.valueOf(System.currentTimeMillis()));	//for everything else
			
			try{
				FileWriter writer = new FileWriter(dataFile);
				writer.write("# N="+String.valueOf(N)+" steps="+String.valueOf(maxT)+"\n");
				for(int i=0; i<maxT-1; i++){
					writer.write(String.valueOf(steps[i])+" "+String.valueOf(avex[i])+" "+String.valueOf(avex2[i])+"\n");
				}
				writer.close();
				JOptionPane.showMessageDialog(frame, "successfully recorded data\nFile: "+dataFile.getName());
			}catch(Exception e){
				System.err.println("Error: " + e.getMessage());
				JOptionPane.showMessageDialog(frame, "file writing error");
			}
		}
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
			int linepos = 575;
			int histpos = 500;
			
			//paint the lines
			g.setColor(Color.BLACK);
			if(4*xrange > width){
				g.drawLine(1, linepos, width, linepos);
			}else{
				int tmp = (int)(width/2);
				for(int i=0; i<xrange; i++){
					g.drawLine(tmp+i*(seglength)+2, linepos, tmp+i*(seglength)+seglength-2, linepos);
					g.drawLine(tmp-i*(seglength)-seglength+2, linepos, tmp-i*(seglength)-2, linepos);
				}
			}
			
			//paint the particles
			g.setColor(pcolor);
			int xp = 0;
			int size = (int)Math.max(seglength, 1);
			if(N<1000){
				for(int i=0; i<xposition.length; i++){
					xp = (int)(width/2 - seglength + xposition[i][currentT]*(seglength));
					g.fillOval(xp, linepos-52, size, 50);
				}
			}
			
			
			// draw a distribution thingy
			g.setColor(Color.BLACK);
			g.drawLine(1, histpos, width, histpos);
			g.setColor(Color.RED);
			int maxh = 450;
			int maxn = N;
			int bin;
			for(int i=0; i<hist.length; i++){
				hist[i] = 0;
			}
			for(int i=0; i<xposition.length; i++){
				bin = (int)((xposition[i][currentT] + xrange)*(hist.length-1)/(2*xrange));
				if(bin > 0 && bin < hist.length){
					hist[bin]++;
					if(hist[bin] > maxn){
						maxn = hist[bin];
					}
				}
			}
			for(int i=0; i<hist.length; i++){
				g.fillRect(i*width/hist.length+2, histpos - hist[i]*maxh/maxn, width/hist.length-4, hist[i]*maxh/maxn);
			}
			
			//draw a central line
			g.setColor(Color.BLACK);
			g.drawLine(width/2, 20, width/2, height);
			
			//draw the time
			g.setColor(Color.BLACK);
			g.drawString("t = "+String.valueOf(currentT), 10, 10);
		}

	}
}
