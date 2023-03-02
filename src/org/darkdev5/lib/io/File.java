package org.darkdev5.lib.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author darkDev5
 * @version 1.0
 * @since 17
 */
public class File {
    private Map<String, Object> attributes;
    private String filePath;

    public File(String filePath) throws IOException {
        setFilePath(filePath);
    }

    /**
     * Set new file path.It also trims end of the path to prevent errors.
     *
     * @param filePath The new file path you want to work with.
     * @throws IOException Throws IOException is from initValues() method.
     */
    public void setFilePath(String filePath) throws IOException {
        this.filePath = filePath.trim();
        initValues();
    }

    /**
     * Initialize attributes and fetch data from operating system and put it in the attributes.
     *
     * @throws IOException Throws IOException if data fetch failed.
     */
    private void initValues() throws IOException {
        if (!Files.exists(Path.of(filePath)) || Files.isDirectory(Path.of(filePath))) {
            throw new IOException("There is no file in this path.");
        }

        attributes = new HashMap<>() {{
            put("name", Path.of(filePath).getFileName().toString());
            put("baseName", FilenameUtils.getBaseName(filePath));
            put("extension", FilenameUtils.getExtension(filePath));
            put("owner", Files.getOwner(Path.of(filePath)).getName());
            put("size", Files.size(Path.of(filePath)));
            put("type", new Tika().detect(new java.io.File(filePath)));
        }};

        attributes.put("parentPath", new java.io.File(filePath).getParentFile().getAbsolutePath());

        if (Path.of(filePath).getParent().getFileName() == null) {
            attributes.put("parentName", Path.of(filePath).getRoot().toString());
        } else {
            attributes.put("parentName", Path.of(filePath).getParent().getFileName().toString());
        }

        BasicFileAttributes attrs = Files.readAttributes(Path.of(filePath),
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
     * Detect if a file is empty or not.
     *
     * @return Returns true if file is empty and false if not.
     * @throws IOException Throws IOException if unable to fetch file size.
     */
    public boolean isEmpty() throws IOException {
        return Files.size(Path.of(filePath)) == 0;
    }

    /**
     * Check the path to see if this file exists or not.
     *
     * @return Returns true if file exists and false if not.
     */
    public boolean exists() {
        return Files.exists(Path.of(filePath)) && !Files.isDirectory(Path.of(filePath));
    }

    /**
     * Delete the file from the path.
     *
     * @return Returns true if file deleted successfully and false if not.
     * @throws IOException Throws IOException if it was unable to delete the file.
     */
    public boolean delete() throws IOException {
        FileUtils.delete(new java.io.File(filePath));
        return false;
    }

    /**
     * Rename this file to the new name in the same folder.
     *
     * @param newName The new name for the file.
     * @return Returns true if renaming was successful and false if not.
     * @throws IOException Throws IOException if an error detected while renaming the file.
     */
    public boolean rename(String newName) throws IOException {
        String parentFolder = new java.io.File(filePath).getParentFile().getAbsolutePath();
        newName = parentFolder + "\\" + newName;

        boolean result = new java.io.File(filePath)
                .renameTo(new java.io.File(FilenameUtils.normalize(newName)));

        if (result) {
            setFilePath(newName);
            return true;
        }

        return false;
    }

    /**
     * Check and detect if the file is locked by another process in the operating system or not.
     *
     * @return Returns true if file is locked and false if not.
     */
    public boolean isLocked() {
        boolean blocked = false;
        java.io.File currentFile = new java.io.File(filePath);

        try (RandomAccessFile fis = new RandomAccessFile(currentFile, "rw")) {
            FileLock lck = fis.getChannel().lock();
            lck.release();
        } catch (Exception ex) {
            blocked = true;
        }

        if (blocked) {
            return blocked;
        }

        String parent = currentFile.getParent(), rnd = UUID.randomUUID().toString();

        java.io.File newName = new java.io.File(parent + "/" + rnd);
        if (currentFile.renameTo(newName)) {
            newName.renameTo(currentFile);
        } else {
            blocked = true;
        }

        return blocked;
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
