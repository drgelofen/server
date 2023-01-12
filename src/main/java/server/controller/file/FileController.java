package server.controller.file;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.lib.model.Request;
import server.lib.utils.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@org.springframework.stereotype.Controller
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public ResponseEntity fileUpload(@RequestParam("file") MultipartFile file, Request request) {
        return upload(null, file, request);
    }

    @GetMapping(value = "/download/{id}", produces = {MediaType.ALL_VALUE})
    public ResponseEntity getFile(@PathVariable("id") String file_name, Request request) {
        return download(null, file_name, request);
    }

    @PostMapping("/upload/{sub}")
    public ResponseEntity upload(@PathVariable("sub") String sub_name, @RequestParam("file") MultipartFile file, Request request) {
        try (Database database = SchemaUtil.getDB()) {
            //if (!Controller.verifyToken(database, request)) return Controller.pass(HttpStatus.UNAUTHORIZED);
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            if (StringUtil.isEmpty(ext)) {
                return Controller.pass(HttpStatus.BAD_REQUEST);
            }
            sub_name = "/" + (StringUtil.isEmpty(sub_name) ? ext : sub_name) + "/";
            String id = Instant.now().toEpochMilli() + "-" + StringUtil.randomAlphaNumeric(10) + "." + ext;
            Path path = Paths.get(FileUtil.getStatic(sub_name).getAbsolutePath() + "/" + id);
            Files.write(path, file.getBytes());
            return Controller.pass(HttpStatus.OK, StringUtil.certifyNonSSL(request.redirect("/file/download" + sub_name + id)));
        } catch (Throwable t) {
            return Controller.pass(HttpStatus.LOCKED, t);
        }
    }

    @GetMapping(value = "/download/{sub}/{id}", produces = {MediaType.ALL_VALUE})
    public ResponseEntity download(@PathVariable("sub") String sub_name, @PathVariable("id") String file_name, Request request) {
        try {
            File absolutePath = FileUtil.getStatic(sub_name == null ? "" : "/" + sub_name);
            Path path = Paths.get(absolutePath.getAbsolutePath() + "/" + file_name);
            Resource resource = new UrlResource(path.toUri());
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Throwable ignored) {
            }
            if (StringUtil.isEmpty(contentType)) contentType = "application/octet-stream";
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static File get(String link) {
        if (StringUtil.isEmpty(link)) {
            return null;
        }
        int indexOfDownload = link.lastIndexOf("/download/");
        int index = link.lastIndexOf("/");
        File dir;
        if (index == indexOfDownload + 9) {
            dir = FileUtil.getStatic();
        } else {
            dir = FileUtil.getStatic(link.substring(link.lastIndexOf("/download/") + 10, link.lastIndexOf("/")));
        }
        Path path = Paths.get(dir.getAbsolutePath() + "/" + link.substring(index));
        File file = path.toFile();
        return file.exists() ? file : null;
    }
}
