// a grid of squares with colors. 
// They alter their colors depending on their surroundings

color[][] boxes;
int rows, cols;
int bwidth, bheight;

int interval, tic;

void setup(){
  size(1000, 550);
  rows = 50;
  cols = 100;
  boxes = new color[cols][rows];
  bwidth = 10;
  bheight = 10;
  
  interval = 0;
  
  randomize();
  
  tic = millis();
}

void draw(){
  noStroke();
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      fill(boxes[i][j]);
      rect(i*bwidth, j*bheight, bwidth, bheight);
    }
  }
  
  if(millis() > tic + interval){
    popularColors();
  }
}

void randomize(){
  for(int i=0; i<cols; i++){
    for(int j=0; j<rows; j++){
      boxes[i][j] = color(random(255), random(255), random(255));
    }
  }
}

void popularColors(){
  // each box looks at its neighbors and finds the most popular color
  // The popular color increases as others decrease
  float r, g, b;
  // First the corners
  r = red(boxes[0][1]) + red(boxes[1][1]) + red(boxes[1][0]);
  g = green(boxes[0][1]) + green(boxes[1][1]) + green(boxes[1][0]);
  b = blue(boxes[0][1]) + blue(boxes[1][1]) + blue(boxes[1][0]);
  changeColor(r, g, b, 0, 0);
  r = red(boxes[cols-2][0]) + red(boxes[cols-2][1]) + red(boxes[cols-1][1]);
  g = green(boxes[cols-2][0]) + green(boxes[cols-2][1]) + green(boxes[cols-1][1]);
  b = blue(boxes[cols-2][0]) + blue(boxes[cols-2][1]) + blue(boxes[cols-1][1]);
  changeColor(r, g, b, cols-1, 0);
  r = red(boxes[cols-1][rows-2]) + red(boxes[cols-2][rows-2]) + red(boxes[cols-2][rows-1]);
  g = green(boxes[cols-1][rows-2]) + green(boxes[cols-2][rows-2]) + green(boxes[cols-2][rows-1]);
  b = blue(boxes[cols-1][rows-2]) + blue(boxes[cols-2][rows-2]) + blue(boxes[cols-2][rows-1]);
  changeColor(r, g, b, cols-1, rows-1);
  r = red(boxes[0][rows-2]) + red(boxes[1][rows-2]) + red(boxes[1][rows-1]);
  g = green(boxes[0][rows-2]) + green(boxes[1][rows-2]) + green(boxes[1][rows-1]);
  b = blue(boxes[0][rows-2]) + blue(boxes[1][rows-2]) + blue(boxes[1][rows-1]);
  changeColor(r, g, b, 0, rows-1);
  // then the top and bottom edges
  for(int i=1; i<cols-1; i++){
    r = red(boxes[i-1][0]) + red(boxes[i-1][1]) + red(boxes[i][1]) + red(boxes[i+1][1]) + red(boxes[i+1][0]);
    g = green(boxes[i-1][0]) + green(boxes[i-1][1]) + green(boxes[i][1]) + green(boxes[i+1][1]) + green(boxes[i+1][0]);
    b = blue(boxes[i-1][0]) + blue(boxes[i-1][1]) + blue(boxes[i][1]) + blue(boxes[i+1][1]) + blue(boxes[i+1][0]);
    changeColor(r, g, b, i, 0);
    r = red(boxes[i-1][rows-1]) + red(boxes[i-1][rows-2]) + red(boxes[i][rows-2]) + red(boxes[i+1][rows-2]) + red(boxes[i+1][rows-1]);
    g = green(boxes[i-1][rows-1]) + green(boxes[i-1][rows-2]) + green(boxes[i][rows-2]) + green(boxes[i+1][rows-2]) + green(boxes[i+1][rows-1]);
    b = blue(boxes[i-1][rows-1]) + blue(boxes[i-1][rows-2]) + blue(boxes[i][rows-2]) + blue(boxes[i+1][rows-2]) + blue(boxes[i+1][rows-1]);
    changeColor(r, g, b, i, rows-1);
  }
  // then the left and right sides
  for(int i=1; i<rows-1; i++){
    r = red(boxes[0][i-1]) + red(boxes[1][i-1]) + red(boxes[1][i]) + red(boxes[1][i+1]) + red(boxes[0][i+1]);
    g = green(boxes[0][i-1]) + green(boxes[1][i-1]) + green(boxes[1][i]) + green(boxes[1][i+1]) + green(boxes[0][i+1]);
    b = blue(boxes[0][i-1]) + blue(boxes[1][i-1]) + blue(boxes[1][i]) + blue(boxes[1][i+1]) + blue(boxes[0][i+1]);
    changeColor(r, g, b, 0, i);
    r = red(boxes[cols-1][i-1]) + red(boxes[cols-2][i-1]) + red(boxes[cols-2][i]) + red(boxes[cols-2][i+1]) + red(boxes[cols-1][i+1]);
    g = green(boxes[cols-1][i-1]) + green(boxes[cols-2][i-1]) + green(boxes[cols-2][i]) + green(boxes[cols-2][i+1]) + green(boxes[cols-1][i+1]);
    b = blue(boxes[cols-1][i-1]) + blue(boxes[cols-2][i-1]) + blue(boxes[cols-2][i]) + blue(boxes[cols-2][i+1]) + blue(boxes[cols-1][i+1]);
    changeColor(r, g, b, cols-1, i);
  }
  // finally the central ones
  for(int i=1; i<cols-1; i++){
    for(int j=1; j<rows-1; j++){
      r = red(boxes[i+1][j]) + red(boxes[i+1][j+1]) + red(boxes[i][j+1]) + red(boxes[i-1][j+1]) + red(boxes[i-1][j]) + red(boxes[i-1][j-1]) + red(boxes[i][j-1]) + red(boxes[i+1][j-1]);
      g = green(boxes[i+1][j]) + green(boxes[i+1][j+1]) + green(boxes[i][j+1]) + green(boxes[i-1][j+1]) + green(boxes[i-1][j]) + green(boxes[i-1][j-1]) + green(boxes[i][j-1]) + green(boxes[i+1][j-1]);
      b = blue(boxes[i+1][j]) + blue(boxes[i+1][j+1]) + blue(boxes[i][j+1]) + blue(boxes[i-1][j+1]) + blue(boxes[i-1][j]) + blue(boxes[i-1][j-1]) + blue(boxes[i][j-1]) + blue(boxes[i+1][j-1]);
      changeColor(r, g, b, i, j);
    }
  }
}

void popularColorsNoEdge(){
  // each box looks at its neighbors and finds the most popular color
  // The popular color increases as others decrease
  float r, g, b;
  
  // only the central ones
  for(int i=1; i<cols-1; i++){
    for(int j=1; j<rows-1; j++){
      r = red(boxes[i+1][j]) + red(boxes[i+1][j+1]) + red(boxes[i][j+1]) + red(boxes[i-1][j+1]) + red(boxes[i-1][j]) + red(boxes[i-1][j-1]) + red(boxes[i][j-1]) + red(boxes[i+1][j-1]);
      g = green(boxes[i+1][j]) + green(boxes[i+1][j+1]) + green(boxes[i][j+1]) + green(boxes[i-1][j+1]) + green(boxes[i-1][j]) + green(boxes[i-1][j-1]) + green(boxes[i][j-1]) + green(boxes[i+1][j-1]);
      b = blue(boxes[i+1][j]) + blue(boxes[i+1][j+1]) + blue(boxes[i][j+1]) + blue(boxes[i-1][j+1]) + blue(boxes[i-1][j]) + blue(boxes[i-1][j-1]) + blue(boxes[i][j-1]) + blue(boxes[i+1][j-1]);
      changeColor(r, g, b, i, j);
    }
  }
}

void changeColor(float r, float g, float b, int i, int j){
  /*
  if(r >= g){
    if(r >= b){
      // r is max
      upRed(i, j);
    }else{
      // b is max
      upBlue(i, j);
    }
  }else if(g >= b){
    // g is max
    upGreen(i, j);
  }
  */
  
  if((r > b) && (r > g)){
    upRed(i, j);
  }else if((b > r) && (b > g)){
    upBlue(i, j);
  }else if((g > r) && (g > b)){
    upGreen(i, j);
  }
  
}

void upRed(int i, int j){
  // increament red and decrement others
  if(red(boxes[i][j]) > 254){
    boxes[i][j] |= 0xFF0000;
  }else{
    boxes[i][j] += 0x010000;
  }
  if(green(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFFFF00FF;
  }else{
    boxes[i][j] -= 0x100;
  }
  if(blue(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFFFFFF00;
  }else{
    boxes[i][j] -= 1;
  }
}

void upGreen(int i, int j){
  if(red(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFF00FFFF;
  }else{
    boxes[i][j] -= 0x010000;
  }
  if(green(boxes[i][j]) > 254){
    boxes[i][j] |= 0xFF00;
  }else{
    boxes[i][j] += 0x0100;
  }
  if(blue(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFFFFFF00;
  }else{
    boxes[i][j] -= 1;
  }
}

void upBlue(int i, int j){
  if(red(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFF00FFFF;
  }else{
    boxes[i][j] -= 0x010000;
  }
  if(green(boxes[i][j]) < 1){
    boxes[i][j] &= 0xFFFF00FF;
  }else{
    boxes[i][j] -= 0x100;
  }
  if(blue(boxes[i][j]) > 254){
    boxes[i][j] |= 0xFF;
  }else{
    boxes[i][j] += 1;
  }
}

void mouseWheel(MouseEvent event){
  int e = event.getCount();
  if(e > 0){
    
  }else{
    
  }
  popularColors();
}

void mouseClicked(){
  randomize();
}