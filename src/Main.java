import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int szer, wys;
        System.out.println("Podaj szerokość");
        szer = scanner.nextInt();
        System.out.println("Podaj wysokość");
        wys = scanner.nextInt();

        double[][] arr = new double[wys][szer];
        double[] temp = new double[4];
        for (int i = 0; i < 4; i++) {
            System.out.println("Podaj temperature nr. " + i);
            temp[i] = scanner.nextDouble();
        }

        for (int i = 1; i < arr[0].length-1; i++) {
            arr[0][i] = temp[0];
            arr[arr.length - 1][i] = temp[1];
        }

        for (int i = 1; i < arr.length-1; i++) {
            arr[i][0] = temp[2];
            arr[i][arr[0].length - 1] = temp[3];
        }


        ArrayList<Double> bVal = new ArrayList<>();
        for (int i = arr.length - 2; i > 0; i--) {
            for (int j = 1; j < arr[i].length - 1; j++) {
                double t = arr[i - 1][j] - 4 * arr[i][j] + arr[i + 1][j] + arr[i][j - 1] + arr[i][j + 1];
                bVal.add(0 - t);
            }
        }
        double[] b = bVal.stream().mapToDouble(aDouble -> aDouble).toArray();

        ArrayList<ArrayList<Double>> fin = new ArrayList<>();
        for (int i = arr.length - 2; i > 0; i--) {
            for (int j = 1; j < arr[i].length - 1; j++) {
                ArrayList<Double> ar = new ArrayList<>();
                if (arr[i - 1][j] == 0) arr[i - 1][j] = 1;
                if (arr[i + 1][j] == 0) arr[i + 1][j] = 1;
                if (arr[i][j - 1] == 0) arr[i][j - 1] = 1;
                if (arr[i][j + 1] == 0) arr[i][j + 1] = 1;
                arr[i][j] = -4;
                for (int k = arr.length - 2; k > 0; k--) {
                    for (int s = 1; s < arr[k].length - 1; s++) {
                        ar.add(arr[k][s]);
                        arr[k][s] = 0;
                    }
                }
                fin.add(ar);
            }
        }

        double[][] ei = new double[fin.size()][];
        IntStream.range(0, fin.size()).forEach(i -> {
            ArrayList<Double> row = fin.get(i);
            double[] copy = row.stream().mapToDouble(aDouble -> aDouble).toArray();
            ei[i] = copy;
        });

        double[][] eiInverted = invert(ei);
        System.out.println();
        double[] d = multiplyMatrices(b, eiInverted);
        int k = 0;
        for (int i = arr.length - 2; i > 0; i--)
        {
            for (int j = 1; j < arr[i].length - 1; j++) arr[i][j] = d[k++];
        }
        show(arr);

    }


    private static void show(double[][] arr){
        Color[][] colors = convert(arr);
        JFrame frame = new JFrame();
        GridLayout gridLayout = new GridLayout(arr.length, arr[0].length);
        JPanel panel = new JPanel();
        panel.setLayout(gridLayout);
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                JLabel jLabel = new JLabel();
                jLabel.setText(String.valueOf(arr[i][j]));
                jLabel.setOpaque(true);
                jLabel.setBackground(colors[i][j]);

                panel.add(jLabel,i, j);
            }
        }
        frame.add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public static Color[][] convert(double[][] temperatures) {
        int rows = temperatures.length;
        int cols = temperatures[0].length;
        Color[][] colors = new Color[rows][cols];

        double[] flatTemperatures = Arrays.stream(temperatures).flatMapToDouble(Arrays::stream).toArray();
        Arrays.sort(flatTemperatures);
        double minTemp = flatTemperatures[0];
        double maxTemp = flatTemperatures[flatTemperatures.length - 1];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double temperature = temperatures[i][j];
                float hue = (float) ((temperature - minTemp) / (maxTemp - minTemp));
                colors[i][j] = Color.getHSBColor(hue, 1f, 1f);
            }
        }

        return colors;
    }


    private static double[] multiplyMatrices(double[] firstMatrix, double[][] secondMatrix) {
        double[] product = new double[firstMatrix.length];
        for (int i = 0; i < firstMatrix.length; i++)
            for (int j = 0; j < secondMatrix.length; j++)
                product[i] += (firstMatrix[j] * secondMatrix[j][i]);
        return product;
    }

    private static double[][] invert(double a[][]) {
        int n = a.length;
        double[][] x = new double[n][n];
        double[][] b = new double[n][n];
        int[] index = new int[n];
        for (int i = 0; i < n; ++i)
            b[i][i] = 1;

        gaussian(a, index);

        for (int i = 0; i < n - 1; ++i)
            for (int j = i + 1; j < n; ++j)
                for (int k = 0; k < n; ++k)
                    b[index[j]][k] -= a[index[j]][i] * b[index[i]][k];

        for (int i = 0; i < n; ++i) {
            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) x[j][i] -= a[index[j]][k] * x[k][i];
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }
    private static void gaussian(double[][] a, int[] index) {
        int n = index.length;
        double[] c = new double[n];

        IntStream.range(0, n).forEach(i -> index[i] = i);

        for (int i = 0; i < n; ++i) {
            double c1 = 0;
            for (int j = 0; j < n; ++j) {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }

        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            double pi1 = 0;
            for (int i = j; i < n; ++i) {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }

            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                double pj = a[index[i]][j] / a[index[j]][j];
                a[index[i]][j] = pj;
                for (int l = j + 1; l < n; ++l)
                    a[index[i]][l] -= pj * a[index[j]][l];
            }
        }
    }
}
