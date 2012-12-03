package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.File;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.junit.Before;
import org.junit.Test;

public class GraphRun
{

	private IMocksControl	ctl;
	private CoverageResult	result;

	@Before
	public void setUpMock() throws Exception
	{
		ctl = EasyMock.createControl();
	}

	protected CoverageResult loadResults( String fileName ) throws Exception
	{
		InputStream in = getClass().getResourceAsStream( fileName );
		CoverageResult result = CoberturaCoverageParser.parse( in, null );
		return result;
	}

	@Test
	public void testGraph() throws Exception
	{
		result = new CoverageResultBuilder( ctl ).data().data().lotsofdata()
				.result( Ratio.create( 1, 10 ), Ratio.create( 1, 10 ), Ratio.create( 2, 10 ), Ratio.create( 3, 10 ), Ratio.create( 4, 10 ), Ratio.create( 5, 10 ) )
				.create();
		JFreeChart chart = new CoverageChart( result ).createChart();
		ChartUtilities.saveChartAsPNG( new File( "temp.png" ), chart, 500, 400 );
		ctl.verify();
	}

}
