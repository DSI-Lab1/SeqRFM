import java.util.ArrayList;

/**
 * Copyright (C), 2023, JNU
 * FileName: Node
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: The node class of SeqRFM algorithm.
 */
import java.util.List;
import java.util.Map;

public class Node {
	private List<String> sequence = new ArrayList<>();
	private MTChain uc = new MTChain();

	public List<String> getSequence() {
		return sequence;
	}
	
	public List<List<Integer>> getSequenceInt() {
		List<String> sequence = this.getSequence();
		List<Integer> itemsetInt = new ArrayList<>();
		List<List<Integer>> sequenceInt = new ArrayList<>();
		for(int i = 0; i < sequence.size(); i++) {
			String[] s = sequence.get(i).split(" ");
			for(int j = 0; j < s.length; j++) {
				int itemInt = Integer.parseInt(s[j]);
				itemsetInt.add(itemInt);
			}
			sequenceInt.add(itemsetInt);
			itemsetInt = new ArrayList<>();
		}
		return sequenceInt;
	}
	
	public boolean containsQuery(List<List<Integer>> query) {
		List<List<Integer>> t = this.getSequenceInt();
		int n = query.size(), m = t.size();
		if(n > m) 
			return false;
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (containsItemset(t.get(j), query.get(i))) {
        	//if (t.get(j).get(0).equals(query.get(i).get(0))) {
                i++;
            }
            j++;
        }
        return i == n;
    }
	
	public boolean containsItemset(List<Integer> t, List<Integer> query) {
		int n = query.size(), m = t.size();
		if(n > m)
			return false;
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (t.get(j).equals(query.get(i))) {
                i++;
            }
            j++;
        }
        return i == n;
    }
	
	public int[] MatchPosition(List<List<Integer>> query) {
		List<List<Integer>> t = this.getSequenceInt();
		int n = query.size(), m = t.size();
        int i = 0, j = 0, ii = 0;
        while (i < n && j < m) {
            if (iiMatchPosition(t.get(j), query.get(i)) == query.get(i).size()) {
                i++;
            }
            else {
            	ii = iiMatchPosition(t.get(j), query.get(i));
            }
            j++;
        }
        int[] position = new int[2];
        position[0] = i;
        position[1] = ii;
//        System.out.println(getSequence() + "position: [" + position[0] + ", " + position[1] + "]");
        return position;
    }
	
	public int iiMatchPosition(List<Integer> t, List<Integer> query) {
		int n = query.size(), m = t.size();
        int ii = 0, jj = 0;
        while (ii < n && jj < m) {
            if (t.get(jj).equals(query.get(ii))) {
                ii++;
            }
            jj++;
        }
        return ii;
    }

	public void addItemSet(String itemset) {
		sequence.add(itemset);
	}

	public MTChain getUc() {
		return uc;
	}

	public Integer[] getSupUtility() {
		Map<Integer, List<Element>> uttilityChain = uc.getUttilityChain();
		Integer utility = 0;
		Integer sup = 0;
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			Integer max = 0;
			for (Element value : valueList) {
				if (value.getAcu() > max) {
					max = value.getAcu();
				}
			}
			if(max != 0) {
				sup++;
			}
			utility += max;
		}
		Integer[] SupUtility = new Integer[2];
		SupUtility[0] = utility;
		SupUtility[1] = sup;
		return SupUtility;
	}
	
	public double getRecency(double delta, Map<Integer, Integer> mapCurrentTime) {
		Map<Integer, List<Element>> uttilityChain = uc.getUttilityChain();
		double recency = 0.0;
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			int currentTime = mapCurrentTime.get(me.getValue().get(0).getSid());
			List<Element> valueList = me.getValue();
			double rec = Math.pow((1-delta), (currentTime - valueList.get(0).getTime()));
			recency += rec;
		}
		return recency;
	}

	public Integer getPEU() {
		Integer peu = 0;
		Map<Integer, List<Element>> uttilityChain = uc.getUttilityChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> valueList = me.getValue();
			Integer max = Integer.MIN_VALUE;
			for (Element value : valueList) {
				Integer tmp = 0;
				if (value.getRu() > 0) {
					tmp = value.getAcu() + value.getRu();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			peu += max;
		}
		return peu;
	}
	
	/**
	 * Calculate the PEU value of each sid (sequence)
	 * 
	 * @param sid
	 * @return
	 */
	public Integer getSequencePEU(Integer sid) {
		Map<Integer, List<Element>> uttilityChain = uc.getUttilityChain();
		List<Element> elementList = uttilityChain.get(sid);
		
		// for each element
		if (elementList != null || elementList.size() > 0) {
			Integer max = -9999;
			
			// for each item in this element
			for (Element value : elementList) {
				Integer tmp = 0;
				if (value.getRu() > 0) {
					tmp = value.getAcu() + value.getRu();
				}
				if (tmp > max) {
					max = tmp;
				}
			}
			return max;
		}
		return 0;
	}
	

	public void printNode() {
		String str = "";
		for (String s : sequence) {
			str += s + ";";
		}
		str = str.substring(0, str.length() - 1);
		System.out.println("---------------" + str + "-----------------");
		Map<Integer, List<Element>> uttilityChain = uc.getUttilityChain();
		for (Map.Entry<Integer, List<Element>> me : uttilityChain.entrySet()) {
			List<Element> value = me.getValue();
			for (Element e : value) {
				System.out.print(e.getSid() + " " + e.getTid() + " " + e.getAcu() + " " + e.getRu() + " " + e.getNext()
						+ "--->");
			}
			System.out.println();
		}
		System.out.println("---------------" + str + "-----------------");
	}
}
