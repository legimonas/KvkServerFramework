package org.kvk.server.сfg;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.kvk.server.classes.User;
import org.kvk.server.javassist.*;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class KvkSessionFactory {

    @Autowired
    private Configuration configuration;
    private SessionFactory sessionFactory;
    @Autowired
    private ApplicationContext applicationContext;

    public Class<?> createAnnotatedClass() throws CannotCompileException, IOException, ClassNotFoundException, URISyntaxException {
        TableAnnotationInfo tableAnnotationInfo = TableAnnotationInfo.builder().name("users").build();
        EntityAnnotationInfo entityAnnotationInfo = new EntityAnnotationInfo();

        List<MemberInfo> fields = new ArrayList<>();
        FieldInfo idField = new FieldInfo(int.class, "id");
        idField.addAnnotationInfo(AnnotationInfo.of(Id.class));
        idField.addAnnotationInfo(AnnotationInfo.of(GeneratedValue.class));
        FieldInfo field1= new FieldInfo(String.class, "firstName");
        field1.addAnnotationInfo(ColumnAnnotationInfo.builder().name("first_name").build());
        fields.add(idField);
        fields.add(field1);
        EntityClass entityClass = new EntityClass(
                "org.kvk.server.models.User",
                entityAnnotationInfo,
                tableAnnotationInfo,
                fields
        );
        String className = "org.kvk.server.models.User";
        CtClass ctClass = entityClass.createEntityByteCode(ClassPool.getDefault(), className);
        System.out.println(new File(".").getAbsolutePath());

        URI uri = new URI(Objects.requireNonNull(getClass().getClassLoader().getResource("")).getPath());
        String rootDir = uri.getPath();
        System.out.println(rootDir);
        ctClass.writeFile(rootDir.substring(1, rootDir.length()-1));
        return Class.forName(className);
    }
    public Class<?> createAnnotatedClass(EntityClass entityClass) throws CannotCompileException, URISyntaxException, IOException, ClassNotFoundException {
        CtClass ctClass = entityClass.createEntityByteCode(ClassPool.getDefault());
        URI uri = new URI(Objects.requireNonNull(getClass().getClassLoader().getResource("")).getPath());
        String rootDir = uri.getPath();
        ctClass.writeFile(rootDir.substring(1, rootDir.length()-1));
        return Class.forName(entityClass.getClassName());
    }
    public void setPackageWithAnnotatedClasses(String packageName) throws ClassNotFoundException, IOException, CannotCompileException, URISyntaxException {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Entity.class);
        for(Class<?> clazz: annotatedClasses){
            configuration.addAnnotatedClass(clazz);
        }
        Map<String, EntityClass> beans = applicationContext.getBeansOfType(EntityClass.class);
        for(Map.Entry<String, EntityClass> bean: beans.entrySet()){
            Class annotatedClass = createAnnotatedClass(bean.getValue());
            configuration.addAnnotatedClass(annotatedClass);
        }

        sessionFactory = configuration.buildSessionFactory();
    }
    @Bean
    public SessionFactory getSessionFactory(){
        return sessionFactory;
    }
}
