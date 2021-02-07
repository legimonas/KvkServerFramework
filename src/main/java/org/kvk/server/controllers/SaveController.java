package org.kvk.server.controllers;

import com.kvk.config.javassist.EntityClass;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.kvk.server.classes.User;
import org.kvk.server.classes.UserForm;
import org.kvk.server.сfg.KvkSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SaveController extends FrameworkController{
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private ApplicationContext applicationContext;

    private Class<?> clazz;
    public void setClassName(String className) throws ClassNotFoundException {
        clazz = Class.forName(className);
    }
    public void setEntityClass(EntityClass entityClass) throws ClassNotFoundException {
        clazz = Class.forName(entityClass.getClassName());
    }

    @PostConstruct
    public void init() throws NoSuchMethodException {

        addMapping(urlPattern, getClass().getDeclaredMethod("save", HttpServletRequest.class), requestMethod);
//        List list = Arrays.asList(applicationContext.getBeansOfType(User.class).values().toArray());
//        for(Object o: list){
//            System.out.println(o);
//        }
    }

    public void addMapping(String urlPath, Method method, RequestMethod requestMethod) throws NoSuchMethodException {
        RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(urlPath)
                .methods(requestMethod)
                .build();

        handlerMapping.registerMapping(requestMappingInfo,this, method);
    }

    private Object getModelFromRequest(HttpServletRequest request) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object obj = clazz.getConstructor().newInstance();
        for(Field field: obj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            field.set(obj, request.getParameter(field.getName()));
        }
        return obj;
    }

    public ModelAndView save(HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Session session = sessionFactory.openSession();
        Object object = getModelFromRequest(request);
        session.beginTransaction();
        session.save(object);
        session.getTransaction().commit();
        return null;
    }


}
