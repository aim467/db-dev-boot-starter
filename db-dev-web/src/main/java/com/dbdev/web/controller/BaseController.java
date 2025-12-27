package com.dbdev.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller 基类
 * 自动添加 uiPath 前缀
 */
@RequestMapping("${db.dev.ui-path:/db-dev}")
public abstract class BaseController {

}
