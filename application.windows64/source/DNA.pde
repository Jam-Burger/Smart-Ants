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
        yoff+=0.1;
      }
      xoff+=0.1;
    }
  }
  DNA crossover_one_point(DNA partner) {
    DNA childgenes= new DNA(genes.length);
    int partition= int(random(genes.length));
    for (int i=0; i<genes.length; i++) {
      if (i < partition) childgenes.genes[i]= genes[i].copy();
      else childgenes.genes[i]= partner.genes[i].copy();
    }
    return childgenes;
  }
  DNA crossover_two_point(DNA partner) {
    DNA childgenes= new DNA(genes.length);
    int partition1= int(random(genes.length/2));
    int partition2= int(random(genes.length/2, genes.length));
    for (int i=0; i<genes.length; i++) {
      if (i < partition1) childgenes.genes[i]= genes[i];
      else if(i < partition2) childgenes.genes[i]= partner.genes[i];
      else childgenes.genes[i]= genes[i];
    }
    return childgenes;
  }
  void mutate(float m) {
    for (int i = 0; i < genes.length; i++) {
      if (random(1) < m) {
        genes[i] = PVector.random2D();
      }
    }
  }
  void debug() {
    stroke(0, 100);
    float r= (gridScale-2)/2;
    for (int i=0; i<cols; i++) {
      for (int j=0; j<rows; j++) {
        int index= i + j * cols;
        float theta= genes[index].heading();
        pushMatrix();
        translate((i+0.5)*gridScale, (j+0.5)*gridScale);
        rotate(theta);
        line(-r*cos(theta), -r*sin(theta), r*cos(theta), r*sin(theta));
        popMatrix();
      }
    }
  }
}
