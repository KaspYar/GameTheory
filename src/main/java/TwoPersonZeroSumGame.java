import org.math.plot.Plot2DPanel;
import org.math.plot.Plot3DPanel;

import javax.swing.*;

public class TwoPersonZeroSumGame {
    private static final double EPSILON = 1E-8;

    private final int m;            // number of rows
    private final int n;            // number of columns
    private LinearProgramming lp;   // linear program solver
    private double constant;        // constant added to each entry in payoff matrix
    // (0 if all entries are strictly positive)

    /**
     * Determines an optimal solution to the two-sum zero-sum game
     * with the specified payoff matrix.
     *
     * @param  payoff the <em>m</em>-by-<em>n</em> payoff matrix
     */
    public TwoPersonZeroSumGame(double[][] payoff) {
        m = payoff.length;
        n = payoff[0].length;

        double[] c = new double[n];
        double[] b = new double[m];
        double[][] A = new double[m][n];
        for (int i = 0; i < m; i++)
            b[i] = 1.0;
        for (int j = 0; j < n; j++)
            c[j] = 1.0;

        // find smallest entry
        constant = Double.POSITIVE_INFINITY;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                if (payoff[i][j] < constant)
                    constant = payoff[i][j];

        // add constant  to every entry to make strictly positive
        if (constant <= 0) constant = -constant + 1;
        else               constant = 0;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                A[i][j] = payoff[i][j] + constant;

        lp = new LinearProgramming(A, b, c);

        assert certifySolution(payoff);

        if (m == 2){
            Plot2DPanel plot = new Plot2DPanel("SOUTH");

            for (int i = 0; i < n; i++) {
                double[] x = {0.0, payoff[0][i]};
                double[] y = {1.0, payoff[1][i]};

                plot.addLinePlot("Strategy "+ i, x, y);
            }


            // put the PlotPanel in a JFrame like a JPanel
            JFrame frame = new JFrame("a plot panel");
            frame.setSize(600, 600);
            frame.setContentPane(plot);
            frame.setVisible(true);
        }
        if (m == 3){
            Plot3DPanel plot = new Plot3DPanel("SOUTH");

            for (int i = 0; i < n; i++) {
                double[] x = {0.0, payoff[0][i]};
                double[] y = {1.0, payoff[1][i]};
                double[] z = {1.0, payoff[2][i]};

                plot.addLinePlot("Strategy "+ i, x, y, z);
            }


//            double [] y0
//            plot.addLinePlot("Zero Oy", )

            // put the PlotPanel in a JFrame like a JPanel
            JFrame frame = new JFrame("a plot panel");
            frame.setSize(600, 600);
            frame.setContentPane(plot);
            frame.setVisible(true);
        }
    }


    /**
     * Returns the optimal value of this two-person zero-sum game.
     *
     * @return the optimal value of this two-person zero-sum game
     *
     */
    public double value() {
        return 1.0 / scale() - constant;
    }


    // sum of x[j]
    private double scale() {
        double[] x = lp.primal();
        double sum = 0.0;
        for (int j = 0; j < n; j++)
            sum += x[j];
        return sum;
    }

    /**
     * Returns the optimal row strategy of this two-person zero-sum game.
     *
     * @return the optimal row strategy <em>x</em> of this two-person zero-sum game
     */
    public double[] row() {
        double scale = scale();
        double[] x = lp.primal();
        for (int j = 0; j < n; j++)
            x[j] /= scale;
        return x;
    }

    /**
     * Returns the optimal column strategy of this two-person zero-sum game.
     *
     * @return the optimal column strategy <em>y</em> of this two-person zero-sum game
     */
    public double[] column() {
        double scale = scale();
        double[] y = lp.dual();
        for (int i = 0; i < m; i++)
            y[i] /= scale;
        return y;
    }


    /**************************************************************************
     *
     *  The code below is solely for testing correctness of the data type.
     *
     **************************************************************************/

    // is the row vector x primal feasible?
    private boolean isPrimalFeasible() {
        double[] x = row();
        double sum = 0.0;
        for (int j = 0; j < n; j++) {
            if (x[j] < 0) {
                System.out.println("row vector not a probability distribution");
                System.out.printf("    x[%d] = %f\n", j, x[j]);
                return false;
            }
            sum += x[j];
        }
        if (Math.abs(sum - 1.0) > EPSILON) {
            System.out.println("row vector x[] is not a probability distribution");
            System.out.println("    sum = " + sum);
            return false;
        }
        return true;
    }

    // is the column vector y dual feasible?
    private boolean isDualFeasible() {
        double[] y = column();
        double sum = 0.0;
        for (int i = 0; i < m; i++) {
            if (y[i] < 0) {
                System.out.println("column vector y[] is not a probability distribution");
                System.out.printf("    y[%d] = %f\n", i, y[i]);
                return false;
            }
            sum += y[i];
        }
        if (Math.abs(sum - 1.0) > EPSILON) {
            System.out.println("column vector not a probability distribution");
            System.out.println("    sum = " + sum);
            return false;
        }
        return true;
    }

    // is the solution a Nash equilibrium?
    private boolean isNashEquilibrium(double[][] payoff) {
        double[] x = row();
        double[] y = column();
        double value = value();

        // given row player's mixed strategy, find column player's best pure strategy
        double opt1 = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += payoff[i][j] * x[j];
            }
            if (sum > opt1) opt1 = sum;
        }
        if (Math.abs(opt1 - value) > EPSILON) {
            System.out.println("Optimal value = " + value);
            System.out.println("Optimal best response for column player = " + opt1);
            return false;
        }

        // given column player's mixed strategy, find row player's best pure strategy
        double opt2 = Double.POSITIVE_INFINITY;
        for (int j = 0; j < n; j++) {
            double sum = 0.0;
            for (int i = 0; i < m; i++) {
                sum += payoff[i][j] * y[i];
            }
            if (sum < opt2) opt2 = sum;
        }
        if (Math.abs(opt2 - value) > EPSILON) {
            System.out.println("Optimal value = " + value);
            System.out.println("Optimal best response for row player = " + opt2);
            return false;
        }


        return true;
    }

    private boolean certifySolution(double[][] payoff) {
        return isPrimalFeasible() && isDualFeasible() && isNashEquilibrium(payoff);
    }


    private static void test(String description, double[][] payoff) {
        System.out.println();
        System.out.println(description);
        System.out.println("------------------------------------");
        int m = payoff.length;
        int n = payoff[0].length;
        TwoPersonZeroSumGame zerosum = new TwoPersonZeroSumGame(payoff);
        double[] x = zerosum.row();
        double[] y = zerosum.column();

        System.out.print("x[] = [");
        for (int j = 0; j < n-1; j++)
            System.out.printf("%8.4f, ", x[j]);
        System.out.printf("%8.4f]\n", x[n-1]);

        System.out.print("y[] = [");
        for (int i = 0; i < m-1; i++)
            System.out.printf("%8.4f, ", y[i]);
        System.out.printf("%8.4f]\n", y[m-1]);
        System.out.println("value =  " + zerosum.value());

    }

    private static void test() {
        double[][] payoff = {
                { -1,  1 , 3, -3},
                {  1, -1 , -2, 2}
        };
        test("bookExample", payoff);
    }


    public static void main(String[] args) {
        test();
    }

}