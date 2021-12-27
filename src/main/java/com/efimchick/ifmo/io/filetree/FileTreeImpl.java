package com.efimchick.ifmo.io.filetree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FileTreeImpl implements FileTree {
    private ArrayList<Integer> lastFolders = new ArrayList<>();

    @Override
    public Optional<String> tree(Path path) {
        if (path == null || !path.toFile().exists())
        {
            return Optional.empty();
        }
        try {
            if (Files.isRegularFile(path))
                return Optional.of(path.getFileName().toString() + " " + Files.size(path) + " bytes");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        StringBuilder sb = new StringBuilder();

        try {
            sb.append(path.getFileName()).append(" ").append(getDirectorySize(path.toFile())).append(" bytes\n");
            sb.append(innerFiles(path.toFile(), 0));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return Optional.of(sb.toString());
    }

    private String innerFiles(File folder, int level) throws IOException {

        File[] files = folder.listFiles();

        StringBuilder hierarchyStrings = new StringBuilder();

        if (files != null) {
            files = Arrays.stream(files).sorted((f1, f2) -> {
                if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                } else if (f1.isFile() && f2.isDirectory()) {
                    return 1;
                } else if (f2.isFile() && f1.isDirectory()) {
                    return -1;
                }
                return 0;
            }).toArray(File[]::new);

            for (int i = 0; i < files.length; i++) {
                for (int j = 0; j < level; j++) {
                    hierarchyStrings.append(lastFolders.contains(j) ? "   " : "│  ");
                }
                if (i != files.length - 1){
                    hierarchyStrings.append("├─ ");
                    for (int j = level; j < 15; j++) {
                        lastFolders.remove(Integer.valueOf(j));
                    }
                } else {
                    hierarchyStrings.append("└─ ");
                    lastFolders.add(level);
                }

                if (files[i].isFile()) {
                    hierarchyStrings.append(files[i].getName()).append(" ").append(Files.size(files[i].toPath())).append(" bytes\n");
                } else if (files[i].isDirectory()) {
                    hierarchyStrings.append(files[i].getName()).append(" ").append(getDirectorySize(files[i])).append(" bytes\n");
                    hierarchyStrings.append(innerFiles(files[i], level + 1));
                }
            }
        }
        return hierarchyStrings.toString();
    }

    private static long getDirectorySize(File file) {
        Path path = Paths.get(file.getPath());
        long size = 0;
        try (Stream<Path> walk = Files.walk(path)) {
            size = walk
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            System.out.printf("Failed to get size of %s%n%s", p, e);
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            System.out.printf("IO errors %s", e);
        }
        return size;
    }
}
