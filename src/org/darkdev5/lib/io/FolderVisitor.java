package org.darkdev5.lib.io;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author darkDev5
 * @version 1.0
 * @since 17
 */
public @Getter class FolderVisitor extends SimpleFileVisitor<Path> {
    private List<Path> visited;
    private List<Path> visitedFailed;

    private @Setter boolean showHidden;

    public FolderVisitor(boolean showHidden) {
        visited = new ArrayList<>();
        visitedFailed = new ArrayList<>();

        this.showHidden = showHidden;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            if (showHidden && !Files.isHidden(file)) {
                visited.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            visitedFailed.add(file);
        } finally {
            if (!visited.contains(file)) {
                visited.add(file);
            }
        }

        return super.visitFile(file, attrs);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        visitedFailed.add(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (showHidden && !Files.isHidden(dir)) {
            visited.add(dir);
        }

        return super.postVisitDirectory(dir, exc);
    }

}
