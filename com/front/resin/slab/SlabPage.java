package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/17.
 */
import java.util.BitSet;

public class SlabPage {

  public static final int SLAB_HEADER_SIZE = 0;
  public static final int CHUNK_HEADER_SIZE = 8;

  private static final boolean isDebug = false;

  MemCache rootMemCache;
  SlabPage prev;
  SlabPage next;
  // variables below could be written to the buffer, which affects HEADER_SIZE
  int chunkNum;
  BitSet chunkInUse;

  StaticAddress pageObj;

  public boolean isTotalFree() {
    return chunkInUse.isEmpty();
  }

  public boolean isFull() {
    int i = chunkInUse.nextClearBit(0);
    return i < 0 || i >= chunkNum;
  }

  public SharedBuffer allocateChunk() {
    int i = this.chunkInUse.nextClearBit(0);
    // TODO debug
    if (isDebug)
      System.out.println("allocChunk:" + i + "/" + chunkNum + ", " + this);
    // full
    if (i < 0 || i >= this.chunkNum)
      return null;
    this.chunkInUse.set(i);
    int offset = this.pageObj.offset + SLAB_HEADER_SIZE + i
        * rootMemCache.getSize();
    this.pageObj.page.putLong(offset, this.pageObj.pageNo);
    SharedBuffer addr = new SharedBuffer(this.pageObj);
    addr.offset = offset + CHUNK_HEADER_SIZE;
    addr.length = rootMemCache.getSize();
    return addr;
  }

  public void init(MemCache memCache) {
    this.rootMemCache = memCache;
    this.chunkNum = (this.pageObj.length - SLAB_HEADER_SIZE)
        / (memCache.getSize());
  }

  public static long extractPageNoFromChunkAddress(StaticAddress addr) {
    return addr.page.getLong(addr.offset - CHUNK_HEADER_SIZE);
  }

  public void freeChunk(StaticAddress addr) {
    int index = (addr.getOffset() - SlabPage.SLAB_HEADER_SIZE)
        / rootMemCache.getSize();
    this.chunkInUse.clear(index);
    // TODO debug
    if (isDebug)
      System.out.println("freeChunk:" + index + "/" + chunkNum + ", " + this);
  }

  @Override
  public String toString() {
    return "page:" + this.pageObj + ", full:" + isFull() + ", inuse:"
        + chunkInUse.cardinality();
  }
}
