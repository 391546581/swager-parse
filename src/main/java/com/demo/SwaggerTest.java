package com.demo;

/**
 * @author BlueWang
 * @ClassName: SwaggerTest
 * @Description:
 * @date 2019/9/5 17:05
 */
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


public class SwaggerTest
{

    static Logger logger = Logger.getLogger(SwaggerTest.class);

    public void Test(String filePath) throws IOException {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        System.getProperties().put("debugParser","true");
        String info = new String(Files.readAllBytes(Paths.get(filePath)));

        //将yaml文件转化为Swagger对象
        Swagger swagger = new SwaggerParser().parse(info);

        //JavaServiceCodegen继承JavaClientCodegen（存放类的信息，类型对应["integer", "Integer"]表等等），用于扩展一些自定义功能
        JavaServiceCodegen serviceCodegen = new JavaServiceCodegen();
        ClientOptInput input = new ClientOptInput().opts(new ClientOpts()).swagger(swagger);
        input.setConfig(serviceCodegen);

        ApiCodegen apiCodegen = new ApiCodegen();
        apiCodegen.opts(input).generate();

    }

    public static void main(String[] args) {
        SwaggerTest swaggerTest = new SwaggerTest();
        try {
            swaggerTest.Test("src/main/resources/swagger.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}