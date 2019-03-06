package org.rxjava;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by hinotohui on 17/3/15.
 */
public class CustomClassLoader extends URLClassLoader {
    public CustomClassLoader(URL[] urls) {
        super(urls);
    }

    public static <T> T factory(Class<T> clazz){
        try {
            return clazz.newInstance();
        }catch (Throwable e){
            return null;
        }

    }

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {
        String s=factory(String.class);
    }
}
