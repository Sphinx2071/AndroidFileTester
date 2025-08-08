package edu.androidfiletester.sharedfileopslib.util;

import java.io.File;

public class fileTester {

    public static void readFileMaybe(String filepath){
        File file = new File(filepath);
        System.out.println("Filename: " + file.getName());
        System.out.println(file.isFile());
        System.out.println(file.isDirectory());
        System.out.println(file.canRead());
        System.out.println(file.delete());
        try{
            System.out.println(file.createNewFile());
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
