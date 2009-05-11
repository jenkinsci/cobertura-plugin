package hudson.plugins.cobertura.targets;

import hudson.model.AbstractBuild;
import hudson.util.TextFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A CoverageResult that also contains the data needed to support source
 * file painting.
 * 
 * @author davidmc24
 * @since 28-Apr-2009 (extracted from CoverageResult)
 */
public class PaintedCoverageResult extends CoverageResult {
    private final CoveragePaint paint;
    private String relativeSourcePath;
    
    public PaintedCoverageResult(CoverageElement elementType, PaintedCoverageResult parent, String name) {
        super(elementType, parent, name);
        this.paint = CoveragePaintRule.makePaint(elementType);
        this.relativeSourcePath = null;
    }
    
    /**
     * Getter for property 'relativeSourcePath'.
     *
     * @return Value for property 'relativeSourcePath'.
     */
    public String getRelativeSourcePath() {
        return relativeSourcePath;
    }
    
    /**
     * Setter for property 'relativeSourcePath'.
     *
     * @param relativeSourcePath Value to set for property 'relativeSourcePath'.
     */
    public void setRelativeSourcePath(String relativeSourcePath) {
        this.relativeSourcePath = relativeSourcePath;
    }
    
    /**
     * Getter for property 'sourceCodeLevel'.
     *
     * @return Value for property 'sourceCodeLevel'.
     */
    public boolean isSourceCodeLevel() {
        return relativeSourcePath != null;
    }
    
    /**
     * Getter for property 'paint'.
     *
     * @return Value for property 'paint'.
     */
    public CoveragePaint getPaint() {
        return paint;
    }

    public void paint(int line, int hits) {
        if (paint != null) {
            paint.paint(line, hits);
        }
    }

    public void paint(int line, int hits, int branchHits, int branchTotal) {
        if (paint != null) {
            paint.paint(line, hits, branchHits, branchTotal);
        }
    }
    
    /**
     * gets the file corresponding to the source file.
     *
     * @return The file where the source file should be (if it exists)
     */
    private File getSourceFile() {
        return new File(owner.getProject().getRootDir(), "cobertura/" + relativeSourcePath);
    }
    
    /**
     * Getter for property 'sourceFileAvailable'.
     *
     * @return Value for property 'sourceFileAvailable'.
     */
    public boolean isSourceFileAvailable() {
        return owner == owner.getProject().getLastStableBuild() && getSourceFile().exists();
    }
    
    /**
     * Getter for property 'sourceFileContent'.
     *
     * @return Value for property 'sourceFileContent'.
     */
    public String getSourceFileContent() {
        try {
            return new TextFile(getSourceFile()).read();
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * Setter for property 'owner'.
     *
     * @param owner Value to set for property 'owner'.
     */
    @Override
    public void setOwner(AbstractBuild owner) {
        super.setOwner(owner);
        for (CoverageResult child : children.values()) {
            assert child != null;
            assert child instanceof PaintedCoverageResult : child.getClass().getName();
            PaintedCoverageResult paintedChild = (PaintedCoverageResult) child;
            if (paint != null && paintedChild.paint != null && CoveragePaintRule.propagatePaintToParent(child.getElement())) {
                paint.add(paintedChild.paint);
            }
        }
        // now inject any results from CoveragePaint as they should be most accurate.
        if (paint != null) {
            aggregateResults.putAll(paint.getResults());
        }
    }
    
    public void doCoverageHighlightedSource(StaplerRequest req, StaplerResponse rsp) throws IOException {
        // TODO
    }
    
    /**
     * Getter for property 'paintedSources'.
     *
     * @return Value for property 'paintedSources'.
     */
    public Map<String, CoveragePaint> getPaintedSources() {
        Map<String, CoveragePaint> result = new HashMap<String, CoveragePaint>();
        // check the children
        for (CoverageResult child : children.values()) {
            assert child != null;
            assert child instanceof PaintedCoverageResult : child.getClass().getName();
            PaintedCoverageResult paintedChild = (PaintedCoverageResult) child;
            result.putAll(paintedChild.getPaintedSources());
        }
        if (relativeSourcePath != null && paint != null) {
            result.put(relativeSourcePath, paint);
        }
        return result;
    }
}
