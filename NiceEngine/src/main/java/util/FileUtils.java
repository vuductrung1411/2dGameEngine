package util;

import editor.Debug;
import components.Sprite;
import editor.MessageBox;
import editor.ReferenceConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {

    final static String defaultAssetFolder = "Assets";
    final static String defaultSprite = "assets/images/Default Sprite.png";
    public final static Map<String, String> icons = new HashMap<>() {
        {
            put("FOLDER", "assets/images/folder-icon.png");
            put("LEFT_ARROW", "assets/images/left-arrow-icon.png");
            put("RIGHT_ARROW", "assets/images/right-arrow-icon.png");
            put("JAVA", "assets/images/java-icon.png");
            put("FILE", "assets/images/file-icon.png");
            put("SOUND", "assets/images/sound-icon.png");
            put("GAME_OBJECT", "assets/images/gameobject-icon.png");
        }
    };

    private static List<String> imageExtensions = List.of("jpg", "jpeg", "png");
    private static List<String> soundExtensions = List.of("ogg", "mp3", "wav", "flac", "aiff", "m4a");

    public static List<File> getAllFiles() {
        return getAllFiles(defaultAssetFolder);
    }

    public static List<File> getAllFiles(String folder) {
        List<File> files = new ArrayList<>();
        File directory = new File(folder);
        if (directory.isDirectory()) {
            File[] filesList = directory.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isFile()) {
                        files.add(file);
                    } else if (file.isDirectory()) {
                        files.addAll(getAllFiles(file.getPath()));
                    }
                }
            }
        }
        return files;
    }

    // Get file only,
    public static List<File> getFilesWithReferenceConfig(ReferenceConfig refConfig) {
        List<File> files = new ArrayList<>();

        switch (refConfig.type) {
            case SPRITE -> {
                files.addAll(getAllFilesWithExtensions(imageExtensions));
                break;
            }
            case JAVA -> {
                List<String> tmp = List.of("java");
                files.addAll(getAllFilesWithExtensions(tmp));
                break;
            }
            case SOUND -> {
                files.addAll(getAllFilesWithExtensions(soundExtensions));
                break;
            }
        }

        return files;
    }

    public static List<File> getAllFilesWithExtensions(List<String> extensions) {
        return getAllFilesWithExtensions(defaultAssetFolder, extensions);
    }

    public static List<File> getAllFilesWithExtensions(String folder, List<String> extensions) {
        List<File> files = new ArrayList<>();
        File directory = new File(folder);
        if (directory.isDirectory()) {
            File[] filesList = directory.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        for (String extension : extensions) {
                            if (fileName.endsWith(extension)) {
                                files.add(file);
                                break;
                            }
                        }
                    } else if (file.isDirectory()) {
                        files.addAll(getAllFilesWithExtensions(file.getPath(), extensions));
                    }
                }
            }
        }
        return files;
    }

    public static boolean isImageFile(File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        //, "gif", "bmp", "tiff", "webp"
        for (String ext : imageExtensions) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSoundFile(File file) {
        String extension = getFileExtension(file.getName());
        for (String ext : soundExtensions) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkFileExtension(String ext, File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return ext.equals(extension);
    }

    public static void copyFile(File srcFile, File desFile) {
        try {
            Path desPath = Paths.get(desFile.getPath());
            Files.copy(srcFile.toPath(), desPath);
            Debug.Log("Copy file " + srcFile.getName() + " to " + desPath);
            if (srcFile.isDirectory()) {
                File[] listOfFiles = srcFile.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    copyFile(listOfFiles[i], new File(desFile.getPath() + "/" + listOfFiles[i].getName()));
                }
            }
        } catch (Exception e) {
            MessageBox.setContext(true, MessageBox.TypeOfMsb.ERROR, "File already exist");
            Debug.Log("Failed to copy file: " + e.getMessage());
        }

    }

    public static String getShorterName(String fileName) {
        String name = getFileNameWithoutExtension(fileName);
        String ext = getFileExtension(fileName);

        final int MAX_LENGTH_ALLOW = 10;
        if (name.length() > MAX_LENGTH_ALLOW) {
            name = name.substring(0, MAX_LENGTH_ALLOW) + "..";
        }

        return name + "." + ext;
    }

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        } else {
            return "";
        }
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(0, dotIndex);
        } else {
            return filename;
        }
    }

    public static String getFileName(String filePath) {
        int slashIndex = filePath.lastIndexOf('/');
        if (slashIndex == -1) slashIndex = filePath.lastIndexOf('\\');

        if (slashIndex == -1) {
            return filePath;
        } else {
            return filePath.substring(slashIndex + 1);
        }
    }

    public static Sprite getIconByFile(File file) {
        Sprite spr = new Sprite();

        String extension = getFileExtension(file.getName()).toLowerCase();

        if (isImageFile(file)) {
            spr.setTexture(AssetPool.getTexture(file.getPath()));
        } else if (extension.equals("java")) {
            spr.setTexture(AssetPool.getTexture(icons.get("JAVA")));
        } else if (isSoundFile(file)) {
            spr.setTexture(AssetPool.getTexture(icons.get("SOUND")));
        } else {
            // Default icon : FILE
            spr.setTexture(AssetPool.getTexture(icons.get("FILE")));
        }
        return spr;
    }

    public static Sprite getGameObjectIcon() {
        Sprite spr = new Sprite();
        spr.setTexture(AssetPool.getTexture(icons.get("GAME_OBJECT")));
        return spr;
    }

    public static Sprite convertImageToSprite(File imgFile) {
        Sprite spr = new Sprite(imgFile.getPath());

        return spr;
    }

    public static Sprite getDefaultSprite() {
        return new Sprite(defaultSprite);
    }
}
