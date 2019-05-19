package com.github.skjolber.dc;

import static com.google.common.truth.Truth.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.github.skjolber.dc.gtfs.mt.StopTimeSequenceChunkerSplitter;

public class StopTimeChunkSplitterTest {

	@Test
	public void testSplitBySequenceNumber1() throws IOException {
		String file = IOUtils.resourceToString("/stop_times1.txt", StandardCharsets.UTF_8);
		
		int index = file.indexOf("\n");
		
		StopTimeSequenceChunkerSplitter splitter = new StopTimeSequenceChunkerSplitter(file.substring(0, index), 16 * 1024 * 1024);

		byte[] content = file.substring(index + 1).getBytes();
		int nextChunkIndex = splitter.getNextChunkIndex(content, 1024);
		assertThat(nextChunkIndex).isEqualTo(-1);
	}

	@Test
	public void testSplitBySequenceNumber2() throws IOException {
		String file = IOUtils.resourceToString("/stop_times2.txt", StandardCharsets.UTF_8);
		
		int index = file.indexOf("\n");
		
		StopTimeSequenceChunkerSplitter splitter = new StopTimeSequenceChunkerSplitter(file.substring(0, index), 16 * 1024 * 1024);

		byte[] content = file.substring(index + 1).getBytes();
		int nextChunkIndex = splitter.getNextChunkIndex(content, 1024);
		assertThat(nextChunkIndex).isEqualTo(-1);
	}

	@Test
	public void testSplitBySequenceNumber3() throws IOException {
		String file = IOUtils.resourceToString("/stop_times3.txt", StandardCharsets.UTF_8);
		
		int index = file.indexOf("\n");
		
		StopTimeSequenceChunkerSplitter splitter = new StopTimeSequenceChunkerSplitter(file.substring(0, index), 16 * 1024 * 1024);

		byte[] content = file.substring(index + 1).getBytes();
		int nextChunkIndex = splitter.getNextChunkIndex(content, content.length - 1);
		assertThat(countLines(content, nextChunkIndex)).isEqualTo(10);
	}
	
	private int countLines(byte[] content, int length) throws IOException {
		int count = 0;
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content, 0, length)));
		
		while(bufferedReader.readLine() != null) {
			count++;
		}
		
		return count;
	}
}
