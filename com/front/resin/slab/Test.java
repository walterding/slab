package com.front.resin.slab;

/**
 * Created by hinotohui on 18/7/17.
 */
public class Test {
    private int b=0;

    public class A{
        private int a;

        public A(){
            this.a=1;
        }

        public void a(){
            b=2;
        }
    }

    public  void test(){
        System.out.println(b);
    }

    public static void  main(String[]  args){
        Test test=new Test();
        Test.A a=test.new A();
        a.a();
        test.test();

    }
}
