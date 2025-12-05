package vn.hbtplus.models;

import org.springframework.security.core.GrantedAuthority;

public class GrantedAuth implements GrantedAuthority {
    public String authority;
    public String url;

    public GrantedAuth(String authority, String url) {
        this.authority = authority;
        this.url = url;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public String getUrl() {
        return url;
    }
}
