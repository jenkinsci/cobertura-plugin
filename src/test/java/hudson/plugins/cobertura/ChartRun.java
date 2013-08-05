package hudson.plugins.cobertura;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.junit.Before;

public class ChartRun extends ChartTest
{
	@Before
	public void setUp() {
		ChartTest.TEMP_IMAGE_FOLDER.mkdirs();
	}

	protected void complete( CoverageChart chartData, String filename ) throws IOException
	{
		JFreeChart chart = chartData.createChart();
		ChartUtilities.saveChartAsPNG( new File( filename ), chart, 500, 200 );
		super.complete( chartData, filename );
	}
}
