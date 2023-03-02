package org.darkdev5.lib.io.search;

import org.darkdev5.lib.io.FolderVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author darkDev5
 * @version 1.0
 * @since 17
 */
public class FolderSearchEngine {
    private final String path;
    private String key;

    private final boolean exactMatch;
    private final boolean caseSensitive;

    private FolderSearchEngine(FolderSearchEngineBuilder builder) {
        this.path = builder.path;
        this.key = builder.key;

        this.exactMatch = builder.exactMatch;
        this.caseSensitive = builder.caseSensitive;
    }

    /**
     * Search entire folder to find some file or folder that match with the specific key.
     *
     * @param showHidden True if you want to also search in hidden files or not.
     * @return True if search found something and false if search failed.
     * @throws IOException Throws IOException when walking inside sub folders.
     */
    public boolean search(boolean showHidden) throws IOException {
        FolderVisitor visitor = new FolderVisitor(showHidden);
        Files.walkFileTree(Path.of(path), visitor);

        visitor.getVisited().remove(visitor.getVisited().size() - 1);

        for (Path pth : visitor.getVisited()) {
            String name = pth.getFileName().toString();

            if (!caseSensitive) {
                name = name.toLowerCase();
                key = key.toLowerCase();
            }

            if (exactMatch) {
                if (name.equals(key)) {
                    return true;
                }
            } else {
                if (name.contains(key)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class FolderSearchEngineBuilder {
        private String path;
        private String key;

        private boolean exactMatch;
        private boolean caseSensitive;

        public FolderSearchEngineBuilder(String path, String key) {
            this.path = path;
            this.key = key;

            this.exactMatch = false;
            this.caseSensitive = true;
        }

        public FolderSearchEngineBuilder setPath(String path) {
            this.path = path;
            return this;
        }

        public FolderSearchEngineBuilder setKey(String key) {
            this.key = key;
            return this;
        }

        public FolderSearchEngineBuilder setExactMatch(boolean exactMatch) {
            this.exactMatch = exactMatch;
            return this;
        }

        public FolderSearchEngineBuilder setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        /**
         * Build a search engine object.
         *
         * @return The build object to start search.
         */
        public FolderSearchEngine build() {
            return new FolderSearchEngine(this);
        }
    }
}
