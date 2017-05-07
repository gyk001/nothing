package com.demo.gen;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by pangdan on 2017/5/6.
 */
public class BigFileGen {
    public static void main(String[] args) throws IOException {
        File b = new File("/Users/pangdan/java/bigfilemerge/b.txt");
        for (int i = 0; i < 10000; i++) {
            FileUtils.write(b, "id-" + i + "\tBBBBB" + i + "\n", Charset.forName("UTF-8"), true);
        }
    }
}
