package org.kvk.server.javassist;

import com.sun.codemodel.*;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class EntityClass {
    private EntityAnnotationInfo entityAnnotationInfo;
    private TableAnnotationInfo tableAnnotationInfo;
    private List<MemberInfo> membersInfo;
    private String className;

    public EntityClass() {

    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public EntityClass(String className, EntityAnnotationInfo entityAnnotationInfo, TableAnnotationInfo tableAnnotationInfo, List<MemberInfo> membersInfo) {
        this.entityAnnotationInfo = entityAnnotationInfo;
        this.tableAnnotationInfo = tableAnnotationInfo;
        this.membersInfo = membersInfo;
    }

    public EntityAnnotationInfo getEntityAnnotationInfo() {
        return entityAnnotationInfo;
    }

    public void setEntityAnnotationInfo(EntityAnnotationInfo entityAnnotationInfo) {
        this.entityAnnotationInfo = entityAnnotationInfo;
    }

    public TableAnnotationInfo getTableAnnotationInfo() {
        return tableAnnotationInfo;
    }

    public void setTableAnnotationInfo(TableAnnotationInfo tableAnnotationInfo) {
        this.tableAnnotationInfo = tableAnnotationInfo;
    }

    public List<MemberInfo> getMembersInfo() {
        return membersInfo;
    }

    public void setMembersInfo(List<MemberInfo> membersInfo) {
        this.membersInfo = membersInfo;
    }

    public EntityClass(String className, EntityAnnotationInfo entityAnnotationInfo, TableAnnotationInfo tableAnnotationInfo) {
        this.entityAnnotationInfo = entityAnnotationInfo;
        this.tableAnnotationInfo = tableAnnotationInfo;
        membersInfo = new ArrayList<>();
    }



    public CtClass createEntityByteCode(ClassPool classPool, String className) throws CannotCompileException {
        CtClass ctClass = classPool.makeClass(className);
        ConstPool constPool = ctClass.getClassFile().getConstPool();

        AnnotationsAttribute classAttribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        classAttribute.addAnnotation(entityAnnotationInfo.getAnnotation(constPool));
        classAttribute.addAnnotation(tableAnnotationInfo.getAnnotation(constPool));

        ctClass.getClassFile().addAttribute(classAttribute);

        for (MemberInfo memberInfo: membersInfo){
            CtField ctField = CtField.make(memberInfo.getType().getName() + " " + memberInfo.getName() + ";", ctClass);
            ConstPool constFieldPool = ctField.getFieldInfo().getConstPool();

            AnnotationsAttribute fieldAttribute = new AnnotationsAttribute(constFieldPool, AnnotationsAttribute.visibleTag);
            for(AnnotationInfo annotationInfo: memberInfo.getAnnotationsInfo())
            fieldAttribute.addAnnotation(annotationInfo.getAnnotation(constPool));

            ctField.getFieldInfo().addAttribute(fieldAttribute);
            ctClass.addField(ctField);
        }

        return ctClass;
    }
    public CtClass createEntityByteCode(ClassPool classPool) throws CannotCompileException {
        return createEntityByteCode(classPool, this.className);
    }
    @Deprecated
    public String getSourceCode(String className){
        StringBuilder codeBuilder = new StringBuilder();
        String packageName = className.substring(0, className.lastIndexOf("."));
        codeBuilder.append("package ").append(packageName).append(";\n\n");

        codeBuilder.append("import ").append(Column.class.getName()).append(";\n");
        codeBuilder.append("import ").append(Entity.class.getName()).append(";\n");
        codeBuilder.append("import ").append(Table.class.getName()).append(";\n\n");

        HashSet<Class<?>> imports = new HashSet<>();
        for(MemberInfo memberInfo: membersInfo){
            imports.add(memberInfo.getType());
        }
        for(Class<?> imp : imports){
            codeBuilder.append("import ").append(imp.getName()).append(";\n");
        }
        codeBuilder.append("\n");


        codeBuilder.append(entityAnnotationInfo.getCode(0));
        codeBuilder.append(tableAnnotationInfo.getCode(0));
        codeBuilder.append("public class ").append(className.substring(className.lastIndexOf(".") + 1)).append("{\n");

        for(MemberInfo memberInfo: membersInfo){
            codeBuilder.append(memberInfo.getCode(1)).append("\n");
        }
        codeBuilder.append("}");

        return codeBuilder.toString();
    }

    public void buildSourceCode(File classPathDir, String className) throws JClassAlreadyExistsException, IOException {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass definedClass = codeModel._class(className);

        entityAnnotationInfo.annotateSource(definedClass);
        tableAnnotationInfo.annotateSource(definedClass);

        for(MemberInfo memberInfo: membersInfo){
            if (memberInfo instanceof FieldInfo){
                memberInfo.addSource(definedClass);
            }
        }
        codeModel.build(classPathDir);

    }

}
