import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            showHelp();
            return;
        }
        String pattern = args[1];
        String path = args[2];
        List<String> files;
        boolean isContent;
        long time = 0;
        try {
            switch (args[0]) {
                case "--name": {
                    isContent = false;
                    break;
                }
                case "--data": {
                    isContent = true;
                    break;
                }
                default: {
                    showHelp();
                    return;
                }
            }
            long start = System.currentTimeMillis();
            files = FileScanner.findFiles(path, pattern, isContent);
            time = System.currentTimeMillis() - start;
        } catch (IOException ex) {
            ex.printStackTrace();
            showHelp();
            return;
        }
        for (String file : files) {
            System.out.println(file);
        }
        System.out.println("Invocation time: " + time + " ms");
    }

    private static void showHelp(){
        System.out.println("Usage: 'java -jar [PATH_TO_JAR]/finder.jar --name [NAME_PATTERN] [ROOT_PATH]'");
        System.out.println("or 'java -jar [PATH_TO_JAR]/finder.jar --data [DATA_PATTERN] [ROOT_PATH]'");
    }
}
