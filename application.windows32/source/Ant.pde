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
  boolean obstacles(ArrayList<Obstacle> obs) {
    for (Obstacle obstacle : obs) {
      if (obstacle.contains(pos)) return true;
    }
    return false;
  }
  void calcFitness() {
    float d= recordDist;
    d/= 10;
    fitness= 1e10f / pow(arriveTime*d, 6);
    if (!alive) fitness/=1000;
    //println(fitness);
  }
  void run(ArrayList<Obstacle> obs) {
    if (!stopped) {
      update();
      if (borders() || obstacles(obs)) {
        alive= false;
        stopped= true;
      }
    }
    display();
  }
  void update() {
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
  boolean finished() {
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
  boolean borders() {
    return (pos.x+vel.x<0 || pos.x+vel.x>width || pos.y+vel.y<0 || pos.y+vel.y>height0);
  }
  void applyForce(PVector f) {
    acc.add(f);
  }
  void highlight() {
    fill(#B200FF, 160);
    stroke(0);
    circle(pos.x, pos.y, r*2.3);
    //line(pos.x, pos.y, target.center.x, target.center.y);
    display();
  }
  void display() {
    pushMatrix();
    fill(100, 50);
    stroke(0, 200);
    translate(pos.x, pos.y);
    rotate(vel.heading());
    triangle(r, 0, -r, -r/2, -r, r/2);
    popMatrix();
  }
}
