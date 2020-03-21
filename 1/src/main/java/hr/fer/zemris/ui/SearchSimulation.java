package hr.fer.zemris.ui;

import hr.fer.zemris.search.SearchAlgorithm;
import hr.fer.zemris.search.heuristic.Astar;
import hr.fer.zemris.search.structure.BasicNode;
import hr.fer.zemris.search.structure.CostNode;
import hr.fer.zemris.search.structure.StateCostPair;
import hr.fer.zemris.search.uninformed.BFS;
import hr.fer.zemris.search.uninformed.UCS;
import hr.fer.zemris.ui.model.HeuristicModel;
import hr.fer.zemris.ui.model.StateSpaceModel;
import hr.fer.zemris.ui.util.FileParser;
import hr.fer.zemris.ui.util.HeuristicChecks;
import hr.fer.zemris.ui.util.PathValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Simple program that simulates searching algorithms.
 */
public class SearchSimulation {

    public static void main(String[] args) {
        if (args.length != 2) {
            exit("Program expects two arguments:\n" +
                    "(1) Path to state space definition,\n" +
                    "(2) Path to heuristic definition");
        }

        // validate provided paths
        Path stateSpaceFile = null;
        Path heuristicFile = null;
        try {
            stateSpaceFile = PathValidator.validateFile(args[0]);
            heuristicFile = PathValidator.validateFile(args[1]);
        } catch (Exception e) {
            exit(e.getMessage());
        }
        String stateSpaceFileName = stateSpaceFile.getFileName().toString();
        String heuristicFileName = heuristicFile.getFileName().toString();

        // DATA LOADING
        StateSpaceModel model = dataLoading(stateSpaceFile);
        System.out.println();

        // BREADTH FIRST SEARCH
        breadthFirstSearch(model, stateSpaceFileName);
        System.out.println();

        // UNIFORM COST SEARCH
        uniformCostSearch(model, stateSpaceFileName);
        System.out.println();

        // ASTAR SEARCH
        HeuristicModel hmodel = dataLoadingH(heuristicFile);
        astarSearch(model, hmodel, heuristicFileName);
        System.out.println();

        // HEURISTIC CHECKS
        heuristicChecks(model, hmodel, heuristicFile.getFileName().toString());
    }

    private static StateSpaceModel dataLoading(Path file) {
        System.out.println("\033[1m   DATA LOADING \033[0m" + file.getFileName() + ":");
        StateSpaceModel model = null;
        try {
            model = FileParser.parseStateSpaceFile(file);
        } catch (IOException e) {
            exit(e.getMessage());
        }
        System.out.println("Start state: " + model.getInitialState());
        System.out.println("End state(s): " + model.getGoalStates());
        System.out.println("State space size: " + model.stateSpaceSize());
        System.out.println("Total transitions: " + model.totalTransitions());
        return model;
    }

    private static HeuristicModel dataLoadingH(Path file) {
        try {
            return FileParser.parseHeuristicFile(file);
        } catch (IOException e) {
            exit(e.getMessage());
        }
        return null;
    }

    private static void breadthFirstSearch(StateSpaceModel model, String file) {
        System.out.println("\033[1m   BREADTH FIRST SEARCH \033[0m" + file + ":");
        System.out.println("Running bfs:");
        performSearchAlgorithm(new BFS<>(), model);
    }

    private static void uniformCostSearch(StateSpaceModel model, String file) {
        System.out.println("\033[1m   UNIFORM COST SEARCH \033[0m" + file + ":");
        System.out.println("Running ucs:");
        performSearchAlgorithm(new UCS<>(), model);
    }

    private static void astarSearch(StateSpaceModel model, HeuristicModel hmodel, String file) {
        System.out.println("\033[1m   A* ALGORITHM \033[0m+ heuristic " + file + ":");
        System.out.println("Running astar:");
        performSearchAlgorithm(new Astar<>(hmodel::heuristicValue), model);
    }

    private static void performSearchAlgorithm(SearchAlgorithm<String> alg, StateSpaceModel model) {
        String s0 = model.getInitialState();
        Function<String, Set<StateCostPair<String>>> succ = model::getTransition;
        Predicate<String> goal = s -> model.getGoalStates().contains(s);
        alg.search(s0, succ, goal).ifPresentOrElse(result -> {
            System.out.println("States visited = " + alg.getStatesVisited());
            System.out.print("Found path of length " + (result.depth() + 1));
            System.out.println((result instanceof CostNode ?
                    " with total cost " + ((CostNode<String>) result).getCost() : "") + ":");
            System.out.println(BasicNode.printPathTowardsNode(result));
        }, () -> exit("No solution was found."));
    }

    private static void heuristicChecks(StateSpaceModel model, HeuristicModel hmodel, String file) {
        System.out.println("\033[1m   HEURISTIC CHECKS \033[0mfor the " + file + " heuristic:");
        System.out.println("Checking heuristic:");
        System.out.println("Checking if heuristic is optimistic.");
        System.out.println(HeuristicChecks.checkOptimistic(model, hmodel));
        System.out.println("Checking if heuristic is consistent.");
        System.out.println(HeuristicChecks.checkConsistent(model, hmodel));
    }

    private static void exit(String message) {
        System.out.println(message);
        System.exit(-1);
    }

}
