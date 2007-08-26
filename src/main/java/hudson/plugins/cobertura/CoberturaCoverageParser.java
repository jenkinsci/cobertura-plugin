package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageResult;
import hudson.plugins.cobertura.targets.CoverageElement;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since 03-Jul-2007 09:03:30
 */
public class CoberturaCoverageParser {

    /**
     * Do not instantiate CoberturaCoverageParser.
     */
    private CoberturaCoverageParser() {
    }

    public static CoverageResult parse(File inFile, String pathPrefix) throws IOException {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(inFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            CoberturaCoverageParser parser = new CoberturaCoverageParser();
            return parse(bufferedInputStream);
        } finally {
            try {
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static CoverageResult parse(InputStream in) throws IOException {
        if (in == null) throw new NullPointerException();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        try {
            SAXParser parser = factory.newSAXParser();
            CoberturaXmlHandler handler = new CoberturaXmlHandler();
            parser.parse(in, handler);
            return handler.getRootCoverage();
        } catch (ParserConfigurationException e) {
            throw new IOException("Cannot parse coverage results", e);
        } catch (SAXException e) {
            throw new IOException("Cannot parse coverage results", e);
        }
    }
}

class CoberturaXmlHandler extends DefaultHandler {
    private CoverageResult rootCoverage = null;
    private Stack<CoberturaXmlHandlerStackItem> stack = new Stack<CoberturaXmlHandlerStackItem>();

    public void startDocument() throws SAXException {
        super.startDocument();
        rootCoverage = new CoverageResult(CoverageElement.PROJECT, null, "Cobertura Coverage Report");
        stack.clear();
    }

    public void endDocument() throws SAXException {
        if (!stack.empty()) {
            throw new SAXException("Unbalanced parse of cobertua coverage results.");
        }
        super.endDocument();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if ("coverage".equals(qName)) {
            assert stack.empty();
            stack.push(new CoberturaXmlHandlerStackItem(rootCoverage));
        } else if ("package".equals(qName)) {
            assert !stack.empty();
        } else if ("class".equals(qName)) {
            assert !stack.empty();
        } else if ("method".equals(qName)) {
            assert !stack.empty();
        } else if ("line".equals(qName)) {
            assert !stack.empty();
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("coverage".equals(qName)) {
            assert !stack.empty();
        } else if ("package".equals(qName)) {
            assert !stack.empty();
            CoberturaXmlHandlerStackItem popped = stack.pop();
            CoberturaXmlHandlerStackItem peek = stack.peek();
            peek.getTotals().addTotal(popped.getTotals());
        } else if ("class".equals(qName)) {
            assert !stack.empty();
            CoberturaXmlHandlerStackItem popped = stack.pop();
            CoberturaXmlHandlerStackItem peek = stack.peek();
            peek.getTotals().addTotal(popped.getTotals());
        }
        super.endElement(uri, localName, qName);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CoverageResult getRootCoverage() {
        return rootCoverage;
    }
}

class CoberturaXmlHandlerStackItem {
    private CoverageResult metric;
    private CoberturaCoverageTotals totals;


    public CoberturaXmlHandlerStackItem(CoverageResult metric) {
        this.metric = metric;
        totals = new CoberturaCoverageTotals();
    }

    public CoverageResult getMetric() {
        return metric;
    }

    public CoberturaCoverageTotals getTotals() {
        return totals;
    }
}

class CoberturaCoverageTotals {
    private long totalLineCount;
    private long coverLineCount;
    private long totalConditionCount;
    private long coverConditionCount;
    private long totalMethodCount;
    private long coverMethodCount;

    public CoberturaCoverageTotals() {
        totalLineCount = 0;
        coverLineCount = 0;
        totalConditionCount = 0;
        coverConditionCount = 0;
        totalMethodCount = 0;
        coverMethodCount = 0;
    }

    public void addLine(boolean covered) {
        totalLineCount++;
        if (covered) {
            coverLineCount++;
        }
    }

    public void addMethod(boolean covered) {
        totalMethodCount++;
        if (covered) {
            coverMethodCount++;
        }
    }

    public void addLine(boolean covered, int condCoverCount, int condTotalCount) {
        addLine(covered);
        totalConditionCount += condTotalCount;
        coverConditionCount += condCoverCount;
    }

    public void addTotal(CoberturaCoverageTotals sub) {
        totalLineCount += sub.totalLineCount;
        coverLineCount += sub.coverLineCount;
        totalConditionCount += sub.totalConditionCount;
        coverConditionCount += sub.coverConditionCount;
        totalMethodCount += sub.totalMethodCount;
        coverMethodCount += sub.coverMethodCount;
    }

    public Ratio getLineCoverage() {
        return Ratio.create(coverLineCount, totalLineCount);
    }

    public Ratio getConditionalCoverage() {
        return Ratio.create(coverLineCount, totalLineCount);
    }

    public Ratio getMethodCoverage() {
        return Ratio.create(coverMethodCount, totalMethodCount);
    }
}