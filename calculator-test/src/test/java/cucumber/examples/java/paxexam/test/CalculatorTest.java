package cucumber.examples.java.paxexam.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

import java.io.File;
import java.util.Collections;

import javax.inject.Inject;

import cucumber.api.java.ObjectFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;

import cucumber.api.CucumberOptions;
import cucumber.java.runtime.osgi.OsgiClassFinder;
import cucumber.java.runtime.osgi.PaxExamObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@CucumberOptions(features = "../../../src/test/resources/features")
public class CalculatorTest {

    @Inject
    private Injector injector;

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] config() {

        MavenArtifactUrlReference karafUrl = maven()
            .groupId("org.apache.karaf")
            .artifactId("apache-karaf")
            .version("4.1.1")
            .type("zip");

        MavenUrlReference karafStandardRepo = maven()
            .groupId("org.apache.karaf.features")
            .artifactId("standard")
            .version("4.1.1")
            .classifier("features")
            .type("xml");

        return new Option[]{

            karafDistributionConfiguration()
                .frameworkUrl(karafUrl)
                .unpackDirectory(new File("target", "exam"))
                .useDeployFolder(false),
            keepRuntimeFolder(),
            configureConsole().ignoreLocalConsole(),
//            features(karafStandardRepo, "scr"),

            mavenBundle("io.cucumber", "pax-exam-calculator-api"),
            mavenBundle("io.cucumber", "pax-exam-calculator-service"),

            mavenBundle("io.cucumber", "gherkin"),
            mavenBundle("io.cucumber", "tag-expressions"),
            mavenBundle("info.cukes", "cucumber-jvm-deps"),
            mavenBundle("io.cucumber", "cucumber-core"),
            mavenBundle("io.cucumber", "cucumber-java"),
            mavenBundle("io.cucumber", "cucumber-osgi"),

//            mavenBundle("org.slf4j", "slf4j-api"),
//            mavenBundle("ch.qos.logback", "logback-core"),
//            mavenBundle("ch.qos.logback", "logback-classic"),

            junitBundles()
        };
    }

    @Test
    public void cucumber() throws Exception {
        assertNotNull(injector);
        assertNotNull(bundleContext);
        final ResourceLoader resourceLoader = new FileResourceLoader();
        final ClassLoader classLoader = Runtime.class.getClassLoader();
        final ObjectFactory objectFactory = new PaxExamObjectFactory(injector);
        final ClassFinder classFinder = new OsgiClassFinder(bundleContext);
        final Backend backend = new JavaBackend(objectFactory, classFinder);

        final RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(getClass());
        final RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        final Runtime runtime = new Runtime(resourceLoader, classLoader, Collections.singleton(backend), runtimeOptions);

        runtime.run();

        if (!runtime.getErrors().isEmpty()) {
            throw new CucumberException(runtime.getErrors().get(0));
        } else if (runtime.exitStatus() != 0x00) {
            throw new CucumberException("There are pending or undefined steps.");
        }
        assertEquals(runtime.getErrors().size(), 0);
    }
}
