import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
/**
 * An IFS fern app
 * 
 *
 * 
 */
public class IFSFern implements ActionListener, ChangeListener{

    // The important numbers for the math
    private double A[][] = {{0,0,0,0.16},{0.85,0.04,-0.04,0.85},
                            {0.2,-0.26,0.23,0.22},{-0.15,0.28,0.26,0.24}};
    private double t[][] = {{0,0},{0,1.6},{0,1.6},{0,0.44}};
    private double prob[] = {0.01,0.85,0.07,0.07};
    private double x;
    private double y;
    private int iters;
    private HashSet<Point> points;
	
	// the whole GUI
	private JFrame frame;
	
    //the ifs picture
    private Picture picture;
    private int pictureHeight;
    private int pictureWidth;
	Color fernColor;

    //the control panel
	JPanel controlPanel, transPanel, tLeftPanel, tRightPanel, buttonPanel;
	JPanel t1Panel, t2Panel, t3Panel, t4Panel;
    TransformationImage[] transimages;
	JSlider prob1Slider;
	JSlider prob2Slider;
	JSlider prob3Slider;
	JSlider prob4Slider;
	JLabel prob1Label;
	JLabel prob2Label;
	JLabel prob3Label;
	JLabel prob4Label;
	JButton waveButton;
    JButton rotateButton;
    JButton resetButton;
	JButton edit1Button;
	JButton edit2Button;
	JButton edit3Button;
	JButton edit4Button;
	JFrame editWindow;
	
	int triangleHold;

    //the option window
    private JFrame optionFrame;
    private JRadioButton first, secon, third, fourth;
    private JTextField begin, end, frames;
    private Button okbutton;
    private JLabel lab1, lab2, lab3;

    //the transformation editor
    private TransEditor tEditor;

    // the constructor doesn't do much
	public IFSFern(){
	
	}

	public void initializeThings(){

        x = 0;
        y = 0;
		iters = 50000;
		
		triangleHold = -1;

        points = new HashSet<Point>();
		
		pictureHeight = 500;
		pictureWidth = 500;
        picture = new Picture(pictureWidth, pictureHeight);
		picture.validate();
		fernColor = Color.green;

        transimages = new TransformationImage[4];
        transimages[0] = new TransformationImage(0);
        transimages[1] = new TransformationImage(1);
        transimages[2] = new TransformationImage(2);
        transimages[3] = new TransformationImage(3);
		transimages[0].setAlignmentX(Component.CENTER_ALIGNMENT);
		transimages[1].setAlignmentX(Component.CENTER_ALIGNMENT);
		transimages[2].setAlignmentX(Component.CENTER_ALIGNMENT);
		transimages[3].setAlignmentX(Component.CENTER_ALIGNMENT);

        tEditor = new TransEditor(0);

        prob1Label = new javax.swing.JLabel("trans 1 p=0."+String.valueOf((int)(prob[0]*100)));
        prob2Label = new javax.swing.JLabel("trans 2 p=0."+String.valueOf((int)(prob[1]*100)));
        prob3Label = new javax.swing.JLabel("trans 3 p=0."+String.valueOf((int)(prob[2]*100)));
        prob4Label = new javax.swing.JLabel("trans 4 p=0."+String.valueOf((int)(prob[3]*100)));
		prob1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob3Label.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob4Label.setAlignmentX(Component.CENTER_ALIGNMENT);

        edit1Button = new javax.swing.JButton("edit");
        edit1Button.addActionListener(this);
        edit2Button = new javax.swing.JButton("edit");
        edit2Button.addActionListener(this);
        edit3Button = new javax.swing.JButton("edit");
        edit3Button.addActionListener(this);
        edit4Button = new javax.swing.JButton("edit");
        edit4Button.addActionListener(this);
		
		edit1Button.setAlignmentX(Component.CENTER_ALIGNMENT);
		edit2Button.setAlignmentX(Component.CENTER_ALIGNMENT);
		edit3Button.setAlignmentX(Component.CENTER_ALIGNMENT);
		edit4Button.setAlignmentX(Component.CENTER_ALIGNMENT);

        waveButton = new javax.swing.JButton("wave");
        waveButton.addActionListener(this);
        rotateButton = new javax.swing.JButton("rotate");
        rotateButton.addActionListener(this);
        resetButton = new javax.swing.JButton("reset");
        resetButton.addActionListener(this);

        prob1Slider = new JSlider();
        prob2Slider = new JSlider();
        prob3Slider = new JSlider();
        prob4Slider = new JSlider();
		
		prob1Slider.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob2Slider.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob3Slider.setAlignmentX(Component.CENTER_ALIGNMENT);
		prob4Slider.setAlignmentX(Component.CENTER_ALIGNMENT);

        prob1Slider.setValue((int)(prob[0]*100));
        prob2Slider.setValue((int)(prob[1]*100));
        prob3Slider.setValue((int)(prob[2]*100));
        prob4Slider.setValue((int)(prob[3]*100));
        prob1Slider.addChangeListener(this);
        prob2Slider.addChangeListener(this);
        prob3Slider.addChangeListener(this);
        prob4Slider.addChangeListener(this);

		// now put everything together on the screen
		controlPanel = new JPanel();
		transPanel = new JPanel();
		tLeftPanel = new JPanel();
		tRightPanel = new JPanel();
		t1Panel = new JPanel();
		t2Panel = new JPanel();
		t3Panel = new JPanel();
		t4Panel = new JPanel();
		buttonPanel = new JPanel();

		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		transPanel.setLayout(new BoxLayout(transPanel, BoxLayout.X_AXIS));
		tLeftPanel.setLayout(new BoxLayout(tLeftPanel, BoxLayout.Y_AXIS));
		tRightPanel.setLayout(new BoxLayout(tRightPanel, BoxLayout.Y_AXIS));
		t1Panel.setLayout(new BoxLayout(t1Panel,BoxLayout.Y_AXIS));
		t2Panel.setLayout(new BoxLayout(t2Panel,BoxLayout.Y_AXIS));
		t3Panel.setLayout(new BoxLayout(t3Panel,BoxLayout.Y_AXIS));
		t4Panel.setLayout(new BoxLayout(t4Panel,BoxLayout.Y_AXIS));

		t1Panel.add(prob1Label);
		t1Panel.add(prob1Slider);
		t1Panel.add(transimages[0]);
		t1Panel.add(edit1Button);

		t2Panel.add(prob2Label);
		t2Panel.add(prob2Slider);
		t2Panel.add(transimages[1]);
		t2Panel.add(edit2Button);
		

		t3Panel.add(prob3Label);
		t3Panel.add(prob3Slider);
		t3Panel.add(transimages[2]);
		t3Panel.add(edit3Button);

		t4Panel.add(prob4Label);
		t4Panel.add(prob4Slider);
		t4Panel.add(transimages[3]);
		t4Panel.add(edit4Button);
		
		t1Panel.validate();
		t2Panel.validate();
		t3Panel.validate();
		t4Panel.validate();

		tLeftPanel.add(t1Panel);
		tRightPanel.add(t2Panel);
		tLeftPanel.add(t3Panel);
		tRightPanel.add(t4Panel);
		
		transPanel.add(tLeftPanel);
		transPanel.add(tRightPanel);

		buttonPanel.add(waveButton);
		buttonPanel.add(rotateButton);
		buttonPanel.add(resetButton);

		controlPanel.add(transPanel);
		controlPanel.add(buttonPanel);
		//controlPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		// let's set all the allignments
		t1Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		t2Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		t3Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		t4Panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tLeftPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		tRightPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		transPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		controlPanel.validate();
		
		// pack everything in a frame
		frame = new JFrame("IFSFern");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(900,550));
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(picture, BorderLayout.CENTER);
		pane.add(controlPanel, BorderLayout.EAST);
		pane.validate();
		frame.pack();
		frame.setVisible(true);

        // now set up the option window
        optionFrame = new JFrame("Choose your animation");
        first = new JRadioButton("1st", false);
        secon = new JRadioButton("2nd", true);
        third = new JRadioButton("3rd", false);
        fourth = new JRadioButton("4th", false);
        first.addActionListener(this);
        secon.addActionListener(this);
        third.addActionListener(this);
        fourth.addActionListener(this);
        begin = new JTextField("0",4);
        end = new JTextField("3.14",4);
        frames = new JTextField("80",4);
        begin.addActionListener(this);
        end.addActionListener(this);
        frames.addActionListener(this);
        okbutton = new Button("ok");
        okbutton.addActionListener(this);
        lab1 = new JLabel("start angle");
        lab2 = new JLabel("end angle");
        lab3 = new JLabel("frames");

        pane = optionFrame.getContentPane();
        pane.setLayout(new GridLayout(0,6));
        pane.add(new JLabel("transformation"));
        pane.add(first);
        pane.add(secon);
        pane.add(third);
        pane.add(fourth);
        pane.add(new JLabel(""));
        pane.add(lab1);
        pane.add(begin);
        pane.add(lab2);
        pane.add(end);
        pane.add(lab3);
        pane.add(frames);
        pane.add(okbutton);
        optionFrame.pack();

        //now finally draw the stupid thing
        buildFern();
        picture.repaint();
    }

	/**
     * The iterative steps
     */
    public void buildFern(){
        //set the initial point
        points.clear();
        points.add(new Point((int)x,(int)y));

        //now compute the iterations
        Random rand = new Random();
        double r;
        int sel;
        double sum[] = new double[4];
        sum[0] = prob[0];
        for(int k=0; k<prob.length-1; k++){
            sum[k+1] = sum[k]+prob[k+1];
        }

        double tempx = 0.0;
        int wide = frame.getSize().width;
        int high = frame.getSize().height;
        boolean in;
        for(int i=1; i<iters; i++){
            r = rand.nextDouble();
            sel = 0;
            for(int k=0; k<prob.length-1; k++){
                if(r > sum[k] && r < sum[k+1])
                    sel = k+1;
            }

            tempx = A[sel][0]*x + A[sel][1]*y + t[sel][0];
            y = A[sel][2]*x + A[sel][3]*y + t[sel][1];
            x = tempx;
            in = points.add(new Point((int)(x*high/2),(int)(y*high/2)));
        }
        transimages[0].makeTriangles();
        transimages[1].makeTriangles();
        transimages[2].makeTriangles();
        transimages[3].makeTriangles();
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == waveButton){
            iters = 15000;
            for(int i=0; i<10; i++){
                rotate(1,.01);
                buildFern();
                picture.paintImmediately(0,0,picture.getWidth(),picture.getHeight());
                for(int k=0; k<4; k++){
                    transimages[k].paintImmediately(0,0,transimages[k].getWidth(),transimages[k].getHeight());
                }
            }
            for(int i=0; i<10; i++){
                rotate(1,-.01);
                buildFern();
                picture.paintImmediately(0,0,picture.getWidth(),picture.getHeight());
                for(int k=0; k<4; k++){
                    transimages[k].paintImmediately(0,0,transimages[k].getWidth(),transimages[k].getHeight());
                }
            }
            iters = 50000;
        }
        else if(e.getSource() == rotateButton){

            optionFrame.setPreferredSize(new Dimension(300,400));
            optionFrame.setLocation(300,300);
            optionFrame.setVisible(true);
        }
        else if(e.getSource() == okbutton){
            double startangle, endangle;
            int n;
            try{
                n = Integer.parseInt(frames.getText());
                startangle = Double.parseDouble(begin.getText());
                endangle = Double.parseDouble(end.getText());
                optionFrame.setVisible(false);
                double interval = endangle - startangle;
                double step = interval/n;
                if(first.isSelected()) rotate(0,startangle);
                if(secon.isSelected()) rotate(1,startangle);
                if(third.isSelected()) rotate(2,startangle);
                if(fourth.isSelected()) rotate(3,startangle);
                
                iters = 10000;
                for(int i=0; i<n; i++){
                    if(first.isSelected()) rotate(0,step);
                    if(secon.isSelected()) rotate(1,step);
                    if(third.isSelected()) rotate(2,step);
                    if(fourth.isSelected()) rotate(3,step);
                    buildFern();
                    picture.paintImmediately(0,0,picture.getWidth(),picture.getHeight());
                    for(int k=0; k<4; k++){
                        transimages[k].paintImmediately(0,0,transimages[k].getWidth(),transimages[k].getHeight());
                    }
                }
                iters = 50000;
            }
            catch(RuntimeException exc){
                JOptionPane.showMessageDialog(frame, "oops, try again");
            }
            optionFrame.setVisible(false);
        }

        else if(e.getSource() == resetButton){
            resetValues();
            prob1Slider.setValue((int) (prob[0]*100));
            prob2Slider.setValue((int) (prob[1]*100));
            prob3Slider.setValue((int) (prob[2]*100));
            prob4Slider.setValue((int) (prob[3]*100));
            iters = 80000;
            buildFern();
            picture.repaint();
            iters = 50000;
        }
        else if(e.getSource() == edit1Button){
			editWindow = new javax.swing.JFrame();
            tEditor.setTrans(0);
			editWindow.add(tEditor);
			editWindow.pack();
            editWindow.setVisible(true);
        }
        else if(e.getSource() == edit2Button){
			editWindow = new javax.swing.JFrame();
            tEditor.setTrans(1);
			editWindow.add(tEditor);
			editWindow.pack();
            editWindow.setVisible(true);
        }
        else if(e.getSource() == edit3Button){
			editWindow = new javax.swing.JFrame();
            tEditor.setTrans(2);
			editWindow.add(tEditor);
			editWindow.pack();
            editWindow.setVisible(true);
        }
        else if(e.getSource() == edit4Button){
			editWindow = new javax.swing.JFrame();
            tEditor.setTrans(3);
			editWindow.add(tEditor);
			editWindow.pack();
            editWindow.setVisible(true);
        }
        
    }
    private boolean changing = false; //sorry, this is an annoying fix
    public void stateChanged(ChangeEvent e) {
        if(changing){
            //nothing
        }
        if(e.getSource() == prob1Slider){
            scaleProb(0, prob1Slider.getValue());
            buildFern();
            picture.repaint();
        }
        else if(e.getSource() == prob2Slider){
            scaleProb(1, prob2Slider.getValue());
            buildFern();
            picture.repaint();
        }
        else if(e.getSource() == prob3Slider){
            scaleProb(2, prob3Slider.getValue());
            buildFern();
            picture.repaint();
        }
        else if(e.getSource() == prob4Slider){
            scaleProb(3, prob4Slider.getValue());
            buildFern();
            picture.repaint();
        }

        if(!changing && !prob1Slider.getValueIsAdjusting() && !prob2Slider.getValueIsAdjusting() && !prob3Slider.getValueIsAdjusting() && !prob4Slider.getValueIsAdjusting()){
            changing = true;
            prob1Slider.setValue((int) (prob[0]*100));
            prob2Slider.setValue((int) (prob[1]*100));
            prob3Slider.setValue((int) (prob[2]*100));
            prob4Slider.setValue((int) (prob[3]*100));
            changing = false;
            buildFern();
            picture.repaint();
        }

    }

	
	
    /**
     * rotate a transformation
     */
    public void rotate(int n, double angle){
        double sint = Math.sin(angle);
        double cost = Math.cos(angle);
        double[] tempA = A[n];
        //double[][] tempt = t;
        double det1 = tempA[0]*tempA[3] - tempA[1]*tempA[2];

        tempA[0] = A[n][0]*cost - A[n][2]*sint;
        tempA[1] = A[n][1]*cost - A[n][3]*sint;
        tempA[2] = A[n][2]*cost + A[n][0]*sint;
        tempA[3] = A[n][3]*cost + A[n][1]*sint;
        double det2 = tempA[0]*tempA[3] - tempA[1]*tempA[2];
        double crapfactor = Math.sqrt(det1/det2);
        tempA[0] = tempA[0]*crapfactor;
        tempA[1] = tempA[1]*crapfactor;
        tempA[2] = tempA[2]*crapfactor;
        tempA[3] = tempA[3]*crapfactor;

        A[n] = tempA;
        //tempt[n][0] = t[n][0]*cost - t[n][1]*sint;
        //tempt[n][1] = t[n][1]*cost + t[n][0]*sint;
        //t = tempt;

    }

    /**
     * resets the values to the original fern
     */
    public void resetValues(){
        double tempA[][] = {{0,0,0,0.16},{0.85,0.04,-0.04,0.85},
                            {0.2,-0.26,0.23,0.22},{-0.15,0.28,0.26,0.24}};
        A = tempA;
        double tempt[][] = {{0,0},{0,1.6},{0,1.6},{0,0.44}};
        t = tempt;
        double[] tempP = {0.01,0.85,0.07,0.07};
        prob = tempP;
        iters = 50000;
    }

    /**
     * scales the probabilities and normalizes
     */
    public void scaleProb(int trans, int newValue){
        double change = newValue*1.0/100 - prob[trans];
        double oldSubtotal = prob[0]+prob[1]+prob[2]+prob[3]-prob[trans];
        prob[trans] = newValue*1.0/100;
        double newSubtotal = prob[0]+prob[1]+prob[2]+prob[3]-prob[trans];
        for(int k=0; k<4; k++){
            if(k != trans){
                prob[k] = prob[k] - prob[k]/oldSubtotal * change;
            }
            if(prob[k] < 0) prob[k] = 0;
            if(prob[k] >=1) prob[k] = 0.99;
        }
        
        prob1Label.setText("p=0."+String.valueOf((int)(prob[0]*100)));
        prob2Label.setText("p=0."+String.valueOf((int)(prob[1]*100)));
        prob3Label.setText("p=0."+String.valueOf((int)(prob[2]*100)));
        prob4Label.setText("p=0."+String.valueOf((int)(prob[3]*100)));
    }

    /**
     * the graphical component for the ifs drawer
     */
    class Picture extends JPanel{

        int maxX, maxY, minX, minY;
        int scale, centerx, centery;
		int height, width;

        private Graphics buffer;
		private Image image;

		public Picture(int wid, int hig){
			super();
			width = wid;
			height = hig;
			setMinimumSize(new Dimension(width,height));
		}
        
        public void paint(Graphics g){
             image = createImage(width, height);
             buffer = image.getGraphics();
             buffer.setColor(Color.black);
             buffer.fillRect(0,0,width, height);

             maxX = 1;
             maxY = 1;
             minX = -1;
             minY = -1;
             getScale();
             buffer.setColor(fernColor);
             for(Point c: points){
                 buffer.fillOval ((c.x*(width-20))/scale - centerx, -c.y*(height-20)/scale + centery, 1, 2);
             }
             buffer.setColor(Color.white);
             buffer.drawString("Iterations: "+iters,20,20);
             buffer.drawString("pixels shown "+points.size(),20,40);

             g.drawImage(image,0,0,this);
         }

         /**
          * finds the bounds and scale for the image
          */
         public void getScale(){
			for(Point c: points){
                 if(maxX < c.x)
                    maxX = c.x;
                 if(maxY < c.y)
                    maxY = c.y;
                 if(minX > c.x)
                    minX = c.x;
                 if(minY > c.y)
                    minY = c.y;
             }
             scale = Math.max(maxX-minX, maxY-minY);
             centerx = (maxX+minX)*width/(2*scale) - width/2;
             centery = (maxY+minY)*height/(2*scale) + height/2;
         }
    }

    /**
     * the graphical component for the transformations
     */
    class TransformationImage extends JComponent implements  MouseListener, MouseMotionListener{
		int tx[] = {0,25,0};
        int ty[] = {25,25,0};
        int ttx[] = {0,0,0};
        int tty[] = {0,0,0};
        Polygon baseTri;
        public Polygon newTri;
        int trans;
        int mousex, mousey;
        int triangleHold;
		int width, height;

        public TransformationImage(int transNumber){
            trans = transNumber;
			width = 150;
			height = 160;
			setPreferredSize(new Dimension(width,height));
			setMinimumSize(new Dimension(width,height));
			setMaximumSize(new Dimension(width,height));
			addMouseListener(this);
			addMouseMotionListener(this);
            makeTriangles();
        }

        public void makeTriangles(){
            baseTri = new Polygon(tx,ty,3);
            
            for(int k=0; k<3; k++){
                ttx[k] = (int)(A[trans][0]*tx[k] + A[trans][1]*ty[k] + t[trans][0]*25);
                tty[k] = (int)(A[trans][2]*tx[k] + A[trans][3]*ty[k] + t[trans][1]*25);
            }
            newTri = new Polygon(ttx,tty,3);

            repaint();
        }

        /**
         * Paint method for the component.
         */
        public void paint(Graphics g){
             Image image = createImage(width, height);
             Graphics buffer = image.getGraphics();
             buffer.setColor(Color.black);
             buffer.fillRect(0,0,width, height);
             buffer.translate(50, 50);

             buffer.setColor(Color.white);
             buffer.drawPolygon(baseTri);
             buffer.setColor(Color.red);
             buffer.drawPolygon(newTri);

             g.drawImage(image,0,0,this);
         }
		
        public void mouseClicked(MouseEvent e) {
			// do nothing
		}
		
		public void mousePressed(MouseEvent me) {
			mousex = me.getX()-50;
			mousey = me.getY()-50;
			if(newTri.contains(mousex, mousey)){
				triangleHold = trans;
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			if(triangleHold != trans){ return; }
			
			buildFern();
			repaint();
			triangleHold = -1;
		}
		
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}
		
		public void mouseExited(MouseEvent e) {
			//do nothing
		}
		
		public void mouseDragged(MouseEvent me) {
			if(triangleHold != trans){ return; }
			int mouseChangex = me.getX()-50 - mousex;
			int mouseChangey = me.getY()-50 - mousey;
			mousex = me.getX()-50;
			mousey = me.getY()-50;
			
			newTri.translate(mouseChangex, mouseChangey);
			t[triangleHold][0] += mouseChangex*1.0/25;
			t[triangleHold][1] += mouseChangey*1.0/25;
			iters = 5000;
			buildFern();
			repaint();
			picture.repaint();
			iters = 20000;
		}
		
		public void mouseMoved(MouseEvent e) {
			// do nothing
		}
    }

    /**
     * lets you change the triangles
     */
    class TransEditor extends JComponent implements MouseListener, MouseMotionListener{
		int tx[] = {0,100,0};
        int ty[] = {100,100,0};
        int ttx[] = {0,0,0};
        int tty[] = {0,0,0};
        Polygon baseTri;
        Polygon newTri;
        Rectangle[] corners;
        int cornerHold;
        int trans;
        int mousex, mousey;
		int height, width;

        public TransEditor(int transNumber){
            trans = transNumber;
			width = 500;
			height = 500;
			setPreferredSize(new Dimension(width,height));
			
            makeTriangles();
			
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void setTrans(int num){
            trans = num;
            makeTriangles();
			repaint();
            picture.repaint();
        }

        public void makeTriangles(){
            baseTri = new Polygon(tx,ty,3);

            for(int k=0; k<3; k++){
                ttx[k] = (int)(A[trans][0]*tx[k] + A[trans][1]*ty[k] + t[trans][0]*100);
                tty[k] = (int)(A[trans][2]*tx[k] + A[trans][3]*ty[k] + t[trans][1]*100);
            }
            newTri = new Polygon(ttx,tty,3);

            corners = new Rectangle[3];
            corners[0] = new Rectangle(ttx[0]-4, tty[0]-4, 9, 9);
            corners[1] = new Rectangle(ttx[1]-4, tty[1]-4, 9, 9);
            corners[2] = new Rectangle(ttx[2]-4, tty[2]-4, 9, 9);
        }

        /**
         * Paint method for the component.
         *
         * @param  g   the Graphics object for this applet
         */
        public void paint(Graphics g){
            Image image = createImage(width, height);
            Graphics buffer = image.getGraphics();
            buffer.setColor(Color.black);
            buffer.fillRect(0,0,width, height);
            buffer.translate(200, 200);

            buffer.setColor(Color.white);
            buffer.drawPolygon(baseTri);
            buffer.setColor(Color.red);
            buffer.drawPolygon(newTri);

            buffer.setColor(Color.white);
            buffer.drawRect(corners[0].x,corners[0].y,9,9);
            buffer.setColor(Color.blue);
            buffer.drawRect(corners[1].x,corners[1].y,9,9);
            buffer.setColor(Color.green);
            buffer.drawRect(corners[2].x,corners[2].y,9,9);
			
			buffer.setColor(Color.white);
			buffer.drawString("Drag the corners to change.",-50,-180);
            
            g.drawImage(image,0,0,this);
         }

        public void mouseClicked(MouseEvent e) {
            //do nothing
        }

        public void mousePressed(MouseEvent me) {
            mousex = me.getX()-200;
            mousey = me.getY()-200;
            cornerHold = -1;
            for(int i=0; i<3; i++){
                if(corners[i].contains(mousex, mousey)){
                    cornerHold = i;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            cornerHold = -1;
            buildFern();
			repaint();
            picture.repaint();
        }

        public void mouseEntered(MouseEvent e) {
            // do nothing
        }

        public void mouseExited(MouseEvent e) {
            // do nothing
        }

        public void mouseDragged(MouseEvent me) {
            int mouseChangex = me.getX()-200 - mousex;
            int mouseChangey = me.getY()-200 - mousey;
            mousex = me.getX()-200;
            mousey = me.getY()-200;
            if(cornerHold >= 0){
                corners[cornerHold].translate(mouseChangex, mouseChangey);
                newTri.xpoints[cornerHold] = (int)corners[cornerHold].getX()+2;
                newTri.ypoints[cornerHold] = (int)corners[cornerHold].getY()+2;

                double x1, x2, x3, y1, y2, y3;
                x1 = corners[0].getX()/100;
                x2 = corners[1].getX()/100;
                x3 = corners[2].getX()/100;
                y1 = corners[0].getY()/100;
                y2 = corners[1].getY()/100;
                y3 = corners[2].getY()/100;
                
                /*
                 * a0 + b1 + t1 = x1      a1 + b1 + t1 = x2     a0 + b0 + t1 = x3
                 * c0 + d1 + t2 = y1      c1 + d1 + t2 = y2     c0 + d0 + t2 = y3
                */
                A[trans][0] = x2-x1;
                A[trans][1] = x1-x3;
                A[trans][2] = y2-y1;
                A[trans][3] = y1-y3;
                t[trans][0] = x3;
                t[trans][1] = y3;
				
                iters = 5000;
                buildFern();
                picture.repaint();
				repaint();
                iters = 10000;
            }
        }

        public void mouseMoved(MouseEvent e) {
            //do nothing
        }
    }
    
}
