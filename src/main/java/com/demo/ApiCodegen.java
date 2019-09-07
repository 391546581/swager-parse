package com.demo;

/**
 * @author BlueWang
 * @ClassName: ApiCodegen
 * @Description:
 * @date 2019/9/5 17:12
 */

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiCodegen extends DefaultGenerator {

    @Override
    public List<File> generate() {
        List<Map<String, Object>> infoList = new ArrayList<>();
        List<Map<String, String>> importList = new ArrayList<>();
        Map<String, Path> pathMap = swagger.getPaths();
        Info info = new Info();
        info.apiPackage = config.apiPackage();
        info.modelPackage = config.modelPackage();
        info.basePath = swagger.getBasePath();
        info.className = swagger.getTags() == null ? "Test" : swagger.getTags().get(0).getName();

        for (Map.Entry<String, Path> entry : pathMap.entrySet()) {
            Map<String, Object> infoMap = new HashMap<>();
            infoMap.put("urlName", entry.getKey());
            Path path = entry.getValue();
            changeType(path, infoMap, importList);
            infoMap.put("path", path);
            infoList.add(infoMap);
        }
        info.infoList = infoList;
        info.importList = importList;
        String outputFilePath = "src/main/java/com/api/" + info.className + ".java";
        String templateFilePath = "src/main/resources/servicerest.mustache";
        String templateFileInfo = "";
        try {
            //获取模板信息
            templateFileInfo = Files.readAllLines(Paths.get(templateFilePath)).toString();
            //生成模板
            Template template = Mustache.compiler().compile(templateFileInfo);
            //解析模板
            String result = template.execute(info);
            //生成Controller文件
            Files.write(Paths.get(outputFilePath).toAbsolutePath(), result.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    private void changeType(Path path, Map<String, Object> infoMap, List<Map<String, String>> importList) {
        List<Parameter> parameterList;
        Map<String, String> typeMap = config.typeMapping();
        if (path.getGet() != null) {
            infoMap.put("hasGet", true);
            parameterList = path.getGet().getParameters();
            for (Parameter parameter : parameterList) {
                if (parameter instanceof PathParameter) {

                    PathParameter pathParameter = (PathParameter) parameter;
                    pathParameter.setType(typeMap.get(pathParameter.getType()));
                } else if (parameter instanceof QueryParameter) {
                    QueryParameter pathParameter = (QueryParameter) parameter;
                    pathParameter.setType(typeMap.get(pathParameter.getType()));
                } else {
                    System.out.println(parameter.getName());
                }
            }
            Property property = path.getGet().getResponses().get("200").getSchema();
            if (property != null) {
                if (property instanceof RefProperty) {
                    RefProperty refProperty = (RefProperty) property;
                    infoMap.put("responseType", refProperty.getSimpleRef());
                    Map<String, String> map = new HashMap<>();
                    map.put("import", config.modelPackage() + "." + refProperty.getSimpleRef());
                    importList.add(map);
                } else if (property instanceof ArrayProperty) {
                    System.out.println(property.getName());
                    ArrayProperty refProperty = (ArrayProperty) property;
                    infoMap.put("responseType", refProperty.getClass());
                    Map<String, String> map = new HashMap<>();
                    map.put("import", config.modelPackage() + "." + refProperty.getClass());
                    importList.add(map);
                } else {
                    System.out.println(property.getName());
                }
            }

        }
        //TODO 其他几种请求 put，post,delete...

    }

    class Info {
        public String apiPackage;
        public String modelPackage;
        public String basePath;
        public String className;
        public List<Map<String, String>> importList;
        public List<Map<String, Object>> infoList;
    }

}
