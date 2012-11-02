/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.tool;

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.xml.sax.SAXParseException;

import com.sun.tools.internal.ws.processor.model.Model;
import com.sun.tools.internal.ws.processor.model.Port;
import com.sun.tools.internal.ws.processor.model.Service;
import com.sun.tools.internal.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.internal.ws.wscompile.AbortException;
import com.sun.tools.internal.ws.wscompile.ErrorReceiver;
import com.sun.tools.internal.ws.wscompile.WsimportOptions;
import com.sun.tools.internal.ws.wscompile.WsimportTool;
import com.sun.tools.internal.ws.wsdl.parser.MetadataFinder;
import com.sun.tools.internal.ws.wsdl.parser.WSDLInternalizationLogic;

/**
 * <p>
 * This class is a wrapper for {@link WsimportTool} to generate a JAX-WS stub.
 * </p>
 * <p>
 * In order to make this class work, the JAR 'tools.jar' must be added to the classpath.
 * </p>
 * <b>Usage:</b><br/>
 * Provide '<cod>help</code>' as first argument to see detailed usage information.
 * <table>
 * <tr>
 * <td valign="top">arg[0]</td>
 * <td>Output directory where to place generated files. This might be a project relative or an absolute path. If it is a
 * file of the type jar, the generated files are put into that archive.<br/>
 * <i>E.g. ws-stub/xy.jar</i></td>
 * </tr>
 * <tr>
 * <td valign="top">arg[1]</td>
 * <td>WSDL file location (project relative)<br/>
 * <i>E.g. WEB-INF/wsdl/xy.wsdl</i></td>
 * </tr>
 * <tr>
 * <td valign="top">arg[2]</td>
 * <td>1 to applies patches, 0 to not apply patches</td>
 * </tr>
 * <tr>
 * <td valign="top">arg[3..x]</td>
 * <td>further options<br/>
 * This can be key-value pairs in the form of <code>KEY=VALUE</code> or simple directives</td>
 * </tr>
 * </table>
 * In order to run the program, follow the subsequent instructions within your Eclipse IDE:
 * <ol>
 * <li>Open this class (CTRL-T > JaxWsStubGenerator)</li>
 * <li>Right-click on the class, choose 'Run As | Run Configurations...' to open the run-configuration</li>
 * <li>In the tab 'Main', choose your Project</li>
 * <li>In the tab 'Arguments', enter the stub generation options. For instance, that could be as follow:</br>
 * <ul>
 * <li>ws-stub/xy.jar (output directory or output JAR-file)</li>
 * <li>WEB-INF/wsdl/xy.wsdl (project relative path to your WSDL file)</li>
 * <li>1</li>
 * <li>verbose</li>
 * <li>Xdebug</li>
 * <li>target=2.0</li>
 * <li>keep</li>
 * </ul>
 * </li>
 * <li>In the tab 'Classpath', add a 'User Entry' and choose 'Add external JARS's...'. Then select the tools.jar from
 * within your Java SDK installation in its lib folder.</li>
 * <li>Then you are done and can run the program</li>
 * <li>Please note, that you manually have to refresh the output Eclipse folders</li>
 * </ol>
 */
public class JaxWsStubGenerator {

  private static final int ARG_OUT_DIR = 0;
  private static final int ARG_WSDL_FILE = 1;
  private static final int ARG_APPLY_PATCH = 2;

  private static final String FILE_PATH_SEPARATOR = System.getProperty("file.separator");
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public static void main(String[] args) throws Exception {
    if ((args.length > 0 && CompareUtility.equals(args[0], "help")) || args.length < 3) {
      logInfo("Usage:");
      logInfo("arg[" + ARG_OUT_DIR + "] output directory where to place generated files. This might be a project relative or an absolute path. If it is a file of the type jar, the generated files are put into that archive.");
      logInfo("arg[" + ARG_WSDL_FILE + "] project relative path to WSDL");
      logInfo("arg[" + ARG_APPLY_PATCH + "] apply patches to stub source");
      logInfo("arg[..] other options");
      logInfo("[options supported in build file]");

      // print help of WsImportTool
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      new WsimportTool(os).run(new String[]{"-help"});
      logInfo(os);
      return;
    }

    String wsdlLocation = args[ARG_WSDL_FILE];
    if (wsdlLocation.startsWith(FILE_PATH_SEPARATOR)) {
      wsdlLocation = wsdlLocation.substring(1);
    }

    String outDir = null;
    String jarFileName = null;

    // prepare output directory
    Path outPath = new Path(args[ARG_OUT_DIR]);
    File tempOutDir = null;
    String suffix = outPath.getFileExtension();
    if (suffix != null && suffix.equalsIgnoreCase("jar")) {
      jarFileName = outPath.lastSegment();
      outDir = outPath.removeLastSegments(1).toPortableString();
    }
    else {
      outDir = outPath.toPortableString();
    }
    new File(outDir).mkdirs();

    // instrument with output directory
    List<String> properties = new LinkedList<String>();

    // instrument with WSDL file
    properties.add("-wsdllocation");
    properties.add(wsdlLocation);

    // instrument with other options
    boolean noCompile = false;

    for (int i = 3; i < args.length; i++) {
      String directive = args[i];
      if (directive == null || directive.trim().length() == 0) {
        continue;
      }
      String[] property = directive.split("=", 2);

      // omit output directories as set by args[0]
      String key = property[0].trim();
      if (key.equals("s") ||
          key.equals("d")) {
        continue;
      }

      // check for compilation
      if (key.equals("Xnocompile")) {
        noCompile = true;
      }

      String param = "-" + key;
      if (property.length == 2 && property[1].trim().length() > 0) {
        String value = property[1].trim();
        properties.add(param);
        properties.add(value);
        logInfo("JAX-WS option: " + StringUtility.join(" ", param, value));
      }
      else {
        properties.add(param);
        logInfo("JAX-WS directive: " + param);
      }
    }

    boolean createJarFile = (jarFileName != null);

    // instrument with output directory
    if (createJarFile) {
      tempOutDir = IOUtility.createTempDirectory(null);
      properties.add("-d");
      properties.add(tempOutDir.getAbsolutePath());
    }
    else {
      properties.add("-d");
      properties.add(outDir);
    }

    // instrument to not compile the source files as source code fixes have to be applied first
    properties.add("-Xnocompile");

    // instrument with WSDL location to be referenced in stub code
    properties.add(wsdlLocation);

    // generate stub
    String[] propertyArray = properties.toArray(new String[properties.size()]);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    boolean status = new WsimportTool(bos).run(propertyArray);
    if (status) {
      // success
      File stubOutDir;
      if (createJarFile) {
        stubOutDir = tempOutDir;
      }
      else {
        stubOutDir = new File(outDir);
      }

      // check that stub files were generated
      if (stubOutDir == null || stubOutDir.list() == null || stubOutDir.list().length == 0) {
        logError("No JAX-WS stub files generated");
        throw new Exception("No JAX-WS stub files generated.");
      }

      // fix resource loading
      if (Integer.valueOf(args[ARG_APPLY_PATCH]) > 0) {
        patchSourceCodeWsdlResources(stubOutDir);
      }

      // compile java files
      if (!noCompile) {
        compile(stubOutDir);
      }

      // create JAR-archive
      if (createJarFile) {
        createJarArchive(stubOutDir, outDir, jarFileName);
      }

      logInfo("JAX-WS stub generation completed. For debugging purpose, please see the following debug output.");
      logInfo(bos);
      printModelInformation(propertyArray);
    }
    else {
      logInfo("JAX-WS stub generation failed.");
      logError(bos);
      throw new Exception("JAX-WS stub generation failed."); // do not remove as Scout SDK expects an exit status different to 0 in case of a failure.
    }
  }

  private static void patchSourceCodeWsdlResources(File folder) {
    Pattern pattern = Pattern.compile("\\.class\\.getResource\\(\\s*\"\\.\"\\s*\\)");
    File[] javaFiles = getAllJavaFiles(folder);
    for (File javaFile : javaFiles) {
      try {
        String content = IOUtility.getContent(new FileReader(javaFile), true);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
          content = matcher.replaceAll(".class.getResource(\"/\")");
          IOUtility.writeContent(new FileWriter(javaFile), content);
          logInfo("Code fix (WSDL resource finding) applied to " + javaFile.getName());
        }
      }
      catch (Throwable t) {
        logError("Failed to apply code fix (WSDL resource finding) in '" + javaFile.getName() + "'", t);
      }
    }
  }

  private static void createJarArchive(File tempOutDir, String outDirPath, String jarFileName) {
    if (jarFileName == null) {
      return;
    }

    try {
      // create archive from created files
      if (!outDirPath.endsWith(FILE_PATH_SEPARATOR)) {
        outDirPath = outDirPath + FILE_PATH_SEPARATOR;
      }
      File jarFile = IOUtility.toFile(outDirPath + jarFileName);
      if (jarFile.exists()) {
        jarFile.delete();
      }
      jarFile.createNewFile();

      File metaInfFolder = new File(tempOutDir, "META-INF");
      metaInfFolder.mkdir();

      File manifestFile = new File(metaInfFolder, "MANIFEST.MF");
      manifestFile.createNewFile();
      FileWriter fileWriter = new FileWriter(manifestFile);
      fileWriter.write("Manifest-Version: 1.0");
      fileWriter.flush();
      fileWriter.close();

      FileUtility.compressArchive(tempOutDir, jarFile);

      logInfo("Stub JAR-file '" + jarFileName + "' created");
    }
    catch (Throwable t) {
      logError("Failed to create stub JAR-file '" + jarFileName + "'", t);
    }
  }

  private static void compile(File folder) {
    try {
      File[] javaFiles = getAllJavaFiles(folder);

      // create dictionary containing all classes to be compiled
      File classesDictionary = IOUtility.createTempFile("classes", null, null);
      FileWriter fileWriter = new FileWriter(classesDictionary);
      for (int i = 0; i < javaFiles.length; i++) {
        if (i > 0) {
          fileWriter.write(LINE_SEPARATOR);
        }
        fileWriter.write(javaFiles[i].getAbsolutePath());
      }
      fileWriter.flush();
      fileWriter.close();

      // create dictionary containing options for compiling process
      File optionsDictionary = IOUtility.createTempFile("options", null, null);
      fileWriter = new FileWriter(optionsDictionary);
      fileWriter.flush();
      fileWriter.close();

      // start compilation
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      int status = compiler.run(System.in, System.out, System.err, "@" + optionsDictionary.getAbsolutePath(), "@" + classesDictionary.getAbsolutePath());
      if (status == 0) {
        logInfo("All the " + javaFiles.length + " stub files compiled");
      }
      else {
        logError("Failed to compile JAX-WS stub files");
      }
    }
    catch (Throwable t) {
      logError("Failed to compile JAX-WS stub files", t);
    }
  }

  private static File[] getAllJavaFiles(File folder) {
    List<File> files = new ArrayList<File>();

    if (!folder.isDirectory()) {
      return new File[0];
    }

    File[] ff = folder.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name) {
        // filter for directories and java files
        return new File(dir, name).isDirectory() || name.endsWith(".java");
      }
    });

    for (File f : ff) {
      if (f.isDirectory()) {
        files.addAll(Arrays.asList(getAllJavaFiles(f)));
      }
      else {
        files.add(f);
      }
    }
    return files.toArray(new File[files.size()]);
  }

  /**
   * Print information about WSDL
   * 
   * @param properties
   * @param logger
   */
  private static void printModelInformation(String[] properties) {
    try {
      WsimportOptions options = new WsimportOptions();
      options.parseArguments(properties);

      ErrorReceiver receiver = new ErrorReceiver() {
        @Override
        public void warning(SAXParseException saxparseexception) throws AbortException {
          logInfo(saxparseexception.toString());
        }

        @Override
        public void info(SAXParseException saxparseexception) {
          logInfo(saxparseexception.toString());
        }

        @Override
        public void fatalError(SAXParseException saxparseexception) throws AbortException {
          logError(saxparseexception.toString());
        }

        @Override
        public void error(SAXParseException saxparseexception) throws AbortException {
          logError(saxparseexception.toString());
        }

        @Override
        public void debug(SAXParseException saxparseexception) {
        }
      };

      MetadataFinder forest = new MetadataFinder(new WSDLInternalizationLogic(), options, receiver);
      WSDLModeler wsdlModeler = createWsdlModeler(options, receiver, forest);
      if (wsdlModeler == null) {
        logInfo("WSDLModeler could not be created to display service properties");
        return;
      }
      Model wsdlModel = wsdlModeler.buildModel();

      if (wsdlModel == null) {
        logInfo("WSDL model could not be parsed to display service properties");
        return;
      }

      if (wsdlModel.getName() != null) {
        logInfo("wsdl model name=" + wsdlModel.getName().getLocalPart());
      }
      logInfo("targetNamespace=" + wsdlModel.getTargetNamespaceURI());

      List<Service> services = wsdlModel.getServices();
      for (Service service : services) {
        logInfo("service=" + service.getName());

        List<Port> ports = service.getPorts();
        for (Port port : ports) {
          logInfo("  port=" + port.getName());
        }
      }
    }
    catch (Throwable e) {
      logError("WSDL model could not be parsed to display service properties: " + e.getMessage());
    }
  }

  private static WSDLModeler createWsdlModeler(WsimportOptions options, ErrorReceiver receiver, MetadataFinder forest) {
    for (Constructor c : WSDLModeler.class.getConstructors()) {
      try {
        Class<?>[] paramTypes = c.getParameterTypes();
        if (paramTypes.length == 2 &&
            paramTypes[0].isAssignableFrom(WsimportOptions.class) &&
            paramTypes[1].isAssignableFrom(ErrorReceiver.class)) {
          // <= JRE6
          return (WSDLModeler) c.newInstance(options, receiver);
        }
        else if (paramTypes.length == 3 &&
            paramTypes[0].isAssignableFrom(WsimportOptions.class) &&
            paramTypes[1].isAssignableFrom(ErrorReceiver.class) &&
            paramTypes[2].isAssignableFrom(MetadataFinder.class)) {
          // >= JRE7
          return (WSDLModeler) c.newInstance(options, receiver, forest);
        }
      }
      catch (Exception e) {
        logError("Error creating WSDLModeler: " + e.getMessage());
      }
    }
    return null;
  }

  private static void logInfo(String message) {
    System.out.println("[INFO] " + message);
  }

  private static void logInfo(ByteArrayOutputStream os) {
    System.out.println("[INFO] " + convertToString(os));
  }

  private static void logError(String message) {
    System.err.println("[ERROR] " + message);
  }

  private static void logError(ByteArrayOutputStream os) {
    System.err.println("[ERROR] " + convertToString(os));
  }

  private static void logError(String message, Throwable t) {
    if (message != null) {
      System.err.println("[ERROR] " + getStacktrace(t));
    }
    else {
      System.err.println("[ERROR] " + message + LINE_SEPARATOR + getStacktrace(t));
    }
  }

  private static String convertToString(ByteArrayOutputStream os) {
    try {
      return os.toString("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      return "Could not access log message due to unsupported encoding.";
    }
  }

  private static String getStacktrace(Throwable t) {
    if (t == null) {
      return null;
    }
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }
}
