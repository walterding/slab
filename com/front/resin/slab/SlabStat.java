package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/17.
 */

public class SlabStat {

  public int freeObjectNum;
  public int usedObjectNum;
  
  public long freeObjectSize;
  public long usedObjectSize;
  
  public long totalSpace;
  
  public void add(SlabStat stat) {
    this.freeObjectNum += stat.freeObjectNum;
    this.usedObjectNum += stat.usedObjectNum;
    this.freeObjectSize += stat.freeObjectSize;
    this.usedObjectSize += stat.usedObjectSize;
    this.totalSpace += stat.totalSpace;
  }
  
  @Override
  public String toString() {
    return "free obj num:" + this.freeObjectNum + ", used obj num:" + this.usedObjectNum
       + ",free obj size:" + this.freeObjectSize + ", used obj size:" + this.usedObjectSize
       + ",total space:" + this.totalSpace;
  }
}
