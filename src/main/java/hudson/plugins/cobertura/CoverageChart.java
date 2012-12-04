package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

public class CoverageChart
{
	private CategoryDataset	dataset;
	private int					lowerBound;
	private int					upperBound;

	/**
	 * @pre chartable!=null && chartable.getPreviousResult()!=null
	 */
	public CoverageChart( Chartable chartable )
	{
		if( chartable == null ) throw new NullPointerException( "Cannot draw null-chart" );
		if( chartable.getPreviousResult() == null ) throw new NullPointerException( "Need at least two result to draw a chart" );
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		int min = 100;
		int max = 0;
		for( Chartable a = chartable; a != null; a = a.getPreviousResult() )
		{
			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( a.getOwner() );
			for( Map.Entry<CoverageMetric, Ratio> value: a.getResults().entrySet() )
			{
				dsb.add( value.getValue().getPercentageFloat(), value.getKey().getName(), label );
				min = Math.min( min, value.getValue().getPercentage() );
				max = Math.max( max, value.getValue().getPercentage() );
			}
		}
		int range=max-min;
		this.dataset = dsb.build();
		this.lowerBound = min - 1;
		this.upperBound = max + (range < 5 ? 0 : 1);
	}

	public JFreeChart createChart()
	{

		final JFreeChart chart = ChartFactory.createLineChart( null, // chart title
				null, // unused
				"%", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
				);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		final LegendTitle legend = chart.getLegend();
		legend.setPosition( RectangleEdge.RIGHT );

		chart.setBackgroundPaint( Color.white );

		final CategoryPlot plot = chart.getCategoryPlot();

		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setBackgroundPaint( Color.WHITE );
		plot.setOutlinePaint( null );
		plot.setRangeGridlinesVisible( true );
		plot.setRangeGridlinePaint( Color.black );

		CategoryAxis domainAxis = new ShiftedCategoryAxis( null );
		plot.setDomainAxis( domainAxis );
		domainAxis.setCategoryLabelPositions( CategoryLabelPositions.UP_90 );
		domainAxis.setLowerMargin( 0.0 );
		domainAxis.setUpperMargin( 0.0 );
		domainAxis.setCategoryMargin( 0.0 );

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
		rangeAxis.setUpperBound( upperBound );
		rangeAxis.setLowerBound( lowerBound );

		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseStroke( new BasicStroke( 1.5f ) );
		ColorPalette.apply( renderer );

		// crop extra space around the graph
		plot.setInsets( new RectangleInsets( 5.0, 0, 0, 5.0 ) );

		return chart;
	}

	protected CategoryDataset getDataset()
	{
		return dataset;
	}

	protected int getLowerBound()
	{
		return lowerBound;
	}

	protected int getUpperBound()
	{
		return upperBound;
	}
}