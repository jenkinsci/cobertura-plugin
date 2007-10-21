package hudson.plugins.cobertura.targets;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: stephen
 * Date: 21-Oct-2007
 * Time: 18:01:31
 * To change this template use File | Settings | File Templates.
 */
public class CoveragePaintTest extends TestCase {

    public CoveragePaintTest(String string) {
        super(string);
    }

    public void testSerializable() throws Exception {
        CoveragePaint instance = new CoveragePaint(CoverageElement.JAVA_FILE);
        instance.paint(5, 7, 4, 5);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(instance);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        CoveragePaint copy = (CoveragePaint) ois.readObject();
        assertEquals(instance.getLineCoverage(), copy.getLineCoverage());
        assertEquals(instance.getConditionalCoverage(), copy.getConditionalCoverage());
    }
}
