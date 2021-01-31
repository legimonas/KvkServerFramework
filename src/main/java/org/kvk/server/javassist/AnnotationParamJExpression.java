package org.kvk.server.javassist;

import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

public class AnnotationParamJExpression extends JExpressionImpl {
    private String source;
    public AnnotationParamJExpression(String source){
        this.source = source;
    }
    @Override
    public void generate(JFormatter jFormatter) {
        jFormatter.p(source);
    }
}
