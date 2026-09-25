package com.ecommerce.order_service.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminAccessFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isAdminOnly(request)) {
            String role = request.getHeader("X-User-Role");
            if (role == null || !role.equals("ADMIN")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Admin access required\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminOnly(HttpServletRequest request) {
        return request.getRequestURI().equals("/api/orders/all");
    }
}
