import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

/**
 * This is an N-body gravity simulation
 *
 */
public class NBodyCells extends JPanel implements ActionListener{
	
	// particle info
	int N;
	Body[] bodies;
	Cell[] cells;
	double[][] dist;
	Vector<Pair> collided;

	// model info
	double G, A;
	double dt, maxT;
	double currentT;
	double angmom;
	boolean withSun;
	double sunMass;

	// integrator parts
	static int RK4_METHOD = 0;
	static int APC4_METHOD = 1;
	int method;
	double[][] rk4tmp1, rk4tmp2, rk4tmp3, rk4tmp4;
	double[][] apc4tmp1, apc4tmp2, apc4tmp3, apc4tmp4, apc4tmp5;
	
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
	JTextField NField, TField, MField, angField, GField, AField;
	JLabel NLabel, TLabel, MLabel, angLabel, GLabel, ALabel;
	JButton startButton, stopButton, randButton, setButton, zoomOutButton;
	JButton zoomInButton, resetButton, solSystButton;
	JRadioButton sunButton, tailsButton;
	BufferedImage image;

	NBodyWorker worker;

    /**
     * The constructor
     */
    public NBodyCells() {
		super();
		setBackground(Color.WHITE);

        N = 0;
		bodies = new Body[0];
		cells = new Cell[25];
		for(int i=0; i<25; i++){
			cells[i] = new Cell(i);
		}
		dist = new double[N][N];
		collided = new Vector<Pair>();

		G = 0.00024;
		A = 10;
		dt = 0.0001;
		maxT = 1.0;
		currentT = 0.0;
		angmom = 200;
		withSun = true;
		sunMass = 330000;
		
		method = 1;
		rk4tmp1 = new double[4][N];
		rk4tmp2 = new double[4][N];
		rk4tmp3 = new double[4][N];
		rk4tmp4 = new double[4][N];
		apc4tmp1 = new double[4][N];
		apc4tmp2 = new double[4][N];
		apc4tmp3 = new double[4][N];
		apc4tmp4 = new double[4][N];
		apc4tmp5 = new double[4][N];
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
		GField = new JTextField(String.valueOf(G), 8);
		AField = new JTextField(String.valueOf(A), 8);
		NLabel = new JLabel("N = ");
		TLabel = new JLabel("maxT = ");
		MLabel = new JLabel("M = ");
		angLabel = new JLabel("i.a.m. = ");
		GLabel = new JLabel("G = ");
		ALabel = new JLabel("scale(AU) = ");

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
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		solSystButton = new JButton("sol system");
		solSystButton.addActionListener(this);

		controlPanel.setLayout(new GridLayout(11,2));
		controlPanel.add(NLabel);
		controlPanel.add(NField);
		controlPanel.add(TLabel);
		controlPanel.add(TField);
		controlPanel.add(MLabel);
		controlPanel.add(MField);
		controlPanel.add(GLabel);
		controlPanel.add(GField);
		controlPanel.add(ALabel);
		controlPanel.add(AField);
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
		controlPanel.add(resetButton);
		controlPanel.add(solSystButton);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(picPanel);
		add(controlPanel);

		frame = new JFrame("N-body gravitation, cells");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container pane = frame.getContentPane();
		pane.add(this);

		pane.validate();
		//Display the window.
        frame.pack();
        frame.setVisible(true);

    }

	public static void main(String[] args){
		NBodyCells nb = new NBodyCells();
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
			N = 0;
			NField.setText(String.valueOf(N));
			for(int i=0; i<25; i++){
				cells[i].n = 0;
			}
			picPanel.clearImage();
			repaint();
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
		}else if(e.getSource() == solSystButton){
			if(!worker.isWorking){
				int extras = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input the number of extra bodies"));
				setSolSystem(extras);
				paintCurrent();
				picPanel.repaint();
			}
		}
	}

	// runs the simulation
	public void startSim(){
		fillCells();
		computeDist();
		collisions();
		worker = new NBodyWorker();
		worker.execute();
	}

	// sets the values written in the fields
	public void setParams(){
		try{
			int tmpN = Integer.valueOf(NField.getText());
			double tmpM = Double.valueOf(MField.getText());
			if(tmpN <= 0){
				tmpN = 1;
				NField.setText("1");
			}
			if(tmpN != N){
				N = tmpN;
				bodies = new Body[N];
				for(int i=0; i<N; i++){
					bodies[i] = new Body();
					bodies[i].index = i;
					bodies[i].r = 0.00005;
					bodies[i].M = tmpM;
				}
				if(withSun){
					bodies[0].M = sunMass;
					bodies[0].r = 0.1;
				}
				dist = new double[N][N];
				rk4tmp1 = new double[4][N];
				rk4tmp2 = new double[4][N];
				rk4tmp3 = new double[4][N];
				rk4tmp4 = new double[4][N];
				apc4tmp1 = new double[4][N];
				apc4tmp2 = new double[4][N];
				apc4tmp3 = new double[4][N];
				apc4tmp4 = new double[4][N];
				apc4tmp5 = new double[4][N];
				removed = new int[N];
			}

			G = Double.valueOf(GField.getText());
			A = Double.valueOf(AField.getText());
			maxT = Double.valueOf(TField.getText());
			angmom = Double.valueOf(angField.getText());

		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
	}

	public void setSolSystem(int extras){
		N = 9+extras;
		bodies = new Body[N];
		for(int i=0; i<N; i++){
			bodies[i] = new Body();
			bodies[i].index = i;
			bodies[i].M = .002;
			bodies[i].r = 0.01;
		}
		bodies[0].M = sunMass;
		bodies[0].r = 0.1;
		bodies[0].x = 0;
		bodies[0].y = 0;
		bodies[0].vx = 0;
		bodies[0].vy = 0;

		bodies[1].M = 0.03;
		bodies[1].r = 0.05;
		bodies[1].x = 0.4;
		bodies[1].y = 0;
		setOrbit(1);

		bodies[2].M = 1.0;
		bodies[2].r = 0.05;
		bodies[2].x = 0;
		bodies[2].y = 0.7;
		setOrbit(2);

		bodies[3].M = 1.0;
		bodies[3].r = 0.05;
		bodies[3].x = -1.0;
		bodies[3].y = 0;
		setOrbit(3);

		bodies[4].M = 0.1;
		bodies[4].r = 0.05;
		bodies[4].x = 0;
		bodies[4].y = -1.5;
		setOrbit(4);

		bodies[5].M = 318;
		bodies[5].r = 0.05;
		bodies[5].x = 5.2;
		bodies[5].y = 0;
		setOrbit(5);

		bodies[6].M = 95;
		bodies[6].r = 0.05;
		bodies[6].x = 0;
		bodies[6].y = 9.5;
		setOrbit(6);

		bodies[7].M = 14;
		bodies[7].r = 0.05;
		bodies[7].x = -19.6;
		bodies[7].y = 0;
		setOrbit(7);

		bodies[8].M = 17;
		bodies[8].r = 0.05;
		bodies[8].x = 0;
		bodies[8].y = -30;
		setOrbit(8);

		Random rand = new Random();
		for(int i=9; i<N; i++){
			bodies[i].x = (rand.nextDouble()-0.5)*A;
			bodies[i].y = (rand.nextDouble()-0.5)*A;
			bodies[i].vx = (rand.nextDouble()-0.5)*1 + extraAM(bodies[i].x, bodies[i].y)[0];
			bodies[i].vy = (rand.nextDouble()-0.5)*1 + extraAM(bodies[i].x, bodies[i].y)[1];
		}

		dist = new double[N][N];
		rk4tmp1 = new double[4][N];
		rk4tmp2 = new double[4][N];
		rk4tmp3 = new double[4][N];
		rk4tmp4 = new double[4][N];
		apc4tmp1 = new double[4][N];
		apc4tmp2 = new double[4][N];
		apc4tmp3 = new double[4][N];
		apc4tmp4 = new double[4][N];
		apc4tmp5 = new double[4][N];
		removed = new int[N];
	}

	public void setOrbit(int ind){
		if(!withSun){
			return;
		}
		double ro = Math.sqrt(bodies[ind].x*bodies[ind].x+bodies[ind].y*bodies[ind].y);
		double vo = Math.sqrt(G*sunMass/ro);
		bodies[ind].vx = -vo*bodies[ind].y/ro;
		bodies[ind].vy = vo*bodies[ind].x/ro;
	}

	// randomizes positions but retains velocity
	public void randomize(){
		Random rand = new Random();
		for(int i=0; i<N; i++){
			bodies[i].x = (rand.nextDouble()-0.5)*A;
			bodies[i].y = (rand.nextDouble()-0.5)*A;
			bodies[i].vx = (rand.nextDouble()-0.5)*0.01*A + extraAM(bodies[i].x, bodies[i].y)[0];
			bodies[i].vy = (rand.nextDouble()-0.5)*0.01*A + extraAM(bodies[i].x, bodies[i].y)[1];
		}
		if(withSun){
			bodies[0].x = 0;
			bodies[0].y = 0;
			bodies[0].vx = 0;
			bodies[0].vy = 0;
		}
	}

	// adds a bit of angular momentum wrt the center
	public double[] extraAM(double x, double y){
		//double sqr = Math.sqrt(Math.sqrt(x*x + y*y)) + .1;
		double sqr = Math.sqrt(x*x + y*y) + .1;
		double[] am = new double[2];
		am[0] = -Math.sqrt(G*sunMass/sqr)*y/sqr*angmom;
		am[1] = Math.sqrt(G*sunMass/sqr)*x/sqr*angmom;
		return am;
	}
	
	public double computeRadius(double mass){
		return Math.min(0.2, 0.01*Math.cbrt(mass));
	}

	public void fillCells(){
		for(int i=0; i<25; i++){
			cells[i].resetCell(N);
		}
		for(int i=0; i<N; i++){
			if(bodies[i].x < -A*3.0/10){
				if(bodies[i].y < -A*3.0/10){
					cells[0].addBody(bodies[i]);
				}else if(bodies[i].y < -A/10){
					cells[5].addBody(bodies[i]);
				}else if(bodies[i].y < A/10){
					cells[10].addBody(bodies[i]);
				}else if(bodies[i].y < A*3.0/10){
					cells[15].addBody(bodies[i]);
				}else{
					cells[20].addBody(bodies[i]);
				}
			}else if(bodies[i].x < -A/10){
				if(bodies[i].y < -A*3.0/10){
					cells[1].addBody(bodies[i]);
				}else if(bodies[i].y < -A/10){
					cells[6].addBody(bodies[i]);
				}else if(bodies[i].y < A/10){
					cells[11].addBody(bodies[i]);
				}else if(bodies[i].y < A*3.0/10){
					cells[16].addBody(bodies[i]);
				}else{
					cells[21].addBody(bodies[i]);
				}
			}else if(bodies[i].x < A/10){
				if(bodies[i].y < -A*3.0/10){
					cells[2].addBody(bodies[i]);
				}else if(bodies[i].y < -A/10){
					cells[7].addBody(bodies[i]);
				}else if(bodies[i].y < A/10){
					cells[12].addBody(bodies[i]);
				}else if(bodies[i].y < A*3.0/10){
					cells[17].addBody(bodies[i]);
				}else{
					cells[22].addBody(bodies[i]);
				}
			}else if(bodies[i].x < A*3.0/10){
				if(bodies[i].y < -A*3.0/10){
					cells[3].addBody(bodies[i]);
				}else if(bodies[i].y < -A/10){
					cells[8].addBody(bodies[i]);
				}else if(bodies[i].y < A/10){
					cells[13].addBody(bodies[i]);
				}else if(bodies[i].y < A*3.0/10){
					cells[18].addBody(bodies[i]);
				}else{
					cells[23].addBody(bodies[i]);
				}
			}else{
				if(bodies[i].y < -A*3.0/10){
					cells[4].addBody(bodies[i]);
				}else if(bodies[i].y < -A/10){
					cells[9].addBody(bodies[i]);
				}else if(bodies[i].y < A/10){
					cells[14].addBody(bodies[i]);
				}else if(bodies[i].y < A*3.0/10){
					cells[19].addBody(bodies[i]);
				}else{
					cells[24].addBody(bodies[i]);
				}
			}
		}
	}
	
	public void updateCells(){
		int newcell;
		for(int i=0; i<N; i++){
			if(bodies[i].x < -A*3.0/10){
				if(bodies[i].y < -A*3.0/10){
					newcell = 0;
				}else if(bodies[i].y < -A/10){
					newcell = 5;
				}else if(bodies[i].y < A/10){
					newcell = 10;
				}else if(bodies[i].y < A*3.0/10){
					newcell = 15;
				}else{
					newcell = 20;
				}
			}else if(bodies[i].x < -A/10){
				if(bodies[i].y < -A*3.0/10){
					newcell = 1;
				}else if(bodies[i].y < -A/10){
					newcell = 6;
				}else if(bodies[i].y < A/10){
					newcell = 11;
				}else if(bodies[i].y < A*3.0/10){
					newcell = 16;
				}else{
					newcell = 21;
				}
			}else if(bodies[i].x < A/10){
				if(bodies[i].y < -A*3.0/10){
					newcell = 2;
				}else if(bodies[i].y < -A/10){
					newcell = 7;
				}else if(bodies[i].y < A/10){
					newcell = 12;
				}else if(bodies[i].y < A*3.0/10){
					newcell = 17;
				}else{
					newcell = 22;
				}
			}else if(bodies[i].x < A*3.0/10){
				if(bodies[i].y < -A*3.0/10){
					newcell = 3;
				}else if(bodies[i].y < -A/10){
					newcell = 8;
				}else if(bodies[i].y < A/10){
					newcell = 13;
				}else if(bodies[i].y < A*3.0/10){
					newcell = 18;
				}else{
					newcell = 23;
				}
			}else{
				if(bodies[i].y < -A*3.0/10){
					newcell = 4;
				}else if(bodies[i].y < -A/10){
					newcell = 9;
				}else if(bodies[i].y < A/10){
					newcell = 14;
				}else if(bodies[i].y < A*3.0/10){
					newcell = 19;
				}else{
					newcell = 24;
				}
			}
			if(newcell != bodies[i].cell){
				cells[bodies[i].cell].removeBody(i);
				cells[newcell].addBody(bodies[i]);
			}
		}
	}

	//paints the current situation
	public void paintCurrent(){
		Graphics2D ig = image.createGraphics();

		picPanel.clearImage();

		ig.setColor(Color.WHITE);
		ig.fillRect(0,0,picPanel.width, picPanel.height);

		int rpix;
		ig.setColor(Color.BLUE);
		for(int i=0; i<N; i++){
			rpix = (int)Math.max(1, (int)(bodies[i].r*picPanel.width/A/zoomfactor));
			ig.fillOval(picPanel.width/2+(int)(bodies[i].x*picPanel.width/A/zoomfactor)-rpix, picPanel.height/2-(int)(bodies[i].y*picPanel.height/A/zoomfactor)-rpix, 2*rpix, 2*rpix);
		}
	}

	public void removeBody(int ind){
		for(int j=ind; j<N-1; j++){
			bodies[j] = bodies[j+1];
			bodies[j].index = j;
			removed[j] = removed[j+1];
		}
		N--;
	}

	public void escapedBodies(){
		// remove far out bodies from the system, man
		for(int i=0; i<N; i++){
			if(bodies[i].x > 5*A || bodies[i].x < -5*A || bodies[i].y > 5*A || bodies[i].y < -5*A){
				for(int j=i; j<N-1; j++){
					bodies[j] = bodies[j+1];
					bodies[j].index = j;
					removed[j] = removed[j+1];
				}
				i--;
				N--;
			}
		}
		NField.setText(String.valueOf(N));
	}

	// handles collisions
	public void collisions(){
		if(N<2){
			return;
		}
		for(int i=0; i<N; i++){
			removed[i] = -1;
		}
		double twomass;
		for(Pair p: collided){
			if(removed[p.i]<0 && removed[p.j]<0){
				//places one higher mass body in slot i
				twomass = bodies[p.i].M+bodies[p.j].M;
				bodies[p.i].x = (bodies[p.i].x*bodies[p.i].M + bodies[p.j].x*bodies[p.j].M) / twomass;
				bodies[p.i].y = (bodies[p.i].y*bodies[p.i].M + bodies[p.j].y*bodies[p.j].M) / twomass;
				bodies[p.i].vx = (bodies[p.i].vx*bodies[p.i].M + bodies[p.j].vx*bodies[p.j].M) / twomass;
				bodies[p.i].vy = (bodies[p.i].vy*bodies[p.i].M + bodies[p.j].vy*bodies[p.j].M) / twomass;
				bodies[p.i].M = twomass;
				bodies[p.i].r = Math.max(bodies[p.i].r, bodies[p.j].r);
				//bodies[p.i].r = computeRadius(bodies[p.i].M);
				removed[p.j] = p.i;
			}
			else if(removed[p.j]<0){
				int pi = removed[p.i];
				//places one higher mass body in the slot that removed i
				twomass = bodies[pi].M+bodies[p.j].M;
				bodies[pi].x = (bodies[pi].x*bodies[pi].M + bodies[p.j].x*bodies[p.j].M) / twomass;
				bodies[pi].y = (bodies[pi].y*bodies[pi].M + bodies[p.j].y*bodies[p.j].M) / twomass;
				bodies[pi].vx = (bodies[pi].vx*bodies[pi].M + bodies[p.j].vx*bodies[p.j].M) / twomass;
				bodies[pi].vy = (bodies[pi].vy*bodies[pi].M + bodies[p.j].vy*bodies[p.j].M) / twomass;
				bodies[pi].M = twomass;
				bodies[pi].r = Math.max(bodies[pi].r, bodies[p.j].r);
				//bodies[pi].r = computeRadius(bodies[pi].M);
				removed[p.j] = pi;
			}
			else if(removed[p.i]<0){
				int pj = removed[p.j];
				//places one higher mass body in the slot that removed j
				twomass = bodies[p.i].M+bodies[pj].M;
				bodies[pj].x = (bodies[p.i].x*bodies[p.i].M + bodies[pj].x*bodies[pj].M) / twomass;
				bodies[pj].y = (bodies[p.i].y*bodies[p.i].M + bodies[pj].y*bodies[pj].M) / twomass;
				bodies[pj].vx = (bodies[p.i].vx*bodies[p.i].M + bodies[pj].vx*bodies[pj].M) / twomass;
				bodies[pj].vy = (bodies[p.i].vy*bodies[p.i].M + bodies[pj].vy*bodies[pj].M) / twomass;
				bodies[pj].M = twomass;
				bodies[pj].r = Math.max(bodies[p.i].r, bodies[pj].r);
				//bodies[pj].r = computeRadius(bodies[pj].M);
				removed[p.i] = pj;
			}
		}
		// remove from the system
		for(int i=0; i<N; i++){
			if(removed[i]>=0){
				for(int j=i; j<N-1; j++){
					bodies[j] = bodies[j+1];
					bodies[j].index = j;
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
		int bi, bj;
		for(int i=0; i<25; i++){ //for each cell
			for(int j=0; j<cells[i].n; j++){ //for each body in that cell
				bi = cells[i].cellBodies[j];
				for(int k=0; k<cells[i].neighbors.length; k++){ //for each neighboring cell
					for(int l=0; l<cells[cells[i].neighbors[k]].n; l++){ //for each body in that neighboring cell
						bj = cells[cells[i].neighbors[k]].cellBodies[l];
						if(bi < bj){
							dist[bi][bj] = Math.sqrt((px[bi] - px[bj])*(px[bi] - px[bj]) + (py[bi] - py[bj])*(py[bi] - py[bj])) + 1e-300;
							dist[bj][bi] = dist[bi][bj];
						}
					}
				}
			}
		}
	}

	// computes the distance between two bodies, i and j
	public double computeDist(int i, int j){
		return Math.sqrt((bodies[i].x - bodies[j].x)*(bodies[i].x - bodies[j].x) + (bodies[i].y - bodies[j].y)*(bodies[i].y - bodies[j].y))+ 1e-300;
	}

	// computes the distance for the current configuration
	public void computeDist(){
		sqrtpercall = 0;
		double mindist = 1.0;
		int bi, bj;
		for(int i=0; i<25; i++){ //for each cell
			for(int j=0; j<cells[i].n; j++){ //for each body in that cell
				bi = cells[i].cellBodies[j];
				for(int k=0; k<cells[i].neighbors.length; k++){ //for each neighboring cell
					for(int l=0; l<cells[cells[i].neighbors[k]].n; l++){ //for each body in that neighboring cell
						bj = cells[cells[i].neighbors[k]].cellBodies[l];
						if(bi < bj){
							sqrtpercall++;
							dist[bi][bj] = computeDist(bi, bj);
							dist[bj][bi] = dist[bi][bj];
							if(dist[bi][bj] <= bodies[bi].r+bodies[bj].r){
								somethingCollided = true;
								collided.add(new Pair(bi,bj));
							}else if(dist[bi][bj] < mindist){
								mindist = dist[bi][bj];
							}
						}
					}
				}
			}
		}
		//adjust the time step
		dt = Math.min(Math.max(0.01*mindist, 1e-6), 0.001);
	}

	// this is the gravitation model
	// it takes the position and velocity data
	// it return the time derivative
	public double[][] diffEq(double[][] posvxy){
		// compute the distances
		computeDist(posvxy[0], posvxy[1]);

		// plug it in the diff eq
		for(int i=0; i<N; i++){
			bodies[i].tvx = posvxy[2][i];
			bodies[i].tvy = posvxy[3][i];
			posvxy[2][i] = 0.0;
			posvxy[3][i] = 0.0;
		}
		
		double tmpf, nandist;
		int bi, bj;
		boolean neighbor = false;
		for(int i=0; i<25; i++){ //for each cell
			for(int j=0; j<cells[i].n; j++){ //for each body in that cell
				bi = cells[i].cellBodies[j];
				// compute the forces from bodies in neighboring cells
				for(int k=0; k<cells[i].neighbors.length; k++){ //for each neighboring cell
					for(int l=0; l<cells[cells[i].neighbors[k]].n; l++){ //for each body in that neighboring cell
						bj = cells[cells[i].neighbors[k]].cellBodies[l];
						if(bi < bj){
							if(dist[bi][bj] > bodies[bi].r+bodies[bj].r){
								tmpf = G/dist[bi][bj]/dist[bi][bj]/dist[bi][bj];
								posvxy[2][bi] += tmpf*bodies[bj].M*(posvxy[0][bj]-posvxy[0][bi]);
								posvxy[3][bi] += tmpf*bodies[bj].M*(posvxy[1][bj]-posvxy[1][bi]);
								posvxy[2][bj] -= tmpf*bodies[bi].M*(posvxy[0][bj]-posvxy[0][bi]);
								posvxy[3][bj] -= tmpf*bodies[bi].M*(posvxy[1][bj]-posvxy[1][bi]);
							}
						}
					}
				}
				// compute the forces from non-neighboring cells
				for(int k=0; k<25; k++){
					neighbor = false;
					for(int l=0; l<cells[i].neighbors.length; l++){
						if(k == cells[i].neighbors[l]){ 
							neighbor = true; 
							break;
						}
					}
					if(!neighbor && cells[k].n>0){
						nandist = Math.sqrt((cells[k].cm[0] - bodies[bi].x)*(cells[k].cm[0] - bodies[bi].x) + (cells[k].cm[1] - bodies[bi].y)*(cells[k].cm[1] - bodies[bi].y))+ 1e-10;
						tmpf = G/nandist/nandist/nandist;
						posvxy[2][bi] += tmpf*cells[k].m*(cells[k].cm[0]-posvxy[0][bi]);
						posvxy[3][bi] += tmpf*cells[k].m*(cells[k].cm[1]-posvxy[1][bi]);
					}
				}
			}
		}
		
		for(int i=0; i<N; i++){
			posvxy[0][i] = bodies[i].tvx;
			posvxy[1][i] = bodies[i].tvy;
		}

		return posvxy;
	}

	// this is the RK4 time integrator
	// it does n time steps per call
	public void rk4(int n){
		for(int step=0; step<n; step++){
			// perform a time step
			//prepare temp1
			for (int i=0; i<N; i++){
				rk4tmp1[0][i] = bodies[i].x;
				rk4tmp1[1][i] = bodies[i].y;
				rk4tmp1[2][i] = bodies[i].vx;
				rk4tmp1[3][i] = bodies[i].vy;
			}
			// compute centers of mass
			for(int i=0; i<25; i++){
				cells[i].computeCM();
			}

			rk4tmp1 = diffEq(rk4tmp1);	//first
			for (int i=0; i<N; i++){
				rk4tmp2[0][i] = bodies[i].x + rk4tmp1[0][i]*dt/2;
				rk4tmp2[1][i] = bodies[i].y + rk4tmp1[1][i]*dt/2;
				rk4tmp2[2][i] = bodies[i].vx + rk4tmp1[2][i]*dt/2;
				rk4tmp2[3][i] = bodies[i].vy + rk4tmp1[3][i]*dt/2;
			}
			rk4tmp2 = diffEq(rk4tmp2);	//second
			for (int i=0; i<N; i++){
				rk4tmp3[0][i] = bodies[i].x + rk4tmp2[0][i]*dt/2;
				rk4tmp3[1][i] = bodies[i].y + rk4tmp2[1][i]*dt/2;
				rk4tmp3[2][i] = bodies[i].vx + rk4tmp2[2][i]*dt/2;
				rk4tmp3[3][i] = bodies[i].vy + rk4tmp2[3][i]*dt/2;
			}
			rk4tmp3 = diffEq(rk4tmp3);	//third
			for (int i=0; i<N; i++){
				rk4tmp4[0][i] = bodies[i].x + rk4tmp3[0][i]*dt;
				rk4tmp4[1][i] = bodies[i].y + rk4tmp3[1][i]*dt;
				rk4tmp4[2][i] = bodies[i].vx + rk4tmp3[2][i]*dt;
				rk4tmp4[3][i] = bodies[i].vy + rk4tmp3[3][i]*dt;
			}
			rk4tmp4 = diffEq(rk4tmp4);	//fourth

			// store values for the step
			for (int i=0; i<N; i++){
				bodies[i].x = bodies[i].x + (rk4tmp1[0][i] + 2*rk4tmp2[0][i] + 2*rk4tmp3[0][i] + rk4tmp4[0][i])*dt/6;
				bodies[i].y = bodies[i].y + (rk4tmp1[1][i] + 2*rk4tmp2[1][i] + 2*rk4tmp3[1][i] + rk4tmp4[1][i])*dt/6;
				bodies[i].vx = bodies[i].vx + (rk4tmp1[2][i] + 2*rk4tmp2[2][i] + 2*rk4tmp3[2][i] + rk4tmp4[2][i])*dt/6;
				bodies[i].vy = bodies[i].vy + (rk4tmp1[3][i] + 2*rk4tmp2[3][i] + 2*rk4tmp3[3][i] + rk4tmp4[3][i])*dt/6;
			}
			currentT += dt;
			//check for collisions
			updateCells();
			computeDist();
			sqrtpercall = sqrtpercall*5;
			if(somethingCollided){
				collisions();
				escapedBodies();
				fillCells();
			}
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
				apc4tmp1[0][i] = bodies[i].x;
				apc4tmp1[1][i] = bodies[i].y;
				apc4tmp1[2][i] = bodies[i].vx;
				apc4tmp1[3][i] = bodies[i].vy;
			}
			// compute centers of mass
			for(int i=0; i<25; i++){
				cells[i].computeCM();
			}
			
			apc4tmp1 = diffEq(apc4tmp1);	//first
			for (int i=0; i<N; i++){
				apc4tmp5[0][i] = bodies[i].x + (55*apc4tmp1[0][i] - 59*apc4tmp2[0][i] + 37*apc4tmp3[0][i] - 9*apc4tmp4[0][i])*dt/24;
				apc4tmp5[1][i] = bodies[i].y + (55*apc4tmp1[1][i] - 59*apc4tmp2[1][i] + 37*apc4tmp3[1][i] - 9*apc4tmp4[1][i])*dt/24;
				apc4tmp5[2][i] = bodies[i].vx + (55*apc4tmp1[2][i] - 59*apc4tmp2[2][i] + 37*apc4tmp3[2][i] - 9*apc4tmp4[2][i])*dt/24;
				apc4tmp5[3][i] = bodies[i].vy + (55*apc4tmp1[3][i] - 59*apc4tmp2[3][i] + 37*apc4tmp3[3][i] - 9*apc4tmp4[3][i])*dt/24;
			}
			apc4tmp5 = diffEq(apc4tmp5);	//second
			
			// store values for the step
			for (int i=0; i<N; i++){
				bodies[i].x = bodies[i].x + (9*apc4tmp5[0][i] + 19*apc4tmp1[0][i] - 5*apc4tmp2[0][i] + apc4tmp3[0][i])*dt/24;
				bodies[i].y = bodies[i].y + (9*apc4tmp5[1][i] + 19*apc4tmp1[1][i] - 5*apc4tmp2[1][i] + apc4tmp3[1][i])*dt/24;
				bodies[i].vx = bodies[i].vx + (9*apc4tmp5[2][i] + 19*apc4tmp1[2][i] - 5*apc4tmp2[2][i] + apc4tmp3[2][i])*dt/24;
				bodies[i].vy = bodies[i].vy + (9*apc4tmp5[2][i] + 19*apc4tmp1[3][i] - 5*apc4tmp2[3][i] + apc4tmp3[3][i])*dt/24;
			}
			apc4tmp4 = apc4tmp3;
			apc4tmp3 = apc4tmp2;
			apc4tmp2 = apc4tmp1;
			
			currentT += dt;
			//check for collisions
			updateCells();
			computeDist();
			sqrtpercall = sqrtpercall*3;
			if(somethingCollided){
				collisions();
				escapedBodies();
				fillCells();
			}
		}
	}
	
	// to prepare for APC4
	public void prepareAPC4(){
		//prepare temp4
		for (int i=0; i<N; i++){
			apc4tmp4[0][i] = bodies[i].x;
			apc4tmp4[1][i] = bodies[i].y;
			apc4tmp4[2][i] = bodies[i].vx;
			apc4tmp4[3][i] = bodies[i].vy;
		}
		rk4(1);
		//prepare temp3
		for (int i=0; i<N; i++){
			apc4tmp3[0][i] = bodies[i].x;
			apc4tmp3[1][i] = bodies[i].y;
			apc4tmp3[2][i] = bodies[i].vx;
			apc4tmp3[3][i] = bodies[i].vy;
		}
		rk4(1);
		//prepare temp2
		for (int i=0; i<N; i++){
			apc4tmp2[0][i] = bodies[i].x;
			apc4tmp2[1][i] = bodies[i].y;
			apc4tmp2[2][i] = bodies[i].vx;
			apc4tmp2[3][i] = bodies[i].vy;
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
		
		Double[] Nxy, tmpxy;
		public boolean cancelled;
		public boolean isWorking = false;
		int thisMethod;

		@Override
        protected String doInBackground() {
			isWorking = true;
			cancelled = false;
			thisMethod = method;
			Nxy = new Double[2*N+1];

			int spf = 10;
			double tmpT = currentT;
			
			if(thisMethod == APC4_METHOD){
				prepareAPC4();
			}
			while(currentT < maxT+tmpT){
				if(cancelled){
					isWorking = false;
					return "cancelled";
				}
				long starttime = System.currentTimeMillis();
				
				// do spf steps
				if(thisMethod == RK4_METHOD){
					rk4(spf);
				}else if(thisMethod == APC4_METHOD){
					apc4(spf);
				}

				// publish the result
				Nxy[0] = Double.valueOf(N);
				for(int j=0; j<N; j++){
					Nxy[j+1] = new Double(bodies[j].x);
					Nxy[j+1+N] = new Double(bodies[j].y);
				}
				publish(Nxy);
				
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

			int rpix, xpix, ypix;
			int thisN = tmpxy[0].intValue();
			ig.setColor(Color.BLUE);
			for(int i=0; i<thisN; i++){
				rpix = (int)Math.max(1, (int)(bodies[i].r*picPanel.width/A/zoomfactor));
				xpix = picPanel.width/2+(int)(tmpxy[i+1]*picPanel.width/A/zoomfactor);
				ypix = picPanel.height/2-(int)(tmpxy[i+1+thisN]*picPanel.height/A/zoomfactor);
				ig.fillOval(xpix-rpix, ypix-rpix, 2*rpix, 2*rpix);
			}

			picPanel.paintImmediately(0,0,picPanel.width, picPanel.height);
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

			//draw the image
			g.drawImage(image, 0, 0, null);

			//draw the time
			g.setColor(Color.BLACK);
			g.drawString(String.format("t = %.4f", currentT), 10, 10);

			//draw the zoom factor
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

	public class Body{
		public double x, y, vx, vy, tvx, tvy, M, r;
		public int cell, index;

		public Body(){
			x = 0.0;
			y = 0.0;
			vx = 0.0;
			vy = 0.0;
			tvx = 0.0;
			tvy = 0.0;
			M = 0.0;
			r = 0.0;
			cell = 1;
			index = -1;
		}
	}

	public class Cell{
		public int[] cellBodies;
		public int n;
		public int index;
		public double[] cm;
		public double m;
		public int[] neighbors;

		public Cell(int ind){
			cellBodies = new int[0];
			n = 0;
			index = ind;
			cm = new double[2];
			m = 0.0;
			if(ind == 0){ neighbors = new int[] {0, 1, 5, 6}; }
			if(ind == 1){ neighbors = new int[] {1, 0, 2, 5, 6, 7}; }
			if(ind == 2){ neighbors = new int[] {2, 1, 3, 6, 7, 8}; }
			if(ind == 3){ neighbors = new int[] {3, 2, 4, 7, 8, 9}; }
			if(ind == 4){ neighbors = new int[] {4, 3, 8, 9}; }
			if(ind == 5){ neighbors = new int[] {5, 0, 1, 6, 10, 11}; }
			if(ind == 6){ neighbors = new int[] {6, 0, 1, 2, 5, 7, 10, 11, 12}; }
			if(ind == 7){ neighbors = new int[] {7, 1, 2, 3, 6, 8, 11, 12, 13}; }
			if(ind == 8){ neighbors = new int[] {8, 2, 3, 4, 7, 9, 12, 13, 14}; }
			if(ind == 9){ neighbors = new int[] {9, 3, 4, 8, 13, 14}; }
			if(ind == 10){ neighbors = new int[] {10, 5, 6, 11, 15, 16}; }
			if(ind == 11){ neighbors = new int[] {11, 5, 6, 7, 10, 12, 15, 16, 17}; }
			if(ind == 12){ neighbors = new int[] {12, 6, 7, 8, 11, 13, 16, 17, 18}; }
			if(ind == 13){ neighbors = new int[] {13, 7, 8, 9, 12, 14, 17, 18, 19}; }
			if(ind == 14){ neighbors = new int[] {14, 8, 9, 13, 18, 19}; }
			if(ind == 15){ neighbors = new int[] {15, 10, 11, 16, 20, 21}; }
			if(ind == 16){ neighbors = new int[] {16, 10, 11, 12, 15, 17, 20, 21, 22}; }
			if(ind == 17){ neighbors = new int[] {17, 11, 12, 13, 16, 18, 21, 22, 23}; }
			if(ind == 18){ neighbors = new int[] {18, 12, 13, 14, 17, 19, 22, 23, 24}; }
			if(ind == 19){ neighbors = new int[] {19, 13, 14, 18, 23, 24}; }
			if(ind == 20){ neighbors = new int[] {20, 15, 16, 21}; }
			if(ind == 21){ neighbors = new int[] {21, 15, 16, 17, 20, 22}; }
			if(ind == 22){ neighbors = new int[] {22, 16, 17, 18, 21, 23}; }
			if(ind == 23){ neighbors = new int[] {23, 17, 18, 19, 22, 24}; }
			if(ind == 24){ neighbors = new int[] {24, 18, 19, 23}; }
		}
		
		public void resetCell(int maxn){
			cellBodies = new int[maxn];
			n = 0;
		}

		public void addBody(Body b){
			if(n == cellBodies.length){
				int[] temp = new int[n+1];
				System.arraycopy(cellBodies, 0, temp, 0, n);
				temp[n] = b.index;
				cellBodies = temp;
			}else{
				cellBodies[n] = b.index;
			}
			b.cell = index;
			n++;
		}

		public void removeBody(int ind){
			int cellindex = -1;
			for(int i=0; i<n; i++){
				if(ind == cellBodies[i]){
					cellindex = i;
					break;
				}
			}
			if(cellindex >= 0){
				for(int i=cellindex; i<n-1; i++){
					cellBodies[i] = cellBodies[i+1];
				}
				n--;
			}
		}

		public void computeCM(){
			cm[0] = 0.0;
			cm[1] = 0.0;
			m = 0;
			if(n ==0){
				return;
			}
			for(int i=0; i<n; i++){
				cm[0] += bodies[cellBodies[i]].M*bodies[cellBodies[i]].x;
				cm[1] += bodies[cellBodies[i]].M*bodies[cellBodies[i]].y;
				m +=bodies[cellBodies[i]].M;
			}
			cm[0] = cm[0]/m;
			cm[1] = cm[1]/m;
		}
	}

	public class Pair{
		public int i, j;
		public Pair(int k, int l){
			i = Math.min(k, l);
			j = Math.max(k, l);
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
