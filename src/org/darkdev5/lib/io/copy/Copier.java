package org.darkdev5.lib.io.copy;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author darkDev5
 * @version 1.0
 * @since 17
 */
public @Getter class Copier {
    private @Setter List<String> sources;
    private @Setter String destination;

    private @Setter boolean replace;
    private @Setter boolean deleteSource;

    private final List<String> successfulCopy;
    private final List<String> failedCopy;

    public Copier(List<String> sources, String destination) {
        this.sources = sources;
        this.destination = destination;

        this.replace = true;
        this.deleteSource = false;

        successfulCopy = new ArrayList<>();
        failedCopy = new ArrayList<>();
    }

    /**
     * Start copy of files to destination folder.
     * @return True if copy was successful and false if an error detected.
     */
    public boolean copy() {
        String objectName = null, destinationPath = null;
        for (String str : sources) {
            Path pth = Path.of(str);

            if (Files.exists(pth)) {
                objectName = pth.getFileName().toString();
                if (destination.endsWith("\\")) {
                    destinationPath = destination + objectName;
                } else {
                    destinationPath = destination + "\\" + objectName;
                }

                if (Files.exists(Path.of(destinationPath)) && !replace)  {
                    continue;
                }

                if (Files.isDirectory(pth)) {
                    try {
                        FileUtils.copyDirectory(new File(str), new File(destinationPath));
                        successfulCopy.add(str);

                        if (deleteSource) {
                            FileUtils.deleteDirectory(new File(str));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        failedCopy.add(str);
                    }
                } else {
                    try {
                        FileUtils.copyFile(new File(str), new File(destinationPath));
                        successfulCopy.add(str);

                        if (deleteSource) {
                            FileUtils.delete(new File(str));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        failedCopy.add(str);
                    }
                }
            } else {
                failedCopy.add(str);
            }
        }
        return true;
    }
}
