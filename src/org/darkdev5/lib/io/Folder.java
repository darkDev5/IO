package org.darkdev5.lib.io;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.darkdev5.lib.io.option.FolderListType;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author darkDev5
 * @version 1.0
 * @since 17
 */
public class Folder {
    private Map<String, Object> attributes;
    private @Getter String folderPath;

    public Folder(String folderPath) throws IOException {
        setFolderPath(folderPath);
    }

    /**
     * Set new folder path.It also trims end of the path to prevent errors.
     *
     * @param folderPath The new folder path you want to work with.
     * @throws IOException Throws IOException is from initValues() method.
     */
    public void setFolderPath(String folderPath) throws IOException {
        this.folderPath = folderPath.trim();
        initValues();
    }

    /**
     * Initialize attributes and fetch data from operating system and put it in the attributes.
     *
     * @throws IOException Throws IOException if data fetch failed.
     */
    private void initValues() throws IOException {
        if (!Files.exists(Path.of(folderPath)) || !Files.isDirectory(Path.of(folderPath))) {
            throw new IOException("There is no directory in this path.");
        }

        attributes = new HashMap<>() {{
            put("owner", Files.getOwner(Path.of(folderPath)).getName());
        }};

        if (List.of(File.listRoots()).contains(new File(folderPath))) {
            attributes.put("name", folderPath);
            attributes.put("parentPath", folderPath);
            attributes.put("parentName", folderPath);
        } else {
            attributes.put("name", Path.of(folderPath).getFileName().toString());
            attributes.put("parentPath", new File(folderPath).getParentFile().getAbsolutePath());

            if (Path.of(folderPath).getParent().getFileName() == null) {
                attributes.put("parentName", Path.of(folderPath).getRoot().toString());
            } else {
                attributes.put("parentName", Path.of(folderPath).getParent().getFileName().toString());
            }
        }

        BasicFileAttributes attrs = Files.readAttributes(Path.of(folderPath),
                BasicFileAttributes.class);

        FileTime time = attrs.lastModifiedTime();
        attributes.put("modifyDate", new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date(time.toMillis())));

        time = attrs.creationTime();
        attributes.put("createDate", new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date(time.toMillis())));

        time = attrs.lastAccessTime();
        attributes.put("accessDate", new SimpleDateFormat("yyyy-MM-dd")
                .format(new Date(time.toMillis())));

        time = attrs.lastModifiedTime();
        attributes.put("modifyTime", new SimpleDateFormat("HH:mm:ss")
                .format(new Date(time.toMillis())));

        time = attrs.creationTime();
        attributes.put("createTime", new SimpleDateFormat("HH:mm:ss")
                .format(new Date(time.toMillis())));

        time = attrs.lastAccessTime();
        attributes.put("accessTime", new SimpleDateFormat("HH:mm:ss")
                .format(new Date(time.toMillis())));
    }

    /**
     * Get the size of folder.
     *
     * @return Returns the size of folder.
     */
    public long getSize() {
        return FileUtils.sizeOfDirectory(new File(folderPath));
    }

    /**
     * Detect if a folder is empty or not.
     *
     * @return Returns true if folder is empty and false if not.
     */
    public boolean isEmpty() {
        return FileUtils.sizeOfDirectory(new File(folderPath)) == 0;
    }

    /**
     * Check the path to see if this folder exists or not.
     *
     * @return Returns true if folder exists and false if not.
     */
    public boolean exists() {
        return Files.exists(Path.of(folderPath), LinkOption.NOFOLLOW_LINKS) &&
                Files.isDirectory(Path.of(folderPath));
    }

    /**
     * List the folder content and filer their type.
     *
     * @param type       You can select to list only files or folders.
     * @param showHidden True if you want hidden files and false if not.
     * @return The list of files you fetched from a path.
     */
    public List<Path> list(FolderListType type, boolean showHidden) {
        List<File> tmp = List.of(
                FileSystemView.getFileSystemView().getFiles(new File(folderPath), !showHidden)
        );

        List<Path> content = new ArrayList<>();
        switch (type) {
            case File -> {
                tmp = tmp.stream().filter(x -> !x.isDirectory()).toList();
            }

            case Folder -> {
                tmp = tmp.stream().filter(File::isDirectory).toList();
            }
        }

        tmp.forEach(file -> content.add(file.toPath()));
        return content;
    }

    /**
     * You can list content of a folder but also you want walk in sub folder and fetch entire content of a folder.
     *
     * @param showHidden True if you want hidden files and false if not.
     * @return Returns a FileVisitor class object and you can fetch data from it easily.
     * @throws IOException Throws IOException if walking has error.
     */
    public FolderVisitor walk(boolean showHidden) throws IOException {
        FolderVisitor visitor = new FolderVisitor(showHidden);
        Files.walkFileTree(Path.of(folderPath), visitor);

        visitor.getVisited().remove(visitor.getVisited().size() - 1);

        return visitor;
    }

    /**
     * Delete the folder from the path.
     *
     * @return Returns true if folder deleted successfully and false if not.
     * @throws IOException Throws IOException if it was unable to delete the folder.
     */
    public boolean delete() throws IOException {
        FileUtils.deleteDirectory(new File(folderPath));
        return true;
    }

    /**
     * Rename this folder to the new name in the same folder.
     *
     * @param newName The new name for the folder.
     * @return Returns true if renaming was successful and false if not.
     * @throws IOException Throws IOException if an error detected while renaming the folder.
     */
    public boolean rename(String newName) throws IOException {
        String parentFolder = new File(folderPath).getParentFile().getAbsolutePath();
        newName = parentFolder + "\\" + newName;

        boolean result = new File(folderPath).renameTo(new File(FilenameUtils.normalize(newName)));

        if (result) {
            setFolderPath(newName);
            return true;
        }

        return false;
    }

    /**
     * Erase the folder entire content and makes it empty.
     *
     * @return True if erasing was successful and false if not.
     * @throws IOException Throws IOException if error detected in renaming operation.
     */
    public boolean erase() throws IOException {
        FileUtils.cleanDirectory(new File(folderPath));
        return true;
    }

    /**
     * Gets an attribute value by the key.
     * @param key The key you want to search.
     * @return Returns the attribute value that found.
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
