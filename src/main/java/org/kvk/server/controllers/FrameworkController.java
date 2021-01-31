package org.kvk.server.controllers;

import org.springframework.web.bind.annotation.RequestMethod;

public abstract class FrameworkController {
    protected String urlPattern;
    protected RequestMethod requestMethod;

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }
}
