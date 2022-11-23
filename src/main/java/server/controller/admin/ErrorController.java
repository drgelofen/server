package server.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.ErrorModel;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.lib.utils.FileUtil;
import server.model.Admin;
import server.model.Error;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/error")
public class ErrorController extends Controller<Error> {

    public static void log(Database database, Request request, Throwable throwable) {
        try {
            Dao<Error, Long> dao = getDaoLongStatic(database, Error.class);
            Error error = new Error();
            ErrorModel trace = generateTrace(throwable);
            error.setError_agent(request.getUserAgent());
            error.setError_auth(String.valueOf(request.getAuth()));
            error.setError_cause(throwable.toString());
            error.setError_message(trace.getMessage());
            error.setError_line(trace.getLine());
            error.setError_class(trace.getClassName());
            error.setError_path(request.getServletPath());
            error.setError_function(trace.getMethod());
            error.setError_route(request.getRoute());
            error.setError_request(request.getBody());
            if (request.getAuth_admin() != null) {
                error.setError_contact(request.getAuth_admin().toJsonObject());
            } else if (request.getAuth_user() != null) {
                error.setError_contact(request.getAuth_user().toJsonObject());
            } else if (request.getAuth_doctor() != null) {
                error.setError_contact(request.getAuth_doctor().toJsonObject());
            }
            dao.create(error);
        } catch (Throwable ignored) {
        }
    }

    @Authorize(permissions = Admin.class)
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<Error, Long> dao = getDaoLong(database);
        FilterModel<Error, Long> filter = filter(dao, request);
        Error parse = parse(request);
        if (parse != null) {
            Where where = filter.where();
            if (parse.getError_route() != null) {
                where.and().like(Error.ROUTE, "%" + parse.getError_route() + "%");
            } else if (parse.getError_auth() != null) {
                where.and().eq(Error.AUTH, parse.getError_auth());
            } else if (parse.getError_path() != null) {
                where.and().like(Error.PATH, "%" + parse.getError_path() + "%");
            } else if (parse.getError_function() != null) {
                where.and().like(Error.FUNCTION, "%" + parse.getError_function() + "%");
            } else if (parse.getError_class() != null) {
                where.and().like(Error.CLASS, "%" + parse.getError_class() + "%");
            }
        }
        return pass(HttpStatus.OK, filter.query());
    }

    @Authorize(permissions = Admin.class)
    public ResponseEntity getTrending(Database database, Request request) throws Throwable {
        Dao<Error, Long> dao = getDaoLong(database);
        List<Error> errors = dao.queryForAll();
        HashMap<String, Long> messages = new HashMap<>();
        HashMap<String, Long> routes = new HashMap<>();
        HashMap<String, Long> paths = new HashMap<>();
        HashMap<String, Long> users = new HashMap<>();
        for (Error error : errors) {
            Long userCounter = users.get(error.getError_auth());
            if (userCounter == null) {
                userCounter = 0L;
            }
            userCounter++;
            users.put(error.getError_auth(), userCounter);

            Long messageCounter = messages.get(error.getError_message());
            if (messageCounter == null) {
                messageCounter = 0L;
            }
            messageCounter++;
            messages.put(error.getError_message(), messageCounter);

            Long routeCounter = routes.get(error.getError_route());
            if (routeCounter == null) {
                routeCounter = 0L;
            }
            routeCounter++;
            routes.put(error.getError_route(), routeCounter);

            Long pathCounter = paths.get(error.getError_path());
            if (pathCounter == null) {
                pathCounter = 0L;
            }
            pathCounter++;
            paths.put(error.getError_path(), pathCounter);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Most Errored Routes: " + "\n\n");
        builder.append(compile(routes));
        builder.append("Most Errored Path: " + "\n\n");
        builder.append(compile(paths));
        builder.append("Most Errored Messages:" + "\n\n");
        builder.append(compile(messages));
        builder.append("Most Errored Users:" + "\n\n");
        builder.append(compile(users));
        builder.append("All Errors: " + errors.size());
        FileWriter writer = null;
        try {
            String id = Instant.now().toEpochMilli() + ".txt";
            File file = new File(FileUtil.getStatic("error"), id);
            writer = new FileWriter(file);
            writer.write(builder.toString());
            return pass(HttpStatus.OK, request.redirect("/file/download/error/") + id);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }

    private String compile(HashMap<String, Long> list) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Data> dataList = new ArrayList<>();
        for (String key : list.keySet()) {
            dataList.add(new Data(key, list.get(key)));
        }
        Collections.sort(dataList, new Comparator<Data>() {
            @Override
            public int compare(Data o2, Data o1) {
                return Double.compare(o1.value, o2.value);
            }
        });
        for (Data data : dataList) {
            builder.append(data.key + "        " + data.value);
        }
        builder.append("\n\n");
        return builder.toString();
    }

    class Data {
        String key;
        Long value;

        Data(String key, Long value) {
            this.key = key;
            this.value = value;
        }
    }
}
