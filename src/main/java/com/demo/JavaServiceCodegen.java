package com.demo;

import io.swagger.codegen.languages.JavaClientCodegen;

/**
 * @author BlueWang
 * @ClassName: as
 * @Description:
 * @date 2019/9/5 17:06
 */
public class JavaServiceCodegen extends JavaClientCodegen
{
    public JavaServiceCodegen()
    {
        apiPackage = "com.api";
        modelPackage = "com.api.bean";
        modelTemplateFiles.put("bean.mustache", ".java");
        apiTemplateFiles.put("servicerest.mustache", ".java");
    }
}