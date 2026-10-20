import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class NeuralNetworkGUI extends JFrame {
    private final JComboBox<String> modelBox = new JComboBox<>(new String[]{"Single Perceptron", "Multilayer Perceptron (MLP)"});
    private final JComboBox<String> activationBox = new JComboBox<>(new String[]{"STEP", "SIGMOID", "TANH"});
    private final JComboBox<String> learningRateBox = new JComboBox<>(new String[]{"0.01", "0.10", "0.50"});
    private final JLabel epochsLabel = new JLabel("Epoch: 500");
    private final JLabel statusLabel = new JLabel("Pilih parameter, lalu klik Jalankan Eksperimen.");
    private final JLabel finalMseLabel = new JLabel("MSE akhir: -");
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Epoch", "MSE"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable resultTable = new JTable(tableModel);
    private final MseChart chart = new MseChart();
    private List<Double> history = new ArrayList<>();
    private JButton runButton;

    public NeuralNetworkGUI() {
        super("Neural Network Experiment Lab");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1080, 720);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(23, 54, 93));
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Neural Network Experiment Lab");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Eksperimen Single Perceptron dan Multilayer Perceptron");
        subtitle.setForeground(new Color(225, 234, 245));
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        header.add(titles, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        controls.setBorder(BorderFactory.createTitledBorder("Parameter Eksperimen"));
        controls.add(new JLabel("Model:"));
        controls.add(modelBox);
        controls.add(new JLabel("Aktivasi:"));
        controls.add(activationBox);
        controls.add(new JLabel("Learning rate:"));
        controls.add(learningRateBox);
        runButton = new JButton("Jalankan Eksperimen");
        JButton exportButton = new JButton("Ekspor CSV");
        controls.add(runButton);
        controls.add(exportButton);

        chart.setBorder(BorderFactory.createTitledBorder("Grafik MSE per Epoch"));
        resultTable.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Riwayat Hasil"));
        tableScroll.setPreferredSize(new Dimension(280, 400));

        JPanel right = new JPanel(new BorderLayout());
        right.add(tableScroll, BorderLayout.CENTER);
        right.add(finalMseLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        center.add(chart, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 12, 10, 12));
        bottom.add(epochsLabel, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(controls, BorderLayout.PAGE_START);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        modelBox.addActionListener(e -> updateModelOptions());
        runButton.addActionListener(e -> runExperiment());
        exportButton.addActionListener(e -> exportCsv());
        updateModelOptions();
    }

    private void updateModelOptions() {
        boolean single = modelBox.getSelectedIndex() == 0;
        String current = (String) activationBox.getSelectedItem();
        activationBox.removeAllItems();
        if (single) {
            activationBox.addItem("STEP");
            activationBox.addItem("SIGMOID");
            activationBox.addItem("TANH");
        } else {
            activationBox.addItem("SIGMOID");
            activationBox.addItem("TANH");
        }
        if (current != null) activationBox.setSelectedItem(current);
        if (activationBox.getSelectedIndex() < 0) activationBox.setSelectedIndex(0);
        epochsLabel.setText("Epoch: " + (single ? "500" : "5.000"));
    }

    private void runExperiment() {
        final boolean single = modelBox.getSelectedIndex() == 0;
        final String activation = (String) activationBox.getSelectedItem();
        final double lr = Double.parseDouble((String) learningRateBox.getSelectedItem());
        final int epochs = single ? 500 : 5000;
        runButton.setEnabled(false);
        statusLabel.setText("Eksperimen sedang berjalan...");
        tableModel.setRowCount(0);
        history = new ArrayList<>();
        chart.setData(history);
        finalMseLabel.setText("MSE akhir: -");

        new SwingWorker<List<Double>, Integer>() {
            protected List<Double> doInBackground() {
                return single ? trainSingle(activation, lr, epochs) : trainMlp(activation, lr, epochs);
            }
            protected void done() {
                try {
                    history = get();
                    for (int i = 0; i < history.size(); i++) {
                        tableModel.addRow(new Object[]{i + 1, String.format(Locale.US, "%.8f", history.get(i))});
                    }
                    chart.setData(history);
                    finalMseLabel.setText(String.format(Locale.US, "MSE akhir: %.8f", history.get(history.size()-1)));
                    statusLabel.setText("Eksperimen selesai. " + history.size() + " epoch tercatat.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NeuralNetworkGUI.this, "Eksperimen gagal: " + ex.getMessage(),
                            "Kesalahan", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Eksperimen gagal.");
                } finally {
                    runButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private List<Double> trainSingle(String activation, double lr, int epochs) {
        double[][] x = {{0,0},{0,1},{1,0},{1,1}};
        double[] y = {0,0,0,1};
        double[] w = {0.15, -0.20};
        double b = -0.10;
        List<Double> mseHistory = new ArrayList<>();
        for (int epoch = 1; epoch <= epochs; epoch++) {
            double total = 0;
            for (int i = 0; i < x.length; i++) {
                double z = w[0]*x[i][0] + w[1]*x[i][1] + b;
                double a = singleActivate(z, activation);
                double error = y[i] - a;
                total += error*error;
                if ("STEP".equals(activation)) {
                    w[0] += lr*error*x[i][0];
                    w[1] += lr*error*x[i][1];
                    b += lr*error;
                } else {
                    double derivative = "SIGMOID".equals(activation) ? a*(1-a) : 1-a*a;
                    double delta = error*derivative;
                    w[0] += lr*delta*x[i][0];
                    w[1] += lr*delta*x[i][1];
                    b += lr*delta;
                }
            }
            mseHistory.add(total / x.length);
        }
        return mseHistory;
    }

    private double singleActivate(double z, String f) {
        if ("STEP".equals(f)) return z > 0 ? 1.0 : 0.0;
        if ("SIGMOID".equals(f)) return 1.0/(1.0+Math.exp(-z));
        return Math.tanh(z);
    }

    private List<Double> trainMlp(String activation, double lr, int epochs) {
        double[][] x = {{0,0},{0,1},{1,0},{1,1}};
        double[] y = {0,1,1,0};
        double[][] w1 = {{0.12, -0.18}, {-0.22, 0.15}};
        double[] b1 = {0.05, -0.03};
        double[] w2 = {0.20, -0.25};
        double b2 = 0.08;
        List<Double> mseHistory = new ArrayList<>();
        for (int epoch=1; epoch<=epochs; epoch++) {
            double total = 0;
            for (int i=0; i<x.length; i++) {
                double[] h = new double[2];
                for (int j=0; j<2; j++) h[j] = mlpActivate(w1[j][0]*x[i][0] + w1[j][1]*x[i][1] + b1[j], activation);
                double output = mlpActivate(w2[0]*h[0] + w2[1]*h[1] + b2, activation);
                double error = y[i] - output;
                total += error*error;
                double deltaOut = error * mlpDerivative(output, activation);
                double deltaH0 = mlpDerivative(h[0], activation) * w2[0] * deltaOut;
                double deltaH1 = mlpDerivative(h[1], activation) * w2[1] * deltaOut;
                w2[0] += lr*deltaOut*h[0];
                w2[1] += lr*deltaOut*h[1];
                b2 += lr*deltaOut;
                w1[0][0] += lr*deltaH0*x[i][0];
                w1[0][1] += lr*deltaH0*x[i][1];
                b1[0] += lr*deltaH0;
                w1[1][0] += lr*deltaH1*x[i][0];
                w1[1][1] += lr*deltaH1*x[i][1];
                b1[1] += lr*deltaH1;
            }
            mseHistory.add(total/x.length);
        }
        return mseHistory;
    }

    private double mlpActivate(double z, String f) {
        return "SIGMOID".equals(f) ? 1.0/(1.0+Math.exp(-z)) : Math.tanh(z);
    }
    private double mlpDerivative(double a, String f) {
        return "SIGMOID".equals(f) ? a*(1-a) : 1-a*a;
    }

    private void exportCsv() {
        if (history == null || history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jalankan eksperimen terlebih dahulu.", "Belum ada data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan hasil eksperimen sebagai CSV");
        chooser.setSelectedFile(new File("hasil_eksperimen.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String model = modelBox.getSelectedIndex() == 0 ? "Single Perceptron" : "MLP";
        String activation = (String) activationBox.getSelectedItem();
        String lr = (String) learningRateBox.getSelectedItem();
        try (PrintWriter out = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            out.println("model,activation,learning_rate,epoch,mse");
            for (int i=0; i<history.size(); i++) {
                out.printf(Locale.US, "\"%s\",%s,%s,%d,%.8f%n", model, activation, lr, i+1, history.get(i));
            }
            statusLabel.setText("CSV berhasil disimpan: " + chooser.getSelectedFile().getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan CSV: " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new NeuralNetworkGUI().setVisible(true));
    }

    private static class MseChart extends JPanel {
        private List<Double> values = new ArrayList<>();
        MseChart() { setPreferredSize(new Dimension(600, 400)); setBackground(Color.WHITE); }
        void setData(List<Double> data) {
            values = new ArrayList<>(data);
            repaint();
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left=65, right=getWidth()-25, top=30, bottom=getHeight()-55;
            g2.setColor(new Color(80,80,80));
            g2.drawLine(left, top, left, bottom);
            g2.drawLine(left, bottom, right, bottom);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("MSE", 20, top+5);
            g2.drawString("Epoch", (left+right)/2, getHeight()-15);
            if (values.isEmpty() || right <= left || bottom <= top) {
                g2.drawString("Grafik akan muncul setelah eksperimen dijalankan.", left+20, (top+bottom)/2);
                g2.dispose();
                return;
            }
            double max = 0;
            for (double v : values) if (Double.isFinite(v) && v > max) max = v;
            if (max <= 0) max = 1;
            max *= 1.05;
            g2.setColor(new Color(220,220,220));
            for (int i=0; i<=5; i++) {
                int y = bottom - (bottom-top)*i/5;
                g2.drawLine(left, y, right, y);
                g2.setColor(new Color(70,70,70));
                g2.drawString(String.format(Locale.US, "%.3f", max*i/5), 20, y+4);
                g2.setColor(new Color(220,220,220));
            }
            g2.setColor(new Color(40,105,180));
            g2.setStroke(new BasicStroke(2f));
            int previousX = left, previousY = bottom;
            for (int i=0; i<values.size(); i++) {
                int x = left + (int)((right-left)*(i/(double)Math.max(1, values.size()-1)));
                int y = bottom - (int)((bottom-top)*(values.get(i)/max));
                if (i > 0) g2.drawLine(previousX, previousY, x, y);
                previousX=x; previousY=y;
            }
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("Epoch 1", left, bottom+20);
            g2.drawString("Epoch " + values.size(), Math.max(left, right-75), bottom+20);
            g2.dispose();
        }
    }
}
