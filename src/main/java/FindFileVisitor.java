import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class FindFileVisitor extends SimpleFileVisitor<Path> {
    private static final int BIG_BUF_SIZE = 104857600;

    private String pattern;
    private boolean isContent;
    private List<String> files = new ArrayList<>();
    char[] patternChars;
    int[] t;
    int l;
    char[] tail;
    CharBuffer buffer;

    public FindFileVisitor(String pattern, boolean isContent){
        this.pattern = pattern;
        this.isContent = isContent;
        if (isContent) {
            patternChars = pattern.toCharArray();
            t = getT();
            l = getL();
            tail = new char[pattern.length()];
            buffer = CharBuffer.allocate(BIG_BUF_SIZE);
        }
    }

    private int[] getT() {
        int[] t = new int[patternChars.length + 1];
        int i = 0;
        int j = t[0] = -1;
        while (i < patternChars.length) {
            while (j > -1 && patternChars[i] != patternChars[j]) {
                j = t[j];
            }
            ++i;
            ++j;
            if (i < patternChars.length && patternChars[i] == patternChars[j]) {
                t[i] = t[j];
            } else {
                t[i] = j;
            }
        }
        return t;
    }

    private int getL() {
        int l = 1;
        while (l < patternChars.length && patternChars[l - 1] == patternChars[l]) {
            ++l;
        }
        if (l == patternChars.length) l = 0;
        return l;
    }

    private boolean containsPattern(File file) {
        buffer.clear();
        String f = file.getAbsolutePath();
        int numberChars;
        try (FileReader input = new FileReader(file)) {
            while (true) {
                numberChars = input.read(buffer);
                if (numberChars <= 0) break;
                char[] part = new char[numberChars];
                buffer.flip();
                buffer.get(part);
                if (contains(part)) return true;
                if (numberChars == BIG_BUF_SIZE) {
                    int offset = BIG_BUF_SIZE - patternChars.length - 1;
                    if (offset < 0) offset = 0;
                    buffer.position(offset);
                    buffer.get(tail);
                    buffer.clear();
                    buffer.put(tail);
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private boolean contains(char[] text) {
        int i = l, j = 0, k = 0;
        while (j <= text.length - patternChars.length) {
            while (i < patternChars.length && patternChars[i] == text[i + j]){
                ++i;
            }
            if (i >= patternChars.length) {
                while (k < l && patternChars[k] == text[j + k]) {
                    ++k;
                }
                if (k >= l) {
                    return true;
                }
            }
            j += i - t[i];
            if (i == l) {
                k = max(0, k - 1);
            } else {
                if (t[i]<= l) {
                    k = max(0, t[i]);
                    i = l;
                } else {
                    k = l;
                    i = t[i];
                }
            }
        }
        return false;
    }

    public List<String> getFiles() {
        return files;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        try {
            File rf = file.toFile();
            if (rf.isFile()) {
                if (isContent) {
                    if (containsPattern(rf)) {
                        files.add(file.toAbsolutePath().toString());
                    }
                } else {
                    if (file.getFileName().toString().contains(pattern)) {
                        files.add(file.toAbsolutePath().toString());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return FileVisitResult.CONTINUE;
    }
}
