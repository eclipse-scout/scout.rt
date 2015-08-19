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

import org.eclipse.scout.commons.Encoding;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;

/**
 * A simple tag-parser used to replace scout-tags in HTML documents.
 */
public class HtmlDocumentParser {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HtmlDocumentParser.class);

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
    replaceScriptTags();
    return m_workingContent.getBytes(Encoding.UTF_8);
  }

  private void replaceIncludeTags() throws IOException {
    // <scout:include template="no-script.html" />
    Pattern pattern = Pattern.compile("<scout\\:include template=\"(.*)\" />", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher m = pattern.matcher(m_workingContent);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String includeName = m.group(1);
      URL inlcudeUrl = BEANS.get(IWebContentService.class).getWebContentResource("/includes/" + includeName);
      if (inlcudeUrl == null) {
        throw new IOException("Could not resolve include '" + includeName + "'");
      }
      else {
        byte[] includeContent = IOUtility.readFromUrl(inlcudeUrl);
        m.appendReplacement(sb, new String(includeContent, Encoding.UTF_8));
        LOG.info("Resolved include '" + includeName + "'");
      }
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  private void replaceMessageTags() {
    // <scout:message key="ui.javascriptDisabledTitle" />
    Pattern pattern = Pattern.compile("<scout\\:message key=\"(.*?)\"(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Matcher m = pattern.matcher(m_workingContent);
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
        if (text == null) {
          text = "Undefined text: " + keyAttr;
        }
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(text));
    }
    m.appendTail(sb);
    m_workingContent = sb.toString();
  }

  private String toJavaScriptString(String text) {
    // escape single quotes
    text = text.replaceAll("'", "\\'");
    // escape new-lines
    text = text.replaceAll("\r\n", "\\\\n");
    return "'" + text + "'";
  }

  /**
   * Process all js and css script tags that contain the marker text "fingerprint". The marker text is replaced by the
   * effective files {@link HttpCacheObject#getFingerprint()} in hex format
   */
  protected void replaceScriptTags() throws IOException {
    Matcher m = ScriptFileBuilder.SCRIPT_URL_PATTERN.matcher(m_workingContent);
    StringBuilder buf = new StringBuilder();
    int lastEnd = 0;
    int replaceCount = 0;
    while (m.find()) {
      buf.append(m_workingContent.substring(lastEnd, m.start()));
      if ("fingerprint".equals(m.group(4))) {
        replaceCount++;
        String fingerprint = null;
        if (m_params.isCacheEnabled()) {
          HttpCacheObject obj = m_params.loadScriptFile(m.group());
          if (obj == null) {
            LOG.warn("Failed to locate resource referenced in html file '" + m_params.getResourcePath() + "': " + m.group());
          }
          fingerprint = (obj != null ? Long.toHexString(obj.getResource().getFingerprint()) : m.group(4));
        }
        buf.append(m.group(1));
        buf.append(m.group(2));
        buf.append("-");
        buf.append(m.group(3));
        if (fingerprint != null) {
          buf.append("-");
          buf.append(fingerprint);
        }
        if (m_params.isMinify()) {
          buf.append(".min");
        }
        buf.append(".");
        buf.append(m.group(5));
      }
      else {
        buf.append(m.group());
      }
      //next
      lastEnd = m.end();
    }
    if (replaceCount == 0) {
      return;
    }
    buf.append(m_workingContent.substring(lastEnd));
    String newHtml = buf.toString();
    if (LOG.isTraceEnabled()) {
      LOG.trace("process html script tags:\nINPUT\n" + m_workingContent + "\n\nOUTPUT\n" + newHtml);
    }
    m_workingContent = newHtml;
  }

}
