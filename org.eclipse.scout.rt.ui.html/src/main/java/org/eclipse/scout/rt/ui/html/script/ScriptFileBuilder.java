/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.servlet.UrlHints;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.eclipse.scout.rt.ui.html.res.loader.HtmlFileLoader;
import org.eclipse.scout.rt.ui.html.script.ScriptSource.FileType;
import org.eclipse.scout.rt.ui.html.scriptprocessor.ScriptProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process JS and LESS (CSS) script templates such as <code>scout-module.js</code>
 * <p>
 * js and less files are automatically compiled if the name matches the names defined in {@link ScriptSource}
 * <p>
 * Version is <code>1.2.3</code> or <code>1.2.3-fingerprint</code> where fingerprint is a hex number
 * <p>
 * If the fingerprint is the text "fingerprint" then {@link HtmlFileLoader} replaces it with the effective hex
 * fingerprint.
 * <p>
 * The js and css minify can be turned on and off using the url param ?minify=true, see {@link UrlHints}
 */
public class ScriptFileBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(ScriptFileBuilder.class);

  /**
   * Matches include directives for JS and LESS files. There directives look differently to not confuse the respective
   * editor's syntax high-lighter. Otherwise, the format has no special meaning (i.e. there is no magic "__include"
   * function).
   * <p>
   * <b>JavaScript:</b> <code>__include("file.js");</code><br>
   * <b>LESS:</b> <code>@import "file.css";</code>
   * <p>
   * Inner whitespace and trailing semicolon are optional. Both <code>"</code> and <code>'</code> may be used as string
   * delimiter. Content before and after the matched include directive is preserved, except <i>leading</i> space and tab
   * characters (= support for JS formatter).
   */
  private static final Pattern JS_INCLUDE_PAT = Pattern.compile("^\\s*__include\\(\\s*(?:\"|')(.*)(?:\"|')\\);*", Pattern.MULTILINE);

  private static final Pattern STYLESHEET_IMPORT_PAT = Pattern.compile("^\\s*@import\\s*\"(.*)\";*", Pattern.MULTILINE);

  /**
   * Pattern for a script url that is not a {@link ScriptSource.NodeType#SRC_FRAGMENT}
   * <p>
   * <code>path/basename-1.0.0.min.js</code> <code>path/basename-1.0.0-34fce3bc.min.js</code>
   * <p>
   *
   * <pre>
   * $1$2-$3[-$4].min.$5 with $1=path, $2=basename, $5="js" or "less" or "css"
   * </pre>
   */
  public static final Pattern SCRIPT_URL_PATTERN = Pattern.compile("([^\"']*/)([-_\\.\\w\\d]+?)(?:\\-([a-f0-9]+))?(?:\\.min)?\\.(js|css|less)");

  private final IWebContentService m_resourceLocator;
  private final ScriptFileLocator m_scriptLocator;
  private final boolean m_minify;
  private final String m_theme;

  public ScriptFileBuilder(IWebContentService locator, String theme, boolean minify) {
    m_resourceLocator = locator;
    m_scriptLocator = new ScriptFileLocator(m_resourceLocator);
    m_theme = theme;
    m_minify = minify;
  }

  public ScriptOutput buildScript(String pathInfo) throws IOException {
    ScriptSource script = locateNonFragmentScript(pathInfo);
    LOG.info("Building script {} theme={} m_minify={}", script, m_theme, m_minify);
    if (script == null) {
      return null; // not found
    }
    switch (script.getNodeType()) {
      case LIBRARY:
      case LIBRARY_MINIFIED: {
        return processLibrary(pathInfo, script, true);
      }
      case MACRO: {
        return processMacroWithIncludesRec(pathInfo, script, true);
      }
      case SRC_MODULE: {
        return processModuleWithIncludes(pathInfo, script, true);
      }
      default:
        throw new IOException("Unexpected node type " + script.getNodeType() + " for " + pathInfo);
    }
  }

  protected ScriptSource locateNonFragmentScript(String requestPath) throws IOException {
    Matcher mat = SCRIPT_URL_PATTERN.matcher(requestPath);
    if (mat.matches()) {
      return m_scriptLocator.locateFile(requestPath, mat, m_minify);
    }
    else {
      LOG.warn("locate {}: does not match SCRIPT_URL_PATTERN {}", requestPath, SCRIPT_URL_PATTERN.pattern());
      return null;
    }
  }

  protected ScriptSource locateFragmentScript(String fragmentPath, FileType fileType) {
    if (FileType.STYLESHEET == fileType && m_theme != null) {
      String[] parts = FileUtility.getFilenameParts(fragmentPath);
      String themeFragmentPath = parts[0] + "-" + m_theme + (parts[1] == null ? "" : "." + parts[1]);
      URL url = m_resourceLocator.getScriptSource(themeFragmentPath);
      if (url != null) {
        return new ScriptSource(fragmentPath, url, ScriptSource.NodeType.SRC_FRAGMENT);
      }
    }

    URL url = m_resourceLocator.getScriptSource(fragmentPath);
    if (url == null) {
      LOG.warn("locate fragment {}: does not exist", fragmentPath);
      return null;
    }
    return new ScriptSource(fragmentPath, url, ScriptSource.NodeType.SRC_FRAGMENT);
  }

  protected ScriptOutput processLibrary(String pathInfo, ScriptSource script, boolean compileAndMinify) throws IOException {
    if (!ObjectUtility.isOneOf(script.getNodeType(), ScriptSource.NodeType.LIBRARY, ScriptSource.NodeType.LIBRARY_MINIFIED)) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + ScriptSource.NodeType.LIBRARY + " or " + ScriptSource.NodeType.LIBRARY_MINIFIED + ", but got " + script.getNodeType());
    }
    long lastModified = script.getURL().openConnection().getLastModified();
    String libraryContent = new String(IOUtility.readFromUrl(script.getURL()), StandardCharsets.UTF_8);
    // only minfy if not already minified
    if (compileAndMinify && script.getNodeType() != ScriptSource.NodeType.LIBRARY_MINIFIED) {
      libraryContent = compileAndMinifyContent(script.getFileType(), libraryContent);
    }
    return new ScriptOutput(pathInfo, libraryContent.getBytes(StandardCharsets.UTF_8), lastModified);
  }

  protected ScriptOutput processMacroWithIncludesRec(String pathInfo, ScriptSource script, boolean compileAndMinify) throws IOException {
    if (script.getNodeType() != ScriptSource.NodeType.MACRO) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + ScriptSource.NodeType.MACRO + ", but got " + script.getNodeType());
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
    String content = new String(IOUtility.readFromUrl(script.getURL()), StandardCharsets.UTF_8);
    int pos = 0;
    Matcher mat = matcherForScriptType(script, content);
    while (mat.find()) {
      buf.write(content.substring(pos, mat.start()).getBytes(StandardCharsets.UTF_8));
      String includePath = basePath + mat.group(1);
      ScriptSource includeScript = locateNonFragmentScript(includePath);
      byte[] replacement = null;
      if (includeScript != null) {
        switch (includeScript.getNodeType()) {
          case LIBRARY:
          case LIBRARY_MINIFIED: {
            ScriptOutput sub = processLibrary(includePath, includeScript, false);
            replacement = sub.getContent();
            lastModified = Math.max(lastModified, sub.getLastModified());
            break;
          }
          case MACRO: {
            ScriptOutput sub = processMacroWithIncludesRec(includePath, includeScript, false);
            replacement = sub.getContent();
            lastModified = Math.max(lastModified, sub.getLastModified());
            break;
          }
          case SRC_MODULE: {
            ScriptOutput sub = processModuleWithIncludes(includePath, includeScript, false);
            replacement = sub.getContent();
            lastModified = Math.max(lastModified, sub.getLastModified());
            break;
          }
          default: {
            LOG.warn("Unexpected {} {} for {}", ScriptSource.NodeType.class.getSimpleName(), includeScript.getNodeType(), includePath);
            break;
          }
        }
      }
      // Add debug information to returned content
      if (!m_minify) {
        if (script.getFileType() == ScriptSource.FileType.JS) {
          buf.write(("// --- " + (includeScript == null ? "" : includeScript.getNodeType() + " ") + includePath + " ---\n").getBytes(StandardCharsets.UTF_8));
          if (replacement == null) {
            buf.write("// !!! NOT PROCESSED\n".getBytes(StandardCharsets.UTF_8));
          }
        }
        else if (script.getFileType() == ScriptSource.FileType.STYLESHEET) {
          buf.write(("/* --- " + (includeScript == null ? "" : includeScript.getNodeType() + " ") + includePath + " --- */\n").getBytes(StandardCharsets.UTF_8));
          if (replacement == null) {
            buf.write("/* !!! NOT PROCESSED */\n".getBytes(StandardCharsets.UTF_8));
          }
        }
      }
      if (replacement != null) {
        buf.write(replacement);
      }
      pos = mat.end();
    }
    buf.write(content.substring(pos).getBytes(StandardCharsets.UTF_8));

    String macroContent = buf.toString(StandardCharsets.UTF_8.name());
    if (compileAndMinify) {
      macroContent = compileAndMinifyContent(script.getFileType(), macroContent);
    }
    return new ScriptOutput(pathInfo, macroContent.getBytes(StandardCharsets.UTF_8), lastModified);
  }

  protected Matcher matcherForScriptType(ScriptSource script, String content) {
    Pattern pattern;
    if (script.getFileType() == FileType.STYLESHEET) {
      pattern = STYLESHEET_IMPORT_PAT;
    }
    else {
      pattern = JS_INCLUDE_PAT;
    }
    return pattern.matcher(content);
  }

  protected ScriptOutput processModuleWithIncludes(String pathInfo, ScriptSource script, boolean compileAndMinify) throws IOException {
    if (script.getNodeType() != ScriptSource.NodeType.SRC_MODULE) {
      throw new IOException(script.getRequestPath() + " / " + script.getURL() + ": expected " + ScriptSource.NodeType.SRC_MODULE + ", but got " + script.getNodeType());
    }
    StringBuilder buf = new StringBuilder();
    long lastModified = script.getURL().openConnection().getLastModified();
    String content = new String(IOUtility.readFromUrl(script.getURL()), StandardCharsets.UTF_8);
    Matcher mat = matcherForScriptType(script, content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      String includePath = mat.group(1);
      ScriptSource includeFragment = locateFragmentScript(includePath, script.getFileType());
      String replacement = null;
      if (includeFragment != null) {
        switch (includeFragment.getNodeType()) {
          case SRC_FRAGMENT: {
            replacement = new String(IOUtility.readFromUrl(includeFragment.getURL()), StandardCharsets.UTF_8);
            lastModified = Math.max(lastModified, includeFragment.getURL().openConnection().getLastModified());
            break;
          }
          default: {
            LOG.warn("Unexpected {} {} for {}", ScriptSource.NodeType.class.getSimpleName(), includeFragment.getNodeType(), includePath);
            break;
          }
        }
      }
      // Add debug information to returned content
      if (!m_minify) {
        if (script.getFileType() == ScriptSource.FileType.JS) {
          buf.append("// --- " + (includeFragment == null ? "" : includeFragment.getNodeType() + " ") + includePath + " ---\n");
          if (replacement == null) {
            buf.append("// !!! NOT PROCESSED\n");
          }
          else {
            replacement = insertLineNumbers(includePath, replacement);
          }
        }
        else if (script.getFileType() == ScriptSource.FileType.STYLESHEET) {
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

    String moduleContent = buf.toString();
    if (compileAndMinify) {
      moduleContent = compileAndMinifyContent(script.getFileType(), moduleContent);
    }
    return new ScriptOutput(pathInfo, moduleContent.getBytes(StandardCharsets.UTF_8), lastModified);
  }

  protected String compileAndMinifyContent(FileType fileType, String content) throws IOException {
    content = compileContent(fileType, content);
    if (m_minify) {
      content = minifyContent(fileType, content);
    }
    return content;
  }

  protected String compileContent(ScriptSource.FileType fileType, String content) throws IOException {
    switch (fileType) {
      case JS:
        return BEANS.get(ScriptProcessor.class).compileJs(content);
      case STYLESHEET:
        return BEANS.get(ScriptProcessor.class).compileCss(content);
      default:
        return content;
    }
  }

  protected String minifyContent(ScriptSource.FileType fileType, String content) throws IOException {
    switch (fileType) {
      case JS:
        return BEANS.get(ScriptProcessor.class).minifyJs(content);
      case STYLESHEET:
        return BEANS.get(ScriptProcessor.class).minifyCss(content);
      default:
        return content;
    }
  }

  protected String insertLineNumbers(final String path, final String text) throws IOException {
    if (text == null) {
      return null;
    }
    // First path segment is the "qualifier"
    int i = path.indexOf('/');
    String qualifier = (i >= 0 ? path.substring(0, i) : "");
    // Last path segment is the filename (inner path segments are ignored)
    i = path.lastIndexOf('/');
    String filename = path.substring(i + 1);
    // Strip file extension
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
      if (qualifier.length() > 0) {
        buf.append(qualifier).append("|");
      }
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
