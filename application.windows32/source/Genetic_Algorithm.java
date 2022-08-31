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

public class Genetic_Algorithm extends PApplet {

final int populationSize= 500;
final float mutationRate= 0.01f;
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

public void settings() {
  size(1080, 640+panelHeight, P2D);
}
public void setup() {
  height0= height-panelHeight;
  cols= width/gridScale;
  rows= height0/gridScale;
  lifeTime= width/3;
  population= new Population(populationSize);
  target= new Obstacle(width*.7f, height0/2-targetSize/2, targetSize, targetSize);
  obstacles= new ArrayList<Obstacle>();

  textSize(15);
}
public void draw() {
  stroke(0);
  fill(255);
  rect(0, 0, width, height0);
  for (Obstacle obstacle : obstacles) {
    obstacle.display(100);
  }
  target.display(0xff00FF00);
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
public void showPanel() {
  fill(255);
  rect(0, height0, width, panelHeight);
  fill(0);
  stroke(0);
  textAlign(LEFT);
  line(width*.5f, height0, width*.5f, height);
  String text;
  text=
    "Population size : " + populationSize + '\n' +
    "Generation : " + population.generation + '\n' +
    "Mutation rate : " + mutationRate*pow(1 + population.failures/10, 1.1f);
  text(text, 20, height0+10, width*.5f, panelHeight-20);
  text=
    "Time : " + lifeCycle + '\n' +
    "Last Arrive Time : " + (population.lastTime==lifeTime? "Did not Arrived" : population.lastTime) + '\n' +
    "Last success rate : " + (float) population.lastWinners*100/populationSize + " %";
  text(text, width*.5f + 20, height0+10, width*.5f, panelHeight-20);
}
public void keyPressed() {
  if (key=='d') {
    debug= !debug;
  }
}
boolean movingTarget= false;
public void mousePressed() {
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
public void mouseDragged() {
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
public void mouseReleased() {
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
class Ant {
  PVector pos, vel, acc, middlePos;
  float r;
  DNA dna;
  boolean alive= true;
  boolean stopped= false;
  float maxSpeed= 8, maxForce= 1;
  float recordDist;
  float fitness;
  int arriveTime= lifeTime;
  Ant(PVector _pos, DNA _dna) {
    pos= _pos;
    dna= _dna;
    vel= new PVector();
    acc= new PVector();
    middlePos= new PVector();
    r= 10;
    recordDist= width;
  }
  public boolean obstacles(ArrayList<Obstacle> obs) {
    for (Obstacle obstacle : obs) {
      if (obstacle.contains(pos)) return true;
    }
    return false;
  }
  public void calcFitness() {
    float d= recordDist;
    d/= 10;
    fitness= 1e10f / pow(arriveTime*d, 6);
    if (!alive) fitness/=1000;
    //println(fitness);
  }
  public void run(ArrayList<Obstacle> obs) {
    if (!stopped) {
      update();
      if (borders() || obstacles(obs)) {
        alive= false;
        stopped= true;
      }
    }
    display();
  }
  public void update() {
    if (!alive || finished()) return;
    int x= (int) pos.x/gridScale;
    int y= (int) pos.y/gridScale;
    x= constrain(x, 0, cols-1);
    y= constrain(y, 0, rows-1);

    PVector desired= dna.genes[x + y * cols].copy();
    desired.setMag(maxSpeed);
    PVector steer= PVector.sub(desired, vel);
    applyForce(steer);

    acc.limit(maxForce);
    vel.add(acc);
    vel.limit(maxSpeed);
    pos.add(vel);
    acc.mult(0);
  }
  public boolean finished() {
    float d= min(recordDist, PVector.dist(pos, target.center));
    if (d < recordDist) {
      recordDist= d;
    }
    if (target.contains(pos)) {
      stopped= true;
      return true;
    }
    return false;
  }
  public boolean borders() {
    return (pos.x+vel.x<0 || pos.x+vel.x>width || pos.y+vel.y<0 || pos.y+vel.y>height0);
  }
  public void applyForce(PVector f) {
    acc.add(f);
  }
  public void highlight() {
    fill(0xffB200FF, 160);
    stroke(0);
    circle(pos.x, pos.y, r*2.3f);
    //line(pos.x, pos.y, target.center.x, target.center.y);
    display();
  }
  public void display() {
    pushMatrix();
    fill(100, 50);
    stroke(0, 200);
    translate(pos.x, pos.y);
    rotate(vel.heading());
    triangle(r, 0, -r, -r/2, -r, r/2);
    popMatrix();
  }
}
class DNA {
  PVector[] genes;
  DNA(int num) {
    genes= new PVector[num];
    float xoff= 0;
    //noiseSeed(int(random(10000)));
    for (int x=0; x<cols; x++) {
      float yoff= 0;
      for (int y=0; y<rows; y++) {
        //float theta= map(noise(xoff, yoff), 0, 1, PI*2, -PI*2);
        //genes[x+y*cols]= PVector.fromAngle(theta);
        genes[x+y*cols]= PVector.random2D();
        yoff+=0.1f;
      }
      xoff+=0.1f;
    }
  }
  public DNA crossover_one_point(DNA partner) {
    DNA childgenes= new DNA(genes.length);
    int partition= PApplet.parseInt(random(genes.length));
    for (int i=0; i<genes.length; i++) {
      if (i < partition) childgenes.genes[i]= genes[i].copy();
      else childgenes.genes[i]= partner.genes[i].copy();
    }
    return childgenes;
  }
  public DNA crossover_two_point(DNA partner) {
    DNA childgenes= new DNA(genes.length);
    int partition1= PApplet.parseInt(random(genes.length/2));
    int partition2= PApplet.parseInt(random(genes.length/2, genes.length));
    for (int i=0; i<genes.length; i++) {
      if (i < partition1) childgenes.genes[i]= genes[i];
      else if(i < partition2) childgenes.genes[i]= partner.genes[i];
      else childgenes.genes[i]= genes[i];
    }
    return childgenes;
  }
  public void mutate(float m) {
    for (int i = 0; i < genes.length; i++) {
      if (random(1) < m) {
        genes[i] = PVector.random2D();
      }
    }
  }
  public void debug() {
    stroke(0, 100);
    float r= (gridScale-2)/2;
    for (int i=0; i<cols; i++) {
      for (int j=0; j<rows; j++) {
        int index= i + j * cols;
        float theta= genes[index].heading();
        pushMatrix();
        translate((i+0.5f)*gridScale, (j+0.5f)*gridScale);
        rotate(theta);
        line(-r*cos(theta), -r*sin(theta), r*cos(theta), r*sin(theta));
        popMatrix();
      }
    }
  }
}
class Obstacle {
  float x, y, w, h;
  PVector center;
  Obstacle(float x, float y, float w, float h) {
    this.x= x;
    this.y= y;
    this.w= w;
    this.h= h;
    this.center= new PVector(x+w/2, y+h/2);
  }
  public boolean contains(PVector v) {
    return v.x>x && v.x<x+w && v.y>y && v.y<y+h;
  }
  public void display(int clr) {
    fill(clr);
    stroke(0);
    rect(x, y, w, h);
  }
}
class Population {
  Ant[] population;
  int generation= 0;
  int minArriveTime= lifeTime, lastTime= lifeTime;
  int winners= 0, lastWinners= 0;
  int failures= 0;
  Population(int num) {
    population= new Ant[num];
    for (int i=0; i<population.length; i++) {
      PVector pos= new PVector(20, height0/2);
      population[i]= new Ant(pos, new DNA(cols*rows));
    }
  }
  public void live(ArrayList<Obstacle> obs) {
    float recordDist= width;
    int closest= 0;
    for (int i=0; i<population.length; i++) {
      if (population[i].finished()) {
        population[i].arriveTime= lifeCycle;
        minArriveTime= min(minArriveTime, population[i].arriveTime);
      }
      float d= population[i].recordDist;
      if (d < recordDist) {
        closest= i;
        recordDist= d;
      }
    }
    if (debug)
      population[closest].dna.debug();
    for (Ant ant : population) {
      ant.run(obs);
    }
    population[closest].highlight();
  }
  public void calcFitness() {
    for (Ant ant : population) {
      ant.calcFitness();
      if (random(1)<.5f && ant.alive && PVector.dist(ant.pos, ant.middlePos)<gridScale*4)
        ant.dna.genes[PApplet.parseInt(PApplet.parseInt(ant.middlePos.x / cols) + PApplet.parseInt(ant.middlePos.y/rows)*cols)]= PVector.random2D();
    }
  }
  public Ant selectOne() {
    int index= 0;
    float r= random(1);
    float totalFitness= totalFitness();
    while (r>0) {
      r-= population[index].fitness / totalFitness;
      index++;
    }
    //println(population[index-1].fitness, index);
    return population[index-1];
  }
  public void generate() {
    for (Ant ant : population) {
      if (ant.stopped && ant.alive) winners++;
    }
    if (winners==0) failures++;
    else failures= 0;

    float goodMutationRate= mutationRate*pow(1 + failures/10, 1.1f);
    //println(goodMutationRate);

    DNA[] newGenerationDNAs= new DNA[population.length];
    for (int i=0; i<population.length; i++) {
      Ant mom= selectOne();
      Ant dad= selectOne();

      DNA momgenes= mom.dna;
      DNA dadgenes= dad.dna;

      DNA childgenes= momgenes.crossover_one_point(dadgenes);
      childgenes.mutate(goodMutationRate);
      newGenerationDNAs[i]= childgenes;
    }
    for (int i=0; i<population.length; i++) {
      PVector pos= new PVector(20, height0/2);
      population[i]= new Ant(pos, newGenerationDNAs[i]);
    }
    updateGeneration();
  }
  public void updateGeneration() {
    generation++;
    lastTime= minArriveTime;
    minArriveTime= lifeTime;
    lastWinners= winners;
    winners= 0;
  }
  public float totalFitness() {
    float total= 0;
    for (Ant ant : population) {
      total+=ant.fitness;
    }
    return total;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Genetic_Algorithm" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
