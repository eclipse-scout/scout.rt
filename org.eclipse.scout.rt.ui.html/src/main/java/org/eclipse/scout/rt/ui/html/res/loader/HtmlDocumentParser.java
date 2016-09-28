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
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.GlobalHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple tag-parser used to replace scout-tags in HTML documents.
 */
public class HtmlDocumentParser {
  private static final Logger LOG = LoggerFactory.getLogger(HtmlDocumentParser.class);

  private static final Pattern PATTERN_KEY_VALUE = Pattern.compile("([^\"\\s]+)=\"([^\"]*)\"");
  private static final Pattern PATTERN_INCLUDE_TAG = Pattern.compile("<scout\\:include template=\"(.*)\" />", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_MESSAGE_TAG = Pattern.compile("<scout\\:message(.*?)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_STYLESHEET_TAG = Pattern.compile("<scout\\:stylesheet src=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile("<scout\\:script src=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final String PATTERN_BASE_TAG = "<scout\\:base\\s*/>";

  private final HtmlDocumentParserParameters m_params;
  private final IHttpResourceCache m_cache;
  private String m_workingContent;

  public HtmlDocumentParser(HtmlDocumentParserParameters params) {
    m_params = params;
    m_cache = BEANS.get(GlobalHttpResourceCache.class);
  }

  public byte[] parseDocument(byte[] document) throws IOException {
    // the order of calls is important: first we must resolve all includes
    m_workingContent = new String(document, StandardCharsets.UTF_8);
    replaceIncludeTags();
    replaceBaseTags();
    replaceMessageTags();
    replaceStylesheetTags();
    replaceScriptTags();
    return m_workingContent.getBytes(StandardCharsets.UTF_8);
  }

  @SuppressWarnings("squid:S1149")
  protected void replaceScriptTags(Pattern pattern, String tagPrefix, String tagSuffix) throws IOException {
    Matcher m = pattern.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String srcPath = m.group(1);
      StringBuilder scriptTag = new StringBuilder(tagPrefix);
      scriptTag.append(createExternalPath(srcPath));
      scriptTag.append(tagSuffix);
      m.appendReplacement(sb, scriptTag.toString());
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  /**
   * Creates the external path of the given resource, including fingerprint and '.min' extensions. This method also
   * deals with caching, since we must build a script file first, before we can calculate its fingerprint.
   */
  protected String createExternalPath(String internalPath) throws IOException {
    File srcFile = new File(internalPath);
    String[] filenameParts = FileUtility.getFilenameParts(srcFile);
    Pair<HttpCacheObject, String> scriptAndFingerprint = null;

    // When caching is enabled we must calculate the fingerprint for the script file
    if (m_params.isCacheEnabled()) {
      scriptAndFingerprint = getScriptAndFingerprint(internalPath);
    }

    // append path to file
    StringBuilder externalPathSb = new StringBuilder();
    if (srcFile.getParent() != null) {
      externalPathSb.append(srcFile.getParent()).append("/");
    }

    // append file name without file-extension
    externalPathSb.append(getScriptFileName(filenameParts[0]));

    // append fingerprint
    if (scriptAndFingerprint != null) {
      String fingerprint = scriptAndFingerprint.getRight();
      externalPathSb.append("-").append(fingerprint);
    }

    // append 'min'
    if (m_params.isMinify()) {
      externalPathSb.append(".min");
    }

    // append file-extension
    externalPathSb.append(".").append(filenameParts[1]);
    String externalPath = externalPathSb.toString();

    // we must put the same resource into the cache with two different cache keys:
    // 1. the 'internal' cache key is the path as it is used when the HTML file is parsed by the HtmlDocumenParser. Example: '/res/scout-all-macro.js'
    // 2. the 'external' cache key is the path as it is used in the browser. Example: '/res/scout-all-0ac567fe1.min.js'
    // We have kind of a chicken/egg problem here: in order to get the fingerprint we must first read and parse the file. So when the parser builds
    // a HTML file it cannot now the fingerprint in advance. That's why we put the 'internal' path into the cache. Later, when the browser requests
    // the script with the 'external' URL, we want to access the already built and cached file. That's why we must also store the external form of
    // the path. Additionally the same script may be used in another HTML file, which is also processed by the HtmlDocumentParser, then again we need
    // want to access the cached file by its internal path.
    if (scriptAndFingerprint != null) {
      HttpCacheObject internalCacheObject = scriptAndFingerprint.getLeft();
      HttpCacheKey externalCacheKey = createScriptFileLoader().createCacheKey("/" + externalPath);
      HttpCacheObject externalCacheObject = new HttpCacheObject(externalCacheKey, internalCacheObject.getResource());
      m_cache.put(internalCacheObject);
      m_cache.put(externalCacheObject);
    }

    return externalPath;
  }

  protected ScriptFileLoader createScriptFileLoader() {
    return new ScriptFileLoader(m_params.getTheme(), m_params.isMinify());
  }

  protected Pair<HttpCacheObject, String> getScriptAndFingerprint(String internalPath) throws IOException {
    ScriptFileLoader scriptLoader = createScriptFileLoader();
    HttpCacheKey cacheKey = scriptLoader.createCacheKey(internalPath);
    HttpCacheObject script = m_cache.get(cacheKey);
    if (script == null) {
      // cache miss: try to load script
      script = scriptLoader.loadResource(cacheKey);
    }
    if (script == null) {
      // script not found -> no fingerprint
      LOG.warn("Failed to locate script referenced in html file '{}': {}", m_params.getHtmlPath(), internalPath);
      return null;
    }
    String fingerprint = Long.toHexString(script.getResource().getFingerprint());
    return new Pair<>(script, fingerprint);
  }

  /**
   * When file is a macro or module, remove that suffix. ScriptFileLocator tries to find the macro and module files by
   * adding the suffix again and looking it up in the classpath.
   *
   * @param fileName
   * @return
   */
  protected String getScriptFileName(String fileName) {
    if (fileName.endsWith("-macro")) {
      return fileName.substring(0, fileName.length() - 6);
    }
    if (fileName.endsWith("-module")) {
      return fileName.substring(0, fileName.length() - 7);
    }
    return fileName;
  }

  protected void replaceStylesheetTags() throws IOException {
    // <scout:stylesheet src="scout-all-macro.css" />
    replaceScriptTags(PATTERN_STYLESHEET_TAG, "<link rel=\"stylesheet\" type=\"text/css\" href=\"", "\">");
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  protected void replaceScriptTags() throws IOException {
    // <scout:script src="scout-all-macro.css" />
    replaceScriptTags(PATTERN_SCRIPT_TAG, "<script src=\"", "\"></script>");
  }

  protected void replaceBaseTags() {
    // <scout:base />
    String basePath = m_params.getBasePath();
    if (StringUtility.isNullOrEmpty(basePath)) {
      basePath = "/";
    }
    else if (basePath.lastIndexOf('/') != basePath.length() - 1) {
      // add / at end of string (unless it already has a slash at the end)
      basePath += "/";
    }
    String baseTag = "<base href=\"" + basePath + "\">";
    m_workingContent = m_workingContent.replaceAll(PATTERN_BASE_TAG, baseTag);
  }

  @SuppressWarnings("squid:S1149")
  protected void replaceIncludeTags() throws IOException {
    // <scout:include template="no-script.html" />
    Matcher m = PATTERN_INCLUDE_TAG.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String includeName = m.group(1);
      URL includeUrl = BEANS.get(IWebContentService.class).getWebContentResource("/includes/" + includeName);
      if (includeUrl == null) {
        throw new IOException("Could not resolve include '" + includeName + "'");
      }
      else {
        byte[] includeContent = IOUtility.readFromUrl(includeUrl);
        String replacement = new String(includeContent, StandardCharsets.UTF_8);
        // Ensure exactly 1 newline before and after the replacement (to improve readability in resulting document)
        replacement = "\n" + replacement.trim() + "\n";
        m.appendReplacement(sb, replacement);
        LOG.info("Resolved include '{}'", includeName);
      }
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  @SuppressWarnings("squid:S1149")
  protected void replaceMessageTags() {
    // <scout:message key="ui.JavaScriptDisabledTitle" />
    Matcher m = PATTERN_MESSAGE_TAG.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    HtmlHelper htmlHelper = BEANS.get(HtmlHelper.class);
    while (m.find()) {
      Matcher m2 = PATTERN_KEY_VALUE.matcher(m.group(1));
      String style = "";
      List<String> keys = new ArrayList<>();
      while (m2.find()) {
        String key = m2.group(1);
        String value = m2.group(2);
        if (StringUtility.equalsIgnoreCase(key, "style")) {
          style = StringUtility.lowercase(value);
        }
        else if (StringUtility.equalsIgnoreCase(key, "key")) {
          keys.add(value);
        }
      }
      // Generate output
      String text = "";
      if (!keys.isEmpty()) {
        switch (style) {
          case "javascript":
            // JavaScript style replacement
            StringBuilder js = new StringBuilder("{");
            for (String key : keys) {
              js.append("'").append(key).append("': ");
              js.append(toJavaScriptString(TEXTS.get(key)));
              js.append(", ");
            }
            int length = js.length();
            js.delete(length - 2, length);
            js.append("}");
            text = js.toString();
            break;
          case "plain":
            // Plain normal replacement
            text = TEXTS.get(keys.get(0));
            break;
          case "tag":
            StringBuilder tags = new StringBuilder();
            for (String key : keys) {
              tags.append("<scout-text data-key=\"").append(htmlHelper.escape(key)).append("\" ");
              tags.append("data-value=\"").append(htmlHelper.escape(TEXTS.get(key))).append("\" />");
            }
            text = tags.toString();
            break;
          case "html":
          default:
            text = htmlHelper.escape(TEXTS.get(keys.get(0)));
            break;
        }
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(text));
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  protected String toJavaScriptString(String text) {
    // escape single quotes
    text = text.replaceAll("'", "\\\\'");
    // escape new-lines
    text = text.replaceAll("(\r\n|\n)", "\\\\n");
    return "'" + text + "'";
  }
}
