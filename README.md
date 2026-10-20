# Neural Network Assignment GUI

A Java-based academic project for exploring Single Perceptron and Multilayer Perceptron (MLP) learning through a graphical user interface.

## Project Overview

This project combines neural network implementations with a GUI to make experimentation and result inspection more accessible.

## Implementations

- **NeuralNetworkGUI.java** — Main graphical user interface for interacting with the neural network application.
- **SinglePerceptron.java** — Implementation for experimenting with single-layer perceptron learning.
- **MultiLayerPerceptron.java** — Implementation for experimenting with multilayer perceptron learning.

## Technologies

- Java
- Java Swing, if used by the GUI implementation
- Eclipse IDE
- CSV experiment data

## Project Structure

```text
NeuralNetworkAssignment_GUI/
├── src/
│   ├── NeuralNetworkGUI.java
│   ├── SinglePerceptron.java
│   └── MultiLayerPerceptron.java
├── mlp_results.csv
├── .classpath
├── .project
├── .settings/
├── .gitignore
└── README.md
```

## Experiment Results

- `mlp_results.csv` — Experiment results for further analysis and visualization.

The CSV file can be opened in Excel or another compatible spreadsheet application.

## Getting Started

1. Open Eclipse.
2. Import the project using **File → Import → Existing Projects into Workspace**.
3. Select the `NeuralNetworkAssignment_GUI` directory.
4. Verify that the required JDK is configured.
5. Open `NeuralNetworkGUI.java`.
6. Run it as a Java Application if the class contains a `main` method.

## Limitations

This project is intended for academic experimentation. Results depend on the implementation, data, and experiment settings. Review the source code to confirm the supported parameters and GUI functionality.

## Author

Yoga Arfiansyah Firdaus

Master of Information Systems  
Telkom University

---

For educational purposes only.
