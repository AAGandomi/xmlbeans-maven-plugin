package org.apache.xmlbeans.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
@author AAGandomi
This plugin is a refactored plugin of the Apache's official project's repository :
https://github.com/apache/xmlbeans, based on the scomp ant task for xmlbeans.
This mojo generates source and class files from XML Schemas (and optionally builds them), similar to the command 'scomp'.
For further explanations about its usage in a maven project's environment, refer to README.md.
 */
@Mojo(name = "scomp", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ScompMojo extends AbstractMojo {
	/**
	sourceDir is a base directory for the list in sourceschema
	 */
	@Parameter
	private String sourceDir;

	/**
	sourceSchemas is a comma-delimited list of all the schemas you want to compile
	*/
	@Parameter
	private String sourceSchemas;

	/**
	xmlConfigs points to your xmlconfig.xml file
	*/
	@Parameter
	private String xmlConfigs;

	@Parameter
	/**
	javaTargetdir is where you want generated java source to appear
  	*/
	private String javaTargetDir;

	/**
	classTargetDir is where you want compiled class files to appear
	*/
	@Parameter
	private String classTargetDir;

	/**
	catalogLocation is the location of an entity resolver catalog to use for resolving namespace to schema locations.
	*/
	@Parameter
	private String catalogLocation;
	
	//TODO Give a proper explaination
	@Parameter
	private String classPath;

	//TODO Give a proper explaination
	@Parameter
	private List<Resource> resources;

	/**
	buildSchemas sets build process of the generated sources 
	*/
	@Parameter(defaultValue="false")
	private boolean buildSchemas;
	
	// this copy should not end in /
	@Parameter
	private String baseSchemaLocation = "schemaorg_apache_xmlbeans/src";

	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getSourceSchemas() {
		return sourceSchemas;
	}

	public void setSourceSchemas(String sourceSchemas) {
		this.sourceSchemas = sourceSchemas;
	}

	public String getXmlConfigs() {
		return xmlConfigs;
	}

	public void setXmlConfigs(String xmlConfigs) {
		this.xmlConfigs = xmlConfigs;
	}

	public String getJavaTargetDir() {
		return javaTargetDir;
	}

	public void setJavaTargetDir(String javaTargetDir) {
		this.javaTargetDir = javaTargetDir;
	}

	public String getClassTargetDir() {
		return classTargetDir;
	}

	public void setClassTargetDir(String classTargetDir) {
		this.classTargetDir = classTargetDir;
	}

	public String getCatalogLocation() {
		return catalogLocation;
	}

	public void setCatalogLocation(String catalogLocation) {
		this.catalogLocation = catalogLocation;
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public List<Resource>  getResources() {
		return resources;
	}

	public void setResources(List<Resource>  resources) {
		this.resources = resources;
	}

	public boolean isBuildSchemas() {
		return buildSchemas;
	}

	public void setBuildSchemas(boolean buildSchemas) {
		this.buildSchemas = buildSchemas;
	}

	public String getBaseSchemaLocation() {
		return baseSchemaLocation;
	}

	public void setBaseSchemaLocation(String baseSchemaLocation) {
		if (baseSchemaLocation != null && !(baseSchemaLocation.length() == 0)) {
			this.baseSchemaLocation = baseSchemaLocation;
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if(sourceDir==null) getLog().debug("sourceDir is null");

		if(baseSchemaLocation==null) getLog().debug("baseSchemaLocation is null");

		if(sourceSchemas==null) getLog().debug("sourceSchemas is null");

		if(xmlConfigs==null) getLog().debug("xmlConfigs is null");

		if(classPath==null) getLog().debug("classPath is null");
		
		List<File> schemas = new ArrayList<File>();
		File base = new File(sourceDir);
		Resource resource = new Resource();
		resource.setDirectory(sourceDir);
		resource.setTargetPath(baseSchemaLocation);
		for (StringTokenizer st = new StringTokenizer(sourceSchemas, ","); st.hasMoreTokens();) {
			String schemaName = st.nextToken();
			schemas.add(new File(base, schemaName));
			resource.addInclude(schemaName);
		}
		resources = new ArrayList<Resource>();
		resources.add(resource);
		if (buildSchemas) {
			List<File> configs = new ArrayList<File>();

			if (xmlConfigs != null) {
				for (StringTokenizer st = new StringTokenizer(xmlConfigs, ","); st.hasMoreTokens();) {
					String configName = st.nextToken();
					configs.add(new File(configName));
				}
			}
			List<File> classPathList = new ArrayList<File>();
			List<URL> urls = new ArrayList<URL>();
			if (classPath != null) {
				for (StringTokenizer st = new StringTokenizer(classPath, ","); st.hasMoreTokens();) {
					String classpathElement = st.nextToken();
					File file = new File(classpathElement);
					classPathList.add(file);
					try {
						urls.add(file.toURI().toURL());
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Adding to classpath: " + file);
				}
			}
			ClassLoader cl = new URLClassLoader((URL[]) urls.toArray(new URL[] {}));
			EntityResolver entityResolver = null;
			if (catalogLocation != null) {
				CatalogManager catalogManager = CatalogManager.getStaticManager();
				catalogManager.setCatalogFiles(catalogLocation);
				entityResolver = new CatalogResolver();
			}
			URI sourceDirURI = new File(sourceDir).toURI();
			entityResolver = new PassThroughResolver(cl, entityResolver, sourceDirURI, baseSchemaLocation);

			SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
			params.setBaseDir(null);
			params.setXsdFiles((File[]) schemas.toArray(new File[] {}));
			params.setWsdlFiles(new File[] {});
			params.setJavaFiles(new File[] {});
			params.setConfigFiles((File[]) configs.toArray(new File[] {}));
			params.setClasspath((File[]) classPathList.toArray(new File[] {}));
			params.setOutputJar(null);
			params.setName(null);
			params.setSrcDir(new File(javaTargetDir));
			params.setClassesDir(new File(classTargetDir));
			params.setCompiler(null);
			params.setMemoryInitialSize(null);
			params.setMemoryMaximumSize(null);
			params.setNojavac(true);
			params.setQuiet(false);
			params.setVerbose(true);
			params.setDownload(false);
			params.setNoUpa(false);
			params.setNoPvr(false);
			params.setDebug(true);
			params.setErrorListener(new ArrayList<Object>());
			params.setRepackage(null);
			params.setExtensions(null);
			params.setMdefNamespaces(null);
			params.setEntityResolver(entityResolver);

			boolean result = SchemaCompiler.compile(params);
			
			if (!result) {
				@SuppressWarnings("unchecked")
				Collection<Object> errors = params.getErrorListener();
				for (Iterator<Object> iterator = errors.iterator(); iterator.hasNext();) {
					Object o = (Object) iterator.next();
					System.out.println("xmlbeans error: " + o);
				}
				throw new MojoFailureException("Schema compilation failed");
			}
		}

	}

	private static class PassThroughResolver implements EntityResolver {
        private final ClassLoader cl;
        private final EntityResolver delegate;
        private final URI sourceDir;
        //this copy has an / appended
        private final String baseSchemaLocation;

        public PassThroughResolver(ClassLoader cl, EntityResolver delegate, URI sourceDir, String baseSchemaLocation) {
            this.cl = cl;
            this.delegate = delegate;
            this.sourceDir = sourceDir;
            this.baseSchemaLocation = baseSchemaLocation + "/";
        }
        public InputSource resolveEntity(String publicId,
                                         String systemId)
                throws SAXException, IOException {
            if (delegate != null) {
                InputSource is = delegate.resolveEntity(publicId, systemId);
                if (is != null) {
                    return is;
                }
            }
            System.out.println("Could not resolve publicId: " + publicId + ", systemId: " + systemId + " from catalog");
            String localSystemId;
            try {
                 localSystemId = sourceDir.relativize(new URI(systemId)).toString();
            } catch (URISyntaxException e) {
                throw (IOException)new IOException("Could not relativeize systemId").initCause(e);
            }
            InputStream in = cl.getResourceAsStream(localSystemId);
            if (in != null) {
                System.out.println("found in classpath at: " + localSystemId);
                return new InputSource(in);
            }
            in = cl.getResourceAsStream(baseSchemaLocation + localSystemId);
            if (in != null) {
                System.out.println("found in classpath at: META-INF/" + localSystemId);
                return new InputSource(in);
            }
            System.out.println("Not found in classpath, looking in current directory: " + systemId);
            return new InputSource(systemId);
        }
    }

}