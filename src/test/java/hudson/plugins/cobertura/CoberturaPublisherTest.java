package hudson.plugins.cobertura;

import static org.junit.Assert.*;
import hudson.plugins.cobertura.targets.CoverageMetric;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

public class CoberturaPublisherTest {

	@Test
	public void testGetOnlyStable() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, true, false, false, false, false, null);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, null);
		assertTrue(testObjectTrue.getOnlyStable());
		assertTrue(!testObjectFalse.getOnlyStable());
	}
	
	@Test
	public void testGetFailUnhealthy() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, true, false, false, false, null);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, null);
		assertTrue(testObjectTrue.getFailUnhealthy());
		assertTrue(!testObjectFalse.getFailUnhealthy());
	}

	@Test
	public void testGetFailUnstable() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, true, false, false, null);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, null);
		assertTrue(testObjectTrue.getFailUnstable());
		assertTrue(!testObjectFalse.getFailUnstable());
	}

	@Test
	public void testGetAutoUpdateHealth() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, true, false, null);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, null);
		assertTrue(testObjectTrue.getAutoUpdateHealth());
		assertTrue(!testObjectFalse.getAutoUpdateHealth());
	}

	@Test
	public void testGetAutoUpdateStability() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, false, true, null);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, null);
		assertTrue(testObjectTrue.getAutoUpdateStability());
		assertTrue(!testObjectFalse.getAutoUpdateStability());
	}

}
