package hudson.plugins.cobertura;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CoberturaPublisherTest {

    @Test
    public void testGetProcessUnstable() {
        CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, true, false, false, false, false, false, false, false, false, null, 0);
        CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
        assertTrue(testObjectTrue.getProcessUnstable());
        assertTrue(!testObjectFalse.getProcessUnstable());
    }

    @Test
    public void testGetProcessFailed() {
        CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, true, false, false, false, false, false, false, false, null, 0);
        CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
        assertTrue(testObjectTrue.getProcessFailed());
        assertTrue(!testObjectFalse.getProcessFailed());
    }

    @Test
    public void testGetKeepSources() {
        CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, true, false, false, false, false, false, false, null, 0);
        CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
        assertTrue(testObjectTrue.getKeepSources());
        assertTrue(!testObjectFalse.getKeepSources());
    }

	@Test
	public void testGetFailUnhealthy() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, true, false, false, false, false, false, null, 0);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
		assertTrue(testObjectTrue.getFailUnhealthy());
		assertTrue(!testObjectFalse.getFailUnhealthy());
	}

	@Test
	public void testGetFailUnstable() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, false, true, false, false, false, false, null, 0);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
		assertTrue(testObjectTrue.getFailUnstable());
		assertTrue(!testObjectFalse.getFailUnstable());
	}

	@Test
	public void testGetAutoUpdateHealth() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, false, false, true, false, false, false, null, 0);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
		assertTrue(testObjectTrue.getAutoUpdateHealth());
		assertTrue(!testObjectFalse.getAutoUpdateHealth());
	}

	@Test
	public void testGetAutoUpdateStability() {
		CoberturaPublisher testObjectTrue = new CoberturaPublisher(null, false, false, false, false, false, false, true, false, false, null, 0);
		CoberturaPublisher testObjectFalse = new CoberturaPublisher(null, false, false, false, false, false, false, false, false, false, null, 0);
		assertTrue(testObjectTrue.getAutoUpdateStability());
		assertTrue(!testObjectFalse.getAutoUpdateStability());
	}

}
