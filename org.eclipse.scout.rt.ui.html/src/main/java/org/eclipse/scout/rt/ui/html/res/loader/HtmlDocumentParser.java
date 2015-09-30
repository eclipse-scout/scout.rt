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
package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.Encoding;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.res.IWebContentService;

/**
 * A simple tag-parser used to replace scout-tags in HTML documents.
 */
public class HtmlDocumentParser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlDocumentParser.class);

  private static final Pattern PATTERN_INCLUDE_TAG = Pattern.compile("<scout\\:include template=\"(.*)\" />", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_MESSAGE_TAG = Pattern.compile("<scout\\:message key=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_STYLESHEET_TAG = Pattern.compile("<scout\\:stylesheet src=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile("<scout\\:script src=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  private final HtmlDocumentParserParameters m_params;
  private String m_workingContent;

  public HtmlDocumentParser(HtmlDocumentParserParameters params) {
    m_params = params;
  }

  public byte[] parseDocument(byte[] document) throws IOException {
    // the order of calls is important: first we must resolve all includes
    m_workingContent = new String(document, Encoding.UTF_8);
    replaceIncludeTags();
    replaceMessageTags();
    replaceStylesheetTags();
    replaceScriptTags_New();
    return m_workingContent.getBytes(Encoding.UTF_8);
  }

  protected void replaceScriptTags(Pattern pattern, String tagPrefix, String tagSuffix) throws IOException {
    Matcher m = pattern.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String srcAttr = m.group(1);
      String fingerprint = null;

      if (m_params.isCacheEnabled()) {
        HttpCacheObject script = m_params.loadScriptFile(srcAttr);
        if (script == null) {
          LOG.warn("Failed to locate script referenced in html file '" + m_params.getHtmlPath() + "': " + srcAttr);
        }
        else {
          fingerprint = Long.toHexString(script.getResource().getFingerprint());
        }
      }

      StringBuffer linkTag = new StringBuffer(tagPrefix);
      File srcFile = new File(srcAttr);
      String[] filenameParts = FileUtility.getFilenameParts(srcFile);
      // append path to file
      if (srcFile.getParent() != null) {
        linkTag.append(srcFile.getParent()).append("/");
      }
      // append file name without file-extension
      linkTag.append(getScriptFileName(filenameParts[0]));
      // append fingerprint
      if (fingerprint != null) {
        linkTag.append("-").append(fingerprint);
      }
      // append 'min'
      if (m_params.isMinify()) {
        linkTag.append(".min");
      }
      // append file-extension
      linkTag.append(".").append(filenameParts[1]);
      linkTag.append(tagSuffix);

      m.appendReplacement(sb, linkTag.toString());
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
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

  protected void replaceScriptTags_New() throws IOException {
    // <scout:script src="scout-all-macro.css" />
    replaceScriptTags(PATTERN_SCRIPT_TAG, "<script src=\"", "\"></script>");
  }

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
        String replacement = new String(includeContent, Encoding.UTF_8);
        // Ensure exactly 1 newline before and after the replacement (to improve readability in resulting document)
        replacement = "\n" + replacement.trim() + "\n";
        m.appendReplacement(sb, replacement);
        LOG.info("Resolved include '" + includeName + "'");
      }
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  protected void replaceMessageTags() {
    // <scout:message key="ui.javascriptDisabledTitle" />
    Matcher m = PATTERN_MESSAGE_TAG.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String keyAttr = m.group(1);
      String otherAttrs = m.group(2);
      String text;
      if (otherAttrs.contains("style=\"javascript\"")) {
        // JavaScript style replacement
        StringBuilder js = new StringBuilder("{");
        String[] keys = keyAttr.split(" ");
        for (String key : keys) {
          js.append("'" + key + "': ");
          js.append(toJavaScriptString(TEXTS.get(key)));
          js.append(", ");
        }
        int length = js.length();
        js.delete(length - 2, length);
        js.append("}");
        text = js.toString();
      }
      else {
        // Plain normal replacement
        text = TEXTS.get(keyAttr);
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(text));
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  protected String toJavaScriptString(String text) {
    // escape single quotes
    text = text.replaceAll("'", "\\'");
    // escape new-lines
    text = text.replaceAll("\r\n", "\\\\n");
    return "'" + text + "'";
  }

}
