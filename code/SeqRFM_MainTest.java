import java.io.IOException;

/**
 * Copyright (C), 2023, JNU
 * FileName: SeqRFM_MainTest
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: The main file of SeqRFM algorithm.
 */

public class SeqRFM_MainTest {
	public static void main(String[] args) throws IOException {
		
		// SIGN_sequence_utility  kosarak10k  example_huspm2
		String s = "BMS_rfm";
		String input = "./input/" + s + ".txt"; 
		// the path for saving the patterns found
		String output = "./output/" + s + "_TT_2_SeqRFM.txt";  
		// the minimum utility threshold 
		double delta = 0.009;
		double minRecency = 20;
		double minsupRatio = 0.000375;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
		double minUtilityRatio = 0.015;
		int timeSpan = 237;
		
		AlgoSeqRFM algo = new AlgoSeqRFM();
        System.out.println("test dataset: " + input);
        System.out.println("minUtilityRatio: " + String.format("%.5f", minUtilityRatio));
        
		// BIBLE_sequence_utility MSNBC_spmf 
        algo.runAlgorithm(input, output, delta, minUtilityRatio, minsupRatio, minRecency, timeSpan);

		algo.printStats();
	}
}


