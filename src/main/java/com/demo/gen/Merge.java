package com.demo.gen;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.HexDump;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.security.MessageDigest.getInstance;

/**
 * Created by pangdan on 2017/5/6.
 */
public class Merge {
    private static final Logger LOG = LoggerFactory.getLogger(Merge.class);
    // 被合并的文件
    static File willBeMerge = new File("/Users/pangdan/java/bigfilemerge/b.txt");
    // 合并到文件
    static File mergeSource = new File("/Users/pangdan/java/bigfilemerge/a.txt");

    // 拆分临时文件夹
    static File splitTmpDir = new File("/Users/pangdan/java/bigfilemerge/split/");
    //
    static File mergeTarget = new File("/Users/pangdan/java/bigfilemerge/result.txt");
    static MessageDigest digest = null;
    static Charset UTF8 = Charset.forName("UTF-8");

    static {
        try {
            digest = getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        splitTmpDir.deleteOnExit();
        split(willBeMerge, splitTmpDir, 2);

        merge(mergeSource, splitTmpDir, mergeTarget);
    }

    static void merge(File mergeSource, File mergeSplit, File target) {

    }

    static OutputStream[] openFiles(File toDir, String prefix, String suffix, int mod) {
        if (!toDir.exists()) {
            if (!toDir.mkdirs()) {
                LOG.error("创建目录失败！{}", toDir.getAbsolutePath());
                throw new RuntimeException("创建目录失败！");
            }
        }
        OutputStream[] splitFiles = new OutputStream[mod];
        int padding = String.valueOf(mod).length();
        for (int i = 0; i < mod; i++) {
            String name = StringUtils.leftPad(i + "", padding, "0");
            File of = new File(toDir.getAbsolutePath() + "/" + prefix + "-" + name + suffix);
            try {
                of.createNewFile();
                splitFiles[i] = new FileOutputStream(of);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return splitFiles;
    }

    static void closeFiles(OutputStream[] files) {
        for (int i = 0; i < files.length; i++) {
            IOUtils.closeQuietly(files[i]);
        }
    }

    static void split(File file, File toDir, int totalRecursion) throws IOException {
        FileUtils.deleteDirectory(toDir);
        split(file, toDir, totalRecursion, "split", 1);
    }

    static void split(File file, File toDir, int totalRecursion, String prefix, int recursion) throws IOException {
        int mod = 2;

        FileUtils.forceMkdir(toDir);
        FileReader fileReader = new FileReader(file);
        LineIterator iterator = IOUtils.lineIterator(fileReader);

        int padding = String.valueOf(mod).length();
        String recursionPadding = StringUtils.leftPad(recursion + "", padding, "0");
        String suffix = "." + recursionPadding + ".txt";
        OutputStream[] splitFiles = openFiles(toDir, prefix, suffix, mod);

        while (iterator.hasNext()) {
            String line = iterator.next();
            String[] tokens = line.split("\t");
            String key = tokens[0];
//            String content = tokens[1];
            int shardKey = shardKey(key, mod);
            System.out.println(shardKey);
            IOUtils.write(line + "\n", splitFiles[shardKey], UTF8);
        }
        closeFiles(splitFiles);
        File[] spilted = splitTmpDir.listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter(suffix));

        if (recursion == totalRecursion) {
            return;
        }
        for (int i = 0; i < spilted.length; i++) {
            String name = StringUtils.leftPad(i + "", padding, "0");
            split(spilted[i], splitTmpDir, totalRecursion, prefix + "-" + name, recursion + 1);
        }
    }

    static int shardKey(String key, int mod) {
        return Math.abs(new String(digest.digest(key.getBytes(UTF8))).hashCode() % mod);
    }
}
