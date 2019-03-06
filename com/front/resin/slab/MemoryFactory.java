package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/14.
 */

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

public class MemoryFactory {

  private List<SlabPage> smallObjList = new java.util.LinkedList<SlabPage>();
  private List<SlabPage> largeObjList = new java.util.LinkedList<SlabPage>();
  PageTable pageTable = new PageTable();

  public static final int DEFAULT_SMALL_PAGE_SIZE = 1 << 20;// 1M
  public static final int DEFAULT_LARGE_PAGE_SIZE = 1 << 25;// 32M
  public static final int DEFAULT_MAX_BUFFER_SIZE = 1 << 26; // 64M

  private int largePageSize = DEFAULT_LARGE_PAGE_SIZE;
  private int smallPageSize = DEFAULT_SMALL_PAGE_SIZE;

  private int maxBufferedSize = DEFAULT_MAX_BUFFER_SIZE;

  private boolean useDirectMemory = false;

  MemoryFactory(boolean useDirectMemory, int smallPageSize, int largePageSize,
      int maxBufferedSize) {
    assert (smallPageSize > 0 && largePageSize > 0 && maxBufferedSize > 0);
    this.useDirectMemory = useDirectMemory;
    this.maxBufferedSize = maxBufferedSize;
  }

  private boolean estimateCacheSize(int size) {
    return size + SlabPage.SLAB_HEADER_SIZE + SlabPage.CHUNK_HEADER_SIZE > smallPageSize;
  }

  public synchronized SlabPage alloc(int size) {
    boolean large = estimateCacheSize(size);
    List<SlabPage> dest = large ? largeObjList : smallObjList;

    SlabPage ret;
    if (dest.size() > 0) {
      ret = dest.remove(0);
      ret.chunkInUse.clear();
    } else {
      ret = new SlabPage();
      ret.chunkInUse = new BitSet(Slab.LARGEST_CHUNK_NUM);
      int pageSize = large ? largePageSize : smallPageSize;
      ByteBuffer buffer = useDirectMemory ? ByteBuffer.allocateDirect(pageSize)
          : ByteBuffer.allocate(pageSize);
      long pageNo = pageTable.generatePageNo(ret);
      if (pageNo == -1) {
        // OutOfMemory
        return null;
      }
      ret.pageObj = new StaticAddress(pageNo, buffer, 0, pageSize);
    }
    return ret;
  }

  public synchronized void reclaim(SlabPage obj) {
    obj.rootMemCache = null;
    obj.prev = null;
    obj.next = null;
    List<SlabPage> dest = (obj.pageObj.getLength() == smallPageSize) ? smallObjList
        : largeObjList;
    if (dest.size() * obj.pageObj.getLength() > maxBufferedSize) {
      long page = obj.pageObj.pageNo;
      SlabPage tableObj = pageTable.removePage(page);
      assert (tableObj == obj);
      obj.pageObj = null;
      obj.chunkInUse = null;
      return;
    } else {
      dest.add(obj);
    }
  }
}
