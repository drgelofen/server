package server.lib.utils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class FileUtil {

    public static final String SYSTEM_ROUTE = "/target/";

    public static File get(String path) {
        Path file = FileSystems.getDefault().getPath(path).toAbsolutePath();
        String absPath = file.toString();
        if (absPath.contains(SYSTEM_ROUTE)) {
            absPath = absPath.replace(SYSTEM_ROUTE, "/");
        }
        File dir = new File(absPath);
        dir.mkdirs();
        return dir;
    }

    public static File getTemplate(String path) {
        return get("src/main/resources/templates/" + path);
    }

    public static File getError(String path) {
        return get("src/main/resources/templates/error" + path);
    }

    public static File getStatic(String path) {
        return get("static/" + path);
    }

    public static File getStatic() {
        return get("static/");
    }
}
