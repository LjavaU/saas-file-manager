package com.example.saasfile.support.tenant;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String header = request.getHeader("X-Tenant-Id");
            if (header == null || header.trim().isEmpty()) {
                header = request.getHeader("tenant-id");
            }
            if (header != null && !header.trim().isEmpty()) {
                return header.trim();
            }
        }
        return "default";
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
