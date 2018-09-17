
/*
 Is this a spirograph? a harmonograph? a cycloidal object drawer?
 I don't really know.
 
 It simulates one rotating canvas and two rotating wheels(or wheel and pivot)
 that drive a "pen" connected by rigid lines.
 
 Play around.
*/
int high;

// the machine parameters
float startX, startY; // measured from center of canvas
float canvasRadius;
float penx, peny; // position of the pen
float wheelr1, wheelp1; // the radius and pivot radius of the wheel
float wheelx1, wheely1; // the center point of the wheel
float wheelpx1, wheelpy1; // position of wheel 1 pivot
float startpx1, startpy1, startpx2, startpy2; // to start the pivots in the same place
float wheelr2, wheelp2;
float wheelx2, wheely2;
float wheelpx2, wheelpy2;
float rod1, rod2; // rod length
int speedp1, speedq1, speedp2, speedq2; // p/q ratios for wheel speed
float speed1, speed2;

int setupMode;
boolean broken;
int whichWheel;
float stepAngle; // the angle between each step should be 1/canvasRadius
int rotationCount; // the number of rotations of the canvas to draw

// for the buttons and text fields
int buttonHeight, buttonx, buttony1, buttony2, buttony3, buttony4;
int sliderx1, slidery1, sliderx2, slidery2;
int textx1, textx2, texty1, texty2;


void setup(){
  size(800, 800);
  high = 800;
  canvasRadius = 300;
  speedp1 = 1;
  speedq1 = 1;
  speedp2 = 3;
  speedq2 = 2;
  
  setupMode = 0;
  broken = false;
  whichWheel = 1;
  stepAngle = 1.0/canvasRadius;
  rotationCount = 1;
  
  buttonx = width-100;
  buttonHeight = 40;
  buttony1 = buttonHeight;
  buttony2 = 2*buttonHeight;
  buttony3 = 3*buttonHeight;
  buttony4 = 4*buttonHeight;
  textx1 = buttonx;
  textx2 = buttonx+50;
  texty1 = 4*buttonHeight+5;
  texty2 = texty1;
  sliderx1 = buttonx+15;
  sliderx2 = buttonx+65;
  slidery1 = texty1 + 45;
  slidery2 = slidery1;
}

void draw(){
  background(0);
  fill(255);
  stroke(0);
  ellipseMode(RADIUS);
  ellipse(canvasRadius, high/2, canvasRadius, canvasRadius);
  
  if(broken){
    stroke(255);
    text("NOOOOO! It's broken!", 10, 40);
  }
  if(setupMode == 0){
    // set wheel 1 center
    fill(255, 0, 0);
    ellipse(pmouseX, pmouseY, 20, 20);
  }else if(setupMode == 1){
    // set wheel 1 pivot
    fill(255, 0, 0);
    float r = sqrt((xPixToPos(pmouseX)-wheelx1)*(xPixToPos(pmouseX)-wheelx1) + (yPixToPos(pmouseY)-wheely1)*(yPixToPos(pmouseY)-wheely1));
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(r), int(r));
    fill(0, 255, 0);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 5, 5);
    ellipse(pmouseX, pmouseY, 5, 5);
  }else if(setupMode == 2){
    // set wheel 2 center
    fill(200);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(wheelp1), int(wheelp1));
    fill(100);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 4, 4);
    ellipse(xPosToPix(wheelpx1), yPosToPix(wheelpy1), 4, 4);
    
    fill(255, 0, 0);
    ellipse(pmouseX, pmouseY, 20, 20);
  }else if(setupMode == 3){
    // set wheel 2 pivot
    fill(200);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(wheelp1), int(wheelp1));
    fill(100);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 4, 4);
    ellipse(xPosToPix(wheelpx1), yPosToPix(wheelpy1), 4, 4);
    
    fill(255, 0, 0);
    float r = sqrt((xPixToPos(pmouseX)-wheelx2)*(xPixToPos(pmouseX)-wheelx2) + (yPixToPos(pmouseY)-wheely2)*(yPixToPos(pmouseY)-wheely2));
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), int(r), int(r));
    fill(0, 255, 0);
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), 5, 5);
    ellipse(pmouseX, pmouseY, 5, 5);
  }else if(setupMode == 4){
    // set pen start point
    fill(200);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(wheelp1), int(wheelp1));
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), int(wheelp2), int(wheelp2));
    fill(100);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 4, 4);
    ellipse(xPosToPix(wheelpx1), yPosToPix(wheelpy1), 4, 4);
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), 4, 4);
    ellipse(xPosToPix(wheelpx2), yPosToPix(wheelpy2), 4, 4);
    
    // also, set pivot starts
    startpx1 = wheelpx1;
    startpy1 = wheelpy1;
    startpx2 = wheelpx2;
    startpy2 = wheelpy2;
    
    fill(255, 0, 0);
    ellipse(pmouseX, pmouseY, 4, 4);
  }else if(setupMode == 5){
    // setup complete show parts
    fill(200);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(wheelp1), int(wheelp1));
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), int(wheelp2), int(wheelp2));
    fill(100);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 4, 4);
    ellipse(xPosToPix(wheelpx1), yPosToPix(wheelpy1), 4, 4);
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), 4, 4);
    ellipse(xPosToPix(wheelpx2), yPosToPix(wheelpy2), 4, 4);
    ellipse(xPosToPix(startX), yPosToPix(startY), 4, 4);
    
    strokeWeight(10);
    stroke(0, 0, 255);
    line(xPosToPix(wheelpx1), yPosToPix(wheelpy1), xPosToPix(startX), yPosToPix(startY));
    line(xPosToPix(wheelpx2), yPosToPix(wheelpy2), xPosToPix(startX), yPosToPix(startY));
    strokeWeight(1);
    stroke(0);
  }else if(setupMode == 6){
    // setup complete hide rods
    fill(200);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), int(wheelp1), int(wheelp1));
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), int(wheelp2), int(wheelp2));
    fill(100);
    ellipse(xPosToPix(wheelx1), yPosToPix(wheely1), 4, 4);
    ellipse(xPosToPix(wheelpx1), yPosToPix(wheelpy1), 4, 4);
    ellipse(xPosToPix(wheelx2), yPosToPix(wheely2), 4, 4);
    ellipse(xPosToPix(wheelpx2), yPosToPix(wheelpy2), 4, 4);
    ellipse(xPosToPix(startX), yPosToPix(startY), 4, 4);
    
    fill(255);
    ellipse(canvasRadius, high/2, canvasRadius, canvasRadius);
  }
  
  // draw the control panel
  fill(220);
  stroke(0);
  rect(buttonx, 0, 100, buttonHeight);
  rect(buttonx, buttonHeight, 100, buttonHeight);
  rect(buttonx, 2*buttonHeight, 100, buttonHeight);
  rect(buttonx, 3*buttonHeight, 100, buttonHeight);
  
  textAlign(CENTER, CENTER);
  textSize(15);
  fill(0);
  text("reset", buttonx+50, buttonHeight/2);
  text("more turns", buttonx+50, buttonHeight/2 + buttonHeight);
  text("fewer turns", buttonx+50, buttonHeight/2 + 2*buttonHeight);
  if(whichWheel == 1){
    text("wheel 1", buttonx+50, buttonHeight/2 + 3*buttonHeight);
  }else{
    text("wheel 2", buttonx+50, buttonHeight/2 + 3*buttonHeight);
  }
  
  fill(255);
  text("rotations: "+str(rotationCount), 100, 20);
  text("ratio:", buttonx+50, texty1+7);
  if(whichWheel == 1){
    text(str(speedp1), buttonx + 20, texty1 + 27);
    text("/", buttonx + 50, texty1 + 27);
    text(str(speedq1), buttonx + 80, texty1 + 27);
  }else{
    text(str(speedp2), buttonx + 20, texty1 + 27);
    text("/", buttonx + 50, texty1 + 27);
    text(str(speedq2), buttonx + 80, texty1 + 27);
  }
  
  stroke(255);
  fill(30);
  rect(sliderx1, slidery1, 15, height-slidery1 - 20);
  rect(sliderx2, slidery2, 15, height-slidery2 - 20);
  fill(220);
  if(whichWheel == 1){
    rect(sliderx1-5, height - 15 - (speedp1*5), 25, 10);
    rect(sliderx2-5, height - 15 - (speedq1*5), 25, 10);
  }else{
    rect(sliderx1-5, height - 15 - (speedp2*5), 25, 10);
    rect(sliderx2-5, height - 15 - (speedq2*5), 25, 10);
  }
  
  
  // draw the picture
  if(setupMode == 6){
    float angle = 0;
    wheelpx1 = startpx1;
    wheelpy1 = startpy1;
    wheelpx2 = startpx2;
    wheelpy2 = startpy2;
    getPenPos();
    float lastX = startX;
    float lastY = startY;
    float newX = penx;
    float newY = peny;
    speed1 = speedp1*1.0/speedq1;
    speed2 = speedp2*1.0/speedq2;
    float tmpx;
    stroke(0, 0, 255);
    
    // iterate 2*PI/stepAngle times for one rotation
    if(speed1 > speed2 && speed1 > 1){
      stepAngle = 2.0/canvasRadius/speed1;
    }else if(speed2 > speed1 && speed2 > 1){
      stepAngle = 2.0/canvasRadius/speed2;
    }
    while(angle < rotationCount*2*PI){
      // rotate the wheels
      tmpx = wheelpx1;
      wheelpx1 = (wheelpx1-wheelx1)*cos(stepAngle*speed1) - (wheelpy1-wheely1)*sin(stepAngle*speed1) + wheelx1;
      wheelpy1 = (tmpx-wheelx1)*sin(stepAngle*speed1) + (wheelpy1-wheely1)*cos(stepAngle*speed1) + wheely1;
      tmpx = wheelpx2;
      wheelpx2 = (wheelpx2-wheelx2)*cos(stepAngle*speed2) - (wheelpy2-wheely2)*sin(stepAngle*speed2) + wheelx2;
      wheelpy2 = (tmpx-wheelx2)*sin(stepAngle*speed2) + (wheelpy2-wheely2)*cos(stepAngle*speed2) + wheely2;
      
      // find the pen position and draw a line to that point
      getPenPos();
      newX = penx*cos(angle) - peny*sin(angle);
      newY = penx*sin(angle) + peny*cos(angle);
      //newX = penx;
      //newY = peny;
      line(xPosToPix(lastX), yPosToPix(lastY), xPosToPix(newX), yPosToPix(newY));
      lastX = newX;
      lastY = newY;
      
      angle += stepAngle;
    }
    
    fill(255);
    text("angle: "+str(angle), 100, 40);
  }
  
}

void getPenPos(){
  float d = sqrt((wheelpx1-wheelpx2)*(wheelpx1-wheelpx2) + (wheelpy1-wheelpy2)*(wheelpy1-wheelpy2));
  float a = (rod1*rod1 - rod2*rod2 + d*d)/(2*d);
  float h = sqrt(rod1*rod1 - a*a);
  float mx = wheelpx1 + a*(wheelpx2-wheelpx1)/d;
  float my = wheelpy1 + a*(wheelpy2-wheelpy1)/d;
  float px = mx + h*(wheelpy2 - wheelpy1)/d;
  float py = my - h*(wheelpx2 - wheelpx1)/d;
  // check to see if this is in the circle
  if((px*px + py*py) <= canvasRadius*canvasRadius){
    penx = px;
    peny = py;
    return;
  }
  px = mx - h*(wheelpy2 - wheelpy1)/d;
  py = my + h*(wheelpx2 - wheelpx1)/d;
  if((px*px + py*py) <= canvasRadius*canvasRadius){
    penx = px;
    peny = py;
    return;
  }
  // if it gets here, the point was off the canvas
  broken = true;
  penx = px;
  peny = py;
}

float xPixToPos(int p){
  return 1.0*p - canvasRadius;
}

float yPixToPos(int p){
  return high*1.0/2 - p;
}

int xPosToPix(float p){
  return int(p+canvasRadius);
}

int yPosToPix(float p){
  return int(high/2 - p);
}

void mouseClicked(){
  // first check for button clicks
  if(mouseX > buttonx){
    if(mouseY < buttony1){
      // reset
      setupMode = 0;
      broken = false;
      rotationCount = 1;
    }else if(mouseY < buttony2){
      // draw more rotations +1
      rotationCount += 1;
    }else if(mouseY < buttony3){
      // draw fewer rotations -1
      rotationCount -= 1;
      if(rotationCount < 1){
        rotationCount = 1;
      }
    }else if(mouseY < buttony4){
      // select wheel
      if(whichWheel == 1){
        whichWheel = 2;
      }else{
        whichWheel = 1;
      }
    }else if(mouseY > slidery1 && mouseX < buttonx+50){
      // set slider 1
      if(whichWheel == 1){
        speedp1 = (height-mouseY-2)/5;
        if(speedp1 < 1){
          speedp1 = 1;
        }
      }else{
        speedp2 = (height-mouseY-2)/5;
        if(speedp2 < 1){
          speedp2 = 1;
        }
      }
    }else if(mouseY > slidery1 && mouseX > buttonx+50){
      // set slider 2
      if(whichWheel == 1){
        speedq1 = (height-mouseY-2)/5;
        if(speedq1 < 1){
          speedq1 = 1;
        }
      }else{
        speedq2 = (height-mouseY-2)/5;
        if(speedq2 < 1){
          speedq2 = 1;
        }
      }
    }
    return;
  }
  
  if(setupMode == 0){
    // set wheel 1 center
    wheelx1 = xPixToPos(mouseX);
    wheely1 = yPixToPos(mouseY);
    setupMode = 1;
  }else if(setupMode == 1){
    // set wheel 1 pivot
    wheelpx1 = xPixToPos(mouseX);
    wheelpy1 = yPixToPos(mouseY);
    if((abs(wheelpx1-wheelx1) < 5) && (abs(wheelpy1-wheely1) < 5)){
      wheelpx1 = wheelx1;
      wheelpy1 = wheely1;
    }
    wheelp1 = sqrt((wheelx1-wheelpx1)*(wheelx1-wheelpx1) + (wheely1-wheelpy1)*(wheely1-wheelpy1));
    setupMode = 2;
  }else if(setupMode == 2){
    // set wheel 2 center
    wheelx2 = xPixToPos(mouseX);
    wheely2 = yPixToPos(mouseY);
    setupMode = 3;
  }else if(setupMode == 3){
    // set wheel 2 pivot
    wheelpx2 = xPixToPos(mouseX);
    wheelpy2 = yPixToPos(mouseY);
    if((abs(wheelpx2-wheelx2) < 5) && (abs(wheelpy2-wheely2) < 5)){
      wheelpx2 = wheelx2;
      wheelpy2 = wheely2;
    }
    wheelp2 = sqrt((wheelx2-wheelpx2)*(wheelx2-wheelpx2) + (wheely2-wheelpy2)*(wheely2-wheelpy2));
    setupMode = 4;
  }else if(setupMode == 4){
    // set pen start point
    startX = xPixToPos(mouseX);
    startY = yPixToPos(mouseY);
    // compute rod length
    rod1 = sqrt((startX-wheelpx1)*(startX-wheelpx1) + (startY-wheelpy1)*(startY-wheelpy1));
    rod2 = sqrt((startX-wheelpx2)*(startX-wheelpx2) + (startY-wheelpy2)*(startY-wheelpy2));
    //make sure the rods are long enough to not bind
    if((sqrt((wheelx1-wheelx2)*(wheelx1-wheelx2)+(wheely1-wheely2)*(wheely1-wheely2)) + wheelp1 + wheelp2) > (rod1+rod2)){
      // too short
      broken = true;
    }
    setupMode = 5;
  }else if(setupMode == 5){
    // setup complete hide parts
    setupMode = 6;
  }
}

void mouseDragged(){
  if(mouseX > buttonx && mouseY > slidery1){
    if(mouseX < buttonx+50){
      if(whichWheel == 1){
        speedp1 = (height-mouseY-2)/5;
        if(speedp1 < 1){
          speedp1 = 1;
        }
      }else{
        speedp2 = (height-mouseY-2)/5;
        if(speedp2 < 1){
          speedp2 = 1;
        }
      }
    }else{
      if(whichWheel == 1){
        speedq1 = (height-mouseY-2)/5;
        if(speedq1 < 1){
          speedq1 = 1;
        }
      }else{
        speedq2 = (height-mouseY-2)/5;
        if(speedq2 < 1){
          speedq2 = 1;
        }
      }
    }
  }
}

void keyPressed(){
  if(keyCode == UP){
    if(whichWheel == 1){
      speedp1++;
    }else{
      speedp2++;
    }
  }else if(keyCode == DOWN){
    if(whichWheel == 1){
      speedp1--;
    }else{
      speedp2--;
    }
  }else if(keyCode == RIGHT){
    if(whichWheel == 1){
      speedq1++;
    }else{
      speedq2++;
    }
  }else if(keyCode == LEFT){
    if(whichWheel == 1){
      speedq1--;
    }else{
      speedq2--;
    }
  }
}
