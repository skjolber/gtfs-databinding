package com.github.skjolber.dc.gtfs.mt;

import com.github.skjolber.unzip.NewlineChunkSplitter;

/**
 * Split the StopTime feed into whole trip sequences, i.e. split at stop_sequence equal to zero.
 * 
 * Note: This runs in a single thread and makes assumptions of the underlying character encoding and CSV escaping.
 */

public class StopTimeSequenceChunkerSplitter extends NewlineChunkSplitter {

	private int sequenceColumn = -1;
	
	public StopTimeSequenceChunkerSplitter(String firstLine, int chunkLength) {
		super(chunkLength);
		int stopSequenceIndex = firstLine.indexOf("stop_sequence");
		this.chunkLength = chunkLength;
		
		int sequenceColumn = 0;
		for(int i = 0; i < stopSequenceIndex; i++) {
			if(firstLine.charAt(i) == ',') {
				sequenceColumn++;
			} else if(firstLine.charAt(i) == '"') {
				// find another quote, and break unless there is another quote
				do {
					i++;
					if(firstLine.charAt(i) == '"') {
						if(firstLine.charAt(i + 1) != '"') {
							// 1x qoute
							break;
						}
						i++;
					}
				} while(true);
			}
		}	
		this.sequenceColumn = sequenceColumn;
	}
	
	public int getNextChunkIndex(byte[] bytes, int fromIndex) {
		fromIndex = super.getNextChunkIndex(bytes, fromIndex);
		
		fromIndex--;
		do {
			fromIndex = super.getNextChunkIndex(bytes, fromIndex);
			// find sequence number
			int sequenceNumber = parseSequenceNumber(bytes, fromIndex + 1);
			if(sequenceNumber == 0) {
				break;
			}
			fromIndex--; // skip newline
		} while(true);
		
		return fromIndex;
	}

	protected int parseSequenceNumber(byte[] bytes, int fromIndex) {
		
		int column = 0;
		do {
			if(bytes[fromIndex] == ',') {
				column++;
			} else if(bytes[fromIndex] == '"') {
				// find another quote, and break unless there is another quote
				do {
					fromIndex++;
					if(bytes[fromIndex] == '"') {
						if(bytes[fromIndex + 1] != '"') {
							// 1x qoute
							break;
						}
						fromIndex++;
					}
				} while(true);
			}
			fromIndex++;
			if(column == sequenceColumn) {
				// parse value
				int start = fromIndex;
				while(bytes[fromIndex] != '\n' && bytes[fromIndex] != ',') {
					fromIndex++;
				}
				
				if(start == fromIndex) {
					throw new RuntimeException();
				}
				return Integer.parseInt(new String(bytes, start, fromIndex - start));
			}
		} while(true);
		
	}

}
