package hudson.plugins.cobertura;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Represents <tt>x/y</tt> where x={@link #numerator} and y={@link #denominator}.
 *
 * @author Kohsuke Kawaguchi
 */
final public class Ratio implements Serializable {
    public final float numerator;
    public final float denominator;

    private Ratio(float numerator, float denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    /**
     * Gets "x/y" representation.
     */
    public String toString() {
        return print(numerator) + "/" + print(denominator);
    }

    private String print(float f) {
        int i = (int) f;
        if (i == f)
            return String.valueOf(i);
        else
            return String.valueOf(f);
    }

    /**
     * Gets the percentage in integer.
     */
    public int getPercentage() {
        return Math.round(getPercentageFloat());
    }

    /**
     * Gets the percentage in float.
     * For exceptional cases of 0/0, return 100% as it corresponds to expected ammout.
     * For error cases of x/0, return 0% as x is unexpected ammout.
     */
    public float getPercentageFloat() {
        return denominator == 0 ? (numerator == 0 ? 100.0f : 0.0f) : (100 * numerator / denominator);
    }
    
    static NumberFormat dataFormat = new DecimalFormat("000.00");

    /**
     * Gets the percentage as a formated string used for sorting the html table
     */
    public String getPercentageString() {
      return dataFormat.format(getPercentageFloat());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ratio ratio = (Ratio) o;

        return Float.compare(ratio.denominator, denominator) == 0
                && Float.compare(ratio.numerator, numerator) == 0;

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result;
        result = numerator != +0.0f ? Float.floatToIntBits(numerator) : 0;
        result = 31 * result + denominator != +0.0f ? Float.floatToIntBits(denominator) : 0;
        return result;
    }

    private static final long serialVersionUID = 1L;

//
// fly-weight patterns for common Ratio instances (x/y) where x<y
    // and x,y are integers.
    //
    private static final Ratio[] COMMON_INSTANCES = new Ratio[256];

    /**
     * Creates a new instance of {@link Ratio}.
     */
    public static Ratio create(float x, float y) {
        int xx = (int) x;
        int yy = (int) y;

        if (xx == x && yy == y) {
            int idx = yy * (yy + 1) / 2 + xx;
            if (0 <= idx && idx < COMMON_INSTANCES.length) {
                Ratio r = COMMON_INSTANCES[idx];
                if (r == null)
                    COMMON_INSTANCES[idx] = r = new Ratio(x, y);
                return r;
            }
        }

        return new Ratio(x, y);
    }
}
