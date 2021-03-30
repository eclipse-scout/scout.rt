/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.testing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IPrettyPrintDataObjectMapper;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;

@ApplicationScoped
public class TestingResourceHelper {

  /**
   * @return Directory in src/test/resource that corresponds to the package of <code>resourceBaseClass</code>.
   */
  public File getTestResourceDirectory(Class<?> resourceBaseClass) {
    File moduleDirectory = getModuleDirectory(resourceBaseClass);

    String modulePath = resourceBaseClass.getPackage().getName().replaceAll("\\.", Matcher.quoteReplacement(File.separator));
    return new File(moduleDirectory, "src/test/resources/" + modulePath + "/");
  }

  protected File getModuleDirectory(Class<?> resourceBaseClass) {
    // Replace the content of the out file when failing
    URL location = resourceBaseClass.getProtectionDomain().getCodeSource().getLocation();
    if (!"file".equalsIgnoreCase(location.getProtocol())) {
      fail(String.format("not a file location (%s)", location));
    }

    try {
      File moduleDirectory = new File(location.toURI()).getParentFile().getParentFile();
      assertTrue("Module directory doesn't exist: " + moduleDirectory.getAbsolutePath(), moduleDirectory.exists());
      return moduleDirectory;
    }
    catch (URISyntaxException e) {
      throw new PlatformException("Failed to create URI from location {}", location, e);
    }
  }

  /**
   * Writes the data object to the test resources.
   *
   * @param resourceBaseClass
   *          Resource base class is used to determine to directory, see {@link #getTestResourceDirectory(Class)}.
   * @param filename
   *          Filename
   * @param dataObject
   *          Data object
   */
  public void writeTestResource(Class<?> resourceBaseClass, String filename, IDataObject dataObject) {
    File outputFile = new File(getTestResourceDirectory(resourceBaseClass), filename);
    if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
      fail("Unable to create target directory for file: " + outputFile.getParentFile()); // in case directory doesn't exist yet and couldn't be created (e.g. initial test execution)
      return;
    }

    String dataObjectJson = BEANS.get(IPrettyPrintDataObjectMapper.class).writeValue(dataObject);
    dataObjectJson = dataObjectJson.replaceAll("\\r\\n", "\\\n").trim(); // only use \n

    try (OutputStream out = new FileOutputStream(outputFile)) {
      out.write(dataObjectJson.getBytes(StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new PlatformException("Failed to write new data object file", e);
    }
  }
}
