package com.apkscanner.test;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.apkspectrum.util.Log;

// import org.apache.commons.lang.StringUtils;

public class ZipUtils {

    private static final int COMPRESSION_LEVEL = 8;

    private static final int BUFFER_SIZE = 1024 * 2;


    /**
     * 지정된 폴더를 Zip 파일로 압축한다.
     * 
     * @param sourcePath - 압축 대상 디렉토리
     * @param output - 저장 zip 파일 이름
     * @throws Exception
     */

    public static InputStream getInputStream(String tarFileName) throws Exception {
        if (tarFileName
                .substring(tarFileName.lastIndexOf(".") + 1, tarFileName.lastIndexOf(".") + 3)
                .equalsIgnoreCase("gz")) {
            System.out.println("Creating an GZIPInputStream for the file");
            return new GZIPInputStream(new FileInputStream(new File(tarFileName)));
        } else {
            System.out.println("Creating an InputStream for the file");
            return new FileInputStream(new File(tarFileName));
        }
    }

    public static void zip(String sourcePath, String output) throws Exception {

        // 압축 대상(sourcePath)이 디렉토리나 파일이 아니면 리턴한다.
        File sourceFile = new File(sourcePath);
        if (!sourceFile.isFile() && !sourceFile.isDirectory()) {
            throw new Exception("압축 대상의 파일을 찾을 수가 없습니다.");
        }

        //

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(output); // FileOutputStream
            bos = new BufferedOutputStream(fos); // BufferedStream
            zos = new ZipOutputStream(bos); // ZipOutputStream
            zos.setLevel(COMPRESSION_LEVEL); // 압축 레벨 - 최대 압축률은 9, 디폴트 8
            zipEntry(sourceFile, sourcePath, zos); // Zip 파일 생성
            zos.finish(); // ZipOutputStream finish
        } finally {
            if (zos != null) {
                zos.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 압축
     * 
     * @param sourceFile
     * @param sourcePath
     * @param zos
     * @throws Exception
     */
    private static void zipEntry(File sourceFile, String sourcePath, ZipOutputStream zos)
            throws Exception {
        // sourceFile 이 디렉토리인 경우 하위 파일 리스트 가져와 재귀호출
        if (sourceFile.isDirectory()) {
            if (sourceFile.getName().equalsIgnoreCase(".metadata")) { // .metadata 디렉토리 return
                return;
            }
            File[] fileArray = sourceFile.listFiles(); // sourceFile 의 하위 파일 리스트
            for (int i = 0; i < fileArray.length; i++) {
                zipEntry(fileArray[i], sourcePath, zos); // 재귀 호출
            }
        } else { // sourcehFile 이 디렉토리가 아닌 경우
            BufferedInputStream bis = null;
            try {
                String sFilePath = sourceFile.getPath();
                String zipEntryName =
                        sFilePath.substring(sourcePath.length() + 1, sFilePath.length());

                bis = new BufferedInputStream(new FileInputStream(sourceFile));
                ZipEntry zentry = new ZipEntry(zipEntryName);
                zentry.setTime(sourceFile.lastModified());
                zos.putNextEntry(zentry);

                byte[] buffer = new byte[BUFFER_SIZE];
                int cnt = 0;
                while ((cnt = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    zos.write(buffer, 0, cnt);
                }
                zos.closeEntry();
            } finally {
                if (bis != null) {
                    bis.close();
                }
            }
        }
    }

    /**
     * Zip 파일의 압축을 푼다.
     *
     * @param zipFile - 압축 풀 Zip 파일
     * @param targetDir - 압축 푼 파일이 들어간 디렉토리
     * @param fileNameToLowerCase - 파일명을 소문자로 바꿀지 여부
     * @throws Exception
     */
    public static ArrayList<String> unimagezipfromfile(File zipFile, File targetDir,
            String findFile, boolean fileNameToLowerCase) throws Exception {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry zentry = null;

        ArrayList<String> arrayImage = new ArrayList<String>();

        try {
            fis = new FileInputStream(zipFile); // FileInputStream
            zis = new ZipInputStream(fis); // ZipInputStream

            while ((zentry = zis.getNextEntry()) != null) {
                String fileNameToUnzip = zentry.getName();
                // if(fileNameToUnzip.endsWith(".png") && fileNameToUnzip.indexOf("res/") >= 0) {
                if (fileNameToUnzip.contains(findFile)) {
                    File subDir = new File(targetDir + "/" + fileNameToUnzip);
                    if (subDir.getParentFile() != null && !subDir.getParentFile().exists()) {
                        subDir.getParentFile().mkdirs();
                    }

                    Log.d("size : " + fis.available() + " : " + subDir);

                    File targetFile = new File(targetDir, fileNameToUnzip);
                    unzipEntry(zis, targetFile);
                    arrayImage.add(targetFile.getAbsolutePath());
                } else {
                    continue;
                }
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        return arrayImage;
    }


    /**
     * Zip 파일의 한 개 엔트리의 압축을 푼다.
     *
     * @param zis - Zip Input Stream
     * @param filePath - 압축 풀린 파일의 경로
     * @return
     * @throws Exception
     */
    protected static File unzipEntry(ZipInputStream zis, File targetFile) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);

            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return targetFile;
    }


    public void zipImageTest() {
        String apkFilePath = "/home/sunggyu.kam/MelOn.apk";
        String tempFilePath = "/tmp/ApkScanner/tmp_image.png";

        ArrayList<String> imagePathList = new ArrayList<String>();
        ArrayList<ImageIcon> imageIconList0 = new ArrayList<ImageIcon>();
        ArrayList<ImageIcon> imageIconList1 = new ArrayList<ImageIcon>();
        ArrayList<ImageIcon> imageIconList2 = new ArrayList<ImageIcon>();
        ArrayList<ImageIcon> imageIconList3 = new ArrayList<ImageIcon>();


        Log.i("start");


        Log.i("get ImagePath List by ZipFile");
        try {
            ZipFile zipFile = new ZipFile(apkFilePath);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                String path = e.getName();
                if (path.endsWith(".png")) imagePathList.add(path);

            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("get ImagePath List by ZipFile completed");
        Log.i("imagePathList size " + imagePathList.size());


        try {
            Log.i("Test #1 - ImageIO start");
            ZipFile zipFile = new ZipFile(apkFilePath);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                String path = e.getName();
                if (path.endsWith(".png") && path.startsWith("res")) {
                    InputStream is = zipFile.getInputStream(e);
                    imageIconList0.add(new ImageIcon(ImageIO.read(is), path));
                }
            }
            zipFile.close();
            Log.i("Test #1 - ImageIO completed");
            // Log.i("zip completed0.." + imageIconList0.size());
            // Log.i("zip completed0.." + imageIconList0.get(0).getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Log.i("Test #2 - BufferedInputStream start");
            ZipFile zipFile = new ZipFile(apkFilePath);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                String path = e.getName();
                if (path.endsWith(".png") && path.startsWith("res")) {
                    byte[] buffer = new byte[(int) e.getSize()];
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(e));
                    bis.read(buffer);
                    imageIconList1.add(new ImageIcon(buffer, path));
                }
            }
            zipFile.close();
            Log.i("Test #2 - BufferedInputStream completed");
            // Log.i("zip completed.." + imageIconList1.size());
            // Log.i("zip completed.." + imageIconList1.get(0).getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Log.i("Test #3 - jar URL start");
            ZipFile zipFile = new ZipFile(apkFilePath);
            String jarPath = "jar:file:" + apkFilePath + "!/";

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                String path = e.getName();
                if (path.endsWith(".png") && path.startsWith("res")) {
                    imageIconList2.add(new ImageIcon(new URL(jarPath + path), path));
                }
            }
            zipFile.close();
            Log.i("Test #3 - jar URL completed");
            // Log.i("zip completed2.." + imageIconList2.size());
            // Log.i("zip completed2.." + imageIconList2.get(0).getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Log.i("Test #4 - unzip start");
            ZipFile zipFile = new ZipFile(apkFilePath);

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry e = zipEntries.nextElement();
                String path = e.getName();
                if (path.endsWith(".png") && path.startsWith("res")) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(tempFilePath);

                        byte[] buffer = new byte[(int) e.getSize()];
                        int len = zipFile.getInputStream(e).read(buffer);
                        fos.write(buffer, 0, len);

                        File iconfile = new File(tempFilePath);
                        imageIconList3.add(new ImageIcon(iconfile.getPath(), path));
                        iconfile.delete();
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                }
            }
            zipFile.close();
            Log.i("Test #4 - unzip completed");
            // Log.i("zip completed3.." + imageIconList3.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
