package hudson.plugins.cobertura.targets;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import hudson.plugins.cobertura.Ratio;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Line-by-line coverage information.
 *
 * @author Stephen Connolly
 * @since 29-Aug-2007 17:44:29
 */
public class CoveragePaint implements Serializable {
	
	/**
	 * Generated
	 */
	private static final long serialVersionUID = -6265259191856193735L;

    private static final CoveragePaintDetails[] EMPTY = new CoveragePaintDetails[0];

	private static class CoveragePaintDetails implements Serializable {
		/**
		 * Generated
		 */
		private static final long serialVersionUID = -9097537016381444671L;

		final int hitCount;
        final int branchCount;
        final int branchCoverage;

		CoveragePaintDetails(int hitCount, int branchCount, int branchCoverage) {
			this.hitCount = hitCount;
			this.branchCount = branchCount;
			this.branchCoverage = branchCoverage;
		}

        CoveragePaintDetails add(CoveragePaintDetails that) {
            return new CoveragePaintDetails(
                    this.hitCount+that.hitCount,
                    // TODO find a better algorithm
                    Math.max(this.branchCount,that.branchCount),
                    Math.max(this.branchCoverage,that.branchCoverage)
            );
        }
	}

	protected TIntObjectMap<CoveragePaintDetails> lines=new TIntObjectHashMap<CoveragePaintDetails>();
	
	public CoveragePaint(CoverageElement source) {
//		there were no getters against the source ...
//      this.source = source;
    }
    
    private void paint(int line, CoveragePaintDetails delta) {
        CoveragePaintDetails d = lines.get(line);
        if (d == null) {
            lines.put(line, delta);
        } else {
            lines.put(line, d.add(delta));
        }
    }

    public void paint(int line, int hits) {
        paint(line, new CoveragePaintDetails(hits, 0, 0));
    }

    public void paint(int line, int hits, int branchCover, int branchCount) {
        paint(line,new CoveragePaintDetails(hits, branchCount, branchCover));
    }

    public void add(CoveragePaint child) {
        TIntObjectIterator<CoveragePaintDetails> it = child.lines.iterator();
        while (it.hasNext()) {
            it.advance();
            paint(it.key(),it.value());
    	}
    }

    /**
     * Getter for property 'lineCoverage'.
     *
     * @return Value for property 'lineCoverage'.
     */
    public Ratio getLineCoverage() {
        int covered = 0;
        for (CoveragePaintDetails d: lines.values(EMPTY)) {
            if (d.hitCount > 0) {
                covered++;
            }        		
        }
        return Ratio.create(covered, lines.size());
    }

    /**
     * Getter for property 'conditionalCoverage'.
     *
     * @return Value for property 'conditionalCoverage'.
     */
    public Ratio getConditionalCoverage() {
        long maxTotal = 0;
        long total = 0;
        for (CoveragePaintDetails d: lines.values(EMPTY)) {
            maxTotal += d.branchCount;
            total += d.branchCoverage;        		
        }
        return Ratio.create(total, maxTotal);
    }

    /**
     * Getter for property 'results'.
     *
     * @return Value for property 'results'.
     */
    public Map<CoverageMetric, Ratio> getResults() {
        Map<CoverageMetric, Ratio> result = new EnumMap<CoverageMetric,Ratio>(CoverageMetric.class);
        result.put(CoverageMetric.LINE, getLineCoverage());
        result.put(CoverageMetric.CONDITIONAL, getConditionalCoverage());
        return result;
    }

    public boolean isPainted(int line) {
    	return lines.get(line) != null;
    }

    public int getHits(int line) {
		CoveragePaintDetails d=lines.get(line);
		if (d==null){
			return 0;
		} else {
            return d.hitCount;
		}
    }

    public int getBranchTotal(int line) {
		CoveragePaintDetails d=lines.get(line);
		if (d==null){
			return 0;
		} else {
            return d.branchCount;
		}
    }

    public int getBranchCoverage(int line) {
		CoveragePaintDetails d=lines.get(line);
		if (d==null){
			return 0;
		} else {
            return d.branchCoverage;    			
		}
    }
}
