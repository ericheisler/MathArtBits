import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.util.*;

/**
 * This is a 3D cellular automaton for non-interacting, semi mean field dust clouds
 *
 * GUI Component
 *
 */
public class DustCloudGUI extends JPanel implements ActionListener, ChangeListener{
	
	// the model
	DustCloudModel model;
	
	// GUI info
	int width, height;
	long elapsed, perstep;
	int imageType;
	double grayAdjust;
	
	// GUI components
	JFrame frame;
	JPanel controlPanel;
	Picture picPanel;
	JTextField NField, TField, MField, LField, collField, diffField, countField;
	JLabel NLabel, TLabel, MLabel, LLabel, collLabel, diffLabel, countLabel;
	JButton startButton, stopButton, randButton, setButton, resetButton, cloudButton, specialButton;
	JSlider graySlider;
	
	DustWorker worker;

    /**
     * The constructor
     */
    public DustCloudGUI() {
		super();
		setBackground(Color.WHITE);
		
		// initialize everything
        model = new DustCloudModel();

		width = 1000;
		height = 800;
		imageType = 1;
		grayAdjust = 0.0;

		worker = new DustWorker();

		controlPanel = new JPanel();
		picPanel = new Picture(width-400, height-400);

		NField = new JTextField(String.valueOf(model.N/2), 8);
		MField = new JTextField(String.valueOf(model.M), 8);
		LField = new JTextField(String.valueOf(model.L), 8);
		TField = new JTextField(String.valueOf(model.Tinterval), 8);
		collField = new JTextField(String.valueOf(model.collision), 8);
		diffField = new JTextField(String.valueOf(model.stopping), 8);
		countField = new JTextField(String.valueOf(model.totalCount), 8);
		
		NLabel = new JLabel("N = ");
		MLabel = new JLabel("M = ");
		LLabel = new JLabel("L = ");
		TLabel = new JLabel("T = ");
		collLabel = new JLabel("collisions = ");
		diffLabel = new JLabel("stopping = ");
		countLabel = new JLabel("part. count = ");

		startButton = new JButton("Start");
		startButton.addActionListener(this);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		randButton = new JButton("Randomize");
		randButton.addActionListener(this);
		setButton = new JButton("Set values");
		setButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		cloudButton = new JButton("Add cloud");
		cloudButton.addActionListener(this);
		specialButton = new JButton("special");
		specialButton.addActionListener(this);

		graySlider = new JSlider();
		graySlider.setValue((int)((grayAdjust+1.0)*50));
		graySlider.addChangeListener(this);

		controlPanel.setLayout(new GridLayout(12,2));
		
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		controlPanel.add(setButton);
		controlPanel.add(resetButton);
		controlPanel.add(randButton);
		controlPanel.add(cloudButton);
		controlPanel.add(specialButton);
		controlPanel.add(new JLabel(" "));

		controlPanel.add(new JLabel("gray adj."));
		controlPanel.add(graySlider);
		
		controlPanel.add(NLabel);
		controlPanel.add(NField);
		controlPanel.add(MLabel);
		controlPanel.add(MField);
		controlPanel.add(LLabel);
		controlPanel.add(LField);
		controlPanel.add(TLabel);
		controlPanel.add(TField);
		controlPanel.add(collLabel);
		controlPanel.add(collField);
		controlPanel.add(diffLabel);
		controlPanel.add(diffField);
		controlPanel.add(countLabel);
		controlPanel.add(countField);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(picPanel);
		add(controlPanel);
		
		// make a frame and put it together
		frame = new JFrame("dusty");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container pane = frame.getContentPane();
		pane.add(this);

		pane.validate();
		
        frame.pack();
        frame.setVisible(true);

    }
	
	/**
	 * The main method
	 */
	public static void main(String[] args){
		DustCloudGUI nb = new DustCloudGUI();
	}
	
	/**
	 * For the action listener
	 */
    public void actionPerformed(ActionEvent e){
		if(e.getSource() == startButton){
			if(!worker.isWorking){
				model.maxT += model.Tinterval;
				startSim();
			}
		}else if(e.getSource() == stopButton){
			worker.cancelled = true;
		}else if(e.getSource() == resetButton){
			if(worker.isWorking){
				worker.cancelled = true;
				while(!worker.isDone()){
					try{
						synchronized(this){
							wait(100);
						}
					}catch(InterruptedException ignore){
						System.out.println("interrupted");
					}
				}
			}
			
			// once the worker has stopped, reset
			model.reset();
			
			repaint();
		}else if(e.getSource() == randButton){
			if(!worker.isWorking){
				model.randomize();
				picPanel.repaint();
			}
		}else if(e.getSource() == setButton){
			if(!worker.isWorking){
				setParams();
				picPanel.repaint();
			}
		}else if(e.getSource() == cloudButton){
			if(!worker.isWorking){
				int xp, yp, rad, bps, dir;
				try{
					xp = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input center X coordinate"));
					yp = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input center Y coordinate"));
					rad = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input radius"));
					bps = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input bodies per point"));
					dir = Integer.valueOf((String)JOptionPane.showInputDialog(frame, "Input direction \n r=0, tr=1, tl=2, \n l=3, bl=4, br=5, s=8"));
				}catch(Exception exce){
					return;
				}
				model.setCloud2D(bps,2*xp,yp,rad,dir);
				picPanel.repaint();
			}
		}else if(e.getSource() == specialButton){
			if(!worker.isWorking){
				model.setCondition();
				picPanel.repaint();
			}
		}
	}

    public void stateChanged(ChangeEvent e) {
		if(e.getSource() == graySlider){
			grayAdjust = graySlider.getValue()/50.0 - 1.0;
			if(grayAdjust > 1.0){ grayAdjust = 1.0; }
			if(grayAdjust < -1.0){ grayAdjust = -1.0; }
		}
		repaint();
	}

	// runs the simulation
	public void startSim(){
		worker = new DustWorker();
		worker.execute();
	}

	// sets the values written in the fields
	public void setParams(){
		try{
			int tmpN = 2*Integer.valueOf(NField.getText());
			int tmpM = Integer.valueOf(MField.getText());
			int tmpL = Integer.valueOf(LField.getText());
			int tmpcount = Integer.valueOf(countField.getText());
			int tmpinter = Integer.valueOf(TField.getText());
			double tmpdiff = Double.valueOf(diffField.getText());
			double tmpcoll = Double.valueOf(collField.getText());
			if(tmpdiff < 0 || tmpdiff > 1){
				tmpdiff = 0.0;
				diffField.setText("0.0");
			}
			if(tmpcoll < 0 || tmpcoll > 1){
				tmpcoll = 0.0;
				collField.setText("0.0");
			}
			
			model.setParams(new int[]{tmpN, tmpM, tmpL, tmpcount, tmpinter}, new double[]{tmpdiff, tmpcoll});
			
		} catch(Exception expt){
			JOptionPane.showMessageDialog(this, "Couldn't read values. Try again.");
			return;
		}
	}
	
	////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	// This is the graphical bit.
	///////////////////////////////////////////////////////////////////////
	
	public class Picture extends JPanel{
		public int pwidth, pheight;
		int topbar;
		int iwidth, iheight;
		BufferedImage image;
		
		public Picture(int w, int h){
			super();
			pwidth = w;
			pheight = h;
			topbar = 30;
			iwidth = w-topbar;
			iheight = h-topbar;
			
			setPreferredSize(new Dimension(pwidth, pheight));
			setBackground(Color.WHITE);
			
			image = new BufferedImage(iwidth, iheight, BufferedImage.TYPE_INT_ARGB);
			Graphics ig = image.getGraphics();
			ig.setColor(Color.WHITE);
			ig.fillRect(0, 0, iwidth, iheight);
		}
		
		// ye olde painting method
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// first clear stuff
			g.setColor(Color.WHITE);
			g.fillRect(0,0,30,30);
			
			// make the image and draw it
			makeImage();
			g.drawImage(image, 0, 30, null);
			
			//draw the time
			g.setColor(Color.BLACK);
			g.drawString("t = "+String.valueOf(model.currentT), 10, 10);
			
			//draw some numbers
			g.drawString("step time(ms) = "+String.valueOf(perstep), 130, 10);
			g.drawString("elapsed time(ms) = "+String.valueOf(elapsed), 130, 22);
			g.drawString("count check = "+String.valueOf(model.countCheck), 350, 10);
			g.drawString("percent moving = "+String.valueOf((model.totalmoving*100)/(model.countCheck+1)), 350, 22);
		}
		
		public void makeImage(){
			Graphics ig = image.getGraphics();
			ig.setColor(Color.WHITE);
			ig.fillRect(0, 0, iwidth, iheight);
			ig.setColor(Color.BLACK);
			// draw a border
			int offset = 20;
			double dx, dy;
			if(0.2885*model.N > model.M){
				dx = (iwidth-2*offset)*1.0/(model.N-1);
				dy = (iheight-2*offset)*3.464/(model.N-1);
			}else{
				dx = (iwidth-2*offset)*0.2887/(model.M);
				dy = (iheight-2*offset)*1.0/(model.M);
			}
			int picheight = (int)(dy*model.M);
			ig.drawRect(offset-6, topbar+offset-6, (int)(dx*(model.N-1))+12, (int)(dy*(model.M-0.5))+12);
			
			// we can draw different things here
			if(imageType == 0){
				//draw simple points
				ig.setColor(Color.BLACK);
				int xpix = 0;
				int ypix = 0;
				int radius = 1;
				
				// determine a good radius scaling
				int maxcount = 1;
				int thiscount = 0;
				for(int i=0; i<model.N; i++){
					for(int j=0; j<model.M; j++){
						for(int k=0; k<model.L; k++){
							thiscount = 0;
							for(int l=0; l<8; l++){
								thiscount += model.partcount[i][j][k][l];
							}
							if(thiscount>maxcount){
								maxcount = thiscount;
							}
						}
					}
				}
				// draw each point
				for(int i=0; i<model.N; i++){
					xpix = (int)(i*dx) + offset;
					for(int j=0; j<model.M; j++){
						ypix = topbar + offset + picheight - (int)(j*dy);
						if(i%2==1){
							ypix -= (int)(dy/2);
						}
						for(int k=0; k<model.L; k++){
							radius = 0;
							for(int l=0; l<8; l++){
								radius += model.partcount[i][j][k][l];
							}
							radius = (int)(radius*dy/(2*maxcount));
							
							ig.fillOval(xpix-radius,ypix-radius,2*radius,2*radius);
						}
					}
				}
			}else if(imageType == 1){
				// draw grayscale squares
				ig.setColor(Color.BLACK);
				int xpix = 0;
				int ypix = 0;
				
				// determine the grayscale billy
				int maxcount = 1;
				int thiscount = 0;
				float graycolor = 1.0f;
				for(int i=0; i<model.N; i++){
					for(int j=0; j<model.M; j++){
						for(int k=0; k<model.L; k++){
							thiscount = 0;
							for(int l=0; l<9; l++){
								thiscount += model.partcount[i][j][k][l];
							}
							if(thiscount>maxcount){
								maxcount = thiscount;
							}
						}
					}
				}
				// draw a square(or point) for each point
				int shigh, swide;
				for(int i=0; i<model.N; i++){
					xpix = (int)(i*dx) + offset;
					swide = (int)((i+1)*dx - i*dx);
					if(swide <=0){ swide = 1; }
					for(int j=0; j<model.M; j++){
						ypix = topbar + offset + picheight - (int)(j*dy);
						shigh = (int)((j+1)*dy - j*dy);
						if(shigh <=0){ shigh = 1; }
						if(i%2==1){
							ypix -= (int)(dy/2);
						}

						for(int k=0; k<model.L; k++){
							thiscount = 0;
							for(int l=0; l<9; l++){
								thiscount += model.partcount[i][j][k][l];
							}
							if(grayAdjust >= 0){
								graycolor = (float)(1.0 - (thiscount-(maxcount-1)*grayAdjust)/(maxcount-(maxcount-1)*grayAdjust));
							}else{
								graycolor = (float)(1.0 - (thiscount)/(maxcount+(maxcount-1)*grayAdjust));
							}
							graycolor = (float)Math.max(0.0,graycolor);
							graycolor = (float)Math.min(1.0,graycolor);
							ig.setColor(new Color(graycolor, graycolor, graycolor));
							
							ig.fillRect(xpix,ypix,swide+1,shigh+1);
						}
					}
				}
				
			}
		}
		
	}

	//////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	/**
	 * This is the worker thread which runs the calculations
	 * in the background and publishes nothing(dummy Integer)
	 */
	private class DustWorker extends SwingWorker<String, Integer> {
		// needed for communication
		public boolean cancelled;
		public boolean isWorking = false;
		Integer dummy = new Integer(1);
		// for timing
		long starttime, stepstarttime;

		@Override
        protected String doInBackground() {
			isWorking = true;
			cancelled = false;
			
			starttime = System.currentTimeMillis();
			while(model.currentT < model.maxT){
				if(cancelled){
					isWorking = false;
					return "cancelled";
				}
				stepstarttime = System.currentTimeMillis();
				
				// do the math stuff things
				// there is a special case of one layer (2D system)
				if(model.L==1){
					// it is written elsewhere
					try{
						if(!model.timeStep2D()){
							cancelled = true;
							isWorking = false;
							return "cancelled";
						}
					}catch(Exception exc){
						System.out.println(exc.getMessage());
					}
					
					
				}else{
					// not yet complete!
					if(!model.timeStep3D()){
						cancelled = true;
						isWorking = false;
						return "cancelled";
					}
				}
				
				// compute times and publish
				elapsed = System.currentTimeMillis() - starttime;
				perstep = System.currentTimeMillis() - stepstarttime;
				publish(dummy);
			}
			// if you get here, congratulations! you finished!
			isWorking = false;
            return "finished";
        }

        @Override
        protected void process(java.util.List<Integer> ignore) {
			repaint();
        }
	}
}
