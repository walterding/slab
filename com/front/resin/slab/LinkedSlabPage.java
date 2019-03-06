package com.front.resin.slab;

/**
 * SlabPage�����������̰߳�ȫ�ġ�
 * Created by hinotohui on 17/3/15.
 *
 */
public class LinkedSlabPage {

	private SlabPage head = null;
	private SlabPage tail = null;
	
	public void add(SlabPage obj) {
		obj.prev = tail;
		obj.next = null;
		if (tail == null) {
			head = obj;
		} else {
			tail.next = obj;
		}
		tail = obj;
	}
	
	public void remove(SlabPage obj) {
		if (obj.prev != null) {
			obj.prev.next = obj.next;
		} else {
			head = obj.next;
		}
		if (obj.next != null) {
			obj.next.prev = obj.prev;
		} else {
			tail = obj.prev;
		}
	}
	
	public SlabPage getFirst() {
		return head;
	}
	
	/**
	 * ����������list���ڲ����ݡ�
	 * @param list
	 */
	public void swap(LinkedSlabPage list) {
		SlabPage temp = this.head;
		this.head = list.head;
		list.head = temp;
		
		temp = this.tail;
		this.tail = list.tail;
		list.tail = temp;
	}
}
