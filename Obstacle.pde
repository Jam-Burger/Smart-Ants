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
  boolean contains(PVector v) {
    return v.x>x && v.x<x+w && v.y>y && v.y<y+h;
  }
  void display(int clr) {
    fill(clr);
    stroke(0);
    rect(x, y, w, h);
  }
}
