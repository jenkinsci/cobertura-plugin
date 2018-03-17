package hudson.plugins.cobertura;

import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.jfree.util.Log;

public class CoverageChart
{
	private CategoryDataset	dataset;
	private int					lowerBound;
	private int					upperBound;

	private static final List<Color> customLineColor = Collections.unmodifiableList(Arrays.asList(
			new Color(0xd65e00),
			new Color(0x0072b2),
			new Color(0x523105),
			new Color(0x009e73),
			new Color(0x56b4e9),
			new Color(0xcc79a7)

		));

	private static final List<BasicStroke> customLineStroke = Collections.unmodifiableList(Arrays.asList(
			new BasicStroke(
					2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
			),
			new BasicStroke(4.0f, // line width
					BasicStroke.CAP_ROUND, // the decoration of ends of a BasicStroke
					BasicStroke.JOIN_ROUND, // the decoration applied where path segement meet
					1.0f,
					new float[] {1.0f, 6.0f}, //the array representing the dashing pattern
					0.0f //the offset to start the dashing pattern
			),

			new BasicStroke(
					2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
					1.0f, new float[] {8.0f, 6.0f}, 0.0f
			),
			new BasicStroke(
					2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
			),
			new BasicStroke(
					2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
					1.0f, new float[] {6.0f, 6.0f}, 0.0f
			),
			new BasicStroke(
					2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,
					1.0f, new float[] {2.0f, 6.0f}, 0.0f
			)
	));

	private static final List<Boolean> customShapeVisible = Collections.unmodifiableList(Arrays.asList(
			false, false, false, true, true, true
	));

	/**
	 * Constructor
	 *
	 * @param chartable Chartable object to chart
	 */
	public CoverageChart( Chartable chartable )
	{
		this( chartable, isZoomCoverageChart( chartable ), getMaximumBuilds( chartable ) );
	}

	/**
	 * Constructor
	 *
	 * @param chartable Chartable object to chart
	 * @param zoomCoverageChart true to zoom coverage chart
	 * @param maximumBuilds maximum builds to include
	 */
	protected CoverageChart( Chartable chartable, boolean zoomCoverageChart, int maximumBuilds )
	{
		if( chartable == null ) throw new NullPointerException( "Cannot draw null-chart" );
		if( chartable.getPreviousResult() == null ) throw new NullPointerException( "Need at least two result to draw a chart" );
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
		int min = 100;
		int max = 0;
		int n = 0;
		for( Chartable a = chartable; a != null; a = a.getPreviousResult())
		{
			ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel( a.getOwner() );
			for( Map.Entry<CoverageMetric, Ratio> value: a.getResults().entrySet() )
			{
				dsb.add( value.getValue().getPercentageFloat(), value.getKey().getName(), label );
				min = Math.min( min, value.getValue().getPercentage() );
				max = Math.max( max, value.getValue().getPercentage() );
			}
			n++;
			if( maximumBuilds != 0 && n >= maximumBuilds ) break;
		}
		int range = max - min;
		this.dataset = dsb.build();
		if( zoomCoverageChart )
		{
			this.lowerBound = min - 1;
			this.upperBound = max + (range < 5 ? 0 : 1);
		}
		else
		{
			this.lowerBound = -1;
			this.upperBound = 101;
		}
	}

	protected static boolean isZoomCoverageChart( Chartable chartable )
	{
		if( chartable == null ) return false;
		CoberturaBuildAction action = chartable.getOwner().getAction(CoberturaBuildAction.class);
		boolean zoomCoverageChart = false;
		if( action != null )
		{
			return action.getZoomCoverageChart();
		}
		else
		{
			Log.warn( "Couldn't find CoberturaPublisher to decide if the graph should be zoomed" );
			return false;
		}
	}

	protected static int getMaximumBuilds( Chartable chartable )
	{
		if( chartable == null ) return 0;
		CoberturaBuildAction action = chartable.getOwner().getAction(CoberturaBuildAction.class);
		if( action != null )
		{
			return action.getMaxNumberOfBuilds();
		}
		else
		{
			Log.warn( "Couldn't find CoberturaPublisher to decide the maximum number of builds to be graphed" );
			return 0;
		}
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
		legend.setPosition( RectangleEdge.BOTTOM );

		chart.setBackgroundPaint( Color.white );

		final CategoryPlot plot = chart.getCategoryPlot();

		// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
		plot.setBackgroundPaint( Color.WHITE );
		plot.setOutlinePaint( null );
		plot.setRangeGridlinesVisible( true );
		plot.setRangeGridlinePaint( Color.gray );

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

		for (int i = 0; i < customLineColor.size(); i++)
		{
			Color c = customLineColor.get(i);
			renderer.setSeriesPaint(i, c);
			Stroke s = customLineStroke.get(i);
			renderer.setSeriesStroke(i, s);
			renderer.setSeriesShapesVisible(i, customShapeVisible.get(i));
		}


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
