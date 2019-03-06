package com.front.resin.slab;

/**
 * Created by hinotohui on 17/2/14.
 */

import java.nio.ByteBuffer;

public class SharedBuffer extends StaticAddress {

  private SharedBuffer(long pageNo, ByteBuffer b, int o, int length) {
    super(pageNo, b, o, length);
  }
  
  public SharedBuffer(StaticAddress addr) {
    super(addr);
  }

  public void arraycopy(byte[] src, int start, int addition, int length) {
    // check length
    assert (addition + length <= this.length);
    
    for (int i = 0; i<length; i++) {
      this.page.put(this.offset + addition + i, src[start + i]);
    }
  }
  
  public byte[] getData() {
    return this.page.array();
  }
  
  public int getOffset() {
    return this.offset;
  }
}
