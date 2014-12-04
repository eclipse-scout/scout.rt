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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.res.Script.NodeType;
import org.eclipse.scout.service.SERVICES;

/**
 * Process JS and CSS script templates such as <code>scout-module.js</code>
 * <p>
 * js and css files are automatically compiled if the name matches the names defined in {@link Script}
 * <p>
 * Version is <code>1.2.3[-qualifier]</code> If the qualifier is the text "qualifier" then cache control is handled
 * automatically in production mode.
 * <p>
 * The js and css compilation can be turned on and off using the url param ?debug=true which only builds the js and css
 * but does not compile/minimize it
 */
public class ScriptBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScriptBuilder.class);

  private static final Pattern INCLUDE_PAT = Pattern.compile("//\\s*@include\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

  /**
   * $1$2-$3.min.$4 with $1=path, $2=basename, $3=version, (-qualifier), $4="js" or "css"
   */
  public static final Pattern NON_FRAGMENT_PATH_PATTERN = Pattern.compile("(.*/)([-_\\w]+)-([0-9.]+)(?:\\-\\w+)?\\.min\\.(js|css)");

  private final IWebArchiveResourceLocator m_resourceLocator;
  private final IScriptProcessorService m_scriptProcessorService;
  private boolean m_debug;

  public ScriptBuilder(IWebArchiveResourceLocator locator) {
    this(locator, SERVICES.getService(IScriptProcessorService.class));
  }

  public ScriptBuilder(IWebArchiveResourceLocator locator, IScriptProcessorService scriptProcessorService) {
    m_resourceLocator = locator;
    m_scriptProcessorService = scriptProcessorService;
    if (m_scriptProcessorService == null) {
      LOG.warn("there is no implementor for " + IScriptProcessorService.class);
    }
  }

  public void setDebug(boolean debug) {
    m_debug = debug;
  }

  public boolean isDebug() {
    return m_debug;
  }

  public byte[] buildScript(String path) throws IOException {
    Script script = locateNonFragmentScript(path);
    switch (script.getNodeType()) {
      case LIBRARY:
        return script.getContentRaw();
      case MACRO:
        return processMacroWithIncludesRec(script).getBytes("UTF-8");
      case SRC_MODULE:
        return processModuleWithIncludes(script).getBytes("UTF-8");
      default:
        throw new IOException("Unexpected " + NodeType.class.getSimpleName() + " " + script.getNodeType());
    }
  }

  protected Script locateNonFragmentScript(String requestPath) throws IOException {
    Matcher mat = NON_FRAGMENT_PATH_PATTERN.matcher(requestPath);
    URL libraryMinimizedUrl = m_resourceLocator.getWebContentResource(requestPath);
    if (!mat.matches()) {
      Script.FileType fileType = (requestPath.endsWith(".js") ? Script.FileType.JS : Script.FileType.CSS);
      URL url = libraryMinimizedUrl;
      if (url == null) {
        throw new IOException("locate " + requestPath + ": does not match NON_FRAGMENT_PATH_PATTERN(" + NON_FRAGMENT_PATH_PATTERN.pattern() + ") and does not exist");
      }
      return new Script(requestPath, url, fileType, Script.NodeType.LIBRARY);
    }

    final Script.NodeType[] nodeTypes;
    final URL[] urls;
    if (isDebug()) {
      nodeTypes = new Script.NodeType[]{
          Script.NodeType.LIBRARY,
          Script.NodeType.MACRO,
          Script.NodeType.SRC_MODULE,
          Script.NodeType.LIBRARY,
      };
      urls = new URL[]{
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-" + mat.group(3) + "." + mat.group(4)), //libraryNonMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-macro." + mat.group(4)), //macroUrl
          m_resourceLocator.getScriptResource("" + mat.group(2) + "-module." + mat.group(4)), //srcModuleUrl
          libraryMinimizedUrl, //libraryMinimizedUrl
      };
    }
    else {
      nodeTypes = new Script.NodeType[]{
          Script.NodeType.LIBRARY,
          Script.NodeType.LIBRARY,
          Script.NodeType.MACRO,
          Script.NodeType.SRC_MODULE,
      };
      urls = new URL[]{
          libraryMinimizedUrl, //libraryMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-" + mat.group(3) + "." + mat.group(4)), //libraryNonMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-macro." + mat.group(4)), //macroUrl
          m_resourceLocator.getScriptResource("" + mat.group(2) + "-module." + mat.group(4)), //srcModuleUrl
      };
    }

    int index = firstWhichIsNotNull(urls);
    if (index < 0) {
      throw new IOException("locate " + requestPath + ": does not exist (no library, macro and source module)");
    }
    Script.FileType fileType = ("js".equals(mat.group(4)) ? Script.FileType.JS : Script.FileType.CSS);
    return new Script(requestPath, urls[index], fileType, nodeTypes[index]);
  }

  protected Script locateFragmentScript(String fragmentPath) throws IOException {
    URL url = m_resourceLocator.getScriptResource(fragmentPath);
    if (url == null) {
      throw new IOException("locate fragment " + fragmentPath + ": does not exist");
    }
    Script.FileType fileType = (fragmentPath.endsWith(".js") ? Script.FileType.JS : Script.FileType.CSS);
    return new Script(fragmentPath, url, fileType, Script.NodeType.SRC_FRAGMENT);
  }

  /**
   * @return the first non-null {@link URL} or null
   */
  protected int firstWhichIsNotNull(URL[] urls) {
    for (int i = 0; i < urls.length; i++) {
      if (urls[i] != null) {
        return i;
      }
    }
    return -1;
  }

  protected String processMacroWithIncludesRec(Script script) throws IOException {
    if (script.getNodeType() != Script.NodeType.MACRO) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + NodeType.MACRO);
    }
    String basePath = script.getRequestPath();
    if (basePath.lastIndexOf('/') < 0) {
      basePath = "";
    }
    else {
      basePath = basePath.substring(0, basePath.lastIndexOf('/') + 1);
    }
    String content = script.getContentUTF8();
    StringBuilder buf = new StringBuilder();
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String includePath = basePath + mat.group(1);
      Script includeScript = locateNonFragmentScript(includePath);
      String replacement;
      switch (includeScript.getNodeType()) {
        case LIBRARY:
          replacement = includeScript.getContentUTF8();
          break;
        case MACRO:
          replacement = processMacroWithIncludesRec(includeScript);
          break;
        case SRC_MODULE:
          replacement = processModuleWithIncludes(includeScript);
          break;
        default:
          throw new IOException("Unexpected " + NodeType.class.getSimpleName() + " " + includeScript.getNodeType());
      }
      buf.append(replacement);
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    return buf.toString();
  }

  protected String processModuleWithIncludes(Script script) throws IOException {
    if (script.getNodeType() != Script.NodeType.SRC_MODULE) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + NodeType.SRC_MODULE);
    }
    String content = script.getContentUTF8();
    StringBuilder buf = new StringBuilder();
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String includePath = mat.group(1);
      Script includeFragment = locateFragmentScript(includePath);
      String replacement;
      switch (includeFragment.getNodeType()) {
        case SRC_FRAGMENT:
          replacement = includeFragment.getContentUTF8();
          if (isDebug() && includeFragment.getFileType() == Script.FileType.JS) {
            replacement = insertLineNumbers(includePath, replacement);
          }
          break;
        default:
          throw new IOException("Unexpected " + NodeType.class.getSimpleName() + " " + includeFragment.getNodeType());
      }
      buf.append(replacement);
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    String result = buf.toString();
    //compile, minimize
    result = compileModule(script.getFileType(), result);
    if (!isDebug()) {
      result = minifyModule(script.getFileType(), result);
    }
    return result;
  }

  protected String compileModule(Script.FileType fileType, String content) throws IOException {
    if (m_scriptProcessorService == null) {
      return content;
    }
    switch (fileType) {
      case JS:
        return m_scriptProcessorService.compileJs(content);
      case CSS:
        return m_scriptProcessorService.compileCss(content);
      default:
        return content;
    }
  }

  protected String minifyModule(Script.FileType fileType, String content) throws IOException {
    if (m_scriptProcessorService == null) {
      return content;
    }
    switch (fileType) {
      case JS:
        return m_scriptProcessorService.minifyJs(content);
      case CSS:
        /*FIXME imo, cgu, now working with separate classloader? ->not yet destroys css!
         * return m_thirdPartyScriptProcessorService.minifyCss(content);
         */
        return content;
      default:
        return content;
    }
  }

  protected String insertLineNumbers(String filename, String text) throws IOException {
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
    if (insideBlockComment) {
      return false;
    }
    int a = line.lastIndexOf("/*");
    int b = line.lastIndexOf("*/");
    int c = line.lastIndexOf("/*/");
    return (a >= 0 && (b < 0 || b < a || (c == a)));
  }

  protected boolean lineIsEndOfMultilineBlockComment(String line, boolean insideBlockComment) {
    if (!insideBlockComment) {
      return false;
    }
    int a = line.indexOf("/*");
    int b = line.indexOf("*/");
    int c = line.lastIndexOf("/*/");
    return (b >= 0 && (a < 0 || a < b || (c == a)));
  }
}
