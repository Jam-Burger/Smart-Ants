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
  void live(ArrayList<Obstacle> obs) {
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
  void calcFitness() {
    for (Ant ant : population) {
      ant.calcFitness();
      if (random(1)<.5 && ant.alive && PVector.dist(ant.pos, ant.middlePos)<gridScale*4)
        ant.dna.genes[int(int(ant.middlePos.x / cols) + int(ant.middlePos.y/rows)*cols)]= PVector.random2D();
    }
  }
  Ant selectOne() {
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
  void generate() {
    for (Ant ant : population) {
      if (ant.stopped && ant.alive) winners++;
    }
    if (winners==0) failures++;
    else failures= 0;

    float goodMutationRate= mutationRate*pow(1 + failures/10, 1.1);
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
  void updateGeneration() {
    generation++;
    lastTime= minArriveTime;
    minArriveTime= lifeTime;
    lastWinners= winners;
    winners= 0;
  }
  float totalFitness() {
    float total= 0;
    for (Ant ant : population) {
      total+=ant.fitness;
    }
    return total;
  }
}
