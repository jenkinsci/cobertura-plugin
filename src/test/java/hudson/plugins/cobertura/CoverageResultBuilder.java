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

			CoberturaBuildAction action = new CoberturaBuildAction( build, result, new CoverageTarget(), new CoverageTarget(), true, false, false, false, false )
			{
				@Override
				public HealthReport getBuildHealth()
				{

					return new HealthReport( 100, (Localizable) null );
				}
			};

			if( c < results.size() ) EasyMock.expect( build.getAction( CoberturaBuildAction.class ) ).andReturn( action );
			EasyMock.expect( build.getDisplayName() ).andReturn( String.valueOf( c ) ).atLeastOnce();
			EasyMock.expect( build.getPreviousNotFailedBuild() ).andReturn( prevBuild );

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

}
