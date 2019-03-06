package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/14.
 */

import java.util.BitSet;

public class Slab {

  // static field
  private static final int[] CACHE_SIZES = new int[] { 32, 64, 96, 128, 128,
      192, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072,
      262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, }; //33554432

  public static final int LARGEST_CHUNK_NUM = 1 << 15;

  // object field
  private MemCache[] sizedCaches;

  final MemoryFactory memFactory;

  private Thread timerThread;

  /**
   * 检测线程，每秒检测并释放空闲的页
   */
  Runnable worker = new Runnable() {

    @Override
    public void run() {
      while (true) {
        for (MemCache cache : sizedCaches) {
          try {
            cache.shrinkMemory();
          } catch (Throwable e) {
            // ignore
          }
        }

        // sleep 1 sec
        synchronized (this) {
          try {
            this.wait(1000l);
          } catch (InterruptedException e) {
            Thread.interrupted();
          }
        }
      }
    }

  };

  // constructor
  public Slab(boolean useDirectMemory) {
    this(useDirectMemory, MemoryFactory.DEFAULT_SMALL_PAGE_SIZE,
        MemoryFactory.DEFAULT_LARGE_PAGE_SIZE, MemoryFactory.DEFAULT_MAX_BUFFER_SIZE);
  }

  public Slab(boolean useDirectMemory, int smallPageSize, int largePageSize, int maxBufferedSize) {
    sizedCaches = new MemCache[CACHE_SIZES.length];
    for (int i = 0; i < sizedCaches.length; i++) {
      sizedCaches[i] = new MemCache(CACHE_SIZES[i], this);
    }

    memFactory = new MemoryFactory(useDirectMemory, smallPageSize,
        largePageSize, maxBufferedSize);

    timerThread = new Thread(worker);
    timerThread.setDaemon(true);
    timerThread.start();
  }

  // functions

  /**
   * 申请内存
   * 
   * @param size
   * @return
   */
  public SharedBuffer allocate(int size) {
    int cacheId = findCacheBySize(size + SlabPage.CHUNK_HEADER_SIZE);
    if (cacheId <  0)
      return null;
    
    MemCache cache = sizedCaches[cacheId];
    SharedBuffer ret = null;
    ret = cache.allocate(cacheId);

    return ret;
  }

  /**
   * 释放内存
   * 
   * @param addr
   */
  public void free(SharedBuffer addr) {
    if (addr == null)
      return;

    long pageNo = SlabPage.extractPageNoFromChunkAddress(addr);
    SlabPage slabObj = this.memFactory.pageTable.getPage(pageNo);
    slabObj.rootMemCache.free(addr, slabObj);

    // remove object ref for GC
    addr.page = null;

    return;
  }

  /**
   * 获取统计信息
   * 
   * @return
   */
  public SlabStat stat() {
    SlabStat ret = new SlabStat();
    for (MemCache cache : sizedCaches) {
      SlabStat stat  = cache.stat();
      stat.freeObjectSize = stat.freeObjectNum * cache.getSize();
      stat.usedObjectSize = stat.usedObjectNum * cache.getSize();
      ret.add(stat);
    }
    return ret;
  }

  private int findCacheBySize(int size) {
    for (int i = 0; i < sizedCaches.length; i++) {
      if (sizedCaches[i].getSize() > size)
        return i;
    }
    return -1;
  }

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception{
    BitSet bs = new BitSet(1);
    bs.set(0);
    System.out.println("id:" + bs.nextClearBit(0));
    Slab slab = new Slab(false);
    SharedBuffer[] list = new SharedBuffer[6];
    for (int i = 0; i < list.length; i++) {
      list[i] = slab.allocate(510*1024 );
      list[i].page.putLong(list[i].offset, i + 1);
    }
    System.out.println(slab.stat());

    for (int i = 0; i < list.length; i++)
      slab.free(list[i]);

    System.out.println(slab.stat());
    while (true){
      Thread.sleep(1500l);
      System.out.println(slab.stat());
    }
  }

}
