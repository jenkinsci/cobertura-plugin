package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.File;
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
	public static final File TEMP_IMAGE_FOLDER = new File("temp");

	private IMocksControl	ctl;

	@Test(expected = NullPointerException.class)
	public void noGraph() throws IOException
	{
		new CoverageChart( null, true, 0 );
	}

	@Test(expected = NullPointerException.class)
	public void oneResult() throws Exception
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl ).data().create();
		new CoverageChart( result, true, 0 );
	}

	@SuppressWarnings("unchecked")
	@Test
	public void simple() throws IOException
	{
		ctl = EasyMock.createControl();
		CoverageResult result = new CoverageResultBuilder( ctl ).data().data().create();
		CoverageChart chartData = new CoverageChart( result, true, 0 );
		Assert.assertEquals( 74, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, new File( TEMP_IMAGE_FOLDER, "chart_simple.png" ).getPath() );

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
		CoverageChart chartData = new CoverageChart( result, true, 0 );
		Assert.assertEquals( 9, chartData.getLowerBound() );
		Assert.assertEquals( 61, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, new File( TEMP_IMAGE_FOLDER, "chart_multiple.png" ).getPath() );

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
		CoverageChart chartData = new CoverageChart( result, true, 0 );
		Assert.assertEquals( -1, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, new File( TEMP_IMAGE_FOLDER, "chart_full_range.png" ).getPath() );

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
		CoverageChart chartData = new CoverageChart( result, true, 0 );
		Assert.assertEquals( 10, chartData.getLowerBound() );
		Assert.assertEquals( 12, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, new File( TEMP_IMAGE_FOLDER, "/chart_closeup.png" ).getPath() );
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
		CoverageChart chartData = new CoverageChart( result, false, 0 );
		Assert.assertEquals( -1, chartData.getLowerBound() );
		Assert.assertEquals( 101, chartData.getUpperBound() );
		assertEquals( Arrays.asList( "#1", "#2", "#3", "#4" ), chartData.getDataset().getColumnKeys() );
		complete( chartData, new File( TEMP_IMAGE_FOLDER, "chart_nozoom.png" ).getPath() );
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
