package org.kvk.server.сfg;

import com.kvk.config.javassist.*;
import com.kvk.config.javassist.builders.ByteCodeBuilder;
import com.kvk.config.xml.XMLEntityHandler;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.xml.sax.SAXException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.*;
import java.util.*;


public class KvkSessionFactory {

    @Autowired
    private Configuration configuration;
    private SessionFactory sessionFactory;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ServletContext servletContext;

//    public Class<?> createAnnotatedClass() throws CannotCompileException, IOException, ClassNotFoundException, URISyntaxException, NotFoundException {
//        TableAnnotationInfo tableAnnotationInfo = TableAnnotationInfo.builder().name("users").build();
//        EntityAnnotationInfo entityAnnotationInfo = new EntityAnnotationInfo();
//
//        List<MemberInfo> fields = new ArrayList<>();
//        FieldInfo idField = new FieldInfo(int.class, "id");
//        idField.addAnnotationInfo(AnnotationInfo.of(Id.class));
//        idField.addAnnotationInfo(AnnotationInfo.of(GeneratedValue.class));
//        FieldInfo field1= new FieldInfo(String.class, "firstName");
//        field1.addAnnotationInfo(ColumnAnnotationInfo.builder().name("first_name").build());
//        fields.add(idField);
//        fields.add(field1);
//        EntityClass entityClass = new EntityClass(
//                "org.kvk.server.models.User",
//                entityAnnotationInfo,
//                tableAnnotationInfo,
//                fields
//        );
//        String className = "org.kvk.server.models.User";
//        entityClass.setClassName(className);
//        System.out.println(new File(".").getAbsolutePath());
//
//        URI uri = new URI(Objects.requireNonNull(getClass().getClassLoader().getResource("")).getPath());
//        String rootDir = uri.getPath();
//        System.out.println(rootDir);
//        ByteCodeBuilder.create().buildCode(entityClass, rootDir.substring(1, rootDir.length()-1));
//        return Class.forName(className);
//    }
    @SneakyThrows
    public File loadConfigSchemaFromGit() throws MalformedURLException {
        URL url = new URL("https://raw.githubusercontent.com/legimonas/KvkConfigLib/development/src/main/resources/config.xsd");
        URLConnection uc = url.openConnection();

        uc.setRequestProperty("X-Requested-With", "Curl");
        ArrayList<String> list=new ArrayList<String>();
        //String userpass = username + ":" + password;
        //String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));//needs Base64 encoder, apache.commons.codec
        //uc.setRequestProperty("Authorization", basicAuth);

        BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String line = null;
        StringBuilder file = new StringBuilder();
        while ((line = reader.readLine()) != null)
            file.append(line).append("\n");
        //System.out.println(file);
        File xsdFile = new File(servletContext.getRealPath("/WEB-IBF/") + "config.xsd");
        xsdFile.getParentFile().mkdirs();
        xsdFile.createNewFile();
        new FileWriter(xsdFile, false).write(file.toString());
        return xsdFile;
    }
    public void validateEntityConfig(File configFile) throws IOException, SAXException {
        URL url = new URL("https://raw.githubusercontent.com/legimonas/KvkConfigLib/development/src/main/resources/config.xsd");
        //File schemaFile = new File(url.getFile());
        //File schemaFile = loadConfigSchemaFromGit();
        Source xmlFile = new StreamSource(configFile);
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI );
        Schema schema = schemaFactory.newSchema(url);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
        System.out.println("Config xml file is valid!!!");
    }
    public List<EntityClass> parseClassesFromConfigXML(File configFile) throws ParserConfigurationException, SAXException, IOException {
        validateEntityConfig(configFile);
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        XMLEntityHandler handler = new XMLEntityHandler();
        parser.parse(configFile, handler);
        return handler.getResult();
    }
    public Class<?> createAnnotatedClass(EntityClass entityClass) throws CannotCompileException, URISyntaxException, IOException, ClassNotFoundException, NotFoundException {
        URI uri = new URI(Objects.requireNonNull(getClass().getClassLoader().getResource("")).getPath());
        String rootDir = uri.getPath();
        ByteCodeBuilder.create().buildCode(entityClass, rootDir.substring(1, rootDir.length()-1));
        return Class.forName(entityClass.getClassName());
    }
    public void setPackageWithAnnotatedClasses(String packageName) throws ClassNotFoundException, IOException, CannotCompileException, URISyntaxException, NotFoundException, ParserConfigurationException, SAXException {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Entity.class);
        for(Class<?> clazz: annotatedClasses){
            configuration.addAnnotatedClass(clazz);
        }
//        Map<String, EntityClass> beans = applicationContext.getBeansOfType(EntityClass.class);
//        for(Map.Entry<String, EntityClass> bean: beans.entrySet()){
//            Class annotatedClass = createAnnotatedClass(bean.getValue());
//            configuration.addAnnotatedClass(annotatedClass);
//        }
        String configLocation = (String) applicationContext.getBean("entity-config-location");
        File file = new File(servletContext.getRealPath(configLocation));
        List<EntityClass> entities = parseClassesFromConfigXML(file);
        for (EntityClass entityClass : entities){
            Class<?> annotatedClass = createAnnotatedClass(entityClass);
            configuration.addAnnotatedClass(annotatedClass);
        }
        sessionFactory = configuration.buildSessionFactory();
    }
    @Bean
    public SessionFactory getSessionFactory(){
        return sessionFactory;
    }
}
