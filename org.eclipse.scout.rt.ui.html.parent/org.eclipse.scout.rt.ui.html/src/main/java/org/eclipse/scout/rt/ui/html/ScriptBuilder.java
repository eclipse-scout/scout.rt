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
package org.eclipse.scout.rt.ui.html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.SERVICES;

/**
 * Process JS and CSS scripts such as <code>/src/main/js/scout-template.css</code> and
 * <code>/src/main/js/scout-template.js</code>
 */
public class ScriptBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScriptBuilder.class);
  private static final Pattern INCLUDE_PAT = Pattern.compile("//\\s*@include\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

  private final IThirdPartyScriptProcessorService m_thirdPartyScriptProcessorService;

  public ScriptBuilder() {
    this(SERVICES.getService(IThirdPartyScriptProcessorService.class));
  }

  public ScriptBuilder(IThirdPartyScriptProcessorService thirdPartyScriptProcessorService) {
    if (thirdPartyScriptProcessorService == null) {
      LOG.warn("there is no implementor for " + IThirdPartyScriptProcessorService.class);
    }
    m_thirdPartyScriptProcessorService = thirdPartyScriptProcessorService;
  }

  public String buildJsScript(Script script, boolean minify) throws IOException {
    String outputFile = processIncludes(script, !minify);
    if (minify) {
      outputFile = compileJs(outputFile);
      outputFile = minifyJs(outputFile);
    }
    return outputFile;
  }

  protected String compileJs(String content) throws IOException {
    if (m_thirdPartyScriptProcessorService == null) {
      return content;
    }
    return m_thirdPartyScriptProcessorService.compileJs(content);
  }

  protected String minifyJs(String content) throws IOException {
    if (m_thirdPartyScriptProcessorService == null) {
      return content;
    }
    return m_thirdPartyScriptProcessorService.minifyJs(content);
  }

  public String buildCssScript(Script script, boolean minify) throws IOException {
    String outputFile = processIncludes(script, false);
    if (minify) {
      outputFile = compileCss(outputFile);
      outputFile = minifyCss(outputFile);
    }
    return outputFile;
  }

  protected String compileCss(String content) throws IOException {
    if (m_thirdPartyScriptProcessorService == null) {
      return content;
    }
    return m_thirdPartyScriptProcessorService.compileCss(content);
  }

  protected String minifyCss(String content) throws IOException {
    if (m_thirdPartyScriptProcessorService == null) {
      return content;
    }
    return content;
    //FIXME imo, cgu, now working with separate classloader? ->not yet destroys css!
    //return m_thirdPartyScriptProcessorService.minifyCss(content);
  }

  protected String processIncludes(Script script, boolean addLineNumbers) throws IOException {
    String basePath = script.getPath();
    if (basePath.lastIndexOf('/') < 0) {
      basePath = "";
    }
    else {
      basePath = basePath.substring(0, basePath.lastIndexOf('/') + 1);
    }
    String content = readUTF8(script.getURL());
    StringBuilder buf = new StringBuilder();
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String includePath = basePath + mat.group(1);
      Script includeScript = script.getScriptLocator().getScriptSource(includePath);
      if (includeScript == null) {
        throw new FileNotFoundException("/src/main/js/" + includePath);
      }
      String replacement = readUTF8(includeScript.getURL());
      if (addLineNumbers) {
        replacement = addLineNumbers(includePath, replacement);
      }
      buf.append(replacement);
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    return buf.toString();
  }

  protected String readUTF8(URL url) throws IOException {
    InputStream in = url.openStream();
    try {
      try {
        return IOUtility.getContentUtf8(in);
      }
      catch (ProcessingException e) {
        throw new IOException(e.getMessage());
      }
    }
    finally {
      in.close();
    }
  }

  protected String addLineNumbers(String filename, String text) throws IOException {
    int i = filename.lastIndexOf('/');
    if (i >= 0) {
      filename = filename.substring(i + 1);
    }
    i = filename.lastIndexOf('.');
    if (i >= 0) {
      filename = filename.substring(0, i);
    }
    int lineNo = 1;
    boolean insideBlockComment = false;
    StringBuilder buf = new StringBuilder();
    String[] lines = text.split("[\\n]");
    for (String line : lines) {
      buf.append((insideBlockComment ? "//" : "/*")).
      append(filename).append(":").
      append(String.format("%-" + ((lines.length + "").length()) + "d", lineNo)).
      append((insideBlockComment ? "//" : "*/")).append(" ").
      append(line).
      append("\n");
      if (lineIsBeginOfMultilineBlockComment(line, insideBlockComment)) {
        //also if line is endMLBC AND beginMLBC
        insideBlockComment = true;
      }
      else if (lineIsEndOfMultilineBlockComment(line, insideBlockComment)) {
        insideBlockComment = false;
      }
      lineNo++;
    }
    return buf.toString();
  }

  protected boolean lineIsBeginOfMultilineBlockComment(String line, boolean insideBlockComment) {
    int a = line.lastIndexOf("/*");
    int b = line.lastIndexOf("*/");
    int c = line.lastIndexOf("/*/");
    return a >= 0 && (b < 0 || b < a || (c == a)) && !insideBlockComment;
  }

  protected boolean lineIsEndOfMultilineBlockComment(String line, boolean insideBlockComment) {
    int a = line.indexOf("/*");
    int b = line.indexOf("*/");
    int c = line.lastIndexOf("/*/");
    return b >= 0 && (a < 0 || a < b || (c == a)) && insideBlockComment;
  }
}
