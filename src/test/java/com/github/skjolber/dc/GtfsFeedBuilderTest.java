package com.github.skjolber.dc;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

public class GtfsFeedBuilderTest {

	private static File file = new File("./src/test/resources/rb_rut-aggregated-gtfs.zip");

	@Test
	public void testBuilder() throws Exception {
		long time = System.currentTimeMillis();
		GtfsFeedBuilder.newInstance().withFile(file).build();
		System.out.println("Done in " + (System.currentTimeMillis() - time));
	}
	
	@Test
	public void testBuilderWithFilter() throws Exception {
		LocalDate date = LocalDate.of(2019, 3, 28);

		long time = System.currentTimeMillis();
		GtfsFeedBuilder.newInstance().withStartDate(date).withEndDate(date).withFile(file).build();
		System.out.println("Done in " + (System.currentTimeMillis() - time));
	}
	
	public static void main(String[] args) throws Exception {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for(int i = 0; i < 10; i++) {
			long time = System.currentTimeMillis();
			GtfsFeed build = GtfsFeedBuilder.newInstance().withFile(file).withExecutor(executor).build();
			if(build == null) {
				throw new RuntimeException();
			}
			System.out.println("Parse done in " + (System.currentTimeMillis() - time));
			System.gc();
		}
		
		executor.shutdown();
	}

}
