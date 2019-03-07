package com.front.resin.slab;

/**
 * 生成一个slab内存分配器，解决小对象分配频繁引发gc的问题
 * Created by hinotohui on 17/2/14.
 */
public class MemCache {

  private static final boolean isDebug = false;
  
  // 目标对象大小
  private final int size;
  private Slab rootSlab;

  LinkedSlabPage full_list = new LinkedSlabPage();
  LinkedSlabPage partial_list = new LinkedSlabPage();
  LinkedSlabPage free_list = new LinkedSlabPage();

  private SlabStat stat = new SlabStat();
  private Object lock = new Object();

  public MemCache(int size, Slab slab) {
    this.size = size;
    this.rootSlab = slab;
  }

  /**
   * 释放所有的整块slab
   */
  public void shrinkMemory() {
    LinkedSlabPage list = new LinkedSlabPage();
    synchronized (lock) {
      this.free_list.swap(list);
    }

    SlabPage head = list.getFirst();
    int objectDelta = 0;
    int spaceDelta = 0;
    while (head != null) {
      objectDelta += head.chunkNum;
      spaceDelta += head.pageObj.length;
      list.remove(head);
      rootSlab.memFactory.reclaim(head);
      head = list.getFirst();
    }
    synchronized (lock) {
      stat.freeObjectNum -= objectDelta;
      stat.totalSpace -= spaceDelta;
    }
  }

  /**
   * 从cache中分配一块内存，用于存放制定的数据。
   * 
   * @param cacheId
   * @return
   */
  public SharedBuffer allocate(int cacheId) {
    synchronized (lock) {
      SlabPage entry = partial_list.getFirst();
      if (entry != null) {
        SharedBuffer ret = entry.allocateChunk();
        assert (ret != null);
        if (entry.isFull()) {
          partial_list.remove(entry);
          full_list.add(entry);
          // TODO debug
          if (isDebug)
            System.out.println("move page from PARTIAL to FULL:" + " " + entry);
        }
        stat.usedObjectNum ++;
        stat.freeObjectNum --;
        return ret;
      }

      entry = free_list.getFirst();

      if (entry == null) {
        // 新建一个Slab块，并初始化
        SlabPage obj = rootSlab.memFactory.alloc(this.size);
        
        // OutOfMemory
        if (obj == null)
          return null;
        obj.init(this);
        stat.freeObjectNum += obj.chunkNum;
        stat.totalSpace += obj.pageObj.length;
        entry = obj;
        assert (obj.chunkNum > 0);
        // TODO debug
        if (isDebug)
          System.out.println("new page:" + " " + entry);
      } else {
        free_list.remove(entry);
        // TODO debug
        if (isDebug)
          System.out.println("move page from FREE:" + " " + entry);
      }
      SharedBuffer ret = entry.allocateChunk();
      assert (ret != null);
      if (entry.isFull()) {
        full_list.add(entry);
        // TODO debug
        if (isDebug)
          System.out.println("move page to FULL:" + " " + entry);
      } else {
        partial_list.add(entry);
        // TODO debug
        if (isDebug)
          System.out.println("move page to PARTIAL:" + " " + entry);
      }
      stat.usedObjectNum ++;
      stat.freeObjectNum --;

      return ret;
    }
  }

  /**
   * 释放数据
   * @param addr
   * @param slabObj
   */
  public void free(StaticAddress addr, SlabPage slabObj) {
    synchronized (lock) {
      boolean wasFull = slabObj.isFull();
      if (wasFull) {
        // TODO debug
        if (isDebug)
          System.out.println("move page from FULL:" + " " + slabObj);
      }
      slabObj.freeChunk(addr);
      if (wasFull) {
        full_list.remove(slabObj);
        if (slabObj.isTotalFree()) {
          free_list.add(slabObj);
          // TODO debug
          if (isDebug)
            System.out.println("move page from FULL to FREE:" + " " + slabObj);
        } else {
          partial_list.add(slabObj);
          // TODO debug
          if (isDebug)
            System.out.println("move page from FULL to PARTIAL:" + " " + slabObj);
        }
      } else {
        if (slabObj.isTotalFree()) {
          partial_list.remove(slabObj);
          free_list.add(slabObj);
          // TODO debug
          if (isDebug)
            System.out.println("move page from PARTIAL to FREE:" + " " + slabObj);
        }
      }
      stat.usedObjectNum --;
      stat.freeObjectNum ++;
    }
    return;
  }

  public int getSize() {
    return size;
  }
  
  public SlabStat stat() {
    SlabStat stat = new SlabStat();
    synchronized (stat) {
      stat.add(this.stat);
    }
    return stat;
  }
}
