import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

/**
 * This is an N-element coupled Fitzhugh-Nagumo simulation with noise
 * useful for exploring stochastic resonance.
 */
public class FitzHughNagumo extends JPanel implements ActionListener{
	
	// discretization info
	int nt;
	double dt, totalT;
	double currentT;
	int currentTind;

	// model info
	int N;
	double[][] wn;
	double[][] vn;
	double[] theta, D;
	double[][] k;
	double alpha, beta, gamma, A, omega, epsilon;
	Random rand;
	boolean withNoise;

	// integrator parts
	static int EM_METHOD = 0;
	static int RK4_METHOD = 1;
	static int APC4_METHOD = 2;
	int method;
	double[] vntemp;
	double[][] rk4tmp1, rk4tmp2, rk4tmp3, rk4tmp4;
	double[][] apc4tmp1, apc4tmp2, apc4tmp3, apc4tmp4, apc4tmp5;
	double[][] emtmp;
	double sqrtdt;

	// visual info
	int width, height;
	double zoomfactor;
	boolean splitGraphs;

	// GUI components
	JFrame frame;
	JPanel controlPanel, infoPanel;
	Picture picPanel;
	JTextField NField, TField, alphaField, betaField, gammaField, epsilonField, AField, omegaField;
	JLabel NLabel, TLabel, alphaLabel, betaLabel, gammaLabel, epsilonLabel, ALabel, omegaLabel;
	JButton startButton, stopButton, resetButton, setButton;
	JButton zoomInButton, zoomOutButton, setkButton, setDButton, splitButton;
	JRadioButton noiseButton;
	BufferedImage image;
	int drawn, drawto;

	FHNWorker worker;

    /**
     * The constructor
     */
    public FitzHughNagumo() {
		super();
		setBackground(Color.WHITE);
		
		//discretization parts
		totalT = 10.0;
		nt = (int)(totalT*2000);
		dt = totalT/(nt-1);
		currentT = 0.0;
		currentTind = 0;
		sqrtdt = Math.sqrt(dt);
		
		//model parts
		N = 1;
		vn = new double[N][nt];
		wn = new double[N][nt];
		for (int i=0; i<N; i++){
			vn[i][0] = 0.0;
			wn[i][0] = 0.0;
		}
		k = new double[N][N];
		theta = new double[N];
		D = new double[N];
		for (int i=0; i<N; i++){
			for (int j=0; j<N; j++){
				k[i][j] = 1.0;
			}
			D[i] = 0.01;
			theta[i] = 0.0;
		}
		alpha = 0.0;
		beta = 1.0;
		gamma = 1.5;
		A = 0.0;
		omega = 3.0;
		epsilon = 0.005;
		rand = new Random();
		withNoise = true;
		
		//integrator parts
		method = EM_METHOD;
		vntemp = new double[N];
		rk4tmp1 = new double[2][N];
		rk4tmp2 = new double[2][N];
		rk4tmp3 = new double[2][N];
		rk4tmp4 = new double[2][N];
		apc4tmp1 = new double[2][N];
		apc4tmp2 = new double[2][N];
		apc4tmp3 = new double[2][N];
		apc4tmp4 = new double[2][N];
		apc4tmp5 = new double[2][N];
		emtmp = new double[2][N];
		
		//view parts
		width = 1200;
		height = 500;
		zoomfactor = 4;
		splitGraphs = false;
		
		worker = new FHNWorker();
		
		//GUI parts
		controlPanel = new JPanel();
		infoPanel = new JPanel();
		picPanel = new Picture(width-300, height);

		NField = new JTextField(String.valueOf(N), 8);
		TField = new JTextField(String.valueOf(totalT), 8);
		alphaField = new JTextField(String.valueOf(alpha), 8);
		betaField = new JTextField(String.valueOf(beta), 8);
		gammaField = new JTextField(String.valueOf(gamma), 8);
		AField = new JTextField(String.valueOf(A), 8);
		omegaField = new JTextField(String.valueOf(omega), 8);
		epsilonField = new JTextField(String.valueOf(epsilon), 8);
		NLabel = new JLabel("N = ");
		TLabel = new JLabel("T = ");
		alphaLabel = new JLabel("alpha = ");
		betaLabel = new JLabel("beta = ");
		gammaLabel = new JLabel("gamma = ");
		ALabel = new JLabel("A = ");
		omegaLabel = new JLabel("omega = ");
		epsilonLabel = new JLabel("epsilon = ");

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		setButton = new JButton("Set values");
		setButton.addActionListener(this);
		setkButton = new JButton("Set k");
		setkButton.addActionListener(this);
		setDButton = new JButton("Set D");
		setDButton.addActionListener(this);
		zoomOutButton = new JButton("Zoom out");
		zoomOutButton.addActionListener(this);
		zoomInButton = new JButton("Zoom in");
		zoomInButton.addActionListener(this);
		splitButton = new JButton("Split graphs");
		splitButton.addActionListener(this);
		noiseButton = new JRadioButton("noise", true);
		noiseButton.addActionListener(this);

		controlPanel.setLayout(new GridLayout(14,2));
		controlPanel.add(NLabel);
		controlPanel.add(NField);
		controlPanel.add(TLabel);
		controlPanel.add(TField);
		controlPanel.add(alphaLabel);
		controlPanel.add(alphaField);
		controlPanel.add(betaLabel);
		controlPanel.add(betaField);
		controlPanel.add(gammaLabel);
		controlPanel.add(gammaField);
		controlPanel.add(epsilonLabel);
		controlPanel.add(epsilonField);
		controlPanel.add(ALabel);
		controlPanel.add(AField);
		controlPanel.add(omegaLabel);
		controlPanel.add(omegaField);
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(resetButton);
		controlPanel.add(setButton);
		controlPanel.add(setkButton);
		controlPanel.add(setDButton);
		controlPanel.add(zoomOutButton);
		controlPanel.add(zoomInButton);
		controlPanel.add(splitButton);
		controlPanel.add(noiseButton);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(picPanel);
		add(controlPanel);

		frame = new JFrame("FitzHugh-Nagumo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container pane = frame.getContentPane();
		pane.add(this);

		pane.validate();
		//Display the window.
        frame.pack();
        frame.setVisible(true);

    }

	public static void main(String[] args){
		FitzHughNagumo fhn = new FitzHughNagumo();
	}

    public void actionPerformed(ActionEvent e){
		if(e.getSource() == startButton){
			if(!worker.isWorking){
				startSim();
			}
		}else if(e.getSource() == stopButton){
			worker.cancelled = true;
		}else if(e.getSource() == resetButton){
			worker.cancelled = true;
			while(!worker.isDone()){
				try{
					Thread.sleep(1);
				}catch(Exception ignore){}
			}
			currentT = 0.0;
			currentTind = 0;
			drawn = 0;
			drawto = 0;
			for (int i=0; i<N; i++){
				vn[i][0] = 0.0;
				wn[i][0] = 0.0;
			}
			picPanel.clearImage();
			repaint();
		}else if(e.getSource() == setButton){
			if(!worker.isWorking){
				setParams();
				picPanel.repaint();
			}
		}else if(e.getSource() == setkButton){
			if(!worker.isWorking){
				InputPanel input = new InputPanel("k");
			}
		}else if(e.getSource() == setDButton){
			if(!worker.isWorking){
				InputPanel input = new InputPanel("D");
			}
		}else if(e.getSource() == zoomOutButton){
			zoomfactor = zoomfactor*2;
			drawn = 0;
			drawto = currentTind;
			picPanel.clearImage();
			picPanel.paintInterval();
			picPanel.repaint();
		}else if(e.getSource() == zoomInButton){
			zoomfactor = zoomfactor/2;
			drawn = 0;
			drawto = currentTind;
			picPanel.clearImage();
			picPanel.paintInterval();
			picPanel.repaint();
		}else if(e.getSource() == splitButton){
			if(!splitGraphs){
				splitGraphs = !splitGraphs;
				splitButton.setText("Join graphs");
				height = 700;
				picPanel.setHeight(height);
			}else{
				splitGraphs = !splitGraphs;
				splitButton.setText("Split graphs");
				height = 500;
				picPanel.setHeight(height);
			}
			frame.pack();
			repaint();
		}else if(e.getSource() == noiseButton){
			withNoise = !withNoise;
		}
	}

	// runs the simulation
	public void startSim(){
		worker = new FHNWorker();
		worker.execute();
	}

	// sets the values written in the fields
	public void setParams(){
		try{
			int tmpN = Integer.valueOf(NField.getText());
			double tmptotalT = Double.valueOf(TField.getText());
			boolean tchanged = tmptotalT != totalT;
			if(tchanged){
				totalT = tmptotalT;
				nt = (int)(totalT*2000);
				dt = totalT/(nt-1);
				sqrtdt = Math.sqrt(dt);
				currentT = 0.0;
				currentTind = 0;
			}
			
			if(tmpN <= 0){
				tmpN = 1;
				NField.setText("1");
			}
			if(tmpN != N || tchanged){
				N = tmpN;
				vn = new double[N][nt];
				wn = new double[N][nt];
				for (int i=0; i<N; i++){
					vn[i][0] = 0.0;
					wn[i][0] = 0.0;
				}
				double tmpk = k[0][0];
				double tmpD = D[0];
				k = new double[N][N];
				theta = new double[N];
				D = new double[N];
				for (int i=0; i<N; i++){
					for (int j=0; j<N; j++){
						k[i][j] = 0.0;
					}
					D[i] = 0.0;
					theta[i] = 0.0;
				}
				k[0][0] = tmpk;
				D[0] = tmpD;
				
				vntemp = new double[N];
				rk4tmp1 = new double[2][N];
				rk4tmp2 = new double[2][N];
				rk4tmp3 = new double[2][N];
				rk4tmp4 = new double[2][N];
				apc4tmp1 = new double[2][N];
				apc4tmp2 = new double[2][N];
				apc4tmp3 = new double[2][N];
				apc4tmp4 = new double[2][N];
				apc4tmp5 = new double[2][N];
				emtmp = new double[2][N];
			}
			
			alpha = Double.valueOf(alphaField.getText());
			beta = Double.valueOf(betaField.getText());
			gamma = Double.valueOf(gammaField.getText());
			epsilon = Double.valueOf(epsilonField.getText());
			A = Double.valueOf(AField.getText());
			omega = Double.valueOf(omegaField.getText());

		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
	}

	// this is the FHN model not including noise
	// it takes the v and w data for each element
	// it returns the time derivative (the overwritten input)
	public double[][] diffEq(double[][] vwn, double t){
		for (int i=0; i<N; i++){
			vntemp[i] = vwn[0][i];
		}
		for (int i=0; i<N; i++){
			vwn[0][i] = (vwn[0][i] - vwn[0][i]*vwn[0][i]*vwn[0][i] - vwn[1][i] + alpha + A*Math.cos(omega*t))/epsilon;
			for (int j=0; j<N; j++){
				if(j != i){
					vwn[0][i] = vwn[0][i] + k[i][j]*(vntemp[j]-vntemp[i]);
				}
			}
			
			vwn[1][i] = gamma*vntemp[i] - vwn[1][i] + beta;
		}
		return vwn;
	}
	
	// this is the Euler-maruyama time integrator
	// it does n time steps per call
	public void EM(int n){
		for(int step=0; step<n; step++){
			//prepare temp
			for (int i=0; i<N; i++){
				emtmp[0][i] = vn[i][currentTind];
				emtmp[1][i] = wn[i][currentTind];
			}
			
			emtmp = diffEq(emtmp, currentT);	//first
			// perform a time step
			for (int i=0; i<N; i++){
				vn[i][(currentTind+1)%nt] = vn[i][currentTind] + dt*emtmp[0][i];
				if(withNoise){
					 vn[i][(currentTind+1)%nt] += D[i]/epsilon*sqrtdt*rand.nextGaussian();
				}
				wn[i][(currentTind+1)%nt] = wn[i][currentTind] + dt*emtmp[1][i];
			}
			currentT += dt;
			currentTind = (currentTind+1)%nt;
		}
	}
	
	// this is the RK4 time integrator
	// it does n time steps per call
	public void rk4(int n){
		for(int step=0; step<n; step++){
			// perform a time step
			//prepare temp1
			for (int i=0; i<N; i++){
				rk4tmp1[0][i] = vn[i][currentTind];
				rk4tmp1[1][i] = wn[i][currentTind];
			}

			rk4tmp1 = diffEq(rk4tmp1, currentT);	//first
			for (int i=0; i<N; i++){
				rk4tmp2[0][i] = vn[i][currentTind] + rk4tmp1[0][i]*dt/2;
				rk4tmp2[1][i] = wn[i][currentTind] + rk4tmp1[1][i]*dt/2;
			}
			rk4tmp2 = diffEq(rk4tmp2, currentT+dt/2);	//second
			for (int i=0; i<N; i++){
				rk4tmp3[0][i] = vn[i][currentTind] + rk4tmp2[0][i]*dt/2;
				rk4tmp3[1][i] = wn[i][currentTind] + rk4tmp2[1][i]*dt/2;
			}
			rk4tmp3 = diffEq(rk4tmp3, currentT+dt/2);	//third
			for (int i=0; i<N; i++){
				rk4tmp4[0][i] = vn[i][currentTind] + rk4tmp3[0][i]*dt;
				rk4tmp4[1][i] = wn[i][currentTind] + rk4tmp3[1][i]*dt;
			}
			rk4tmp4 = diffEq(rk4tmp4, currentT+dt);	//fourth

			// store values for the step
			for (int i=0; i<N; i++){
				vn[i][(currentTind+1)%nt] = vn[i][currentTind] + (rk4tmp1[0][i] + 2*rk4tmp2[0][i] + 2*rk4tmp3[0][i] + rk4tmp4[0][i])*dt/6;
				wn[i][(currentTind+1)%nt] = wn[i][currentTind] + (rk4tmp1[1][i] + 2*rk4tmp2[1][i] + 2*rk4tmp3[1][i] + rk4tmp4[1][i])*dt/6;
			}
			currentT += dt;
			currentTind = (currentTind+1)%nt;
		}
	}
	
	// this is the Adams-PC4 time integrator
	// it does n time steps per call
	public void apc4(int n){
		for(int step=0; step<n; step++){
			/////////////// BE SURE TO COMPUTE THE FIRST 3 STEPS FIRST //////////////
			// perform a time step
			//prepare temp1
			for (int i=0; i<N; i++){
				apc4tmp1[0][i] = vn[i][currentTind];
				apc4tmp1[1][i] = wn[i][currentTind];
			}
			
			apc4tmp1 = diffEq(apc4tmp1, currentT);	//first
			for (int i=0; i<N; i++){
				apc4tmp5[0][i] = vn[i][currentTind] + (55*apc4tmp1[0][i] - 59*apc4tmp2[0][i] + 37*apc4tmp3[0][i] - 9*apc4tmp4[0][i])*dt/24;
				apc4tmp5[1][i] = wn[i][currentTind] + (55*apc4tmp1[1][i] - 59*apc4tmp2[1][i] + 37*apc4tmp3[1][i] - 9*apc4tmp4[1][i])*dt/24;
			}
			apc4tmp5 = diffEq(apc4tmp5, currentT+dt);	//second
			
			// store values for the step
			for (int i=0; i<N; i++){
				vn[i][(currentTind+1)%nt] = vn[i][currentTind] + (9*apc4tmp5[0][i] + 19*apc4tmp1[0][i] - 5*apc4tmp2[0][i] + apc4tmp3[0][i])*dt/24;
				wn[i][(currentTind+1)%nt] = wn[i][currentTind] + (9*apc4tmp5[1][i] + 19*apc4tmp1[1][i] - 5*apc4tmp2[1][i] + apc4tmp3[1][i])*dt/24;
			}
			apc4tmp4 = apc4tmp3;
			apc4tmp3 = apc4tmp2;
			apc4tmp2 = apc4tmp1;
			
			currentT += dt;
			currentTind = (currentTind+1)%nt;
		}
	}
	
	// to prepare for APC4
	public void prepareAPC4(){
		//prepare temp4
		for (int i=0; i<N; i++){
			apc4tmp4[0][i] = vn[i][currentTind];
			apc4tmp4[1][i] = wn[i][currentTind];
		}
		rk4(1);
		//prepare temp3
		for (int i=0; i<N; i++){
			apc4tmp3[0][i] = vn[i][currentTind];
			apc4tmp3[1][i] = wn[i][currentTind];
		}
		rk4(1);
		//prepare temp2
		for (int i=0; i<N; i++){
			apc4tmp2[0][i] = vn[i][currentTind];
			apc4tmp2[1][i] = wn[i][currentTind];
		}
	}

	//////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	/**
	 * This is the worker thread which runs the model calculations
	 * in the background and publishes the results
	 */
	private class FHNWorker extends SwingWorker<String, Integer> {
		
		public boolean cancelled;
		public boolean isWorking = false;
		int thisMethod;
		double steptime;

		@Override
        protected String doInBackground() {
			isWorking = true;
			cancelled = false;
			thisMethod = method;

			int spf = 50;
			double tmpT = currentT;
			
			if(thisMethod == APC4_METHOD){
				prepareAPC4();
			}
			while(currentT < tmpT + totalT - spf*dt){
				if(cancelled){
					isWorking = false;
					return "cancelled";
				}
				long starttime = System.currentTimeMillis();
				
				// do spf steps
				if(thisMethod == EM_METHOD){
					EM(spf);
				}else if(thisMethod == RK4_METHOD){
					rk4(spf);
				}else if(thisMethod == APC4_METHOD){
					apc4(spf);
				}

				// publish the maximum index of safe steps
				publish(Integer.valueOf(currentTind));
				
				steptime = System.currentTimeMillis() - starttime;
				if(steptime < 1){
					spf = Math.max(spf-1, 3);
					try{
						Thread.sleep(1);
					}catch(Exception ignore){}
				}else{
					spf = 10;
				}
			}

			isWorking = false;
            return "finished"; //just because they said to do it
        }

        @Override
        protected void process(java.util.List<Integer> index) {
			drawto = index.get(index.size()-1).intValue();
			if(drawto < drawn){
				drawn = 0;
				picPanel.clearImage();
			}
			picPanel.paintInterval();
			picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
        }

	}

////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	public class Picture extends JPanel{
		public int width, height;
		
		public Picture(int w, int h){
			super();
			width = w;
			height = h;
			setPreferredSize(new Dimension(width, height));
			setBackground(Color.WHITE);

			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D ig = image.createGraphics();
			ig.setColor(Color.WHITE);
			ig.fillRect(0, 0, width, height);
			
			drawn = 0;
			drawto = 0;
		}

		// ye olde painting method
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			//draw the image
			g.drawImage(image, 0, 0, null);

			//draw the time
			g.setColor(Color.BLACK);
			g.drawString(String.format("t = %.3f", currentT), 10, 10);

			//draw the zoom factor
			g.drawString("vert. scale = +-"+String.format("%.3f",zoomfactor), 10, 22);
			
		}
		
		//paints up to a certain time
		public void paintInterval(){
			Graphics2D ig = image.createGraphics();
			int xpix, ypix;
			//draw them all on one graph
			if(!splitGraphs){
				//draw v
				ig.setColor(Color.BLUE);
				for(int t=drawn; t<drawto; t++){
					for(int i=0; i<N; i++){
						xpix = (int)(t*1.0/nt*picPanel.width);
						ypix = (int)(picPanel.height/2 - vn[i][t]*picPanel.height/zoomfactor);
						ig.fillRect(xpix,ypix,2,2);
					}
				}
				//draw w
				ig.setColor(Color.RED);
				for(int t=drawn; t<drawto; t++){
					for(int i=0; i<N; i++){
						xpix = (int)(t*1.0/nt*picPanel.width);
						ypix = (int)(picPanel.height/2 - wn[i][t]*picPanel.height/zoomfactor);
						ig.fillRect(xpix,ypix,2,2);
					}
				}
			}else{// draw them all separate
				int partheight = height/N;
				//draw v
				ig.setColor(Color.BLUE);
				for(int t=drawn; t<drawto; t++){
					for(int i=0; i<N; i++){
						xpix = (int)(t*1.0/nt*picPanel.width);
						ypix = (int)(partheight*(i+1) - partheight/2 - vn[i][t]*partheight/zoomfactor);
						if(ypix >= partheight*i && ypix < partheight*(i+1)){
							ig.fillRect(xpix,ypix,2,2);
						}
					}
				}
				//draw w
				ig.setColor(Color.RED);
				for(int t=drawn; t<drawto; t++){
					for(int i=0; i<N; i++){
						xpix = (int)(t*1.0/nt*picPanel.width);
						ypix = (int)(partheight*(i+1) - partheight/2 - wn[i][t]*partheight/zoomfactor);
						if(ypix >= partheight*i && ypix < partheight*(i+1)){
							ig.fillRect(xpix,ypix,2,2);
						}
						ig.drawLine(0,partheight*(i+1),width,partheight*(i+1));
					}
				}
				ig.setColor(Color.BLACK);
				for(int i=0; i<N; i++){
					ig.drawLine(0,partheight*(i+1),width,partheight*(i+1));
				}
			}
			
			drawn = drawto;
		}

		// clears the image
		public void clearImage(){
			Graphics2D ig = image.createGraphics();
			ig.setColor(Color.WHITE);
			ig.fillRect(0,0,picPanel.width, picPanel.height);
		}

		public void setHeight(int newh){
			height = newh;
			setPreferredSize(new Dimension(width, height));

			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D ig = image.createGraphics();
			ig.setColor(Color.WHITE);
			ig.fillRect(0, 0, width, height);
		}
	}
	
	// this is a panel for inputting a matrix of parameters name = k, D
	public class InputPanel extends JPanel implements ActionListener{
		int rows, cols;
		JTextField[][] fields;
		JButton setButton;
		String name;
		JFrame frame;
		
		public InputPanel(String nam){
			super();
			name = nam;
			
			fillFields();
			
			setButton = new JButton("set "+name+" values");
			setButton.addActionListener(this);
			
			setLayout(new GridLayout(rows+1,cols));
			for(int i=0; i<rows; i++){
				for(int j=0; j<cols; j++){
					add(fields[i][j]);
				}
			}
			add(setButton);
			
			frame = new JFrame(name);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			Container pane = frame.getContentPane();
			pane.add(this);
			
			pane.validate();
			//Display the window.
			frame.pack();
			frame.setVisible(true);
		}
		
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == setButton){
				setParameters();
			}
		}
		
		public void fillFields(){
			if(name == "k"){
				rows = N;
				cols = N;
				fields = new JTextField[N][N];
				for(int i=0; i<N; i++){
					for(int j=0; j<N; j++){
						fields[i][j] = new JTextField(String.valueOf(k[i][j]), 8);
					}
				}
			}else if(name == "D"){
				rows = 1;
				cols = N;
				fields = new JTextField[1][N];
				for(int i=0; i<N; i++){
					fields[0][i] = new JTextField(String.valueOf(D[i]), 8);
				}
			}
		}
		
		public void setParameters(){
			try{
				if(name == "k"){
					for(int i=0; i<N; i++){
						for(int j=i; j<N; j++){
							k[i][j] = Double.valueOf(fields[i][j].getText());
							k[j][i] = k[i][j];
						}
					}
				}else if(name == "D"){
					for(int i=0; i<N; i++){
						D[i] = Double.valueOf(fields[0][i].getText());
					}
				}
			}catch(Exception e){
				JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
				return;
			}
			
			frame.setVisible(false);
			frame = null;
			fields = null;
		}
	}
}
