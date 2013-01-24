How to make the Plug-In to compile
==================================
The JRE jar-file 'tools.jar' is used by 'org.eclipse.scout.jaxws.tool.JaxWsStubGenerator' to build webservice stubs.
As this jar is not part of the JRE system libraries, you manually have to add it.

1. Open Eclipse Preferences
2. Open 'Java | Installed JREs'
3. Edit default JRE
4. Add 'External JAR' as system library
5. Choose the tools.jar which typically is located at %JAVA_HOME%/lib/tools.jar

Please note: At SDK runtime, the JAR is located as follows:
1. If 'tools.jar' is part of the workspace classpath, this one is used
2. It is tried to locate the JAR by using the %JAVA_HOME% path variable

How webservice providers are discovered
=======================================
Providers are to be registered in sun-jaxws.xml of their respective Plug-Ins and must be located at WEB-INI/sun-jaxws.xml.
At runtime, all bundles (Plug-Ins, fragments) installed in the OSGi environment are scanned for such a descriptor file and their webservices published.
The endpoints are published at the address that is composed of the JaxWsServlet-alias and the respective URL-pattern in sun-jaxws.xml.
To see all published endpoints, please enter the JaxWsServlet-alias into the browsser's address bar.
To customize this summary page, create a HTML template named 'jaxws-services.html' and put it into your server project (e.g. /resources/html/jaxws-services.html).
To use your template over the default one, configure the init-paramters 'bundle-name' and 'bundle-path' on the JaxWsServlet-registration to point to your resources.
