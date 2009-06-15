package neurocars.controllers;

import neurocars.entities.Car;
import neurocars.neuralNetwork.Network;
import neurocars.valueobj.NeuralNetworkInput;
import neurocars.valueobj.NeuralNetworkOutput;

/**
 * Ovladac vozidla prostrednictvim neuronove site
 * 
 * @author Lukas Holcik
 */
public class NeuroController extends Controller {

  private NeuralNetworkInput in;
  private NeuralNetworkOutput out;
  // private double maxSpeed;
  private Network net;
  private double threshold;

  public NeuroController(Network net, double threshold) {
    // net.setLearningMode(false);
    this.net = net;
    this.threshold = threshold;
  }

  public void next(Car car) {
    this.in = car.getNeuralNetworkInput();
    // System.out.println(in);
    System.out.println(out);
    // this.maxSpeed = car.getSetup().getMaxForwardSpeed();
    this.out = net.runNetwork(in);
  }

  public boolean accelerate() {
    return out.getSpeed() > threshold;
  }

  public boolean brake() {
    return out.getSpeed() < -threshold;
  }

  public boolean left() {
    return out.getTurn() < -threshold;
  }

  public boolean right() {
    return out.getTurn() > threshold;
  }

}
