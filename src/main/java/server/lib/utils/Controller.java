package server.lib.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.Application;
import server.controller.admin.AdminController;
import server.controller.admin.ErrorController;
import server.controller.admin.HackController;
import server.controller.doctor.DoctorController;
import server.controller.user.UserController;
import server.lib.interfacing.Authenticate;
import server.lib.interfacing.Authorize;
import server.lib.interfacing.GET;
import server.lib.interfacing.POST;
import server.lib.model.*;
import server.lib.orm.dao.Dao;
import server.lib.orm.dao.DaoManager;
import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;
import server.model.Admin;
import server.model.Doctor;
import server.model.User;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class Controller<T extends DatabaseModel<T>> {

    private static final Gson GSON = Application.GSON;
    private Class<T> persistentClass;

    public Controller() {
        try {
            this.persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Throwable ignored) {
            this.persistentClass = null;
        }
    }

    @RequestMapping("{path}")
    public final Object router(@PathVariable("path") String router, Request request) {
        Method function = null;
        for (Method method : getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase(router)
                    && !Modifier.isStatic(method.getModifiers())
                    && Modifier.isPublic(method.getModifiers())) {
                if (request.getMethod().equalsIgnoreCase(GET.class.getSimpleName())) {
                    if (method.getAnnotation(GET.class) == null) {
                        return pass(HttpStatus.NOT_FOUND);
                    }
                } else if (!request.getMethod().equalsIgnoreCase(POST.class.getSimpleName())) {
                    return pass(HttpStatus.NOT_FOUND);
                }
                for (Parameter parameter : method.getParameters()) {
                    if (parameter.getType().getName().equals(Database.class.getName())) {
                        function = method;
                        function.setAccessible(true);
                        break;
                    }
                }
                break;
            }
        }
        if (function == null) {
            return pass(HttpStatus.NOT_FOUND);
        }
        Database database = null;
        try {
            database = SchemaUtil.getDB();
            ResponseEntity response;
            if ((response = checkToken(database, request, function)) != null) {
                return response;
            }
            for (Parameter parameter : function.getParameters()) {
                if (parameter.getType().getName().equals(Request.class.getName())) {
                    return function.invoke(this, database, request);
                } else if (DatabaseModel.class.isAssignableFrom(parameter.getType())
                        || parameter.getType().getName().equals(JsonObject.class.getName())) {
                    Object object = GSON.fromJson(request.getBody(), parameter.getType());
                    if (object == null) {
                        return pass(HttpStatus.BAD_REQUEST);
                    }
                    return function.invoke(this, database, object);
                }
            }
            return function.invoke(this, database);
        } catch (Throwable t) {
            t.printStackTrace();
            ErrorController.log(database, request, t);
            return pass(HttpStatus.INTERNAL_SERVER_ERROR, t);
        } finally {
            try {
                if (database != null) {
                    database.close();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private ResponseEntity checkToken(Database database, Request request, Method function) throws Throwable {
        Authenticate authenticate = function.getAnnotation(Authenticate.class);
        Authorize authorize = function.getAnnotation(Authorize.class);
        boolean verified = verifyToken(database, request, authorize != null ? getPermissions(authorize) : null, authorize != null ? authorize.methods() : null);
        if (!verified && (authenticate != null || authorize != null)) {
            if (authorize != null && !authorize.important()) {
                return pass(HttpStatus.OK);
            }
            return pass(HttpStatus.UNAUTHORIZED);
        }
        if (authenticate != null && authorize == null && request.getAuth_type() == TokenUtil.TokenType.ADMIN) {
            return pass(HttpStatus.FORBIDDEN);
        }
        if (authorize != null && authenticate == null && request.getAuth_type() != TokenUtil.TokenType.ADMIN) {
            return pass(HttpStatus.UNAUTHORIZED);
        }
        if (request.getAuth_user() != null && request.getAuth_user().getSuspend()) {
            return pass(User.SUSPEND_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        if (request.getAuth_doctor() != null && request.getAuth_doctor().getSuspend()) {
            return pass(Doctor.SUSPEND_MESSAGE, HttpStatus.UNAUTHORIZED);
        }
        if (request.isHack()) {
            HackController.log(database, request);
            return pass(Request.HACK_MESSAGE, HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    public static boolean verifyToken(Database database, Request request) throws Throwable {
        return verifyToken(database, request, null, null);
    }

    public static boolean verifyToken(Database database, Request request
            , Class<? extends DatabaseModel>[] permissions, Authorize.Method[] methods) throws Throwable {
        request.setToken(TokenUtil.verifyToken(request));
        boolean verified = false;
        if (request.getAuth_type() != null) {
            switch (request.getAuth_type()) {
                case ADMIN:
                    verified = AdminController.isAuthorized(database, request, permissions, methods);
                    break;
                case DOCTOR:
                    verified = DoctorController.isAuthorized(database, request);
                    break;
                case USER:
                    verified = UserController.isAuthorized(database, request);
                    break;
            }
        }
        return verified;
    }

    @Authorize(important = false)
    public ResponseEntity count(Database database, Request request) throws Throwable {
        Dao<T, ?> dao = DaoManager.createDao(database, persistentClass);
        FilterModel<T, ?> filter = filter(dao, request);
        QueryBuilder<T, ?> builder = filter.builder();
        builder.clearOrder();
        builder.limit(null);
        return pass(HttpStatus.OK, builder.countOf());
    }

    private Class<? extends DatabaseModel>[] getPermissions(Authorize authorize) {
        ArrayList<Class<? extends DatabaseModel>> permissions = new ArrayList<>();
        if (authorize.permissions().length > 0) {
            permissions.addAll(Arrays.asList(authorize.permissions()));
        } else {
            permissions.add(persistentClass);
        }
        return permissions.toArray(new Class[permissions.size()]);
    }

    public final <X, Y> FilterModel<X, Y> filter(Dao<X, Y> dao, Request request) throws Throwable {
        FilterModel<X, Y> model = request.toFilter();
        QueryBuilder<X, Y> builder = dao.queryBuilder();
        Where<X, Y> where = builder.where();
        model.init(builder, where);
        if (model.getLimit_by() == null) {
            model.setLimit_by(15L);
        } else if (model.getLimit_by() > 500L) {
            model.setLimit_by(500L);
        }
        if (model.getOffset_by() == null) {
            model.setOffset_by(0L);
        }
        builder.limit(model.getLimit_by());
        builder.offset(model.getOffset_by() * model.getLimit_by());
        if (model.getOrder_by() == null) {
            model.setOrder_by(DatabaseModel.CREATE_AT);
        }
        if (model.getSort_by() == null) {
            model.setSort_by(FilterModel.DESCENDING);
        }
        builder.orderBy(model.getOrder_by(), model.getSortType());
        if (model.getDate_from() != null || model.getDate_to() != null) {
            if (model.getDate_to() == null) {
                model.setTime_to(Long.MAX_VALUE);
            } else {
                model.setTime_to(DateUtil.toMillis(model.getDate_to()));
            }
            if (model.getDate_from() == null) {
                model.setTime_from(0L);
            } else {
                model.setTime_from(DateUtil.toMillis(model.getDate_from()));
            }
        }
        if (model.getTime_from() != null || model.getTime_to() != null) {
            if (model.getTime_to() == null) {
                model.setTime_to(Long.MAX_VALUE);
            }
            if (model.getTime_from() == null) {
                model.setTime_from(0L);
            }
            where.between(DatabaseModel.CREATE_AT, model.getTime_from(), model.getTime_to());
        } else {
            where.isNotNull(DatabaseModel.CREATE_AT);
        }
        return model;
    }

    public final String toJson(Object body) {
        return GSON.toJson(body);
    }

    public final T parse(Request request) {
        return parse(request.getBody(), persistentClass);
    }

    public static <M> M parse(Request request, Class<M> className) {
        return parse(request.getBody(), className);
    }

    public static <M> M parse(String body, Class<M> className) {
        return GSON.fromJson(body, className);
    }

    public final Dao<T, UUID> getDao(Database database) throws Throwable {
        return getDaoStatic(database, persistentClass);
    }

    public final <M extends DatabaseModel> Dao<M, UUID> getDao(Database database, Class<M> model) throws
            Throwable {
        return getDaoStatic(database, model);
    }

    public final Dao<T, Long> getDaoLong(Database database) throws Throwable {
        return getDaoLong(database, persistentClass);
    }

    public final <M extends DatabaseModel> Dao<M, Long> getDaoLong(Database database, Class<M> model) throws
            Throwable {
        return getDaoLongStatic(database, model);
    }

    public static <M> Dao<M, UUID> getDaoStatic(Database database, Class<M> model) throws Throwable {
        return DaoManager.createDao(database, model);
    }

    public static <M> Dao<M, Long> getDaoLongStatic(Database database, Class<M> model) throws Throwable {
        return DaoManager.createDao(database, model);
    }

    public final <D extends DatabaseModel> List<D> trim(List<D> query) {
        for (D t : query) {
            t.trim();
        }
        return query;
    }

    public static ResponseEntity pass(HttpStatus status) {
        return pass(status, null);
    }

    public static ResponseEntity pass(String message, HttpStatus status) {
        return pass(status, null, message, null);
    }

    public static ResponseEntity pass(HttpStatus status, Object object) {
        return pass(status, object, null, null);
    }

    public static ResponseEntity passAuth(HttpStatus status, String authorization) {
        return pass(status, null, null, authorization);
    }

    public static ResponseEntity pass(HttpStatus status, Object object, Long resultSize) {
        return pass(status, object, null, null, resultSize);
    }

    public static ResponseEntity pass(HttpStatus status, Object object, String message, String authorization) {
        return pass(status, object, message, authorization, null);
    }

    private static ResponseEntity pass(HttpStatus status, Object object, String message, String authorization, Long resultSize) {
        ResponseEntity.BodyBuilder responder = ResponseEntity.status(status);
        responder.contentType(MediaType.APPLICATION_JSON_UTF8);
        responder.header(TokenUtil.CONNECTION, TokenUtil.KEEP_ALIVE);
        if (!StringUtil.isEmpty(authorization)) {
            responder.header(TokenUtil.AUTHORIZATION_HEADER, "Bearer " + authorization);
        }
        return responder.body(createBody(status, object, message, resultSize));
    }

    public static ResponseEntity passHTML(String url) throws Throwable {
        return passHTML(url, null, null);
    }

    public static ResponseEntity passHTML(String url, String redirectLink, String tag) throws Throwable {
        File file = FileUtil.getTemplate(url);
        if (!file.exists()) {
            file = FileUtil.getError("404.html");
        }
        Document document = Jsoup.parse(file, "UTF-8");
        if (redirectLink != null && tag != null) {
            Element redirect = document.getElementById("redirect");
            redirect.attr("href", redirectLink);
            redirect.html(tag);
        }
        return passHTMLDOM(document);
    }

    public static ResponseEntity passHTMLDOM(Document document) throws Throwable {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType("text/html; charset=UTF-8"));
        return new ResponseEntity(document.toString(), responseHeaders, HttpStatus.OK);
    }

    private static ResponseModel createBody(HttpStatus statusCode, Object data, String message, Long resultSize) {
        ResponseModel model = new ResponseModel();
        model.setStatus(statusCode.value());
        model.setSuccess(statusCode.value() < 400);
        if (data instanceof Throwable) {
            model.setTrace(generateTrace((Throwable) data).getMessage());
        }
        if (statusCode.value() < 400) {
            if (data != null) {
                if (resultSize != null) {
                    model.setMax(resultSize);
                }
                if (data instanceof Object[]) {
                    model.setLimit((long) ((Object[]) data).length);
                } else if (data instanceof Collection) {
                    model.setLimit((long) ((List) data).size());
                } else if (data instanceof Map) {
                    model.setLimit((long) ((Map) data).size());
                }
                model.setData(data);
            }
        } else {
            model.setError(statusCode.getReasonPhrase());
        }
        if (StringUtil.isEmpty(message)) {
            model.setMessage(StringUtil.translate(statusCode));
        } else {
            model.setMessage(message);
        }
        return model;
    }

    public static ErrorModel generateTrace(Throwable data) {
        String reason = null, className = null, methodName = null, lineNumber = null;
        try {
            while (data != null) {
                reason = data.toString();
                if (data.getStackTrace() != null && data.getStackTrace().length > 0) {
                    for (StackTraceElement element : data.getStackTrace()) {
                        if (element != null && element.getClassName() != null
                                && element.getClassName().startsWith("server.controller.")) {
                            className = element.getClassName().replace("server.controller.", "");
                            methodName = element.getMethodName();
                            lineNumber = String.valueOf(element.getLineNumber());
                        }
                    }
                }
                data = data.getCause();
            }
        } catch (Throwable ignored) {
        }
        return new ErrorModel(lineNumber, methodName, className, reason, className + "[" + methodName + "(" + lineNumber + ")]: " + reason);
    }
}
