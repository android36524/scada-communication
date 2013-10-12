package com.ht.scada.communication.web.controller;

import org.thymeleaf.TemplateEngine;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-16 下午12:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractController {
    protected final TemplateEngine templateEngine;

    public AbstractController(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
}
