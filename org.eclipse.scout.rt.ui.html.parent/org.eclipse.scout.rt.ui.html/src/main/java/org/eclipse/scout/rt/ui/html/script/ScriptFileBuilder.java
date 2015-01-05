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
package org.eclipse.scout.rt.ui.html.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.StreamUtility;
import org.eclipse.scout.rt.ui.html.UiHints;
import org.eclipse.scout.rt.ui.html.res.IWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.res.StaticResourceRequestInterceptor;
import org.eclipse.scout.rt.ui.html.script.ScriptSource.NodeType;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;

/**
 * Process JS and CSS script templates such as <code>scout-module.js</code>
 * <p>
 * js and css files are automatically compiled if the name matches the names defined in {@link ScriptSource}
 * <p>
 * Version is <code>1.2.3</code> or <code>1.2.3-fingerprint</code> where fingerprint is a hex number
 * <p>
 * If the fingerprint is the text "fingerprint" then
 * {@link StaticResourceRequestInterceptor#resolveIndexHtml(javax.servlet.http.HttpServletRequest)} replaces it with the
 * effective hex fingerprint.
 * <p>
 * The js and css minify can be turned on and off using the url param ?minify=true, see {@link UiHints}
 */
public class ScriptFileBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScriptFileBuilder.class);

  private static final Pattern INCLUDE_PAT = Pattern.compile("(?://\\s*@|__)include\\s*\\(\\s*(?:\"([^\"]+)\"|'([^']+)')\\s*\\)[;]*");

  private static final String UTF_8 = "UTF-8";

  /**
   * Pattern for a script url that is not a {@link NodeType#SRC_FRAGMENT}
   * <p>
   * <code>path/basename-1.0.0.min.js</code> <code>path/basename-1.0.0-34fce3bc.min.js</code>
   * <p>
   *
   * <pre>
   * $1$2-$3[-$4].min.$5 with $1=path, $2=basename, $3=version, $4=fingerprint, $5="js" or "css"
   * </pre>
   */
  public static final Pattern SCRIPT_URL_PATTERN = Pattern.compile("([^\"']*/)([-_\\w]+)-([0-9.]+)(?:\\-([a-f0-9]+|fingerprint))?(?:\\.min)?\\.(js|css)");

  private final IWebContentResourceLocator m_resourceLocator;
  private final ScriptProcessor m_scriptProcessor;
  private boolean m_minifyEnabled;

  public ScriptFileBuilder(IWebContentResourceLocator locator, ScriptProcessor scriptProcessor) {
    m_resourceLocator = locator;
    m_scriptProcessor = scriptProcessor;
  }

  public boolean isMinifyEnabled() {
    return m_minifyEnabled;
  }

  public void setMinifyEnabled(boolean minifyEnabled) {
    m_minifyEnabled = minifyEnabled;
  }

  public ScriptOutput buildScript(String pathInfo) throws IOException {
    ScriptSource script = locateNonFragmentScript(pathInfo);
    if (script == null) {
      return null; // not found
    }
    switch (script.getNodeType()) {
      case LIBRARY: {
        return new ScriptOutput(pathInfo, StreamUtility.readResource(script.getURL()), script.getURL().openConnection().getLastModified());
      }
      case MACRO: {
        return processMacroWithIncludesRec(pathInfo, script);
      }
      case SRC_MODULE: {
        return processModuleWithIncludes(pathInfo, script);
      }
      default:
        throw new IOException("Unexpected " + NodeType.class.getSimpleName() + " " + script.getNodeType() + " for " + pathInfo);
    }
  }

  protected ScriptSource locateNonFragmentScript(String requestPath) throws IOException {
    Matcher mat = SCRIPT_URL_PATTERN.matcher(requestPath);
    URL libraryMinimizedUrl = m_resourceLocator.getWebContentResource(requestPath);
    if (!mat.matches()) {
      if (libraryMinimizedUrl == null) {
        LOG.warn("locate " + requestPath + ": does not match SCRIPT_URL_PATTERN " + SCRIPT_URL_PATTERN.pattern() + " and does not exist");
        return null;
      }
      return new ScriptSource(requestPath, libraryMinimizedUrl, ScriptSource.NodeType.LIBRARY);
    }

    final ScriptSource.NodeType[] nodeTypes;
    final URL[] urls;
    if (isMinifyEnabled()) {
      nodeTypes = new ScriptSource.NodeType[]{
          ScriptSource.NodeType.LIBRARY,
          ScriptSource.NodeType.LIBRARY,
          ScriptSource.NodeType.MACRO,
          ScriptSource.NodeType.SRC_MODULE,
      };
      urls = new URL[]{
          libraryMinimizedUrl, //libraryMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-" + mat.group(3) + "." + mat.group(5)), //libraryNonMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-macro." + mat.group(5)), //macroUrl
          m_resourceLocator.getScriptSource("" + mat.group(2) + "-module." + mat.group(5)), //srcModuleUrl
      };
    }
    else {
      nodeTypes = new ScriptSource.NodeType[]{
          ScriptSource.NodeType.LIBRARY,
          ScriptSource.NodeType.MACRO,
          ScriptSource.NodeType.SRC_MODULE,
          ScriptSource.NodeType.LIBRARY,
      };
      urls = new URL[]{
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-" + mat.group(3) + "." + mat.group(5)), //libraryNonMinimizedUrl
          m_resourceLocator.getWebContentResource(mat.group(1) + mat.group(2) + "-macro." + mat.group(5)), //macroUrl
          m_resourceLocator.getScriptSource("" + mat.group(2) + "-module." + mat.group(5)), //srcModuleUrl
          libraryMinimizedUrl, //libraryMinimizedUrl
      };
    }

    int index = firstWhichIsNotNull(urls);
    if (index < 0) {
      LOG.warn("locate " + requestPath + ": does not exist (no library, macro or source module)");
      return null;
    }
    return new ScriptSource(requestPath, urls[index], nodeTypes[index]);
  }

  protected ScriptSource locateFragmentScript(String fragmentPath) {
    URL url = m_resourceLocator.getScriptSource(fragmentPath);
    if (url == null) {
      LOG.warn("locate fragment " + fragmentPath + ": does not exist");
      return null;
    }
    return new ScriptSource(fragmentPath, url, ScriptSource.NodeType.SRC_FRAGMENT);
  }

  /**
   * @return the index of the first non-null {@link URL} in the given array, or <code>-1</code> if no non-null elements
   *         are present.
   */
  protected int firstWhichIsNotNull(URL[] urls) {
    if (urls != null) {
      for (int i = 0; i < urls.length; i++) {
        if (urls[i] != null) {
          return i;
        }
      }
    }
    return -1;
  }

  protected ScriptOutput processMacroWithIncludesRec(String pathInfo, ScriptSource script) throws IOException {
    if (script.getNodeType() != ScriptSource.NodeType.MACRO) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + NodeType.MACRO + ", but got " + script.getNodeType());
    }
    String basePath = script.getRequestPath();
    if (basePath.lastIndexOf('/') < 0) {
      basePath = "";
    }
    else {
      basePath = basePath.substring(0, basePath.lastIndexOf('/') + 1);
    }
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    long lastModified = script.getURL().openConnection().getLastModified();
    String content = new String(StreamUtility.readResource(script.getURL()), UTF_8);
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.write(content.substring(pos, mat.start()).getBytes(UTF_8));
      String includePath = basePath + StringUtility.nvl(mat.group(1), mat.group(2));
      ScriptSource includeScript = locateNonFragmentScript(includePath);
      byte[] replacement = null;
      if (includeScript != null) {
        switch (includeScript.getNodeType()) {
          case LIBRARY: {
            replacement = StreamUtility.readResource(includeScript.getURL());
            lastModified = Math.max(lastModified, includeScript.getURL().openConnection().getLastModified());
            break;
          }
          case MACRO: {
            ScriptOutput sub = processMacroWithIncludesRec(includePath, includeScript);
            replacement = sub.getContent();
            lastModified = Math.max(lastModified, sub.getLastModified());
            break;
          }
          case SRC_MODULE: {
            ScriptOutput sub = processModuleWithIncludes(includePath, includeScript);
            replacement = sub.getContent();
            lastModified = Math.max(lastModified, sub.getLastModified());
            break;
          }
          default: {
            LOG.warn("Unexpected " + NodeType.class.getSimpleName() + " " + includeScript.getNodeType() + " for " + includePath);
            break;
          }
        }
      }
      // Add debug information to returned content
      if (!isMinifyEnabled()) {
        if (script.getFileType() == ScriptSource.FileType.JS) {
          buf.write(("// --- " + (includeScript == null ? "" : includeScript.getNodeType() + " ") + includePath + " ---\n").getBytes(UTF_8));
          if (replacement == null) {
            buf.write("// !!! NOT PROCESSED\n".getBytes(UTF_8));
          }
        }
        else if (script.getFileType() == ScriptSource.FileType.CSS) {
          buf.write(("/* --- " + (includeScript == null ? "" : includeScript.getNodeType() + " ") + includePath + " --- */\n").getBytes(UTF_8));
          if (replacement == null) {
            buf.write("/* !!! NOT PROCESSED */\n".getBytes(UTF_8));
          }
        }
      }
      if (replacement != null) {
        buf.write(replacement);
      }
      pos = mat.end();
    }
    buf.write(content.substring(pos).getBytes(UTF_8));
    return new ScriptOutput(pathInfo, buf.toByteArray(), lastModified);
  }

  protected ScriptOutput processModuleWithIncludes(String pathInfo, ScriptSource script) throws IOException {
    if (script.getNodeType() != ScriptSource.NodeType.SRC_MODULE) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + NodeType.SRC_MODULE + ", but got " + script.getNodeType());
    }
    StringBuilder buf = new StringBuilder();
    long lastModified = script.getURL().openConnection().getLastModified();
    String content = new String(StreamUtility.readResource(script.getURL()), UTF_8);
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String includePath = StringUtility.nvl(mat.group(1), mat.group(2));
      ScriptSource includeFragment = locateFragmentScript(includePath);
      String replacement = null;
      if (includeFragment != null) {
        switch (includeFragment.getNodeType()) {
          case SRC_FRAGMENT: {
            replacement = new String(StreamUtility.readResource(includeFragment.getURL()), UTF_8);
            lastModified = Math.max(lastModified, includeFragment.getURL().openConnection().getLastModified());
            break;
          }
          default: {
            LOG.warn("Unexpected " + NodeType.class.getSimpleName() + " " + includeFragment.getNodeType() + " for " + includePath);
            break;
          }
        }
      }
      // Add debug information to returned content
      if (!isMinifyEnabled()) {
        if (script.getFileType() == ScriptSource.FileType.JS) {
          if (replacement == null) {
            buf.append("// --- " + (includeFragment == null ? "" : includeFragment.getNodeType() + " ") + includePath + " ---\n");
            buf.append("// !!! NOT PROCESSED\n");
          }
          else {
            replacement = insertLineNumbers(includePath, replacement);
          }
        }
        else if (script.getFileType() == ScriptSource.FileType.CSS) {
          buf.append("/* --- " + (includeFragment == null ? "" : includeFragment.getNodeType() + " ") + includePath + " --- */\n");
          if (replacement == null) {
            buf.append("/* !!! NOT PROCESSED */\n");
          }
        }
      }
      if (replacement != null) {
        buf.append(replacement);
      }
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    String result = buf.toString();
    //compile, minimize
    result = compileModule(script.getFileType(), result);
    if (isMinifyEnabled()) {
      result = minifyModule(script.getFileType(), result);
    }
    return new ScriptOutput(pathInfo, result.getBytes(UTF_8), lastModified);
  }

  protected String compileModule(ScriptSource.FileType fileType, String content) throws IOException {
    switch (fileType) {
      case JS:
        return m_scriptProcessor.compileJs(content);
      case CSS:
        return m_scriptProcessor.compileCss(content);
      default:
        return content;
    }
  }

  protected String minifyModule(ScriptSource.FileType fileType, String content) throws IOException {
    switch (fileType) {
      case JS:
        return m_scriptProcessor.minifyJs(content);
      case CSS:
        /*FIXME imo, cgu: destroys css!
         * return m_thirdPartyScriptProcessorService.minifyCss(content);
         */
        return content;
      default:
        return content;
    }
  }

  protected String insertLineNumbers(String filename, String text) throws IOException {
    if (text == null) {
      return null;
    }
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
      buf.append((insideBlockComment ? "//" : "/*"));
      buf.append(filename).append(":");
      buf.append(String.format("%-" + ((lines.length + "").length()) + "d", lineNo));
      buf.append((insideBlockComment ? "//" : "*/")).append(" ");
      buf.append(line).append("\n");
      if (lineIsBeginOfMultilineBlockComment(line, insideBlockComment)) {
        // also if line is endMLBC AND beginMLBC
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
