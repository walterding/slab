package com.front.resin.slab;

/**
 * Created by hinotohui on 18/7/17.
 */
public class Test {

    public static void  main(String[]  args){
        Slab slab=new Slab();

        for(int i=0;i<1000;i++){
            SharedBuffer sharedBuffer=slab.allocate(125);
            slab.free(sharedBuffer)
        }
    }
}
