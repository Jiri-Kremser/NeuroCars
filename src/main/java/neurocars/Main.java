package neurocars;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

import neurocars.neuralNetwork.Network;
import neurocars.neuralNetwork.service.InputManager;
import neurocars.neuralNetwork.service.InputManagerImpl;
import neurocars.utils.RaceResultComparator;
import neurocars.valueobj.NeuralNetworkInput;
import neurocars.valueobj.RaceResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Main {

  public static void requiredParameter(CommandLine line, String name,
      Options options) {
    if (!line.hasOption(name)) {
      System.out.println("Required parameter missing: " + name);
      printHelp(options);
      System.exit(1);
    }
  }

  public static void printHelp(Options options) {
    HelpFormatter help = new HelpFormatter();
    help.printHelp("neurocars", options, true);
  }

  public static void printResults(RaceResult[] results) {
    Arrays.sort(results, new RaceResultComparator());

    System.out.println("RACE RESULTS");
    System.out.println("+----+----------------------+--------+--------+--------+--------+--------+");
    System.out.println("| #  | id                   | total  | avg    | min    | max    | stddev |");
    System.out.println("+----+----------------------+--------+--------+--------+--------+--------+");
    for (int i = 0; i < results.length; i++) {
      RaceResult r = results[i];
      System.out.println(String.format(
          "| %7$2d | %1$-20s | %2$6.0f | %3$6.0f | %4$6.0f | %5$6.0f | %6$6.0f |",
          r.getCar().getId(), r.getTotal(), r.getAvg(), r.getMin(), r.getMax(),
          r.getStandardDeviation(), i + 1));
    }
    System.out.println("+----+----------------------+--------+--------+--------+--------+--------+");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Options options = new Options();
    options.addOption("mode", true,
        "program mode [train/race/test/interactive]");
    options.addOption("scenario", true, "game scenario file [race mode]");
    options.addOption("input", true,
        "neural network training input file [train/test mode]");
    options.addOption("network", true,
        "serialized neural network output file [train mode]");
    options.addOption("threshold", true, "error threshold [train mode]");
    options.addOption("layers", true, "hidden layers count [train mode]");
    options.addOption("neurons", true,
        "number of neurons in every hidden layer [train mode]");
    options.addOption("learningconstant", true,
        "learning constant [train mode]");
    options.addOption("iterations", true,
        "maximum iterations count [train mode]");
    options.addOption("help", false, "print this help");

    CommandLineParser parser = new PosixParser();

    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {
        printHelp(options);
        System.exit(0);
      }

      requiredParameter(line, "mode", options);
      // print the value of block-size
      String mode = line.getOptionValue("mode");

      if ("train".equals(mode)) {
        requiredParameter(line, "input", options);
        requiredParameter(line, "network", options);

        File inputFile = new File(line.getOptionValue("input"));
        File outputFile = new File(line.getOptionValue("network"));
        double thresholdError = Double.valueOf(line.getOptionValue("threshold",
            "0.3"));
        int hiddenLayersNumber = Integer.valueOf(line.getOptionValue("layers",
            "1"));
        int hiddenLayerNeurons = Integer.valueOf(line.getOptionValue("neurons",
            "7"));
        double learningConstant = Double.valueOf(line.getOptionValue(
            "learningconstant", "0.01"));
        int maxIterations = Integer.valueOf(line.getOptionValue("iterations",
            "10000"));
        Network network = new Network(thresholdError, outputFile,
            hiddenLayersNumber, hiddenLayerNeurons, learningConstant,
            maxIterations);
        InputManager mngr = new InputManagerImpl(inputFile);
        // InputManager mngr = new InputManagerXorImpl();
        // InputManager mngr = new InputManagerFunctionlImpl();
        network.setInputManager(mngr);
        network.learn();
      } else if ("race".equals(mode)) {
        requiredParameter(line, "scenario", options);

        String scenario = line.getOptionValue("scenario");
        Game game = new Game(scenario);
        RaceResult[] results = game.run();
        printResults(results);
      } else if ("test".equals(mode)) {
        requiredParameter(line, "input", options);
        requiredParameter(line, "network", options);

        File net = new File(line.getOptionValue("network"));
        File input = new File(line.getOptionValue("input"));
        Network network = Network.loadNetwork(net);
        InputManager mngr = new InputManagerImpl(input);
        network.setInputManager(mngr);
        network.testNet();
      } else if ("interactive".equals(mode)) {
        requiredParameter(line, "network", options);
        File net = new File(line.getOptionValue("network"));
        Network network = Network.loadNetwork(net);
        NeuralNetworkInput nni = new NeuralNetworkInput();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            System.in));
        System.out.println("Welcome to interactive mode :] For exit please insert any non-double-value.");
        while (true) {
          try {
            System.out.println("speed");
            nni.setSpeed(Double.parseDouble(reader.readLine()));
            // System.out.println("steeringWheel");
            // nni.setSteeringWheel(Double.parseDouble(reader.readLine()));
            System.out.println("wayPointDistance");
            nni.setWayPointDistance(Double.parseDouble(reader.readLine()));
            System.out.println("wayPointAngle");
            nni.setWayPointAngle(Double.parseDouble(reader.readLine()));
            // System.out.println("curveAngle");
            // nni.setCurveAngle(Double.parseDouble(reader.readLine()));
          } catch (NumberFormatException nfe) {
            System.out.println("Thanks for using interactive mode.");
            break;
          }
          network.runNetwork(nni, true);
        }
      }
    } catch (ParseException pe) {
      System.err.println("Command line parse exception: " + pe.getMessage());
    } catch (Exception e) {
      System.err.println("Fatal application exception: " + e.getMessage());
      e.printStackTrace();
    }

    System.exit(0);
  }
}
