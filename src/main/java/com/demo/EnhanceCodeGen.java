package com.demo;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.properties.Property;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author BlueWang
 * @ClassName: EnhanceCodeGen
 * @Description:
 * @date 2019/9/7 18:19
 */
public class EnhanceCodeGen extends DefaultGenerator {
    class Info {
        public String className;
        public List<EnhanceCodeGen.Bean> beanList;
//        public List<Map<String,String>> beanList;
    }

    class Bean {
        public String name;
        public String desc;
        public String type;
    }

    @Override
    public List<File> generate() {
        List<Map<String, Object>> infoList = new ArrayList<>();
        List<Map<String, String>> importList = new ArrayList<>();
        Map<String, Path> pathMap = swagger.getPaths();
        EnhanceCodeGen.Info info = new EnhanceCodeGen.Info();
        Map<String, Model> definitions = swagger.getDefinitions();
        info.beanList = genBean(definitions);
        ;

        info.className = swagger.getTags() == null ? "Test" : swagger.getTags().get(0).getName();
        String outputFilePath = "src/main/java/com/api/" + info.className + ".java";
        String templateFilePath = "src/main/resources/bean.mustache";
        String templateFileInfo = "";

        info.beanList.stream().forEach(x -> {
            System.out.println(String.format("@ApiModelProperty(value = \"%s\")\n" +
                    "\tprivate %s %s;", x.desc, x.type, x.name));
        });
        return null;
    }

    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    private List<EnhanceCodeGen.Bean> genBean(Map<String, Model> definitions) {
        List<EnhanceCodeGen.Bean> beans = new ArrayList<>();
        Iterator<String> it = definitions.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            EnhanceCodeGen.Bean b = new EnhanceCodeGen.Bean();
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

    private List<EnhanceCodeGen.Bean> genPropBean(Map<String, Property> prop) {
        List<EnhanceCodeGen.Bean> beans = new ArrayList<>();
        Iterator<String> it = prop.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            EnhanceCodeGen.Bean b = new EnhanceCodeGen.Bean();
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
}
