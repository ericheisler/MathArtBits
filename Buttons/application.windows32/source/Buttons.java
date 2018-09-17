import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Buttons extends PApplet {

/*
If you look at completely random noise you will find patterns and see faces.
Take a look at these randomly colored squares and see what you can see.

Scroll to make new patterns.
*/

Button[][] buttons;
int back;
float aveRed, aveGreen, aveBlue;
int rows, cols;

public void setup(){
  
  rows = 60;
  cols = 100;
  buttons = new Button[cols][rows];
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      buttons[i][j] = new Button(i*10, j*10, 9, 9, color(random(255), random(255), random(255)));
    }
  }
  colorCalc();
  back = color(255);
}

public void draw(){
  background(back);
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      buttons[i][j].display();
    }
  }
  fill(0);
  text("ave", 5, height - 10);
  text(aveRed, 30, height - 10);
  text(aveGreen, 100, height - 10);
  text(aveBlue, 200, height - 10);
  fill(color(aveRed, aveGreen, aveBlue));
  rect(270, height - 25, 30, 20);
}

public void randomizeColors(){
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      buttons[i][j].setColor(color(random(255), random(255), random(255)));
    }
  }
  colorCalc();
}

public void colorCalc(){
  aveRed = 0;
  aveGreen  = 0;
  aveBlue = 0;
  int bcount = 0;
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      aveRed += red(buttons[i][j].getColor());
      aveGreen += green(buttons[i][j].getColor());
      aveBlue += blue(buttons[i][j].getColor());
      bcount++;
    }
  }
  aveRed = aveRed/bcount;
  aveGreen = aveGreen/bcount;
  aveBlue = aveBlue/bcount;
}

public void keyPressed(){
  if(key == 'a'){
    
  }else if(key == 'b'){
    
  }else if(key == 's'){
    
  }
}

public void mouseClicked(){
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      if(buttons[i][j].containsMouse()){
        back = buttons[i][j].getColor();
        return;
      }
    }
  }
  back = color(255);
}

public void mousePressed(){
  
}

public void mouseReleased(){
  
}

public void mouseDragged(){
  
}

public void mouseWheel(MouseEvent event){
  int e = event.getCount();
  if(e > 0){
    
  }else{
    
  }
  randomizeColors();
}

class Button{
  int x, y, wide, high;
  int col, textCol;
  boolean hasText;
  String theText;
  
  Button(int nx, int ny, int nwide, int nhigh, int ncol){
    x = nx;
    y = ny;
    wide = nwide;
    high = nhigh;
    col = ncol;
    textCol = color(0);
    
    hasText = false;
  }
  
  public boolean containsMouse(){
    if((mouseX >= x)&&(mouseX <= (x+wide))&&(mouseY >= y)&&(mouseY <= (y+high))){
      return true;
    }
    return false;
  }
  
  public void display(){
    stroke(0);
    fill(col);
    rect(x,y,wide,high);
    if(hasText){
      fill(textCol);
      text(theText, x + 5, y + high/2 + 5);
    }
  }
  
  public int getColor(){
    return col;
  }
  
  public void setColor(int c){
    col = c;
  }
  
  public void setText(String s, int tc){
    theText = s;
    textCol = tc;
    if(theText.length() > 0){
      hasText = true;
    }else{
      hasText = false;
    }
  }
}
  public void settings() {  size(1000, 700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Buttons" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
