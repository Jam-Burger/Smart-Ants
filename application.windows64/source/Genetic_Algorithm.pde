final int populationSize= 500;
final float mutationRate= 0.01;
final int panelHeight= 100;
int height0;
int lifeTime;
int lifeCycle= 0;

final int gridScale= 10;
final int targetSize= 40;
int cols, rows;

Population population;

Obstacle target;
ArrayList<Obstacle> obstacles;
boolean debug= false;
Obstacle current= null;

void settings() {
  size(1080, 640+panelHeight, P2D);
}
void setup() {
  height0= height-panelHeight;
  cols= width/gridScale;
  rows= height0/gridScale;
  lifeTime= width/3;
  population= new Population(populationSize);
  target= new Obstacle(width*.7, height0/2-targetSize/2, targetSize, targetSize);
  obstacles= new ArrayList<Obstacle>();

  textSize(15);
}
void draw() {
  stroke(0);
  fill(255);
  rect(0, 0, width, height0);
  for (Obstacle obstacle : obstacles) {
    obstacle.display(100);
  }
  target.display(#00FF00);
  if (lifeCycle < lifeTime) {
    if (lifeCycle==lifeTime/2) {
      for (Ant a : population.population) {
        a.middlePos= a.pos.copy();
      }
    }
    population.live(obstacles);
    lifeCycle++;
  } else {
    lifeCycle= 0;
    population.calcFitness();
    population.generate();
  }
  if (current!=null) current.display(50);
  showPanel();
}
void showPanel() {
  fill(255);
  rect(0, height0, width, panelHeight);
  fill(0);
  stroke(0);
  textAlign(LEFT);
  line(width*.5, height0, width*.5, height);
  String text;
  text=
    "Population size : " + populationSize + '\n' +
    "Generation : " + population.generation + '\n' +
    "Mutation rate : " + mutationRate*pow(1 + population.failures/10, 1.1);
  text(text, 20, height0+10, width*.5, panelHeight-20);
  text=
    "Time : " + lifeCycle + '\n' +
    "Last Arrive Time : " + (population.lastTime==lifeTime? "Did not Arrived" : population.lastTime) + '\n' +
    "Last success rate : " + (float) population.lastWinners*100/populationSize + " %";
  text(text, width*.5 + 20, height0+10, width*.5, panelHeight-20);
}
void keyPressed() {
  if (key=='d') {
    debug= !debug;
  }
}
boolean movingTarget= false;
void mousePressed() {
  if (mouseY>height0) return;
  if (target.contains(new PVector(mouseX, mouseY))) {
    movingTarget= true;
    target.x= mouseX - target.w/2;
    target.y= mouseY - target.h/2;
    target.center.x= mouseX;
    target.center.y= mouseY;
    return;
  }
  if (mouseButton==RIGHT) {
    for (Obstacle ob : obstacles) {
      if (ob.contains(new PVector(mouseX, mouseY))) {
        obstacles.remove(ob);
        break;
      }
    }
  } else current= new Obstacle(mouseX, mouseY, 0, 0);
}
void mouseDragged() {
  if (movingTarget && mouseY<height0) {
    target.x= mouseX - target.w/2;
    target.y= mouseY - target.h/2;
    target.center.x= mouseX;
    target.center.y= mouseY;
    return;
  }
  if (current==null) return;
  if (mouseButton==RIGHT) return;

  current.w= mouseX-current.x;
  current.h= mouseY-current.y;
}
void mouseReleased() {
  movingTarget= false;
  if (current==null) return;
  if (mouseButton==RIGHT) return;
  if (current.w<0) {
    current.x+=current.w;
    current.w*=-1;
  }
  if (current.h<0) {
    current.y+=current.h;
    current.h*=-1;
  }
  obstacles.add(current);
  current= null;
}
