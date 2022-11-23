package server.controller.blog;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.lib.interfacing.Authorize;
import server.lib.model.FilterModel;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.orm.stmt.Where;
import server.lib.utils.Controller;
import server.lib.utils.Database;
import server.model.Blog;
import server.model.BlogCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blogCategory")
public class BlogCategoryController extends Controller<BlogCategory> {

    @Authorize
    public ResponseEntity getAll(Database database, Request request) throws Throwable {
        Dao<BlogCategory, UUID> dao = getDao(database);
        FilterModel<BlogCategory, UUID> filter = filter(dao, request);
        Where where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().eq(BlogCategory.PARENT_ID, filter.getParent_by());
        }
        if (filter.getSearch_by() != null) {
            where.and().like(BlogCategory.NAME, "%" + filter.getSearch_by() + "%");
        }
        List<BlogCategory> query = filter.builder().leftJoin(BlogCategory.PARENT_ID, BlogCategory.ID, dao.queryBuilder()).query();
        return pass(HttpStatus.OK, query);
    }

    @Authorize
    public ResponseEntity create(Database database, BlogCategory info) throws Throwable {
        if (info.getParent_id() != null) {
            BlogCategory parent = new BlogCategory();
            parent.setCategory_id(info.getParent_id());
            info.setParent(parent);
        }
        Dao<BlogCategory, UUID> dao = getDao(database);
        BlogCategory banner = dao.createIfNotExists(info);
        return banner == null ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, dao.queryForId(banner.getCategory_id()));
    }

    @Authorize
    public ResponseEntity delete(Database database, BlogCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<BlogCategory, UUID> dao = getDao(database);
        return dao.delete(info) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK);
    }

    @Authorize
    public ResponseEntity update(Database database, BlogCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        if (info.getParent_id() != null) {
            BlogCategory parent = new BlogCategory();
            parent.setCategory_id(info.getParent_id());
            info.setParent(parent);
        }
        Dao<BlogCategory, UUID> dao = getDao(database);
        BlogCategory blog = dao.queryForId(info.getCategory_id());
        if (blog == null) {
            return pass(HttpStatus.EXPECTATION_FAILED);
        }
        blog.merge(info);
        return dao.update(blog) == 0 ? pass(HttpStatus.EXPECTATION_FAILED) : pass(HttpStatus.OK, blog);
    }

    public ResponseEntity getList(Database database, Request request) throws Throwable {
        Dao<BlogCategory, UUID> dao = getDao(database);
        FilterModel<BlogCategory, UUID> filter = filter(dao, request);
        if (filter.getOrder_by() == null) {
            filter.builder().orderBy(BlogCategory.PRIORITY, false);
        }
        Where where = filter.where();
        if (filter.getParent_by() != null) {
            where.and().and(where.eq(BlogCategory.PARENT_ID, filter.getParent_by()), where.eq(BlogCategory.VISIBILITY, true));
        } else {
            where.and().eq(BlogCategory.VISIBILITY, true).and().isNull(BlogCategory.PARENT_ID);
        }
        List<BlogCategory> query = filter.query();
        Dao<Blog, UUID> blogs = getDao(database, Blog.class);
        for (BlogCategory category : query) {
            category.setCategory_count(blogs.queryBuilder().where().eq(Blog.CATEGORY_ID, category.getCategory_id()).and().eq(Blog.VISIBILITY, true).countOf());
        }
        return pass(HttpStatus.OK, trim(query));
    }

    public ResponseEntity getOne(Database database, BlogCategory info) throws Throwable {
        if (info.getCategory_id() == null) {
            return pass(HttpStatus.BAD_REQUEST);
        }
        Dao<BlogCategory, UUID> dao = getDao(database);
        BlogCategory blog = dao.queryForId(info.getCategory_id());
        if (blog != null) {
            blog.setCategory_views(blog.getCategory_views() + 1);
            dao.update(blog);
            return pass(HttpStatus.OK, blog);
        }
        return pass(HttpStatus.EXPECTATION_FAILED);
    }
}
