import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

public class FindFileVisitor extends SimpleFileVisitor<Path> {
    private static final int BIG_BUF_SIZE = 1048576;

    private String pattern;
    private boolean isContent;
    private List<String> files = new ArrayList<>();
    private char[] patternChars;
    private Map<Character, Integer> shifts = new HashMap<>();



//    private int[] t;
//    private int l;
    private char[] tail;
    private CharBuffer buffer;

    FindFileVisitor(String pattern, boolean isContent){
        if (pattern.length() >= BIG_BUF_SIZE) {
            throw new RuntimeException();
        }
        this.pattern = pattern;
        this.isContent = isContent;
        if (isContent) {
            patternChars = pattern.toCharArray();
//            t = getT();
//            l = getL();
            generateShifts();
            tail = new char[pattern.length()];
            buffer = CharBuffer.allocate(BIG_BUF_SIZE);
        }
    }

    private void generateShifts(){
        for (int i = 0; i < 255; ++i) {
            shifts.put((char)i, patternChars.length);
        }
        for (int i = 0; i < patternChars.length - 1; ++i) {
            shifts.put(patternChars[i], patternChars.length - i - 1);
        }
    }
//
//    private int[] getT() {
//        int[] t = new int[patternChars.length + 1];
//        int i = 0;
//        int j = t[0] = -1;
//        while (i < patternChars.length) {
//            while (j > -1 && patternChars[i] != patternChars[j]) {
//                j = t[j];
//            }
//            ++i;
//            ++j;
//            if (i < patternChars.length && patternChars[i] == patternChars[j]) {
//                t[i] = t[j];
//            } else {
//                t[i] = j;
//            }
//        }
//        return t;
//    }
//
//    private int getL() {
//        int l = 1;
//        while (l < patternChars.length && patternChars[l - 1] == patternChars[l]) {
//            ++l;
//        }
//        if (l == patternChars.length) l = 0;
//        return l;
//    }

    private int containsPattern(File file) {
        buffer.clear();
        String f = file.getAbsolutePath();
        int numberChars, pos = 0;
        try (FileReader input = new FileReader(file)) {
            while (true) {
                numberChars = input.read(buffer);
                if (numberChars <= 0) break;
                buffer.flip();
                char[] part = new char[buffer.limit()];
                buffer.get(part);
                int localPos = contains(part);
                if (localPos > 0) {
                    System.out.println("Found in " + (pos + localPos));
                    return pos + localPos;
                }
                if (buffer.position() == BIG_BUF_SIZE) {
                    int offset = BIG_BUF_SIZE - patternChars.length;
                    pos += offset;
                    System.out.println("New start position is " + pos);
                    buffer.position(offset);
                    buffer.get(tail);
                    buffer.clear();
                    buffer.put(tail);
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return -1;
    }

    private boolean substringIsEqual(char[] text, int startPos) {
        for (int i = 0; i < patternChars.length; ++i) {
            if (text[startPos + i] != patternChars[i]) return false;
        }
        return true;
    }

    private int contains(char[] text) {
        int textPos = 0;
        while (textPos < text.length - patternChars.length) {
            if (substringIsEqual(text, textPos)) {
                return textPos;
            } else {
                textPos += shifts.get(text[textPos + patternChars.length - 1]);
            }
        }
        return -1;
//        int i = l, j = 0, k = 0;
//        while (j <= text.length - patternChars.length) {
//            while (i < patternChars.length && patternChars[i] == text[i + j]){
//                ++i;
//            }
//            if (i >= patternChars.length) {
//                while (k < l && patternChars[k] == text[j + k]) {
//                    ++k;
//                }
//                if (k >= l) {
//                    return j;
//                }
//            }
//            j += i - t[i];
//            if (i == l) {
//                k = max(0, k - 1);
//            } else {
//                if (t[i]<= l) {
//                    k = max(0, t[i]);
//                    i = l;
//                } else {
//                    k = l;
//                    i = t[i];
//                }
//            }
//        }
//        return -1;
    }

    List<String> getFiles() {
        return files;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        try {
            File rf = file.toFile();
            if (rf.isFile()) {
                if (isContent) {
                    int pos = containsPattern(rf);
                    if (pos > -1) {
                        files.add(file.toAbsolutePath().toString() + ": " + pos);
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
