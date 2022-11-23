package server.lib.model;

import server.lib.orm.stmt.QueryBuilder;
import server.lib.orm.stmt.Where;

import java.util.List;
import java.util.UUID;

public class FilterModel<X, Y> {

    public static final String DESCENDING = "des";
    public static final String ASCENDING = "asc";

    private transient QueryBuilder<X, Y> builder;
    private transient Where<X, Y> where;

    private Long limit_by;
    private Long offset_by;

    private String search_by;
    private UUID parent_by;

    private Long time_from;
    private Long time_to;

    private String date_from;
    private String date_to;

    private String sort_by;
    private String order_by;

    private String filter_by;

    public final void init(QueryBuilder<X, Y> builder, Where<X, Y> where) {
        this.builder = builder;
        this.where = where;
    }

    public final List<X> query() throws Throwable {
        return builder.query();
    }

    public QueryBuilder<X, Y> builder() {
        return builder;
    }

    public Where<X, Y> where() {
        return where;
    }

    public Long getLimit_by() {
        return limit_by;
    }

    public void setLimit_by(Long limit_by) {
        this.limit_by = limit_by;
    }

    public Long getOffset_by() {
        return offset_by;
    }

    public void setOffset_by(Long offset_by) {
        this.offset_by = offset_by;
    }

    public String getSearch_by() {
        return search_by;
    }

    public void setSearch_by(String search_by) {
        this.search_by = search_by;
    }

    public UUID getParent_by() {
        return parent_by;
    }

    public void setParent_by(UUID parent_by) {
        this.parent_by = parent_by;
    }

    public Long getTime_from() {
        return time_from;
    }

    public void setTime_from(Long time_from) {
        this.time_from = time_from;
    }

    public Long getTime_to() {
        return time_to;
    }

    public void setTime_to(Long time_to) {
        this.time_to = time_to;
    }

    public String getDate_from() {
        return date_from;
    }

    public void setDate_from(String date_from) {
        this.date_from = date_from;
    }

    public String getDate_to() {
        return date_to;
    }

    public void setDate_to(String date_to) {
        this.date_to = date_to;
    }

    public String getSort_by() {
        return sort_by;
    }

    public void setSort_by(String sort_by) {
        this.sort_by = sort_by;
    }

    public String getOrder_by() {
        return order_by;
    }

    public void setOrder_by(String order_by) {
        this.order_by = order_by;
    }

    public String getFilter_by() {
        return filter_by;
    }

    public void setFilter_by(String filter_by) {
        this.filter_by = filter_by;
    }

    public boolean getSortType() {
        return sort_by != null && sort_by.equalsIgnoreCase(ASCENDING);
    }
}
