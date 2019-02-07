import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
public class Main {
    public static double min = 0.0;
    public static double max = 1.0;
    public static Graph graph;
    public static NumberFormat formatter = new DecimalFormat("#0.000");
    public static Random r = new Random();
    public static double exectime;
    public static int minstring = 1;
    public static int maxString = 999;
    public static void main(String[] args) throws IOException {
        String fileName, numberOfGenerations, populationSize, crossOverProbability, mutationProbability;
        fileName = args[0];
        numberOfGenerations = args[1];
        populationSize = args[2];
        crossOverProbability = args[3];
        mutationProbability = args[4];
        exectime = System.currentTimeMillis();
        //file read
        BufferedReader file = new BufferedReader(new FileReader(fileName));
        String line = file.readLine();
        int nodeCount = Integer.valueOf(line);
        line = file.readLine();
        int vertexCount = Integer.valueOf(line);
        System.out.println("Node Count: " + nodeCount + "\nVertex Count: " + vertexCount);
        graph = new Graph(nodeCount);
        //reading all the node weights
        for (int i = 0; i < nodeCount; i++) {
            line = file.readLine();
            String[] splitted = line.split(" ");
            graph.nodes[i] = new Node(Integer.valueOf(splitted[0]), Double.valueOf(splitted[1]));
        }
        while ((line = file.readLine()) != null) {
            String[] splitted = line.split(" ");
            graph.nodes[Integer.valueOf(splitted[0])].vertices.add(Integer.valueOf(splitted[1]));
        }
        //Randomly generated population is repaired
        Population population = new Population(Integer.valueOf(populationSize));
        for(int i = 0; i < Integer.valueOf(populationSize) ; i++) {
              System.arraycopy(repair(population.individuals[i]),0,population.repairedPopulation[i],0,1000);

        }
        char [] maxFitnessClique = getBestFitness(population.repairedPopulation);
        char [] currentBestClique = maxFitnessClique;
        double maxFitness;
        //int duringGeneration = 0;
        maxFitness = getFitness(maxFitnessClique);
        double thisFitness = 0 ;
        //looping each generation
        for(int i = 1; i <= Integer.valueOf(numberOfGenerations); i++){
            //mating pool is created randomly by using the values created by roulette function
            ArrayList <Integer> matingPool = pickedOnes(population.repairedPopulation);
            for(int m = 0; m < population.matingPool.length; m++){
                System.arraycopy(population.repairedPopulation[matingPool.get(m)],0,population.matingPool[m],0,1000);
                //population.matingPool[m] = population.repairedPopulation[matingPool.get(m)];
            }
            //mating pool is crossovered here
            ArrayList<String> crossOvered = Crossover(population.matingPool,Double.valueOf(crossOverProbability));
            for(int j = 0; j < crossOvered.size() ; j++){
                System.arraycopy(crossOvered.get(j).toCharArray(),0,population.matingPool[j],0,1000);
            }
            //mutation is applied on the offspring on each of the individuals.
            ArrayList <String> mutated = Mutation(population.matingPool,Double.valueOf(mutationProbability));
            for(int k = 0 ; k < mutated.size(); k++){
                System.arraycopy(mutated.get(k).toCharArray(),0,population.matingPool[k],0,1000);
            }
            //Mating pool is repaired here.
            for(int z = 0; z < population.repairedPopulation.length; z++) {
                char[] repaired = repair(population.matingPool[z]);
                System.arraycopy(repaired, 0, population.repairedPopulation[z], 0, 1000);
            }
            //Best fitness is modified here if it has been changed
            currentBestClique = getBestFitness(population.repairedPopulation);
            thisFitness = getFitness(currentBestClique);
            //System.out.println("Highest fitness in generation " + i + " is " + thisFitness);
            if(thisFitness > maxFitness){
                System.arraycopy(currentBestClique,0,maxFitnessClique,0,1000);
                maxFitness = thisFitness;
                //duringGeneration = i;
                //System.out.println("New best fitness: " + getFitness(maxFitnessClique));
            }
        }
        System.out.println("File: " + fileName + " ,# Generations: " + numberOfGenerations + ",Pop. size: " + populationSize + " ,Crossover Prob.: " + crossOverProbability + " ,Mutation Prob.: " + mutationProbability +" ,Fitness: " + thisFitness );
        //System.out.println("Fitness: " + thisFitness + "(generation) "+ duringGeneration + "Max Clique is: \n" + String.valueOf(maxFitnessClique));
        exectime = System.currentTimeMillis() - exectime;
        System.out.println("Execution time: " + exectime);
    }
    //repair function
    public static char[] repair(char[] ind) {

        ArrayList<Integer> possibleClique = new ArrayList<>();
        for (int i = 0; i < ind.length; i++) {
            if (ind[i] == 49) {
                possibleClique.add(i);
            }
        }
        HashMap<Integer, Integer> adjacentCount = new HashMap<>();
        for (int i : possibleClique) {
            adjacentCount.put(i, 0);

            for (int j : possibleClique) {
                for (int k : graph.nodes[i].vertices) {
                    if (j == k) {
                        int x = adjacentCount.get(i);

                        x += 1;
                        adjacentCount.put(i, x);
                    }
                }
            }
        }
        List<Map.Entry<Integer, Integer>> tempList = new LinkedList<>(adjacentCount.entrySet());
        Collections.sort(tempList, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        int[] orderedIndexes = new int[tempList.size()];
        HashMap<Integer, Integer> countsOrdered = new LinkedHashMap<>();
        int c = 0;
        for (Map.Entry<Integer, Integer> x : tempList) {
            countsOrdered.put(x.getKey(), x.getValue());
            orderedIndexes[c] = x.getKey();
            c++;
        }
        char[] maxCliqueCount = new char[1000];
        char[] maxCliqueFitness = new char[1000];
        double maxFitness = 0;
        int maxNodeCount = 0;
        ArrayList<Integer>[] cliques = new ArrayList[tempList.size()];
        for (int i = 0; i < cliques.length; i++) {
            char[] tempInd = ind.clone();
            cliques[i] = new ArrayList<>();
            cliques[i].add(orderedIndexes[i]);
            for (int current : countsOrdered.keySet()) {
                if (current == orderedIndexes[i]) {
                    continue;
                } else if (isCliqueWith(current, cliques[i])) {
                    cliques[i].add(current);
                } else {
                    tempInd[current] = 48;
                }
            }
            //We get the best possible clique with maximum fitness or maximum node count.
            if (getFitness(tempInd) > maxFitness){
                maxFitness = getFitness(tempInd);
                maxCliqueFitness = tempInd;
            }
            if (getNodeCount(tempInd) > maxNodeCount) {
                maxNodeCount = getNodeCount(tempInd);
                maxCliqueCount = tempInd;
            }
            //System.out.println("Fitness: " + formatter.format(getFitness(tempInd)) + "\tNode Count: "+ getNodeCount(tempInd) +"\n"+ String.valueOf(tempInd));
        }
        //Randomly decide which clique will be added, with maximum node count or maximum weight ? Each option has .50 chance.
        double randomDice = min + (max - min) * r.nextDouble();
        if (randomDice >= 0.50) {
            ind = maxCliqueFitness;
        } else {
            ind = maxCliqueCount;
        }

        //this code can be used for randomly adding "legal bits" into a best clique.
/*        ArrayList <Integer> newClique = new ArrayList<>();
        for(int i = 0 ; i < ind.length ; i++){
            if(ind[i]==49){
                newClique.add(i);
            }
        }
        //System.out.println("Before adding more bits: \n" + String.valueOf(ind));
        for(int first : newClique){
            for(int i = 0; i < graph.nodes[first].vertices.size() ; i++){ //4 te biri kadar dene
                for(int nextEdge: graph.nodes[first].vertices) {
                    if(newClique.contains(nextEdge))
                        continue;
                    //int nextEdge = graph.nodes[first].vertices.get(r.nextInt(graph.nodes[first].vertices.size()));
                    if (isCliqueWith(nextEdge, newClique) && !newClique.contains(nextEdge)) { //o edge diger kalanlar ile clique ise ve icerde yoksa
                        ind[nextEdge] = 49; //clique e ekle
                        //System.out.println("Bit added to " + nextEdge);
                    }
                }
            }
        }*/
        //System.out.println("Max fitness selected. Fitness: "+formatter.format(getFitness(maxCliqueFitness))+"\nNode count for this clique: "+getNodeCount(maxCliqueFitness)+"\n"+ String.valueOf(maxCliqueFitness));
        //System.out.println("Max node count selected. Fitness: " + formatter.format(getFitness(maxCliqueCount)) + "\nNode count for this clique: " + getNodeCount(maxCliqueCount) + "\n" + String.valueOf(maxCliqueCount));
        return ind;
    }
    //this function checks if a given node can be a part of clique if added into it.
    public static boolean isCliqueWith(int number, ArrayList<Integer> clique) {
        boolean isClique = false;
        for (int currentNumber : clique) {
            if (currentNumber == number)
                continue;
            isClique = graph.nodes[number].vertices.contains(currentNumber);
            if (!isClique) return false;
        }
        return isClique;
    }
    //this function returns the fitness of a given individual
    public static double getFitness(char[] ind) {
        double fitness = 0.0;
        for (int i = 0; i < ind.length; i++) {
            if (ind[i] == 49) {
                fitness += graph.nodes[i].weight;
            }
        }
        return fitness;
    }
    //this function returns the individual with highest fitness in a given offspring.
    public static char[] getBestFitness(char [][] population){
        double fitness = 0.0;
        char [] best = new char [1000];
        for(int i = 0; i < population.length; i++){
            double thisFitness = getFitness(population[i]);
            if(thisFitness > fitness){
                fitness = thisFitness;
                best = population[i];
            }
        }
        return best;
    }
    //this returns the amount of nodes in an individual
    public static int getNodeCount(char[] ind) {
        int count = 0;
        for (int i = 0; i < ind.length; i++) {
            if (ind[i] == 49) {
                count++;
            }
        }
        return count;
    }
    //this checks if 1s in given individual represents a legal clique.
    public static boolean isCliqueLegal(char[] ind) {
        ArrayList<Integer> candidate = new ArrayList<>();
        boolean isClique = false;
        for (int i = 0; i < ind.length; i++) {
            if (ind[i] == 49) {
                candidate.add(i);
            }
        }
        for (int i : candidate) {
            if(isCliqueWith(i,candidate)){
                isClique = true;
            }else return false;

        }
        return isClique;
    }
    //this function applies mutation on an offspring with a given mutation probability.
    public static ArrayList<String> Mutation(char [][] crossovered, double mutationProbability){
        ArrayList<String> mutated = new ArrayList<>();
        int[] flipNumbers = new int[10];
        for(int i = 0; i <  crossovered.length ; i++){
            if(Probability(mutationProbability)){
                //System.out.println("MutationS1: " + String.valueOf(crossovered[i]));
                //generate random ten integer to flip in the range 1 - 1000
                for(int f=0; f<flipNumbers.length; f++) {
                    flipNumbers[f] = (int)(Math.random()*999 + 1);
                }
                for(int k=0; k<flipNumbers.length ; k++) {
                    if (crossovered[i][flipNumbers[k]] == 48) {
                        crossovered[i][flipNumbers[k]] = 49;
                    } else if (crossovered[i][flipNumbers[k]] == 49 ){
                        crossovered[i][flipNumbers[k]] = 48;
                    }
                }
            }
        }
        for(char [] mutatedInd : crossovered){
            mutated.add(String.valueOf(mutatedInd));
        }
        return mutated;
    }
    //this returns if a given probability has came.
    public static boolean Probability(double getProb){
        double randomDice = min + (max - min) * r.nextDouble();
        boolean dice;
        if(randomDice<=getProb){
            dice = true;
        }
        else
            dice = false;
        return dice;
    }
    //this function crossovers given repaired offspring
    public static ArrayList<String> Crossover(char [][] repaired,Double probability){
        //number i as population size
        ArrayList<String> crossovered = new ArrayList<>();
        for(int i =0; i< repaired.length; i++){ // tek tek butun kromozomlarda dolas
            int randomPoint = minstring + r.nextInt(maxString - minstring + 1);
            if(Probability(probability)) {
                //check if parents are not equal, then
                if (!String.valueOf(repaired[i]).equals(String.valueOf(repaired[i + 1]))) {
                    String concatenatedString1 = String.valueOf(repaired[i + 1]).substring(randomPoint, 1000);
                    String concatenatedString2 = String.valueOf(repaired[i]).substring(randomPoint, 1000);
                    String string1 = (String.valueOf(repaired[i]).substring(0, randomPoint)).concat(concatenatedString1);
                    String string2 = (String.valueOf(repaired[i + 1]).substring(0, randomPoint)).concat(concatenatedString2);
                    crossovered.add(string1);
                    crossovered.add(string2);
                }else{
                    crossovered.add(String.valueOf(repaired[i]));
                    crossovered.add(String.valueOf(repaired[i+1]));
                }
            }else{
                crossovered.add(String.valueOf(repaired[i]));
                crossovered.add(String.valueOf(repaired[i+1]));
            }
            i++;
        }
        return crossovered;
    }
    //this function has been used for creating mating pool with given probabilities.
    public static int Roulette(double[] fitnessProb){
        double a = 0.0;
        int count = 1;
        double b = fitnessProb[0];
        double roulette = min + (max - min) * r.nextDouble();
        int k = 0;
        for(int i = 0 ; i < fitnessProb.length ; i++){
            //System.out.println("a: " + a + " i: " + i + " b: " + b);
            if((roulette < b) && (roulette >= a)){
                k = i;
            }
            if(count < fitnessProb.length){
                a = b;
                b = b + fitnessProb[count];
                count++;
            }
        }
        return k;
    }
    //this returns a new mating pool order by using the values returned from roulette.
    public static ArrayList<Integer> pickedOnes(char [][] crossovered){
        double totalWeight = 0.0;
        double a = 0.0;
        double[] weights = new double[crossovered.length];
        double [] fitnessProbability = new double[crossovered.length];
        for(int i = 0 ; i<weights.length ; i++){
            double individualWeight = 0.0;
            for(int k = 0 ; k<1000 ; k++){
                if(crossovered[i][k] == 49){
                    individualWeight += graph.nodes[k].weight;
                }
            }
            weights[i] = individualWeight;
            totalWeight += individualWeight;
        }

        for(int j = 0; j< weights.length ; j++){
            fitnessProbability[j] = weights[j] / totalWeight;
            a += fitnessProbability[j];
        }
        ArrayList <Integer> pickedones = new ArrayList<>();
        int n = crossovered.length;
        for(int i = 0; i < n ; i++){
            int x = Roulette(fitnessProbability);
            pickedones.add(x);
        }
        return pickedones;
    }
}