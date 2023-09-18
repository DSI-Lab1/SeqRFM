import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C), 2023, JNU
 * FileName: MTChain
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: The MT-Chain data structure of SeqRFM algorithm.
 */

public class MTChain {
	private Map<Integer, List<Element>> uc = new HashMap<Integer, List<Element>>();

	public void addElements(Integer sid, List<Element> elements) {
		uc.put(sid, elements);
	}
	
	// new peu value of each sid
	private Map<Integer, Integer> peu  = new HashMap<Integer, Integer>(); 

	/**
	 * Add element
	 * 
	 * @param sid
	 * @param element
	 */
	public void addElement(Integer sid, Element element) {
		List<Element> list = this.uc.get(sid);
		if (list == null || list.size() == 0) {
			List<Element> elements = new ArrayList<Element>();
			elements.add(element);
			addElements(sid, elements);
		} else {
			Element ele = list.get(list.size() - 1);
			ele.setNext(list.size());
			list.add(element);
			uc.put(sid, list);
		}
	}

	/**
	 * Add element for S-extension
	 * 
	 * @param sid
	 * @param element
	 */
	public void addElementForSExtendsion(Integer sid, Element element) {
		List<Element> list = this.uc.get(sid);
		if (list == null || list.size() == 0) {
			// the first item
			List<Element> elements = new ArrayList<Element>();
			elements.add(element);
			addElements(sid, elements);
		} else {
			int index = -1;
			for (int i = 0; i < list.size(); i++) {
				Element e = list.get(i);
				if (e.getTid().intValue() == element.getTid().intValue()) {
					index = i;
					break;
				}
			}
			if (index != -1) {
				Element e = list.get(index);
				
				// update the ACU value
				if (e.getAcu() < element.getAcu()) {
					e.setAcu(element.getAcu());
				}
			} else {
				Element ele = list.get(list.size() - 1);
				ele.setNext(list.size());
				list.add(element);
				uc.put(sid, list);
			}
		}
	}

	public Map<Integer, List<Element>> getUttilityChain() {
		return this.uc;
	}
	
	// ===========================
	public Integer getPEU(Integer sid) {
		return peu.get(sid);
	}

	public void setPEU(Integer sid, Integer peu) {
		this.peu.put(sid, peu);
	}
	// ===========================
}
