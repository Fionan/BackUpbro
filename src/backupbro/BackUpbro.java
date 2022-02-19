/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backupbro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import static java.nio.file.StandardCopyOption.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author fiona
 */
public class BackUpbro {

    /**
     * @param args the command line arguments
     */
    static int fileMoved = 0;
    static int totalFilesToMove = 0;

    public static void main(String[] args) throws FileNotFoundException, IOException {
        Path monitorPath;
        Path backupPath;
        //starting folder  File folder ;
        monitorPath = (args.length > 0) ? Paths.get(args[0]) : Paths.get("C:\\Z\\Folder1\\");
        //destintation folder
        backupPath = (args.length > 0) ? Paths.get(args[1]) : Paths.get("C:\\Z\\BACKUP");

        LinkedList backupFiles = getFilePaths(backupPath);
        //get file list of folder
        LinkedList moniteredFiles = getFilePaths(monitorPath);
        //return a list of files that are not in backup
        LinkedList fileList = (LinkedList) getListOfMissingFiles(backupFiles, moniteredFiles, backupPath, monitorPath);
        //move only the file not in backup already
        totalFilesToMove = fileList.size();
        System.out.println(totalFilesToMove);

        fileList.forEach((f) -> {

            Path to = buildPath((Path) f, monitorPath, backupPath);
            to = Paths.get(backupPath.toString(), to.toString());

            simpleCopy(
                    (Path) f, to);

        }
        );

        //process files that exist
        //check a filebasic get CRC of file
        //check file detail
        //break org file into blocks of bytes
        // create block array of mod block size
        // get file list of backup 
    }

    static List getListOfMissingFiles(LinkedList bF, LinkedList mF, Path backupPath, Path monitorPath) {

        HashSet missingFiles = new HashSet();
        LinkedList newFiles = new LinkedList();

        //fill the set
        bF.forEach((p1) -> {
            missingFiles.add(
                    stripRoot((Path) p1, backupPath)
            );
        });

        mF.forEach((p2) -> {

            if (missingFiles.add(stripRoot((Path) p2, monitorPath))) {

                newFiles.add(p2);
            } else {

            }
        });

        return newFiles;
    }

    static Path buildPath(Path orginalFile, Path orgnRoot, Path backupRoot) {

        return Paths.get(stripRoot(orginalFile, orgnRoot));

    }

    static String stripRoot(Path p1, Path root) {

        String s1 = p1.toString();
        String s2 = root.toString();

        return s1.substring(s2.length());

    }

    static LinkedList getFilePaths(Path root) {

        File rootFile = root.toFile();

        Stack<File> filesToCkeck = new Stack();

        LinkedList myList = new LinkedList<>();

        filesToCkeck.push(rootFile);

        while (!filesToCkeck.empty()) {

            File f = filesToCkeck.pop();

            if (f.isDirectory()) {

                File[] current_dir = f.listFiles();

                for (File F : current_dir) {

                    filesToCkeck.push(F);
                }

            } else {
                myList.add(f.toPath());

            }

        }

        return myList;
    }

    static boolean simpleCopy(Path from, Path to) {

        try {
            File directory = to.toFile();
            if (!directory.exists()) {
                directory.mkdirs();

            }

            Files.copy(from, to, REPLACE_EXISTING);

            System.out.print("Moving: " + from.getFileName().toString() + "\t\t\t\t" + ((totalFilesToMove / ++fileMoved) * 100) + "%");

            System.out.print("\r");
            return true;
        } catch (IOException ex) {
            Logger.getLogger(BackUpbro.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;

    }

    static boolean simpleCopy(String org, String dest) {

        Path from = Paths.get(org);
        Path to = Paths.get(dest);

        return simpleCopy(from, to);
    }

    static boolean compareFilesQuick(long org_crc, String current) {
        try {

            FileInputStream f2 = new FileInputStream(current);

            long crc_current = crc2(f2);

            return org_crc == crc_current;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BackUpbro.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BackUpbro.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    static boolean compareFilesQuick(String org, String current) {

        try {
            FileInputStream f1 = new FileInputStream(org);
            FileInputStream f2 = new FileInputStream(current);

            long crc_old = crc2(f1);
            long crc_current = crc2(f2, true);

            return crc_current == crc_old;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BackUpbro.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BackUpbro.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;

    }

    static long crc2(FileInputStream file) throws IOException {

        CheckedInputStream check = new CheckedInputStream(file, new CRC32());
        BufferedInputStream in
                = new BufferedInputStream(check);
        while (in.read() != -1) {
            // Read file in completely
        }
        in.close();

        return check.getChecksum().getValue();

    }

    static long crc2(FileInputStream file, boolean log) throws IOException {

        CheckedInputStream check = new CheckedInputStream(file, new CRC32());
        BufferedInputStream in
                = new BufferedInputStream(check);
        while (in.read() != -1) {
            // Read file in completely
        }
        in.close();

        if (log) {
            // for logging 
            System.out.println("" + check.getChecksum().getValue());
        }

        return check.getChecksum().getValue();

    }
}
