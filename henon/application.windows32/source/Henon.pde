/*
* This produces an image of the basin of attraction and the attractor for the Henon map.
* The basin is much more interesting to look at. The map is as follows:
* For a point in a plane (x,y), 
* x' = 1-ax^2 + y
* y' = bx
* 
* a and b are adjustable, and the defaults are not the classical values.
* 
* The darkness represents the rate of divergence. The lighter areas diverge more slowly
* and white areas might not diverge at all. 
* 
* This code is in the public domain.
*/

// for the math
double xn, yn, tmpyn;
double a, b;
int maxdiv, mindiv;
double divThreshold;
int maxIters;
int[][] diviters;
double scale;

// for the GUI
int px, py;
boolean somethingChanged;
int picWidth;
double centerX, centerY, tmpCX, tmpCY;
int pressedX, pressedY;
boolean picDragging;

// This is the picture
PImage picture;

// for the controls
int barx, bary, barWidth, barHeight;
char changingParameter;
double aScale, bScale, sScale;
double aDefault, bDefault, sDefault;

void setup(){
  size(600, 650);
  picWidth = 600; // The picture will be square.
  
  // These parameters give a cool picture, but are not the classical Henon values.
  aDefault = 0.5;
  bDefault = 1.2;
  sDefault = 5;
  
  // initialize everything else
  xn = 0;
  yn = 0;
  a = aDefault;
  b = bDefault;
  px = 0;
  py = 0;
  maxdiv = 0;
  mindiv = 1000;
  divThreshold = 100;
  maxIters = 500;
  diviters = new int[picWidth][picWidth];
  scale = sDefault;
  somethingChanged = true;
  centerX = 0;
  centerY = 0;
  tmpCX = 0;
  tmpCY = 0;
  pressedX = 0;
  pressedY = 0;
  picDragging = false;
  
  // make the square picture
  picture = createImage(picWidth, picWidth, RGB);
  
  // for the control panel
  barx = 150;
  bary = width+15;
  barWidth = 300;
  barHeight = 30;
  changingParameter = 'a';
  aScale = 50;
  bScale = 150;
  sScale = 15;
  
}

/*
* This redraws everything as needed, but only recomputes the picture when 
* something has changed.
*/
void draw(){
  // first write the parameters and such at the bottom
  fill(0);
  stroke(0);
  rect(0, width, width, 50);
  fill(255);
  text("a = "+str((float)a), 10, width+10);
  text("b = "+str((float)b), 10, width+25);
  text("scale = "+str((float)scale), 10, width+40);
  // the center coordinates
  text("center: x = "+str((float)centerX)+"  y = "+str((float)centerY), barx, width+10);
  // some instructions
  text("press a, b, or s then", barx+barWidth+10, width+10);
  text("click the rectangle.", barx+barWidth+10, width+25);
  text("Scroll to zoom.", barx+barWidth+10, width+40);
  
  // this is the control bar
  stroke(255);
  fill(0);
  rect(barx, bary, barWidth, barHeight);
  line(barx + barWidth/2, bary, barx + barWidth/2, bary+barHeight);
  
  stroke(0, 255, 0);
  fill(255);
  int linePos = barx;
  if(changingParameter == 'a'){
    text("a", barx - 15, bary + 20);
    linePos = barx + barWidth/2 + (int)((a - aDefault)*aScale);
  }else if(changingParameter == 'b'){
    text("b", barx - 15, bary + 20);
    linePos = barx + barWidth/2 + (int)((b - bDefault)*bScale);
  }else if(changingParameter == 's'){
    text("scale", barx - 35, bary + 20);
    linePos = barx + (int)(barWidth*(scale/sScale));
  }
  line(linePos, bary, linePos, bary+barHeight);
  
  // only do the math and redraw the thing when needed
  if(somethingChanged == true){
    picture.loadPixels();
    
    doMath();
    // use the full diviters data to color each pixel
    // for the odd case of maxdiv = mindiv
    if(maxdiv <= mindiv){
      fill(200);
      rect(0, 0, picWidth, picWidth);
      fill(0);
      text("something is wrong", 10, 20);
    }else{
      double colorVal = 0;
      int colorInt = 0;
      double divisor = 255.0/(maxdiv-mindiv);
      color c = color(255);
      int pixpos = 0;
      for(int j=0; j<picWidth; j++){
        for(int i=0; i<picWidth; i++){
          if(diviters[i][j] >= 0){
            colorVal = (diviters[i][j] - mindiv)*divisor;
            colorInt = (int)(colorVal);
            c = color(colorInt, 0, 0);
          }else{
            colorVal = (-diviters[i][j] - mindiv)*divisor;
            colorInt = (int)(colorVal);
            c = color(colorInt, colorInt, colorInt);
          }
          
          picture.pixels[i+(j*picWidth)] = c;
        }
      }
      picture.updatePixels();
      somethingChanged = false;
    }
  }
  
  // draw the picture
  image(picture, 0, 0);
}

// As the name implies, this does the math and generates color values for each pixel.
void doMath(){
  maxdiv = 0;
  mindiv = maxIters;
  
  // iterate for each pixel
  for(int i=0; i<picWidth; i++){
    for(int j=0; j<picWidth; j++){
      // set yn and xn depending on pixel position and scale
      xn = i*scale/picWidth - scale/2 + centerX;
      yn = j*scale/picWidth - scale/2 + centerY;
      // perform iteration until the value gets above threshold or maxiters
      for (int k=0; k<maxIters; k++){
        tmpyn = yn;
        yn = 1 - a*yn*yn + b*xn;
        xn = tmpyn;
        if(yn > divThreshold){
          diviters[i][j] = k;
          if(k > maxdiv){ maxdiv = k; }
          if(k < mindiv){ mindiv = k; }
          k = maxIters;
        }else if(yn < -divThreshold){
          diviters[i][j] = -k;
          if(k > maxdiv){ maxdiv = k; }
          if(k < mindiv){ mindiv = k; }
          k = maxIters;
        }else if(k == maxIters){
          if(yn >= 0){
            diviters[i][j] = k;
          }else{
            diviters[i][j] = -k;
          }
          maxdiv = k;
        }
      }
    }
  }
}

void keyPressed(){
  if(key == 'a'){
    changingParameter = 'a';
  }else if(key == 'b'){
    changingParameter = 'b';
  }else if(key == 's'){
    changingParameter = 's';
  }
}

void mouseClicked(){
  // if the mouse is over the control bar
  if((mouseX > barx)&&(mouseX < barx+barWidth)&&(mouseY > bary)&&(mouseY < bary+barHeight)){
    int newPos = (mouseX - barx);
    if(changingParameter == 'a'){
      a = aDefault + (newPos - barWidth/2)*(1.0/aScale);
      somethingChanged = true;
    }else if(changingParameter == 'b'){
      b = bDefault + (newPos - barWidth/2)*(1.0/bScale);
      somethingChanged = true;
    }else if(changingParameter == 's'){
      scale = newPos*(1.0/barWidth)*sScale;
      //scale = sDefault + (newPos - barWidth/2)*(1.0/sScale);
      if(scale <= 0){ scale = 0.001; }
      somethingChanged = true;
    }
  }
}

void mousePressed(){
  // just stores the initial coordinates for dragging
  // if the mouse is over the picture
  if((mouseX > 0)&&(mouseX < picWidth)&&(mouseY > 0)&&(mouseY < picWidth)){
    pressedX = mouseX;
    pressedY = mouseY;
    tmpCX = centerX;
    tmpCY = centerY;
    picDragging = true;
  }
}

void mouseReleased(){
  picDragging = false;
}

void mouseDragged(){
  // if the mouse is over the control bar
  if((mouseX > barx)&&(mouseX < barx+barWidth)&&(mouseY > bary)&&(mouseY < bary+barHeight)){
    int newPos = (mouseX - barx);
    if(changingParameter == 'a'){
      a = aDefault + (newPos - barWidth/2)*(1.0/aScale);
      somethingChanged = true;
    }else if(changingParameter == 'b'){
      b = bDefault + (newPos - barWidth/2)*(1.0/bScale);
      somethingChanged = true;
    }else if(changingParameter == 's'){
      scale = newPos*(1.0/barWidth)*sScale;
      //scale = sDefault + (newPos - barWidth/2)*(1.0/sScale);
      if(scale <= 0){ scale = 0.001; }
      somethingChanged = true;
    }
  }
  
  // for dragging the picture
  if(picDragging){
    centerX = tmpCX - (mouseX - pressedX)*(scale/picWidth);
    centerY = tmpCY - (mouseY - pressedY)*(scale/picWidth);
    somethingChanged = true;
  }
}

void mouseWheel(MouseEvent event){
  int e = event.getCount();
  if(e > 0){
    // zoom out
    scale = scale*1.2;
    somethingChanged = true;
  }else{
    // zoom in on the mouse position
    scale = scale/1.2;
    //centerX = centerX + mouseX*(scale/picWidth) - scale/2;
    //centerY = centerY + mouseY*(scale/picWidth) - scale/2;
    centerX = centerX + mouseX*(0.2*scale/picWidth) - 0.2*scale/2;
    centerY = centerY + mouseY*(0.2*scale/picWidth) - 0.2*scale/2;
    somethingChanged = true;
  }
}
