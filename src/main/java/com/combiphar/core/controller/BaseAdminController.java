package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;

/**
 * Base controller dengan method umum untuk semua admin controllers.
 */
public abstract class BaseAdminController {

    protected Map<String, Object> buildBaseModel(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("currentUser", ctx.sessionAttribute("currentUser"));
        return model;
    }
}
