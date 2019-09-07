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
import io.swagger.models.Model;
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
import java.util.*;

public class ApiCodegen extends DefaultGenerator {

    @Override
    public List<File> generate() {
        List<Map<String, Object>> infoList = new ArrayList<>();
        List<Map<String, String>> importList = new ArrayList<>();
        Map<String, Path> pathMap = swagger.getPaths();
        Info info = new Info();
        Map<String, Model> definitions = swagger.getDefinitions();
        info.beanList = genBean(definitions);
        ;

        info.className = swagger.getTags() == null ? "Test" : swagger.getTags().get(0).getName();
        String outputFilePath = "src/main/java/com/api/" + info.className + ".java";
        String templateFilePath = "src/main/resources/bean.mustache";
        String templateFileInfo = "";
        try {
            //获取模板信息
            templateFileInfo = Files.readAllLines(Paths.get(templateFilePath)).toString();
            //生成模板
            Template template = Mustache.compiler().compile(templateFileInfo);

            info.beanList.stream().forEach(x -> {

            System.out.println(String.format("@ApiModelProperty(value = \"%s\")\n" +
                                "\tprivate %s %s;",x.desc,x.type ,x.name));
            });
            //解析模板
//            String result = template.execute(info);
//            Files.write(Paths.get(outputFilePath).toAbsolutePath(), result.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    private List<Bean> genBean(Map<String, Model> definitions) {
        List<Bean> beans = new ArrayList<>();
        Iterator<String> it = definitions.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            Bean b = new Bean();
            b.name = name;
            Model model = definitions.get(name);
            b.type = (String) ReflectionUtil.getValue(model, "type");
            b.type = toUpperCaseFirstOne(b.type);
            b.desc = model.getDescription();
            beans.add(b);
            beans.addAll(genPropBean(model.getProperties()));
        }
        return beans;
    }

    private List<Bean> genPropBean(Map<String, Property> prop) {
        List<Bean> beans = new ArrayList<>();
        Iterator<String> it = prop.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            Bean b = new Bean();
            b.name = name;
            Property property = prop.get(name);

            Object type = ReflectionUtil.getValue(property, "type");
            if (type == null) {
                type = ReflectionUtil.getValue(property, "TYPE");
            }
            if (type != null) {
                b.type = (String) type;
                b.type = toUpperCaseFirstOne(b.type);
                b.desc = property.getDescription();
                beans.add(b);
            } else {
                b.type = getTypeByClass(property.getClass().getSimpleName());
                b.type = toUpperCaseFirstOne(b.type);
                b.desc = property.getDescription();
                beans.add(b);
            }
        }
        return beans;
    }

    private String getTypeByClass(String simpleName) {
        String res;
        switch (simpleName) {
            case "DoubleProperty":
                res = "BigDecimal";
                break;
            case "LongProperty":
                res = "Long";
                break;
            case "DateTimeProperty":
                res = "String";
                break;
            case "MapProperty":
                res = "Map";
                break;
            default:
                res = "String";
        }
        return res;
    }

    //    private List<Map<String,String>> genMap(Map<String, Model> definitions) {
//        List<Map<String,String>> beans = new ArrayList<>();
//        Iterator<String> it = definitions.keySet().iterator();
//        while (it.hasNext()) {
//            Map<String, String> infoMap = new HashMap<>();
//            String name = it.next();
//            infoMap.put("name", name);
//            Model model = definitions.get(name);
//            infoMap.put("type", (String) ReflectionUtil.getValue(model,"type"));
//            beans.add(infoMap);
//            beans.addAll(genPropMap(model.getProperties()));
//        }
//        return beans;
//    }
//    private List<Map<String,String>> genPropMap(Map<String, Property> prop) {
//        List<Map<String,String>> beans = new ArrayList<>();
//        Iterator<String> it = prop.keySet().iterator();
//        while (it.hasNext()) {
//            String name = it.next();
//            Property property = prop.get(name);
//
//            String type = (String) ReflectionUtil.getValue(property, "type");
//            if(type!=null){
//                Map<String, String> infoMap = new HashMap<>();
//                infoMap.put("name", name);
//                infoMap.put("type",type);
//                beans.add(infoMap);
//            }
//        }
//        return beans;
//    }

    //    @Override
    public List<File> generate1() {
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
        public List<Bean> beanList;
//        public List<Map<String,String>> beanList;
    }

    class Bean {
        public String name;
        public String desc;
        public String type;
    }
}
