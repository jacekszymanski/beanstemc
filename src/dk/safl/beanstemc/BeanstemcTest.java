package dk.safl.beanstemc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

public class BeanstemcTest extends TestCase {
	
	private String host = "localhost";
	private int port = 11300;
	private Beanstemc beanstemc;
	
	// Test-job supplied to all tests
	private byte[] testPayload = "beanie man, bo!".getBytes();	
	private long testId;
	private Job testJob;
	
	private byte[] delayedPayload = "beanie man, bo! delayed yo!".getBytes();
	
	// Ids to cleanup / delete after running tests
	private List<Long> trash = new ArrayList<Long>();

	public BeanstemcTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		beanstemc = new Beanstemc(host, port);

		// Put a job into the "default" tube
		testId	= beanstemc.put(testPayload);			
		testJob	= beanstemc.peek(testId);
		trash.add(testId);
		
		assertTrue(testId>0);
		assertNotNull(testJob);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		HashMap<String, String> stats = beanstemc.stats();
		long someId = -1;
		for(Long jobId : trash) {
			try {
				Job job = beanstemc.reserve(1);
				someId = job.getId();
				beanstemc.delete(job);
			} catch(BeanstemcException e) {
				System.out.println(String.format("ERR :Could not delete job jobId %d?", someId));
			}
		}
		
		beanstemc.close();
		
		
	}

	public void testUse() throws IOException, BeanstemcException {
		
	}

	public void testReserveInt() throws IOException, BeanstemcException {
				
		Job job = beanstemc.reserve(10);		
		beanstemc.release(job, 0, 0);
		
		assertNotNull(job);
		assertEquals(testId, job.getId());
		assertEquals(new String(testPayload), new String(job.getData()));
	}

	public void testReserve() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		beanstemc.release(job, 0, 0);
		
		assertNotNull(job);
		assertEquals(testId, job.getId());
		assertEquals(new String(testPayload), new String(job.getData()));
		
	}

	public void testDeleteJob() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		beanstemc.delete(job);
		
	}

	public void testDeleteLong() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		beanstemc.delete(job.getId());
		
	}

	public void testReleaseJobIntInt() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		beanstemc.release(job.getId(), Long.valueOf(10).longValue(), 0);
	}

	public void testReleaseLongIntInt() throws IOException, BeanstemcException {
		Job job = beanstemc.reserve();
		beanstemc.release(job.getId(), 10, 0);
	}

	public void testBury() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		beanstemc.bury(job.getId(), 10);
		int kicked = beanstemc.kick(10);
		assertEquals(1, kicked);
	}

	public void testWatch() throws IOException, BeanstemcException {
		
		beanstemc.watch("tube1");
		beanstemc.watch("tube2");
		beanstemc.watch("tube3");
		int watching = beanstemc.watch("tube4");
		
		assertTrue(watching == 5);

	}

	public void testIgnore() throws IOException, BeanstemcException {
		
		beanstemc.watch("tube1");
		beanstemc.watch("tube2");
		beanstemc.watch("tube3");
		beanstemc.watch("tube4");
		
		int watching = beanstemc.ignore("tube3");
		assertTrue(watching == 4);
	}

	public void testPeekReady() throws IOException, BeanstemcException {
		
		Job job = beanstemc.peekReady();
		
		assertNotNull(job);
		assertEquals(testId, job.getId());
		assertEquals(new String(testPayload), new String(job.getData()));
	}

	public void testPeekDelayed() throws IOException, BeanstemcException, InterruptedException {
		
		long jobId = beanstemc.put(delayedPayload, 10, 3);
		trash.add(jobId);
		
		Job job = beanstemc.peekDelayed();
		assertNotNull(job);
		assertEquals(jobId, job.getId());
		assertEquals(new String(delayedPayload), new String(job.getData()));
		
		Thread.sleep(4);		
		
	}

	public void testPeekBuried() throws IOException, BeanstemcException {
		
		Job job = beanstemc.reserve();
		
		assertNotNull(job);
		assertEquals(true, job.getId()>0);
		assertEquals(job.getId(), testId);
		assertEquals(new String(testPayload), new String(job.getData()));
		
		beanstemc.bury(job.getId(), 10);
		
		Job buried_job = beanstemc.peekBuried();
		
		assertEquals(job.getId(), buried_job.getId());
		int kicked = beanstemc.kick(10);
		assertEquals(1, kicked);
		
	}

	public void testKick() throws IOException, BeanstemcException {
		int kicked = beanstemc.kick(10);
		assertEquals(0, kicked);
	}

	public void testStatsJob() throws IOException, BeanstemcException {
		beanstemc.statsJob(testId);
	}

	public void testStatsTube() throws IOException, BeanstemcException {
		beanstemc.statsTube("default");
	}

	public void testStats() throws IOException, BeanstemcException {
		beanstemc.stats();
	}

	public void testListTubes() throws IOException, BeanstemcException {
		beanstemc.listTubes();
	}
	
	public void testListTubesMoreThanOne() throws IOException, BeanstemcException {
		beanstemc.use("colgate");
		beanstemc.use("zendium");
		beanstemc.use("sensodyne");
		beanstemc.listTubes();		
	}

	public void testListTubeUsed() throws IOException, BeanstemcException {
		beanstemc.listTubesWatched();
	}

	public void testListTubesWatched() throws IOException, BeanstemcException {
		beanstemc.listTubesWatched();
	}

}
