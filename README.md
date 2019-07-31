# Unofficial XMLBeans's Maven Plugin
This plugin has been developed as a byproduct of a personal (**AAGandomi**) ongoing project : any constructive feedback is very appreciated.<br>
The objective of the xmlbeans-maven-plugin is to provide an automated procedure to generate and build class object for the development of schema-based java application models.
## Setup
The following snippet provides an example of a simple plugin configuration to include within a project's ``pom.xml`` :

```xml
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>org.apache.xmlbeans</groupId>
            <artifactId>xmlbeans-maven-plugin</artifactId>
            <configuration>
                <buildSchemas>true</buildSchemas>
                <javaTargetDir>${project.basedir}\src\main\java</javaTargetDir>
                <classTargetDir>${project.basedir}\target\classes</classTargetDir>
                <sourceSchemas>myschema1.xsd,myschema2.xsd,myschema3.xsd</sourceSchemas>
                <xmlConfigs>${project.basedir}\schemas\xmlbeans.xsdconfig</xmlConfigs>
                <sourceDir>${project.basedir}\schemas</sourceDir>
            </configuration>
        </plugin>
        ...
      </plugins>
    ...
</build>
```

**Note that xmlbeans-maven-plugin latches on default to the "generate-sources" phase of the Maven's lifecycle: consult the Maven plugin's execution documentation to customize this aspect.**

### Notes for a quick parameters' setup
**buildSchema** is a boolean configuration parameter that instructs the plugin to follow up the source generation procedure with the relative build process.<br>
This option is set on default as false, although it is usually suggested to leverage on the plugin to build the generated sources to ease the project's build pipeline, due to the XMLBean specifics that relies on both java source and schema-related binary files.<br>
In case the generated sources reside amid the build path of different building processes, it might be advisable to set a distinct source target directory (**javaTargetDir**) in order to avoid any possible antagonism amongst the build tasks.<br><br>
XMLBeans configuration files are best described in the [Apache XMLBeans official website](https://xmlbeans.apache.org/)

## Notice
This product includes software developed by [The Apache Software Foundation](https://www.apache.org/).