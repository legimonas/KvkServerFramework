package org.kvk.server.javassist;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Map;

public class EntityAnnotationInfo extends AnnotationInfo{
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name = "";
    public EntityAnnotationInfo(){

    }
    public EntityAnnotationInfo(String name){
        this.name = name;
    }

    @Override
    public Annotation getAnnotation(ConstPool constPool) {
        Annotation annotation = new Annotation(Entity.class.getName(), constPool);
        if(!"".equals(name))
            annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        return annotation;
    }

    public void annotateSource(JDefinedClass definedClass){
        JAnnotationUse annotationUse = definedClass.annotate(Entity.class);
        if(!"".equals(name))
            annotationUse.param("name", name);
    }

    @Deprecated
    @Override
    public String getCode(int countTabs) {
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("\t".repeat(Math.max(0, countTabs)));
        codeBuilder.append("@").append(Entity.class.getSimpleName());
        boolean begin = true;

        if (!"".equals(name)) {
            if(begin) {
                codeBuilder.append("(\n");
                begin = false;
            }
            codeBuilder.append("\t".repeat(Math.max(0, countTabs + 1)));
            codeBuilder.append("name=\"").append(name).append("\"\n");
        }
        codeBuilder.append("\t".repeat(Math.max(0, countTabs)));
        if(!begin){
            codeBuilder.append(")\n");
        }else {
            codeBuilder.append("\n");
        }

        return codeBuilder.toString();
    }
}
