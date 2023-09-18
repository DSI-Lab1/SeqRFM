import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Copyright (C), 2023, JNU
 * FileName: AlgoMSeqRFM
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: MSeqRFM algorithm.
 */

public class AlgoMSeqRFM {
	public long startTimestamp = 0;
	public long endTimestamp = 0;
	
	public double databaseUtility;
	public double minUtility = 0;
	public double minsup = 0;
	public double minRecency = 0;
	public int timeSpan = 0;
	public double delta = 0;
	public double alpha = 0;
	public double beta = 0;
	public double gamma = 0;
	
	public int huspCount = 0;
	public int totalSequence = 0;
	Map<Integer, Integer> mapItemToSWU;
	Map<Integer, Integer> mapItemFre;
	Map<Integer, Double> mapItemRec;
	Map<Integer, Double> mapSeqRecency;
	Map<Integer, Integer> mapCurrentTime;
	
	public int candidateCount = 0;
	BufferedWriter writer = null;
	Map<String, List<Double>> mapMaximal = new HashMap<>();
	/**
	 * Run MSeqRFM algorithm
	 * 
	 * @param input
	 * @param output
	 * @param threshold
	 * 
	 * @throws IOException
	 */
	public void runAlgorithm(String input, String output, double delta1,
			double minUtilityRatio, double minsupRatio, double minRecency1, int timeSpan1) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		startTimestamp = System.currentTimeMillis();
		mapItemToSWU = new HashMap<Integer, Integer>();
		mapItemFre = new HashMap<Integer, Integer>();
		mapItemRec = new HashMap<Integer, Double>();
		int Sid = -1;
		
		BufferedReader myInput = null;
		String thisLine = null;
		try {
			Sid++;
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.equals("")) {
					continue;
				}
				
				String[] spilts = thisLine.split("-2");
				int SUtility = Integer.parseInt(spilts[1].substring(spilts[1].indexOf(":") + 1));
				databaseUtility += SUtility; // update the utility of each sequence
				totalSequence += 1;  // update the number of sequences
				
				Set<Integer> tmpSet = new HashSet<Integer>();
				spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf("-1")).trim();
				String[] itemsetString = spilts[0].split("-1");
				
				// calculate the SWU value
				for (String value : itemsetString) {
					String[] itemAndUtility = value.trim().split(" ");
					int currentTime = Integer.parseInt(itemAndUtility[0].trim().substring(itemAndUtility[0].trim().indexOf("(") + 1, itemAndUtility[0].trim().indexOf(")")));
					for (String val : itemAndUtility) {
						Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
						Integer time = Integer.parseInt(val.trim().substring(val.trim().indexOf("(") + 1, val.trim().indexOf(")")));
						Double recency = Math.pow(1-delta, currentTime - time);
						if (!tmpSet.contains(item)) {
							Integer swu = mapItemToSWU.get(item);
							Integer fre = mapItemFre.get(item);
							Double rec = mapItemRec.get(item);
							if (swu == null) {
								mapItemToSWU.put(item, SUtility);
								mapItemFre.put(item, 1);
								mapItemRec.put(item, recency);
								tmpSet.add(item);
							} else {
								mapItemToSWU.put(item, swu + SUtility);
								mapItemFre.put(item, fre + 1);
								mapItemRec.put(item, rec + recency);
								tmpSet.add(item);
							}
						}

					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				try {
					myInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		this.delta = delta1;
//		double sumRecency = 0.0;
//		mapSeqRecency = new HashMap<>();
//		for(int sid = 1; sid <= totalSequence; sid++) {
//			double SRecency = Math.pow((1 - delta), (totalSequence - sid));
//			mapSeqRecency.put(sid, SRecency);
//			sumRecency += SRecency;
//		}

		// the total utility in database, and the minimum utility value (using percentage here)
		System.out.println("TEST: databaseUtility= " + databaseUtility + "; total number sequences= " + totalSequence);
		alpha = minRecency1;
		beta = minsupRatio;
		gamma = minUtilityRatio;
		minUtility = databaseUtility * minUtilityRatio;
		minsup = totalSequence * minsupRatio;
		minRecency = minRecency1;
		timeSpan = timeSpan1;
		List<Node> nodeList = new ArrayList<Node>();
		Map<Integer, Node> mapItemToNode = new HashMap<Integer, Node>();
		mapCurrentTime = new HashMap<Integer, Integer>();
		
		// Filter the unpromising 1-sequences: SWU >= minUtil
		for (Integer item : mapItemToSWU.keySet()) {
			if (mapItemToSWU.get(item) >= minUtility && mapItemFre.get(item) >= minsup && mapItemRec.get(item) >= minRecency) {
				Node node = new Node();
				node.addItemSet(String.valueOf(item));
				//System.out.println(item);
				nodeList.add(node);
				mapItemToNode.put(item, node);
			}
		}
		
		
		System.out.println("TEST: 1-sequences's size " + nodeList.size());
		Map<Integer, List<List<UItem>>> revisedDataBase = new HashMap<Integer, List<List<UItem>>>();
		
		// build the utility-chain
		try {
			int sid = 0;
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			while ((thisLine = myInput.readLine()) != null) {
				sid++;
				if (thisLine.equals("")) {
					continue;
				}
				String[] spilts = thisLine.split("-2");
				int remainingUtility = 0;
				List<List<UItem>> sequence = new ArrayList<List<UItem>>();
				spilts[0] = spilts[0].substring(0, spilts[0].lastIndexOf("-1")).trim();
				// System.out.println(sid);
				String[] itemsetString = spilts[0].split("-1");
				
				// for each itemset/element
				for (String value : itemsetString) {
					String[] itemAndUtility = value.trim().split(" ");
					List<UItem> tmp = new ArrayList<UItem>();
					for (String val : itemAndUtility) {
						Integer item = Integer.parseInt(val.trim().substring(0, val.trim().indexOf("[")));
						Integer utility = Integer
								.parseInt(val.trim().substring(val.trim().indexOf("[") + 1, val.trim().indexOf("]")));
						Integer timeStamp = Integer
								.parseInt(val.trim().substring(val.trim().indexOf("(") + 1, val.trim().indexOf(")")));
						if(!mapCurrentTime.containsKey(sid)) {
							mapCurrentTime.put(sid, timeStamp);
							//System.out.println(sid + "," + timeStamp);
						}
						Integer swu = mapItemToSWU.get(item);
						if (swu >= minUtility && mapItemFre.get(item) >= minsup && mapItemRec.get(item) >= minRecency) {
							UItem uItem = new UItem();
							uItem.setItem(item);
							uItem.setUtility(utility);
							uItem.setTime(timeStamp);
							tmp.add(uItem);
							remainingUtility += utility;
						}
					}
					
					if (tmp.size() == 0) {
						continue;
					}
					
					// add this sequence
					sequence.add(tmp);
				}
				
				revisedDataBase.put(sid, sequence);
				int tid = 0;
				// for each sequence in the set of uitemList
				for (List<UItem> uitemList : sequence) {
					tid++;
					for (UItem uItem : uitemList) {
						Node node = mapItemToNode.get(uItem.getItem());
						//System.out.println(node.getSequence());
						MTChain uc = node.getUc();
						Element element = new Element();
						element.setSid(sid);
						element.setTid(tid);
						element.setNext(-1);
						element.setAcu(uItem.getUtility());
						element.setTime(uItem.getTime());
						remainingUtility = remainingUtility - uItem.getUtility();
						element.setRu(remainingUtility);
						uc.addElement(sid, element);
					}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				try {
					myInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// System.out.println("--------------Test Utility Chain-----------------");
		// for(Node node:nodeList) {
		// node.printNode();
		// }
		// System.out.println("-------------------------------------------------");
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
		candidateCount += nodeList.size();
		
		
		// for each 1-sequences
		for (Node node : nodeList) {
			Integer utility = node.getSupUtility()[0];
			Integer support = node.getSupUtility()[1];
			double recency = node.getRecency(delta, mapCurrentTime);
			if (utility >= minUtility && support >= minsup && recency >= minRecency) {
//				System.out.println(node.getSequence() + "  #UTIL:" + utility + "  #SUP:" + support + "  #RECENCY:" + recency);
				huspCount++;
				writeToFile(node.getSequence(), utility, support, recency);
			}
			
			if (support < minsup || recency < minRecency) {
				continue;
			}
			
			// call the MSeqRFM function
			MSeqRFM(node, revisedDataBase, minUtility, minsup);
			// System.out.println("huspCount:"+huspCount);
		}
		//writer.write("\n================== Maximal Pattern ==================\n");
		//writer.write(" Total number of Maximal RFMs: " + mapMaximal.size() + "\n");
		for(Entry<String, List<Double>> entry: mapMaximal.entrySet()) {
			writeToFileMaximal(entry.getKey(), entry.getValue());
		}
		
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		//writer.close();
		endTimestamp = System.currentTimeMillis();
	}

	
	/**
	 * MSeqRFM algorithm
	 * 
	 * @param node
	 * @param revisedDataBase
	 * @param minUtil
	 * @throws IOException
	 */
	private void MSeqRFM(Node node, Map<Integer, List<List<UItem>>> revisedDataBase, double minUtil, double minsup)
			throws IOException {
		
		// prune early
		Integer peu = node.getPEU();
		
		if (peu < minUtil) {
			return;
		}
		
		// I-extension and S-extension
		List<List<Node>> lists = extension(node, revisedDataBase, minUtil);
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		// for each sequence in candidates
		if (lists != null) {

			// I-extension
			List<Node> iNode = lists.get(0);
			candidateCount += iNode.size();
			for (Node n : iNode) {
				Integer utility = n.getSupUtility()[0];
				Integer support = n.getSupUtility()[1];
				double recency = n.getRecency(delta, mapCurrentTime);
				if (utility >= minUtil && support >= minsup && recency >= minRecency) {
//					System.out.println(n.getSequence() + "  #UTIL:" + utility + "  #SUP:" + support + "  #RECENCY:" + recency);
					huspCount++;
					writeToFile(n.getSequence(), utility, support, recency);
				}
				
				if (support >= minsup && recency >= minRecency) {
					// call the HUSSpan function
					MSeqRFM(n, revisedDataBase, minUtil, minsup);
				}
			}

			// S-extension
			List<Node> sNode = lists.get(1);
			candidateCount += sNode.size();
			for (Node n : sNode) {
				Integer utility = n.getSupUtility()[0];
				Integer support = n.getSupUtility()[1];
				double recency = n.getRecency(delta, mapCurrentTime);
				if (utility >= minUtil && support >= minsup && recency >= minRecency) {
//					System.out.println(n.getSequence() + "  #UTIL:" + utility + "  #SUP:" + support + "  #RECENCY:" + recency);
					huspCount++;
					writeToFile(n.getSequence(), utility, support, recency);
				}
				
				if (support >= minsup && recency >= minRecency) {
					// call the HUSSpan function
					MSeqRFM(n, revisedDataBase, minUtil, minsup);
				}
			}
		}

	}

	public void printStats() throws IOException {
		writer.write("===========  MSeqRFM v4.0 ALGORITHM - STATS =========\n");
		System.out.println("===========  MSeqRFM v4.0 ALGORITHM - STATS =========");
		
		writer.write(" delta: " + this.delta + "\n");
		System.out.println(" delta: " + this.delta); 
		
		writer.write(" alpha: " + this.alpha + "\n");
		System.out.println(" alpha: " + alpha); 
		
		writer.write(" beta: " + this.beta + "\n");
		System.out.println(" beta: " + this.beta); 
		
		writer.write(" gamma: " + gamma + "\n");
		System.out.println(" gamma: " + gamma); 
		
		writer.write(" timeSpan: " + timeSpan + "\n");
		System.out.println(" timeSpan: " + timeSpan); 
		
		writer.write(" Total utility of DB: " + databaseUtility + "\n");
		System.out.println(" Total utility of DB: " + databaseUtility); 
		
		writer.write(" minUtility: "+ minUtility + "\n");
		System.out.println(" minUtility: "+ minUtility);
		
		writer.write(" minsup: "+ minsup + "\n");
		System.out.println(" minsup: "+ minsup);
		
		writer.write(" minRecency: "+ minRecency + "\n");
		System.out.println(" minRecency: "+ minRecency);
		
		writer.write(" Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s" + "\n");
		System.out.println(" Total time: " + (endTimestamp - startTimestamp)/1000.0 + " s");
		
		writer.write(" Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB" + "\n");
		System.out.println(" Max memory: " + MemoryLogger.getInstance().getMaxMemory() + " MB");
		
		writer.write(" RFMs: " + huspCount + "\n");
		System.out.println(" RFMs: " + huspCount);
		
		
		writer.write(" MRFMs: " + mapMaximal.size() + "\n");
		System.out.println(" MRFMs: " + mapMaximal.size());
		
		writer.write(" Candidates: " + candidateCount + "\n");
		System.out.println(" Candidates: " + candidateCount);
		System.out.println("===================================================");
		writer.close();
	}
	

	
	/**
	 * Extension list
	 * 
	 * @param node
	 * @param revisedDataBase
	 * @param minUtil
	 * @return
	 */
	private List<List<Node>> extension(Node node, Map<Integer, List<List<UItem>>> revisedDataBase, double minUtil) {
		
		// the utility-chain of this node
		MTChain uc = node.getUc();
		Map<Integer, List<Element>> ucMap = uc.getUttilityChain();
		List<String> sequence = node.getSequence();
		Integer lastItem = getLastItem(sequence);
		Set<Integer> sidSet = ucMap.keySet();
		
		List<Integer> itemsForSExtension = new ArrayList<Integer>();
		List<Integer> itemsForIExtension = new ArrayList<Integer>();
		
		Map<Integer, Integer> iMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> sMap = new HashMap<Integer, Integer>();
		
		// calculate the ilist and slist with the PEU upper bound
		for (Integer sid : sidSet) {
			List<List<UItem>> revisedSequence = revisedDataBase.get(sid);
			List<Element> elementList = ucMap.get(sid);
			
			
			/************************/
			int sPeu = 0;
			// the initial utility-chain does not contain PEU value
			if(uc.getPEU(sid) == null) {
				sPeu = node.getSequencePEU(sid);
			}else {
				sPeu = uc.getPEU(sid);
			}
			/************************/
			
			
			// I-extension
			Set<Integer> tmp = new HashSet<Integer>();
			for (Element e : elementList) {
				int tid = e.getTid().intValue();
				List<UItem> itemset = revisedSequence.get(tid - 1);
				for (UItem uitem : itemset) {
					Integer item = uitem.getItem();

					if(lastItem < item) {
					    tmp.add(item);
						if (!itemsForIExtension.contains(item)) {
							itemsForIExtension.add(item);

						}
					}
				}
			}
			
			// update the PEU upper bound
			for (Integer key : tmp) {
				if (iMap.get(key) == null) {
					iMap.put(key, sPeu);
				} else {
					iMap.put(key, iMap.get(key) + sPeu);
				}
			}
			tmp.clear();
			
			
			// S-extension
			Element ele = elementList.get(0);
			Integer tid = ele.getTid();
			for (int i = tid.intValue(); i < revisedSequence.size(); i++) {
				List<UItem> itemsetList = revisedSequence.get(i);
				for (UItem u : itemsetList) {
					tmp.add(u.getItem());
					if (!itemsForSExtension.contains(u.getItem())) {
						itemsForSExtension.add(u.getItem());
					}
				}
			}
			
			// update the PEU upper bound
			for (Integer key : tmp) {
				if (sMap.get(key) == null) {
					sMap.put(key, sPeu);
				} else {
					sMap.put(key, sMap.get(key) + sPeu);
				}
			}
		}
		
		// Early prune
		if (itemsForSExtension.size() == 0 && itemsForIExtension.size() == 0) {
			return null;
		}

		// remove the unpromising item in the set of ilist
		for (Integer key : iMap.keySet()) {
			Integer rsuValue = iMap.get(key);
			if (rsuValue < minUtil) {
				itemsForIExtension.remove(key);
			}
		}
		
		// remove the unpromising item in the set of slist
		for (Integer key : sMap.keySet()) {
			Integer rsuValue = sMap.get(key);
			if (rsuValue < minUtil) {
				itemsForSExtension.remove(key);
			}
		}
		
		// construct the utility-chain
		List<Node> iNodeList = new ArrayList<Node>();
		List<Node> sNodeList = new ArrayList<Node>();
		Map<Integer, Node> mapToINode = new HashMap<Integer, Node>();
		Map<Integer, Node> mapToSNode = new HashMap<Integer, Node>();
		
		// I-extension
		for (Integer item : itemsForIExtension) {
			Node n = new Node();
			for (int i = 0; i < sequence.size(); i++) {
				if (i != sequence.size() - 1) {
					n.getSequence().add(sequence.get(i));
				} else {
					n.getSequence().add(sequence.get(i) + " " + item);
				}
			}
			iNodeList.add(n);
			mapToINode.put(item, n);
		}
		
		// S-extension
		for (Integer item : itemsForSExtension) {
			Node n = new Node();
			for (int i = 0; i < sequence.size(); i++) {
				n.getSequence().add(sequence.get(i));
			}
			n.getSequence().add(item + "");
			sNodeList.add(n);
			mapToSNode.put(item, n);
		}
		
		// scan each sid in the utility-chain (this projected DB)
		for (Integer sid : sidSet) {
			// scan the database
			List<List<UItem>> revisedSequence = revisedDataBase.get(sid);
			List<Element> elementList = ucMap.get(sid);
			
			/************************/
			int peuOfSID = 0;
			/************************/
			
			// for each element (subsequence) in sid
			for (Element e : elementList) {
				int tid = e.getTid().intValue();
				List<UItem> uItemset = revisedSequence.get(tid - 1);
				int indexOf = indexOf(lastItem, uItemset);
				Integer ru = e.getRu();
				
				// I-extension
				for (int i = indexOf + 1; i < uItemset.size(); i++) {
					ru = ru - uItemset.get(i).getUtility();
					
					if (itemsForIExtension.contains(uItemset.get(i).getItem())
							&& e.getTime() - uItemset.get(i).getTime() <= timeSpan) {
						Element ele = new Element();
						ele.setSid(sid);
						ele.setTid(tid);
						ele.setNext(-1);
						ele.setAcu(uItemset.get(i).getUtility() + e.getAcu());
						ele.setTime(e.getTime());
						ele.setRu(ru);
						
						Node node2 = mapToINode.get(uItemset.get(i).getItem());
						node2.getUc().addElement(sid, ele);
						
						/**************************/
						if(peuOfSID < ele.getAcu()+ ele.getRu()) {
							peuOfSID = ele.getAcu()+ ele.getRu();
						}
						/**************************/
					} 
										
				}
				
				
				// S-extension
				for (int j = tid; j < revisedSequence.size(); j++) {
					List<UItem> uItemset2 = revisedSequence.get(j);
					for (UItem u : uItemset2) {
						ru = ru - u.getUtility();
						
						if (itemsForSExtension.contains(u.getItem())
								&& e.getTime() - u.getTime() <= timeSpan) {
							Element ele = new Element();
							ele.setNext(-1);
							ele.setTid(j + 1);
							ele.setSid(sid);
							ele.setAcu(e.getAcu() + u.getUtility());
							ele.setTime(e.getTime());
							ele.setRu(ru);
							
							Node node2 = mapToSNode.get(u.getItem());
							node2.getUc().addElementForSExtendsion(sid, ele);
							
							/**************************/
							if(peuOfSID < ele.getAcu()+ ele.getRu()) {
								peuOfSID = ele.getAcu()+ ele.getRu();
							}
							/**************************/
						} 
					}
				}
			}
			
			
			/**************************/
			if(uc.getPEU(sid) != null) {
				uc.setPEU(sid, peuOfSID);
			}			
			/**************************/
		}
		
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
	    
		List<List<Node>> lists = new ArrayList<List<Node>>();
		lists.add(iNodeList);
		lists.add(sNodeList);
		
		
		return lists;
	}

	/**
	 * Get last item in a sequence
	 * 
	 * @param sequence
	 * @return
	 */
	private Integer getLastItem(List<String> sequence) {
		String lastItemset = sequence.get(sequence.size() - 1);
		Integer lastItem = -1;
		if (!lastItemset.contains(" ")) {
			lastItem = Integer.parseInt(lastItemset);
		} else {
			String[] splits = lastItemset.split(" ");
			lastItem = Integer.parseInt(splits[splits.length - 1]);
		}
		
		return lastItem;
	}

	/**
	 * Index of item
	 * 
	 * @param item
	 * @param uList
	 * @return
	 */
	private int indexOf(Integer item, List<UItem> uList) {
		for (int i = 0; i < uList.size(); i++) {
			if (item == uList.get(i).getItem().intValue()) {
				return i;
			}
		}
		return -1;
	}

	private void writeToFile(List<String> sequence, Integer utility, Integer support, double recency) throws IOException {
		String tmp = "";
		for (int i=0; i < sequence.size() - 1; i++) {
			tmp += sequence.get(i) + " -1 ";
		}
		List<Double> value = new ArrayList<>();
		value.add(utility.doubleValue());
		value.add(support.doubleValue());
		value.add(recency);
		if(mapMaximal.containsKey(tmp)) {
			mapMaximal.remove(tmp);
		}
		tmp += sequence.get(sequence.size() - 1) + " -1 ";
		if(mapMaximal.size()!=0) {
			Map<String, List<Double>> mapMaximalCopy = new HashMap<>();
			for(Entry<String, List<Double>> entry:mapMaximal.entrySet()) {
				String PatternString = new String(entry.getKey());
				List<Double> newValue = new ArrayList(entry.getValue());
				mapMaximalCopy.put(PatternString, newValue);
			}
			boolean judge = true;
		leq:for(Entry<String, List<Double>> entry:mapMaximalCopy.entrySet()) {
				String PatternString = entry.getKey();
				//System.out.println("tmp: " + tmp + " patternstring: " + PatternString);
				Integer index = 0;
				String[] PatternStringGroups = PatternString.split("-1");
				String[] tmpGroups = tmp.split("-1");
				int pToMPatternInMap = 0;
				int pToCheckPattern = 0;
				int searchtimes = 0;
				
				if(tmpGroups.length < PatternStringGroups.length) {
					while(true) {
						String checktmp = tmpGroups[pToCheckPattern].strip();
						String patterntmp = PatternStringGroups[pToMPatternInMap].strip();
						if(JudgeSubseqItemset(checktmp, patterntmp)) {
							pToCheckPattern ++;
							pToMPatternInMap ++;
						}else {
							searchtimes ++;
							pToCheckPattern = 0;
							pToMPatternInMap = searchtimes;
						}
						if(PatternStringGroups.length - searchtimes < tmpGroups.length) {
							break;
						}
						if(pToCheckPattern == tmpGroups.length - 1) {
							break;
						}
						
					}
				}else if(tmpGroups.length > PatternStringGroups.length) {
					while(true) {
						String checktmp = tmpGroups[pToCheckPattern].strip();
						//System.out.println(pToMPatternInMap + " " + pToCheckPattern + " " + searchtimes);
						String patterntmp = PatternStringGroups[pToMPatternInMap].strip();
						if(JudgeSubseqItemset(patterntmp, checktmp)) {
							pToCheckPattern ++;
							pToMPatternInMap ++;
						}else {
							searchtimes ++;
							pToCheckPattern = searchtimes;
							pToMPatternInMap = 0;
						}
						if(tmpGroups.length - searchtimes < PatternStringGroups.length) {
							break;
						}
						if(pToMPatternInMap == PatternStringGroups.length - 1) {
							mapMaximal.remove(PatternString);
							break;
						}
						
					}
				}else if(tmp.length() < PatternString.length()){
					while(true) {
						if(!JudgeSubseqItemset(tmpGroups[pToCheckPattern],PatternStringGroups[pToMPatternInMap])){
							continue leq;
						}
						pToCheckPattern ++;
						pToMPatternInMap ++;
						if(pToCheckPattern == tmpGroups.length - 1) break;
					}
					judge = false;
				}else if(tmp.length() > PatternString.length()) {
					while(true) {
						if(!JudgeSubseqItemset(PatternStringGroups[pToMPatternInMap], tmpGroups[pToCheckPattern])){
							continue leq;
						}
						pToCheckPattern ++;
						pToMPatternInMap ++;
						if(pToMPatternInMap == PatternStringGroups.length - 1) break;
					}
					mapMaximal.remove(PatternString);
				}else {
					if(tmp.equals(PatternString)) {
						judge = false;
					}
				}
				
			}
			if(judge) {
				mapMaximal.put(tmp, value);
			}
		}else {
			mapMaximal.put(tmp, value);
		}
		//tmp += "";
		
		/*
		 * if(huspCount%1000==0) { System.out.println(huspCount); }
		 */
		 //System.out.println(tmp+":"+utility);
		 //writer.write(tmp + "   #UTIL: " + utility + "   #SUP: " + support + "   #RECENCY: " + String.format("%.2f", recency));
		 //writer.newLine();
		 //
		//writer.flush();

	}
	 public boolean JudgeSubseqItemset(String subitemset, String itemset) {
		 boolean Judge = true;
		 String[] items = subitemset.split(" ");
		 Integer index = 0;
		 for(String item: items) {
			 index = itemset.substring(index).indexOf(item);
			 if(index.equals(-1)) {
				 Judge = false;
				 break;
			 }
		 }
		 return Judge;
	 }
	private void writeToFileMaximal(String MaximalPattern, List<Double> value) throws IOException {
		 //writer.write(MaximalPattern + "   #UTIL: " + value.get(0) + "   #SUP: " + value.get(1) + "   #RECENCY: " + String.format("%.2f", value.get(2)));
		 //writer.newLine();
		 writer.flush();
	}

}
