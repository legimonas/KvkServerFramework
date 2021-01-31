package org.kvk.server.javassist;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import javax.persistence.Column;
import javax.persistence.Table;

public class TableAnnotationInfo extends AnnotationInfo {

    String name = "";

    String catalog = "";

    String schema = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public TableAnnotationInfo() {
    }

    public TableAnnotationInfo(String name) {
        this.name = name;
    }

    public static class Builder {
        private final TableAnnotationInfo tableAnnotationInfo;

        private Builder() {
            tableAnnotationInfo = new TableAnnotationInfo();
        }

        public Builder name(String name) {
            tableAnnotationInfo.name = name;
            return this;
        }

        public Builder catalog(String catalog) {
            tableAnnotationInfo.catalog = catalog;
            return this;
        }

        public Builder schema(String schema) {
            tableAnnotationInfo.schema = schema;
            return this;
        }

        public TableAnnotationInfo build() {
            return tableAnnotationInfo;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Annotation getAnnotation(ConstPool constPool) {
        Annotation annotation = new Annotation(Table.class.getName(), constPool);
        if (!"".equals(name))
            annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        if (!"".equals(catalog))
            annotation.addMemberValue("catalog", new StringMemberValue(catalog, constPool));
        if (!"".equals(schema))
            annotation.addMemberValue("schema", new StringMemberValue(schema, constPool));
        return annotation;
    }

    public void annotateSource(JDefinedClass definedClass){
        JAnnotationUse tableAnnotationUse = definedClass.annotate(Table.class);
        if(!"".equals(name))
            tableAnnotationUse.param("name", name);
        if(!"".equals(catalog))
            tableAnnotationUse.param("catalog", catalog);
        if(!"".equals(schema))
            tableAnnotationUse.param("schema", schema);
    }

    @Deprecated
    @Override
    public String getCode(int countTabs) {
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("\t".repeat(Math.max(0, countTabs)));
        codeBuilder.append("@").append(Table.class.getSimpleName());
        boolean begin = true;

        if (!"".equals(name)) {
            if(begin) {
                codeBuilder.append("(\n");
                begin = false;
            }
            codeBuilder.append("\t".repeat(Math.max(0, countTabs + 1)));
            codeBuilder.append("name=\"").append(name).append("\"\n");
        }
        if (!"".equals(catalog)) {
            if(begin) {
                codeBuilder.append("(\n");
                begin = false;
            }
            codeBuilder.append("\t".repeat(Math.max(0, countTabs + 1)));
            codeBuilder.append("catalog=\"").append(catalog).append("\"\n");
        }
        if (!"".equals(schema)) {
            if(begin) {
                codeBuilder.append("(\n");
                begin = false;
            }
            codeBuilder.append("\t".repeat(Math.max(0, countTabs + 1)));
            codeBuilder.append("schema=\"").append(schema).append("\"\n");
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
