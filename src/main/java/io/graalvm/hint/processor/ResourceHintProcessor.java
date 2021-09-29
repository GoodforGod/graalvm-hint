package io.graalvm.hint.processor;

import io.graalvm.hint.annotation.ResourceHint;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.naming.Context;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Please Add Description Here.
 *
 * @author Anton Kurako (GoodforGod)
 * @see ResourceHint
 * @since 27.09.2021
 */
@SupportedAnnotationTypes("io.graalvm.hint.annotation.ResourceHint")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ResourceHintProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(ResourceHint.class);
        Set<TypeElement> types = ElementFilter.typesIn(annotated);
        try {
            TypeElement element = types.iterator().next();
            PackageElement packageElem = (PackageElement) element.getEnclosingElement();
            String s = packageElem.getQualifiedName().toString();
            System.out.println(s);

            Map<String, String> options = processingEnv.getOptions();
            System.out.println("options: " + options);

            FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                    "resources/META-INF",
                    "test-resource.json");

            Writer writer = fileObject.openWriter();
            writer.write("test");
            writer.close();

            ResourcesScanner scanner = new ResourcesScanner();
            Collection<URL> urls = ClasspathHelper.forClassLoader(ResourceHintProcessor.class.getClassLoader());
            System.out.println("URLs: " + urls);
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .addScanners(scanner)
                    .setUrls(urls)
                    .addClassLoader(getClass().getClassLoader()));
            Set<String> resources = reflections.getResources(Pattern.compile(".*"));
            System.out.println("Resources: " + resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--------------------- [ResourceHint] annotations: " + annotations + ", types: " + types);
        return false;
    }
}
