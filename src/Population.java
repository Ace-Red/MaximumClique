public class Population {

    public char [][] individuals;
    public char [][] repairedPopulation;
    public char [][] matingPool;
    public int populationSize;
    public Population(int populationSize){
        this.populationSize = populationSize;
        individuals= new char[populationSize][1000];
        repairedPopulation = new char[populationSize][1000];
        matingPool = new char[populationSize][1000];
        for(int i = 0 ; i<populationSize ; i++){
            for(int j = 0 ; j<1000; j++){
                double k = (Math.random()*System.currentTimeMillis()) % 2 ;
                if(k>=0.5){
                    individuals[i][j] = 49;
                }
                else{
                    individuals[i][j] = 48;
                }
            }
        }
    }
}
