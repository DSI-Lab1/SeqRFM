/**
 * Copyright (C), 2023, JNU
 * FileName: Element
 * Author:   Yanxin Zheng
 * Date:     2023/9/15 23:25
 * Description: The element class of SeqRFM algorithm.
 */

public class Element {
	private Integer sid;
	private Integer tid;
	private Integer acu;
	private Integer ru;
	private Integer next;
	private Integer timeStamp;

	public Integer getSid() {
		return sid;
	}

	public void setSid(Integer sid) {
		this.sid = sid;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public Integer getAcu() {
		return acu;
	}

	public void setAcu(Integer acu) {
		this.acu = acu;
	}
	
	public Integer getTime() {
		return timeStamp;
	}

	public void setTime(Integer timeStamp) {
		this.timeStamp = timeStamp;
	}


	public Integer getRu() {
		return ru;
	}

	public void setRu(Integer ru) {
		this.ru = ru;
	}

	public Integer getNext() {
		return next;
	}

	public void setNext(Integer next) {
		this.next = next;
	}

	@Override
	public String toString() {
		return "Element [sid=" + sid + ", tid=" + tid + ", acu=" + acu + ", ru=" + ru + ", time=" + timeStamp + ", next=" + next + "]";
	}

}
