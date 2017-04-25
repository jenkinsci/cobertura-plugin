package hudson.plugins.cobertura;

import hudson.model.FreeStyleBuild;
import hudson.model.HealthReport;
import hudson.plugins.cobertura.targets.CoverageElement;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.cobertura.targets.CoverageTarget;
import hudson.plugins.cobertura.targets.CoverageResult;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jvnet.localizer.Localizable;

public class CoverageResultBuilder
{
	private IMocksControl			ctl;
	private List<CoverageResult>	results	= new LinkedList<CoverageResult>();

	public CoverageResultBuilder( IMocksControl ctl )
	{
		this.ctl = ctl;
	}

	public CoverageResultBuilder data() throws IOException
	{

		results.add( CoberturaCoverageParser.parse( getClass().getResourceAsStream( "coverage-with-data.xml" ), null ) );
		return this;
	}

	public CoverageResultBuilder lotsofdata() throws IOException
	{
		results.add( CoberturaCoverageParser.parse( getClass().getResourceAsStream( "coverage-with-lots-of-data.xml" ), null ) );
		return this;
	}

	public CoverageResult create() throws IOException
	{
		FreeStyleBuild prevBuild = null;
		FreeStyleBuild build;
		int c = 1;
		for( CoverageResult result: results )
		{
			build = ctl.createMock( FreeStyleBuild.class );
			build.number = c;
			CoberturaBuildAction action = new CoberturaBuildAction( result, new CoverageTarget(), new CoverageTarget(), true, false, false, false, false, false, 0 )
			{
				@Override
				public HealthReport getBuildHealth()
				{

					return new HealthReport( 100, (Localizable) null );
				}
			};

			EasyMock.expect( build.getAction( CoberturaBuildAction.class ) ).andReturn( action ).anyTimes();
			EasyMock.expect( build.getDisplayName() ).andReturn( "#" + String.valueOf( c ) ).anyTimes();
			EasyMock.expect( build.getPreviousNotFailedBuild() ).andReturn( prevBuild ).anyTimes();
			EasyMock.expect( build.isBuilding() ).andReturn( false ).anyTimes();

			result.setOwner( build );

			prevBuild = build;
			c++;
		}

		ctl.replay();
		return results.get( results.size() - 1 );
	}

	public CoverageResultBuilder result( final Ratio classes, final Ratio contitional, final Ratio files, final Ratio line, final Ratio method,
			final Ratio packages )
	{
		results.add( new CoverageResult( CoverageElement.PROJECT, null, null )
		{
			private static final long	serialVersionUID	= 1L;

			public Map<CoverageMetric, Ratio> getResults()
			{
				Map<CoverageMetric, Ratio> results = new HashMap<CoverageMetric, Ratio>();
				results.put( CoverageMetric.CLASSES, classes );
				results.put( CoverageMetric.CONDITIONAL, contitional );
				results.put( CoverageMetric.FILES, files );
				results.put( CoverageMetric.LINE, line );
				results.put( CoverageMetric.METHOD, method );
				results.put( CoverageMetric.PACKAGES, packages );
				return Collections.unmodifiableMap( results );

			};
		} );
		return this;
	}

	/**
	 * using Ratio.create(param,1000)
	 */
	public CoverageResultBuilder result( final int classes, final int contitional, final int files, final int line, final int method, final int packages )
	{
		results.add( new CoverageResult( CoverageElement.PROJECT, null, null )
		{
			private static final long	serialVersionUID	= 1L;

			public Map<CoverageMetric, Ratio> getResults()
			{
				Map<CoverageMetric, Ratio> results = new HashMap<CoverageMetric, Ratio>();
				results.put( CoverageMetric.CLASSES, Ratio.create( classes, 1000 ) );
				results.put( CoverageMetric.CONDITIONAL, Ratio.create( contitional, 1000 ) );
				results.put( CoverageMetric.FILES, Ratio.create( files, 1000 ) );
				results.put( CoverageMetric.LINE, Ratio.create( line, 1000 ) );
				results.put( CoverageMetric.METHOD, Ratio.create( method, 1000 ) );
				results.put( CoverageMetric.PACKAGES, Ratio.create( packages, 1000 ) );
				return Collections.unmodifiableMap( results );

			};
		} );
		return this;
	}
	
	/**
	 * using Ratio.create(param,1000) for all metrix
	 */
	public CoverageResultBuilder result( final int coverage )
	{
		return result( coverage, coverage, coverage, coverage, coverage, coverage );
	}

}
