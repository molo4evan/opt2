import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class FileScanner {

    static List<String> findFiles(String root, String pattern, boolean isContent) throws IOException {
        FindFileVisitor visitor = new FindFileVisitor(pattern, isContent);
        Files.walkFileTree(Paths.get(root), visitor);
        return visitor.getFiles();
    }
}
