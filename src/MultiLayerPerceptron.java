import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;

public class MultiLayerPerceptron {
    static final double[][] X = {{0,0},{0,1},{1,0},{1,1}};
    static final double[] Y = {0,1,1,0};

    static double activate(double z, String f) {
        if (f.equals("SIGMOID")) return 1.0/(1.0+Math.exp(-z));
        if (f.equals("TANH")) return Math.tanh(z);
        throw new IllegalArgumentException("Use SIGMOID or TANH");
    }
    static double deriv(double a, String f) {
        if (f.equals("SIGMOID")) return a*(1.0-a);
        return 1.0-a*a;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        String[] activations = {"SIGMOID", "TANH"};
        double[] rates = {0.01, 0.1, 0.5};
        int maxEpochs = 5000;

        try (PrintWriter out = new PrintWriter(new FileWriter("mlp_results.csv"))) {
            out.println("activation,learning_rate,epoch,mse");
            for (String activation : activations) {
                for (double lr : rates) {
                    double[][] w1 = {{0.12, -0.18}, {-0.22, 0.15}};
                    double[] b1 = {0.05, -0.03};
                    double[] w2 = {0.20, -0.25};
                    double b2 = 0.08;

                    for (int epoch=1; epoch<=maxEpochs; epoch++) {
                        double totalLoss = 0.0;
                        for (int i=0; i<X.length; i++) {
                            double[] h = new double[2];
                            for (int j=0; j<2; j++) {
                                double z = w1[j][0]*X[i][0] + w1[j][1]*X[i][1] + b1[j];
                                h[j] = activate(z, activation);
                            }
                            double zOut = w2[0]*h[0] + w2[1]*h[1] + b2;
                            double output = activate(zOut, activation);
                            double error = Y[i] - output;
                            totalLoss += error*error;

                            double deltaOut = error * deriv(output, activation);
                            double deltaH0 = deriv(h[0], activation) * w2[0] * deltaOut;
                            double deltaH1 = deriv(h[1], activation) * w2[1] * deltaOut;

                            w2[0] += lr * deltaOut * h[0];
                            w2[1] += lr * deltaOut * h[1];
                            b2 += lr * deltaOut;

                            w1[0][0] += lr * deltaH0 * X[i][0];
                            w1[0][1] += lr * deltaH0 * X[i][1];
                            b1[0] += lr * deltaH0;
                            w1[1][0] += lr * deltaH1 * X[i][0];
                            w1[1][1] += lr * deltaH1 * X[i][1];
                            b1[1] += lr * deltaH1;
                        }
                        out.printf("%s,%.2f,%d,%.8f%n", activation, lr, epoch, totalLoss/X.length);
                    }
                }
            }
        }
        System.out.println("Selesai. File dibuat: mlp_results.csv");
    }
}
