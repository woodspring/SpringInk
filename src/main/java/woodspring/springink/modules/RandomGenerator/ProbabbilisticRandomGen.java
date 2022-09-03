package woodspring.springink.modules.RandomGenerator;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public interface ProbabbilisticRandomGen {
	public int nextFromSample();
	
	static class NumAndProbability {
		private final int number;
		private final float probabilityOfSample;
		public NumAndProbability( int number, float probabilityOfSample ) {
			this.number = number;
			this.probabilityOfSample = probabilityOfSample;
		}
		public int getNumber() {
			return number;
		}
		public float getProbabilityOfSample() {
			return probabilityOfSample;
		}		
	}
	
	ConcurrentSkipListMap<Integer, ProbabbilisticRandomGen.NumAndProbability> sampleMap = new ConcurrentSkipListMap<>();
	default void loadSample( List<NumAndProbability> samples) {
		samples.stream().forEach( item ->
			sampleMap.computeIfAbsent( item.getNumber(), key -> item));
	}
	int addSample(NumAndProbability aSample);
	void addSamples(List<ProbabbilisticRandomGen.NumAndProbability > samples);
	
	

}
