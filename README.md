# Cobertura Plugin

[![Build
Status](https://ci.jenkins.io/job/Plugins/job/cobertura-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/cobertura-plugin/job/master/)

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/cobertura.svg)](https://plugins.jenkins.io/cobertura)

[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/cobertura.svg?color=blue)](https://plugins.jenkins.io/cobertura)

This plugin allows you to capture code coverage report from
[Cobertura](http://cobertura.sourceforge.net/). Jenkins will generate
the trend report of coverage.The Cobertura plugin can be [downloaded
here](http://updates.jenkins-ci.org/download/plugins/cobertura/).

## Version History

- Version history can be found in the [changelog](CHANGELOG.md)
- Tagged releases can be found [here](https://github.com/jenkinsci/cobertura-plugin/tags)

⚠️ Older versions of this plugin may not be safe to use. Please review the
following warnings before using an older version:|

- [Arbitrary file write vulnerability](https://jenkins.io/security/advisory/2020-03-09/#SECURITY-1668)
- [XXE vulnerability](https://jenkins.io/security/advisory/2020-03-09/#SECURITY-1700)

The current thinking is to merge this plugin into more generic coverage
plugin. Help appreciated.

## Configuring the Cobertura Plugin

1. Install the cobertura plugin (via Manage Jenkins -\> Manage Plugins)
2. Configure your project's build script to generate cobertura XML
   reports (See below for examples with Ant and Maven2)
3. Enable the "Publish Cobertura Coverage Report" publisher
4. Specify the directory where the coverage.xml report is generated.
5. (Optional) Configure the coverage metric targets to reflect your goals.

## Configuring build tools

Here are the configuration details for common build tools.
Please feel free to update this with corrections or additions.

### Maven 2

#### Quick configuration

You can either, enable "cobertura" analysis in your 'pom.xml' files or
just tell Jenkins to run "cobertura" goal.

If you don't want to change your pom files, add the goal `cobertura:cobertura`
to the Maven commands of your job in Jenkins:

```groovy
pipeline {
    // ...
    stage('Code Coverage') {
        steps {
            sh 'mvn clean cobertura:cobertura'
        }
    }
}
```

#### Single Project

If you are using a single module configuration, add the following into
your pom.xml. This will cause cobertura to be called each time you run
"mvn package".

```xml
<project ...>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>cobertura-maven-plugin</artifactId>
                <version>2.5.1</version>
                <configuration>
                    <formats>
                        <format>xml</format>
                    </formats>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>cobertura</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
    ...
</project>
```

#### Project hierarchies

If you are using a common parent for all Maven2 modules you can move the
plugin configuration to the pluginManagement section of the common
*parent*...

```xml
<project ...>
    ...
    <build>
        ...
        <pluginManagement>
            <plugins>
                ...
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>cobertura-maven-plugin</artifactId>
                    <version>2.2</version>
                    <configuration>
                        <formats>
                            <format>xml</format>
                        </formats>
                    </configuration>
                    <executions>
                        <execution>
                            <phase>package</phase>
                            <goals>
                                <goal>cobertura</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                ...
            </plugins>
        </pluginManagement>
        ...
    </build>
    ...
</project>
```

And add the plugin group and artifact to the children:

```xml
<project ...>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>cobertura-maven-plugin</artifactId>
            </plugin>
            ...
        </plugins>
        ...
    </build>
    ...
</project>
```

#### Execute cobertura only from Jenkins using profiles

It is highly recommend to reduce the workload of the developers machines
by disabling the cobertura plugin and only using it from within Jenkins.
The following excerpt from the *parent* shows how to do so:

```xml
<project ...>
    ...
    <profiles>
        <!-- Jenkins by default defines a property BUILD_NUMBER which is used to enable the profile. -->
        <profile>
            <id>jenkins</id>
            <activation>
                <property>
                    <name>env.BUILD_NUMBER</name>
                </property>
            </activation>
            <build>
                <pluginManagement>
                    <plugins>
                        <plugin>
                            <groupId>org.codehaus.mojo</groupId>
                            <artifactId>cobertura-maven-plugin</artifactId>
                            <version>2.2</version>
                            <configuration>
                                <formats>
                                    <format>xml</format>
                                </formats>
                            </configuration>
                            <executions>
                                <execution>
                                    <phase>package</phase>
                                    <goals>
                                        <goal>cobertura</goal>
                                    </goals>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
                </pluginManagement>
            </build>
        </profile>
    </profiles>
    ...
</project>
```

Now that your parent is only using the plugin management section if it is running
from within Jenkins, you need the children poms adapted as well:

```xml
<project ...>
    ...
    <!-- If we are running in Jenkins use cobertura. -->
    <profiles>
        <profile>
            <id>jenkins</id>
            <activation>
                <property>
                    <name>env.BUILD_NUMBER</name>
                </property>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>cobertura-maven-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
    ...
</project>
```

### Ant

You must first tell Ant about the Cobertura Ant tasks using a taskdef
statement. The best place to do this is near the top of your build.xml
script, before any target statements.

```xml
<property name="cobertura.dir" value="C:/javastuff/cobertura" />

<path id="cobertura.classpath">
    <fileset dir="${cobertura.dir}">
        <include name="cobertura.jar" />
        <include name="lib/**/*.jar" />
    </fileset>
</path>

<taskdef classpathref="cobertura.classpath" resource="tasks.properties" />
```

You'll need to instrument the classes that JUnit will be testing (not
the test classes themselves) as such:

```xml
<cobertura-instrument todir="${instrumented.dir}">
    <ignore regex="org.apache.log4j.*" />
    <fileset dir="${classes.dir}">
        <include name="**/*.class" />
        <exclude name="**/*Test.class" />
    </fileset>
    <fileset dir="${guiclasses.dir}">
        <include name="**/*.class" />
        <exclude name="**/*Test.class" />
    </fileset>
    <fileset dir="${jars.dir}">
        <include name="my-simple-plugin.jar" />
    </fileset>
</cobertura-instrument>
```

Here's an example call to the JUnit ant task that has been modified to
work with Cobertura.

```xml
<junit fork="yes" dir="${basedir}" failureProperty="test.failed">
    <!--
        Specify the name of the coverage data file to use.
        The value specified below is the default.
    -->
    <sysproperty key="net.sourceforge.cobertura.datafile"
        file="${basedir}/cobertura.ser" />

    <!--
        Note the classpath order: instrumented classes are before the
        original (uninstrumented) classes.  This is important.
    -->
    <classpath location="${instrumented.dir}" />
    <classpath location="${classes.dir}" />

    <!--
        The instrumented classes reference classes used by the
        Cobertura runtime, so Cobertura and its dependencies
        must be on your classpath.
    -->
    <classpath refid="cobertura.classpath" />

    <formatter type="xml" />
    <test name="${testcase}" todir="${reports.xml.dir}" if="testcase" />
    <batchtest todir="${reports.xml.dir}" unless="testcase">
        <fileset dir="${src.dir}">
            <include name="**/*Test.java" />
        </fileset>
    </batchtest>
</junit>
```

Finally, you need a task to generate the xml report, where:

- `destdir` is where you want the report (coverage.xml) generated.
- Your `cobertura.ser` is generated to your module root.
- `srcdir` is where your `*.java` files are located. If you use multiple modules in
  one build process you need to include the module name, if you use the simple `srcdir` parameter.
  It is not required to include module name if you use fileset.

```xml
<cobertura-report format="xml" destdir="${coveragereport.dir}" srcdir="${src.dir}" />
<!-- You can use multiple source directories this way: -->
<cobertura-report format="xml" destdir="${coveragereport.dir}" >

    <fileset dir="${src.dir.java}">

        <include name="**/*.java" />

    </fileset>

    <fileset dir="${src.dir.main}">

        <include name="**/*.java" />

    </fileset>

</cobertura-report>
```

### Gradle

Running Cobertura in gradle, copied from Piotr Gabryanczyk's post at
<http://piotrga.wordpress.com/2010/04/17/gradle-cobertura-integration-revisited/> and
tweaked to work for gradle 1.5:

Create cobertura.gradle in the root of your project:

```groovy
logger.info "Configuring Cobertura Plugin"

configurations{
  coberturaRuntime {extendsFrom testRuntime}
}

dependencies {
  coberturaRuntime 'net.sourceforge.cobertura:cobertura:1.9.4'
}

def serFile="${project.buildDir}/cobertura.ser"
def classes="${project.buildDir}/classes/main"
def classesCopy="${classes}-copy"


task cobertura(type: Test){
  dependencies {
    testRuntime 'net.sourceforge.cobertura:cobertura:1.9.4'
  }

  systemProperty "net.sourceforge.cobertura.datafile", serFile
}

cobertura.doFirst  {
  logger.quiet "Instrumenting classes for Cobertura"
  ant {
    delete(file:serFile, failonerror:false)
    delete(dir: classesCopy, failonerror:false)
    copy(todir: classesCopy) { fileset(dir: classes) }

    taskdef(resource:'tasks.properties', classpath: configurations.coberturaRuntime.asPath)
    'cobertura-instrument'(datafile: serFile) {
      fileset(dir: classes,
              includes:"**/*.class",
              excludes:"**/*Test.class")
    }
  }
}

cobertura.doLast{
  if (new File(classesCopy).exists()) {
    //create html cobertura report
    ant.'cobertura-report'(destdir:"${project.reportsDir}/cobertura",
            format:'html', srcdir:"src/main/java", datafile: serFile)
    //create xml cobertura report
     ant.'cobertura-report'(destdir:"${project.reportsDir}/cobertura",
            format:'xml', srcdir:"src/main/java", datafile: serFile)
    ant.delete(file: classes)
    ant.move(file: classesCopy, tofile: classes)
  }
}
```

Apply Cobertura.gradle in your build.gradle.

Either (if single project build):

```groovy
apply plugin: 'java'
apply from: 'cobertura.gradle'
```

Or (if multi project build):

```groovy
subprojects {
  apply plugin: 'java'
  apply from: "${parent.projectDir.canonicalPath}/cobertura.gradle"
}
```

