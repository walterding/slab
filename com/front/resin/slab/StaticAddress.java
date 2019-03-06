package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/20.
 */

import java.nio.ByteBuffer;

class StaticAddress {
	protected ByteBuffer page;
	protected int offset;
	protected int length;
	protected final long pageNo;
	public StaticAddress(long pageNo, ByteBuffer b, int o, int length) { this.pageNo = pageNo; this.page = b; this.offset = o; this.length = length;}
	public StaticAddress(StaticAddress that) { this.pageNo = 0l; this.page = that.page; this.offset = that.offset; this.length = that.length; }
	public int getOffset() {
		return offset;
	}
	public int getLength() {
		return length;
	}
	
	@Override
	public String toString() {
	  return "page:" + pageNo + "(" + page + ")" + ", offset:"  + offset + ",length:" + length;
	}
}