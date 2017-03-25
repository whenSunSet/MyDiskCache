package com.example.heshixiyang.mydiskcache.fileTree;

/**
 * Created by heshixiyang on 2017/3/23.
 */

import java.io.File;

/**
 * 一个可以访问文件树的实用class
 */
public class FileTree {

    /**
     * 遍历文件树中所有文件，该方法接收一个visitor并且将会在这个文件夹中所有的文件都调用这个方法
     * 对每个目录进行递归
     * @param directory the directory to iterate
     * @param visitor the visitor that will be invoked for each directory/file in the tree
     */
    public static void walkFileTree(File directory, FileTreeVisitor visitor) {
        visitor.preVisitDirectory(directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.isDirectory()) {
                    walkFileTree(file, visitor);
                } else {
                    visitor.visitFile(file);
                }
            }
        }
        visitor.postVisitDirectory(directory);
    }

    /**
     * 删除这个文件目录中的所有文件包括子目录
     */
    public static boolean deleteContents(File directory) {
        File[] files = directory.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                success &= deleteRecursively(file);
            }
        }
        return success;
    }

    /**
     * 安全地删除这个目录中的所有文件
     * @param file a file or directory
     * @return true if the file/directory could be deleted
     */
    public static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            deleteContents(file);
        }
        // if I can delete directory then I know everything was deleted
        return file.delete();
    }

}
