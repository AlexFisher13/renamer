package org.example;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RenameFilesExample {
    public static void main(String[] args) {
        String folderPath = "/Users/fisher/Desktop/Photos";
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            System.out.println("Specified path is not a folder");
            return;
        }

        File[] files = folder.listFiles();
        for (File file : files) {
            try {
                if (file.isFile()) {
                    String newName = getNewFileName(file);
                    File newFile = new File(folderPath + "/" + newName);
                    boolean success = file.renameTo(newFile);
                    if (!success) {
                        System.out.println("Could not rename file: " + file.getName());
                    }
                }
            } catch (IOException | ImageProcessingException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String getNewFileName(File file) throws IOException, ImageProcessingException {
        String extension = getFileExtension(file.getName()).toLowerCase();
        Metadata metadata;
        String dateStr;
        switch (extension) {
            case "jpg":
            case "jpeg":
                metadata = ImageMetadataReader.readMetadata(file);
                ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                Date date = directory != null ? directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) : new Date(file.lastModified());
                if (date == null) {
                    date = new Date(file.lastModified());
                }
                dateStr = formatDate(date);
                break;
            case "mov":
                IsoFile isoFile = new IsoFile(file.getPath());
                MovieHeaderBox movieHeaderBox = isoFile.getMovieBox().getMovieHeaderBox();
                Date creationTime = new Date(movieHeaderBox.getCreationTime().getTime());
                dateStr = formatDate(creationTime);
                break;
            default:
                dateStr = "";
                break;
        }
        return dateStr + "." + extension;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        } else {
            return fileName.substring(dotIndex + 1);
        }
    }

    private static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
