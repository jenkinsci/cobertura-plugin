package hudson.plugins.cobertura;

import junit.framework.TestCase;

/**
 * JUnit test for {@link Ratio}
 */
public class RatioTest extends TestCase {

    final void assertRatio(Ratio r, float numerator, float denominator) {
        assertEquals(numerator, r.numerator);
        assertEquals(denominator, r.denominator);
    }

    /**
     * Tests that {@link Ratio#parseValue(String)} parses correctly float
     * numbers with either dot or comma as decimal point.
     *
     * @throws Exception
     */
    public void testParseValue() throws Exception {
        assertRatio(Ratio.create(1, 2), 1.0f, 2.0f);
    }

    public void testGetPercentage() {
        assertEquals(98, Ratio.create(246, 250).getPercentage());
        assertEquals(99, Ratio.create(247, 250).getPercentage());
        assertEquals(99, Ratio.create(248, 250).getPercentage());
        assertEquals(99, Ratio.create(249, 250).getPercentage());
        assertEquals(100, Ratio.create(250, 250).getPercentage());
    }

    public void testGetPercentageFloat() {
        assertEquals("099.99", Ratio.create(24998, 25000).getPercentageString());
        assertEquals("099.996", Ratio.create(24999, 25000).getPercentageString());
        assertEquals("100.000", Ratio.create(25000, 25000).getPercentageString());
    }
}