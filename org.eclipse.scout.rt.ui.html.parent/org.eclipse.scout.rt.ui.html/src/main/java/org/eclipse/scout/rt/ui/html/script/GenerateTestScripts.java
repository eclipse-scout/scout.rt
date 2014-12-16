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
package org.eclipse.scout.rt.ui.html.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.eclipse.scout.rt.ui.html.res.IWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * Generate js and css files used for testing. This java class is run using the maven
 * org.codehaus.mojo/exec-maven-plugin in the phase generate-test-resources
 * see {@link #GenerateTestScripts(File, File)}
 *
 * @since 5.0.0
 */
public class GenerateTestScripts {

  public static void main(String[] args) throws Exception {
    if (args == null || args.length < 3) {
      throw new IllegalArgumentException("expected args=[path-to-input-src/main/js, path-to-output-src/test/js, moduleName1, moduleName2, ...]");
    }
    File errorFile = new File(args[1], GenerateTestScripts.class.getSimpleName() + "-error.log");
    errorFile.delete();
    try {
      GenerateTestScripts gen = new GenerateTestScripts(new File(args[0]), new File(args[1]));
      for (String scriptName : Arrays.copyOfRange(args, 2, args.length)) {
        errorFile = new File(args[1], scriptName + "-error.log");
        errorFile.delete();
        gen.generate(scriptName);
      }
    }
    catch (Throwable t) {
      writeErrorFile(errorFile, t);
      //re-throw
      throw t;
    }
  }

  private static void writeErrorFile(File f, Throwable t) {
    try (PrintStream out = new PrintStream(f)) {
      out.println("java.class.path: " + System.getProperty("java.class.path"));
      t.printStackTrace(out);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private final File m_inDir;
  private final File m_outDir;
  private final IWebContentResourceLocator m_loc;

  public GenerateTestScripts(File inDir, File outDir) {
    if (!inDir.exists()) {
      throw new IllegalArgumentException("path-to-input does not exist: " + inDir + " (" + inDir.getAbsolutePath() + ")");
    }
    m_inDir = inDir;
    m_outDir = outDir;
    m_outDir.mkdirs();
    m_loc = new IWebContentResourceLocator() {
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
    };
  }

  public void generate(String scriptName) throws IOException {
    try (ScriptProcessor scriptProcessor = new ScriptProcessor()) {
      ScriptFileBuilder builder = new ScriptFileBuilder(m_loc, scriptProcessor);
      builder.setMinifyEnabled(false);
      scriptName = "/" + scriptName;
      ScriptOutput obj = builder.buildScript(scriptName);
      try (FileOutputStream fout = new FileOutputStream(new File(m_outDir, scriptName))) {
        fout.write(obj.getContent());
      }
    }
  }

}
