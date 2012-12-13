package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.junit.Test;

public class ChartTest
{
	private IMocksControl	ctl;

	@Test(expected = NullPointerException.class)
	public void noGraph() throws IOException
	{
		new CoverageChart( null, true );
	}

	@Test(expected = NullPointerException.class)
	public void oneResult() throws Exception
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl ).data().create();
		new CoverageChart( result, true );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simple() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl ).data().data().create();
		CoverageChart chartData = new CoverageChart( result, true );
		Assert.assertEquals( 74, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, "temp/chart_simple.png" );

	}

	@SuppressWarnings("unchecked")
	@Test
	public void someMore() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl )//
				.result( 100, 100, 200, 300, 400, 500 )//
				.result( 100, 100, 200, 300, 400, 500 )//
				.result( 100, 200, 300, 400, 500, 600 )//
				.create();
		CoverageChart chartData = new CoverageChart( result, true );
		Assert.assertEquals( 9, chartData.getLowerBound() );
		Assert.assertEquals( 61, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, "temp/chart_multiple.png" );

	}

	@SuppressWarnings("unchecked")
	@Test
	public void fullRange() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl )//
				.result( 0 )//
				.result( 0 )//
				.result( 1000 )//
				.result( 1000 ).create();
		CoverageChart chartData = new CoverageChart( result, true );
		Assert.assertEquals( -1, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, "temp/chart_full_range.png" );

	}

	@SuppressWarnings("unchecked")
	@Test
	public void closeup() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl )//
				.result( 105 )//
				.result( 115 )//
				.result( 108 )//
				.result( 111, 108, 107, 114, 113, 109 ).create();
		CoverageChart chartData = new CoverageChart( result, true );
		Assert.assertEquals( 10, chartData.getLowerBound() );
		Assert.assertEquals( 12, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, "temp/chart_closeup.png" );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void nozoom() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl )//
				.result( 105 )//
				.result( 115 )//
				.result( 108 )//
				.result( 111, 108, 107, 114, 113, 109 ).create();
		CoverageChart chartData = new CoverageChart( result, false );
		Assert.assertEquals( -1, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, "temp/chart_nozoom.png" );
	}
	
	protected void complete( CoverageChart chartData, String filename ) throws IOException
	{
		ctl.verify();
	}

	public <T> void assertEquals( List<T> expected, List<T> actual )
	{
		Assert.assertEquals( new ArrayList<T>( expected ).toString(), new ArrayList<T>( actual ).toString() );
	}
}
