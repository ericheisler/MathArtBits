import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

/**
 * This is an N-body gravity simulation
 * 
 */
public class NBody extends JPanel implements ActionListener{

	// particle info
	int N;
	double[] posx, posy;
	double[] vx, vy, tmpvx, tmpvy;
	double[] M;
	double[][] dist;
	Vector<Pair> collided;
	double radius;

	// model info
	double G, A;
	double dt, maxT;
	double currentT;
	double angmom;
	boolean withSun;
	double sunMass;

	// integrator parts
	double[][] rk4tmp1, rk4tmp2, rk4tmp3, rk4tmp4;
	boolean somethingCollided;
	int[] removed;

	// visual info
	int width, height;
	double zoomfactor;
	boolean withTails;
	long steptime;
	int sqrtpercall;

	// GUI components
	JFrame frame;
	JPanel controlPanel, infoPanel;
	Picture picPanel;
	JTextField NField, TField, MField, angField;
	JLabel NLabel, TLabel, MLabel, angLabel;
	JButton startButton, stopButton, randButton, setButton, zoomOutButton, zoomInButton;
	JRadioButton sunButton, tailsButton;
	BufferedImage image;
	
	NBodyWorker worker;

    /**
     * The constructor
     */
    public NBody() {
		super();
		setBackground(Color.WHITE);
		
        N = 0;
		posx = new double[N];
		posy = new double[N];
		tmpvx = new double[N];
		tmpvy = new double[N];
		vx = new double[N];
		vy = new double[N];
		M = new double[N];
		radius = .01;
		dist = new double[N][N];
		collided = new Vector<Pair>();
		
		G = 0.01;
		A = 10;
		dt = 0.0001;
		maxT = 1.0;
		currentT = 0.0;
		angmom = 200;
		withSun = true;
		sunMass = 1000000;

		rk4tmp1 = new double[4][N];
		rk4tmp2 = new double[4][N];
		rk4tmp3 = new double[4][N];
		rk4tmp4 = new double[4][N];
		removed = new int[N];

		width = 700;
		height = 500;
		zoomfactor = 1;
		withTails = true;
		steptime = 0;
		sqrtpercall = 0;
		
		worker = new NBodyWorker();
		
		controlPanel = new JPanel();
		infoPanel = new JPanel();
		picPanel = new Picture(width-200, height);

		NField = new JTextField(String.valueOf(N), 8);
		TField = new JTextField(String.valueOf(maxT), 8);
		MField = new JTextField(String.valueOf(1.0), 8);
		angField = new JTextField(String.valueOf(A), 8);
		NLabel = new JLabel("N = ");
		TLabel = new JLabel("maxT = ");
		MLabel = new JLabel("M = ");
		angLabel = new JLabel("i.a.m. = ");

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		randButton = new JButton("Randomize");
		randButton.addActionListener(this);
		setButton = new JButton("Set values");
		setButton.addActionListener(this);
		zoomOutButton = new JButton("Zoom out");
		zoomOutButton.addActionListener(this);
		zoomInButton = new JButton("Zoom in");
		zoomInButton.addActionListener(this);
		sunButton = new JRadioButton("sun", true);
		sunButton.addActionListener(this);
		tailsButton = new JRadioButton("tails", true);
		tailsButton.addActionListener(this);

		controlPanel.setLayout(new GridLayout(8,2));
		controlPanel.add(NLabel);
		controlPanel.add(NField);
		controlPanel.add(TLabel);
		controlPanel.add(TField);
		controlPanel.add(MLabel);
		controlPanel.add(MField);
		controlPanel.add(angLabel);
		controlPanel.add(angField);
		controlPanel.add(sunButton);
		controlPanel.add(tailsButton);
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(randButton);
		controlPanel.add(setButton);
		controlPanel.add(zoomOutButton);
		controlPanel.add(zoomInButton);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(picPanel);
		add(controlPanel);
		
		frame = new JFrame("N-body gravitation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container pane = frame.getContentPane();
		pane.add(this);
		
		pane.validate();
		//Display the window.
        frame.pack();
        frame.setVisible(true);
		
    }
	
	public static void main(String[] args){
		NBody nb = new NBody();
	}

    public void actionPerformed(ActionEvent e){
		if(e.getSource() == startButton){
			if(!worker.isWorking){
				startSim();
			}
		}else if(e.getSource() == stopButton){
			worker.cancelled = true;
		}else if(e.getSource() == randButton){
			if(!worker.isWorking){
				randomize();
				paintCurrent();
				picPanel.repaint();
			}
		}else if(e.getSource() == setButton){
			if(!worker.isWorking){
				setParams();
				paintCurrent();
				picPanel.repaint();
			}
		}else if(e.getSource() == zoomOutButton){
			zoomfactor = zoomfactor*2;
			picPanel.clearImage();
			paintCurrent();
			picPanel.repaint();
		}else if(e.getSource() == zoomInButton){
			zoomfactor = zoomfactor/2;
			picPanel.clearImage();
			paintCurrent();
			picPanel.repaint();
		}else if(e.getSource() == sunButton){
			withSun = !withSun;
		}else if(e.getSource() == tailsButton){
			withTails = !withTails;
		}
	}
	
	// runs the simulation
	public void startSim(){
		computeDist();
		collisions();
		worker = new NBodyWorker();
		worker.execute();
	}
	
	// sets the values written in the fields
	public void setParams(){
		try{
			int tmpN = Integer.valueOf(NField.getText());
			if(tmpN <= 0){
				tmpN = 1;
				NField.setText("1");
			}
			if(tmpN != N){
				N = tmpN;
				posx = new double[N];
				posy = new double[N];
				tmpvx = new double[N];
				tmpvy = new double[N];
				vx = new double[N];
				vy = new double[N];
				M = new double[N];
				dist = new double[N][N];
				rk4tmp1 = new double[4][N];
				rk4tmp2 = new double[4][N];
				rk4tmp3 = new double[4][N];
				rk4tmp4 = new double[4][N];
				removed = new int[N];
			}
			
			maxT = Double.valueOf(TField.getText());
			
			double tmpM = Double.valueOf(MField.getText());
			for(int i=0; i<N; i++){
				M[i] = tmpM;
			}
			if(withSun){
				M[0] = sunMass;
			}
			
			angmom = Double.valueOf(angField.getText());
			
			
		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
	}
	
	// randomizes positions but retains velocity
	public void randomize(){
		Random rand = new Random();
		for(int i=0; i<N; i++){
			posx[i] = (rand.nextDouble()-0.5)*A;
			posy[i] = (rand.nextDouble()-0.5)*A;
			vx[i] = (rand.nextDouble()-0.5)*0.1*A + extraAM(posx[i], posy[i])[0];
			vy[i] = (rand.nextDouble()-0.5)*0.1*A + extraAM(posx[i], posy[i])[1];
		}
		if(withSun){
			posx[0] = 0;
			posy[0] = 0;
			vx[0] = 0;
			vy[0] = 0;
		}
	}
	
	//paints the current situation
	public void paintCurrent(){
		Graphics2D ig = image.createGraphics();
		
		picPanel.clearImage();
		
		ig.setColor(Color.WHITE);
		ig.fillRect(0,0,picPanel.width, picPanel.height);
		
		int rpix = (int)Math.max(1, (int)(radius*picPanel.width/A/zoomfactor));
		ig.setColor(Color.BLUE);
		for(int i=0; i<N; i++){
			ig.fillOval(picPanel.width/2+(int)(posx[i]*picPanel.width/A/zoomfactor)-rpix, picPanel.height/2+(int)(posy[i]*picPanel.height/A/zoomfactor)-rpix, 2*rpix+2, 2*rpix+2);
		}
	}
	
	// adds a bit of angular momentum wrt the center
	public double[] extraAM(double x, double y){
		double sqr = Math.sqrt(Math.sqrt(x*x + y*y)) + .1;
		double[] am = new double[2];
		am[0] = angmom*y/sqr;
		am[1] = -angmom*x/sqr;
		return am;
	}

	public void removeBody(int ind){

	}
	
	// handles collisions
	public void collisions(){
		if(N<2){
			return;
		}
		for(int i=0; i<N; i++){
			removed[i] = -1;
		}
		
		for(Pair p: collided){
			if(removed[p.i]<0 && removed[p.j]<0){
				// places one higher mass body in slot i
				//posx[p.i] = (posx[p.i]+posx[p.j])/2;
				//posy[p.i] = (posy[p.i]+posy[p.j])/2;
				vx[p.i] = (M[p.i]*vx[p.i]+M[p.j]*vx[p.j])/(M[p.i]+M[p.j]);
				vy[p.i] = (M[p.i]*vy[p.i]+M[p.j]*vy[p.j])/(M[p.i]+M[p.j]);
				M[p.i] = M[p.i]+M[p.j];
				removed[p.j] = p.i;
			}
		}
		for(int i=0; i<N-1; i++){
			if(removed[i]>=0){
				for(int j=i; j<N-1; j++){
					posx[j] = posx[j+1];
					posy[j] = posy[j+1];
					vx[j] = vx[j+1];
					vy[j] = vy[j+1];
					M[j] = M[j+1];
					removed[j] = removed[j+1];
				}
				i--;
				N--;
			}
		}
		NField.setText(String.valueOf(N));
		collided.clear();
		somethingCollided = false;
	}
	
	// computes the distance between each body
	public void computeDist(double[] px, double[] py){
		for(int i=0; i<N; i++){
			for(int j=i+1; j<N; j++){
				dist[i][j] = Math.sqrt((px[i] - px[j])*(px[i] - px[j]) + (py[i] - py[j])*(py[i] - py[j])) + 1e-10;
				dist[j][i] = dist[i][j];
			}
			dist[i][i] = 0.0;
		}
	}

	// computes the distance between two bodies, i and j
	public double computeDist(int i, int j){
		return Math.sqrt((posx[i] - posx[j])*(posx[i] - posx[j]) + (posy[i] - posy[j])*(posy[i] - posy[j]));
	}
	
	// computes the distance for the current configuration
	public void computeDist(){
		sqrtpercall = 0;
		double mindist = 1.0;
		for(int i=0; i<N; i++){
			for(int j=i+1; j<N; j++){
				sqrtpercall++;
				dist[i][j] = Math.sqrt((posx[i] - posx[j])*(posx[i] - posx[j]) + (posy[i] - posy[j])*(posy[i] - posy[j])) + 1e-10;
				dist[j][i] = dist[i][j];
				if(dist[i][j] <= 2*radius){
					somethingCollided = true;
					collided.add(new Pair(i,j));
				}else if(dist[i][j] < mindist){
					mindist = dist[i][j];
				}
			}
			dist[i][i] = 0.0;
		}
		//adjust the time step
		dt = Math.min(Math.max(0.0001*mindist, 1e-6), 0.001);
	}
	
	// this is the gravitation model
	// it takes the position and velocity data
	// it return the time derivative
	public double[][] diffEq(double[][] posvxy){
		// compute the distances
		computeDist(posvxy[0], posvxy[1]);
		
		// plug it in the diff eq
		for(int i=0; i<N; i++){
			tmpvx[i] = posvxy[2][i];
			tmpvy[i] = posvxy[3][i];
			posvxy[2][i] = 0.0;
			posvxy[3][i] = 0.0;
		}
		double tmpf;
		for(int i=0; i<N; i++){
			for(int j=i+1; j<N; j++){
				if(dist[i][j] >= 2*radius){
					tmpf = G/dist[i][j]/dist[i][j];
					posvxy[2][i] += tmpf*M[j]*(posvxy[0][j]-posvxy[0][i])/dist[i][j];
					posvxy[3][i] += tmpf*M[j]*(posvxy[1][j]-posvxy[1][i])/dist[i][j];
					posvxy[2][j] -= tmpf*M[i]*(posvxy[0][j]-posvxy[0][i])/dist[i][j];
					posvxy[3][j] -= tmpf*M[i]*(posvxy[1][j]-posvxy[1][i])/dist[i][j];
				}
			}
		}
		for(int i=0; i<N; i++){
			posvxy[0][i] = tmpvx[i];
			posvxy[1][i] = tmpvy[i];
		}
		
		return posvxy;
	}
	
	// this is the time integrator
	// it does n time steps per call
	public void rk4(int n){
		for(int step=0; step<n; step++){
			// perform a time step
			//prepare temp1
			for (int i=0; i<N; i++){
				rk4tmp1[0][i] = posx[i];
				rk4tmp1[1][i] = posy[i];
				rk4tmp1[2][i] = vx[i];
				rk4tmp1[3][i] = vy[i];
			}

			rk4tmp1 = diffEq(rk4tmp1);	//first
			for (int i=0; i<N; i++){
				rk4tmp2[0][i] = posx[i] + rk4tmp1[0][i]*dt/2;
				rk4tmp2[1][i] = posy[i] + rk4tmp1[1][i]*dt/2;
				rk4tmp2[2][i] = vx[i] + rk4tmp1[2][i]*dt/2;
				rk4tmp2[3][i] = vy[i] + rk4tmp1[3][i]*dt/2;
			}
			rk4tmp2 = diffEq(rk4tmp2);	//second
			for (int i=0; i<N; i++){
				rk4tmp3[0][i] = posx[i] + rk4tmp2[0][i]*dt/2;
				rk4tmp3[1][i] = posy[i] + rk4tmp2[1][i]*dt/2;
				rk4tmp3[2][i] = vx[i] + rk4tmp2[2][i]*dt/2;
				rk4tmp3[3][i] = vy[i] + rk4tmp2[3][i]*dt/2;
			}
			rk4tmp3 = diffEq(rk4tmp3);	//third
			for (int i=0; i<N; i++){
				rk4tmp4[0][i] = posx[i] + rk4tmp3[0][i]*dt;
				rk4tmp4[1][i] = posy[i] + rk4tmp3[1][i]*dt;
				rk4tmp4[2][i] = vx[i] + rk4tmp3[2][i]*dt;
				rk4tmp4[3][i] = vy[i] + rk4tmp3[3][i]*dt;
			}
			rk4tmp4 = diffEq(rk4tmp4);	//fourth
			
			// store values for the step
			for (int i=0; i<N; i++){
				posx[i] = posx[i] + (rk4tmp1[0][i] + 2*rk4tmp2[0][i] + 2*rk4tmp3[0][i] + rk4tmp4[0][i])*dt/6;
				posy[i] = posy[i] + (rk4tmp1[1][i] + 2*rk4tmp2[1][i] + 2*rk4tmp3[1][i] + rk4tmp4[1][i])*dt/6;
				vx[i] = vx[i] + (rk4tmp1[2][i] + 2*rk4tmp2[2][i] + 2*rk4tmp3[2][i] + rk4tmp4[2][i])*dt/6;
				vy[i] = vy[i] + (rk4tmp1[3][i] + 2*rk4tmp2[3][i] + 2*rk4tmp3[3][i] + rk4tmp4[3][i])*dt/6;
			}
			currentT += dt;
			
			//check for collisions
			computeDist();
			if(somethingCollided){
				collisions();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	/**
	 * This is the worker thread which runs the model calculations
	 * in the background and publishes the results
	 */
	private class NBodyWorker extends SwingWorker<String, Double[]> {

		BufferedImage tmpimage;
		Double[] Nxy, tmpxy;
		public boolean cancelled;
		public boolean isWorking = false;

		@Override
        protected String doInBackground() {
			isWorking = true;
			cancelled = false;
			tmpimage = new BufferedImage(picPanel.width, picPanel.height, BufferedImage.TYPE_INT_ARGB);
			Nxy = new Double[2*N+1];
			
			int spf = 10;
			double tmpT = currentT;
			
			while(currentT < maxT+tmpT){
				if(cancelled){
					isWorking = false;
					return "cancelled";
				}
				long starttime = System.currentTimeMillis();
				
				// do spf steps
				rk4(spf);
				
				// publish the result
				Nxy[0] = Double.valueOf(N);
				for(int j=0; j<N; j++){
					Nxy[j+1] = new Double(posx[j]);
					Nxy[j+1+N] = new Double(posy[j]);
				}
				publish(Nxy);
				
				steptime = System.currentTimeMillis() - starttime;
				if(steptime < 2){
					try{
						Thread.sleep(2);
					}catch(Exception ignore){}
				}
			}
			
			isWorking = false;
            return "finished"; //just because they said to do it
        }

        @Override
        protected void process(java.util.List<Double[]> tmpxys) {
			tmpxy = tmpxys.get(tmpxys.size()-1);
			
			Graphics2D ig = image.createGraphics();
			
			if(withTails){
				//fade the image
				picPanel.fade();
			}else{
				//clear the image
				ig.setColor(Color.WHITE);
				ig.fillRect(0,0,picPanel.width, picPanel.height);
			}
			
			int rpix = (int)Math.max(1, (int)(radius*picPanel.width/A/zoomfactor));
			int thisN = tmpxy[0].intValue();
			ig.setColor(Color.BLUE);
			for(int i=0; i<thisN; i++){
				ig.fillOval(picPanel.width/2+(int)(tmpxy[i+1]*picPanel.width/A/zoomfactor)-rpix, picPanel.height/2+(int)(tmpxy[i+1+thisN]*picPanel.height/A/zoomfactor)-rpix, 2*rpix+1, 2*rpix+1);
			}
			
			picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
			
			//spaceTime.getRaster().setRect(((BufferedImage) billy[3]).getRaster());
        }
		
	}

////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	public class Picture extends JPanel{
		public int width, height;
		
		ColorFilter filter;
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
			
			filter = new ColorFilter();
		}

		// ye olde painting method
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			//if(withTails){
//				Graphics2D ig = image.createGraphics();
//
//				// draw the bodies
//				int rpix = (int)Math.max(1, (int)(radius*width/A/zoomfactor));
//				ig.setColor(Color.BLUE);
//				for(int i=0; i<N; i++){
//					ig.fillOval(width/2+(int)(posx[i]*width/A/zoomfactor)-rpix, height/2+(int)(posy[i]*height/A/zoomfactor)-rpix, 2*rpix, 2*rpix);
//				}
//			}
			
			//draw the image
			g.drawImage(image, 0, 0, null);

			//draw the time
			g.setColor(Color.BLACK);
			g.drawString(String.format("t = %.4f", currentT), 10, 10);

			//draw the length scale
			g.drawString("zoom factor = "+String.format("%.3f",zoomfactor), 10, 22);
			
			//draw some numbers
			g.drawString("step time(ms) = "+String.valueOf(steptime), 200, 10);
			g.drawString("sqrt per step = "+String.valueOf(sqrtpercall), 200, 22);
		}
		
		// just fade away
		public void fade(){
			if(image == null){
				return;
			}
			image = filter.fadeALittle(image);
		}
		
		// clears the image
		public void clearImage(){
			
			image = filter.fadeALot(image);
		}

	}

	public class Pair{
		public int i, j;
		public Pair(int k, int l){
			i = k;
			j = l;
		}
	}
	
	class ColorFilter {
		float[][] colorMatrix = { { 1f, 0f, 0f, 0f}, { 0f, 1f, 0f, 0f }, { 0f, 0f, 1f, 0f}, {0f, 0f, 0f, 0.8f} };
		float[][] colorMatrix2 = { { 1f, 0f, 0f, 0f}, { 0f, 1f, 0f, 0f }, { 0f, 0f, 1f, 0f}, {0f, 0f, 0f, 0f} };
		BandCombineOp changeColors, changeColors2;
		
		public ColorFilter(){
			changeColors = new BandCombineOp(colorMatrix, null);
			changeColors2 = new BandCombineOp(colorMatrix2, null);
		}
		
		public BufferedImage fadeALittle(BufferedImage image) {
			WritableRaster sourceRaster = image.getRaster();
			changeColors.filter(sourceRaster, sourceRaster);
			return image;
			
		}
		public BufferedImage fadeALot(BufferedImage image) {
			WritableRaster sourceRaster = image.getRaster();
			changeColors2.filter(sourceRaster, sourceRaster);
			return image;
			
		}
	}
}
