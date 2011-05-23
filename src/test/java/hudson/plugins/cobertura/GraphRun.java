package hudson.plugins.cobertura;

import hudson.model.FreeStyleBuild;
import hudson.model.HealthReport;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.File;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.localizer.Localizable;

public class GraphRun
{

	protected static final String	FILE_COVERAGE_DATA	= "coverage-with-lots-of-data.xml";
	private IMocksControl			ctl;
	private FreeStyleBuild			build;
	private FreeStyleBuild			prevBuild;
	private FreeStyleBuild			prevPrevBuild;
	private CoverageResult			result;
	private CoverageResult			prevPrevResult;
	private CoverageResult			prevResult;

	@Before
	public void setUpMock() throws Exception
	{

		ctl = EasyMock.createControl();
		prevPrevBuild = ctl.createMock( "prevPrevBuild", FreeStyleBuild.class );
		prevBuild = ctl.createMock( "prevBuild", FreeStyleBuild.class );
		build = ctl.createMock( "build", FreeStyleBuild.class );

		prevPrevResult = loadResults( FILE_COVERAGE_DATA );
		prevResult = loadResults( FILE_COVERAGE_DATA );
		result = loadResults( FILE_COVERAGE_DATA );

		CoberturaBuildAction prevAction = new CoberturaBuildAction( prevBuild, prevResult, new CoverageTarget(), new CoverageTarget(), true, false, false, false,
				false )
		{
			@Override
			public HealthReport getBuildHealth()
			{

				return new HealthReport( 100, (Localizable) null );
			}
		};

		CoberturaBuildAction prevPrevAction = new CoberturaBuildAction( prevPrevBuild, prevPrevResult, new CoverageTarget(), new CoverageTarget(), true, false,
				false, false, false )
		{
			@Override
			public HealthReport getBuildHealth()
			{

				return new HealthReport( 100, (Localizable) null );
			}
		};

		EasyMock.expect( build.getPreviousNotFailedBuild() ).andReturn( prevBuild );

		EasyMock.expect( prevBuild.getAction( CoberturaBuildAction.class ) ).andReturn( prevAction );

		EasyMock.expect( prevBuild.getPreviousNotFailedBuild() ).andReturn( prevPrevBuild );

		EasyMock.expect( prevPrevBuild.getAction( CoberturaBuildAction.class ) ).andReturn( prevPrevAction );

		EasyMock.expect( prevPrevBuild.getPreviousNotFailedBuild() ).andReturn( null );

		EasyMock.expect( build.getDisplayName() ).andReturn( "3" ).atLeastOnce();
		EasyMock.expect( prevBuild.getDisplayName() ).andReturn( "2" ).atLeastOnce();
		EasyMock.expect( prevPrevBuild.getDisplayName() ).andReturn( "1" ).atLeastOnce();

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
		ctl.replay();
		prevResult.setOwner( prevBuild );
		prevPrevResult.setOwner( prevPrevBuild );
		result.setOwner( build );

		JFreeChart chart = new CoverageChart( result ).createChart();
		ChartUtilities.saveChartAsPNG( new File( "temp.png" ), chart, 500, 400 );
		ctl.verify();
	}

}
