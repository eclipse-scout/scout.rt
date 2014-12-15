/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;

/**
 * Generate js and css files used for testing. This java class is run using the maven
 * org.codehaus.mojo/exec-maven-plugin in the phase generate-test-resources
 * see {@link #GenerateTestScripts(File, File, String...)}
 *
 * @since 5.0.0
 */
public class GenerateTestScripts implements IWebContentResourceLocator {

  public static void main(String[] args) throws Exception {
    if (args == null || args.length < 3) {
      throw new IllegalArgumentException("expected args=[path-to-input-src/main/js, path-to-output-src/test/js, moduleName1, moduleName2, ...]");
    }
    try {
      new GenerateTestScripts(new File(args[0]), new File(args[1]), Arrays.copyOfRange(args, 2, args.length)).run();
    }
    catch (Throwable ex) {
      //try to write a log file
      PrintStream out = new PrintStream(new File(args[1], GenerateTestScripts.class.getSimpleName() + "-error.log"));
      ex.printStackTrace(out);
      out.close();
      //re-throw
      throw ex;
    }
  }

  private final File m_inDir;
  private final File m_outDir;
  private final String[] m_moduleNames;

  public GenerateTestScripts(File inDir, File outDir, String... moduleNames) {
    if (!inDir.exists()) {
      throw new IllegalArgumentException("path-to-input does not exist: " + inDir + " (" + inDir.getAbsolutePath() + ")");
    }
    m_inDir = inDir;
    m_outDir = outDir;
    m_outDir.mkdirs();
    m_moduleNames = moduleNames;
  }

  public void run() throws IOException {
    ScriptFileBuilder builder = new ScriptFileBuilder(this);
    builder.setMinifyEnabled(false);
    for (String moduleName : m_moduleNames) {
      if (moduleName.indexOf('/') < 0) {
        moduleName = "/" + moduleName;
      }
      HttpCacheObject obj = builder.buildScript(moduleName);
      try (FileOutputStream fout = new FileOutputStream(new File(m_outDir, moduleName))) {
        fout.write(obj.getContent());
      }
    }
  }

  @Override
  public URL getWebContentResource(String path) {
    return null;
  }

  @Override
  public URL getScriptSource(String path) {
    File f = new File(m_inDir, path);
    try {
      return f.exists() ? f.toURI().toURL() : null;
    }
    catch (MalformedURLException e) {
      throw new IllegalArgumentException("cannot convert file to url: " + f, e);
    }
  }
}
