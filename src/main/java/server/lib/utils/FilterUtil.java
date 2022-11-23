package server.lib.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import server.lib.model.Request;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;

@Component
public class FilterUtil extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        Calendar startCalendar = Calendar.getInstance();
        long now = startCalendar.getTimeInMillis();
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept, User-Agent, Accept-Language");
        response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(new Request((HttpServletRequest) servletRequest), servletResponse);
        long then = Calendar.getInstance().getTimeInMillis();
        System.out.println("[" + ((HttpServletResponse) servletResponse).getStatus() + "]At:" + startCalendar.toInstant().toString() + "  Processed:(" + ((then - now)) + ")  Path:" + ((HttpServletRequest) servletRequest).getServletPath());
    }
}
