/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.shared.ui.webresource.ScriptResourceIndexes;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceDescriptor;
import org.eclipse.scout.rt.shared.ui.webresource.WebResources;
import org.eclipse.scout.rt.ui.html.UiThemeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple tag-parser used to replace scout-tags in HTML documents.
 * <p>
 * Note: This is not a &#64;Bean, because the constructor requires an argument. To customize this class, override the
 * factory method in {@link HtmlFileLoader}.
 */
public class HtmlDocumentParser {
  private static final Logger LOG = LoggerFactory.getLogger(HtmlDocumentParser.class);

  protected static final String ENTRY_POINT_VALUE_REGEX = "[^\"~]*";
  protected static final Pattern PATTERN_INCLUDE_TAG = Pattern.compile("<scout:include\\s+template=\"([^\"]*)\"\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_MESSAGE_TAG = Pattern.compile("<scout:message(.*?)\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_STYLESHEET_TAG = Pattern.compile("<scout:stylesheet\\s+src=\"([^\"]*)\"\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile("<scout:script\\s+src=\"([^\"]*)\"\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_SCRIPTS_TAG = Pattern.compile("<scout:scripts\\s+entrypoint=\"(" + ENTRY_POINT_VALUE_REGEX + ")\"\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_STYLESHEETS_TAG = Pattern.compile("<scout:stylesheets\\s+entrypoint=\"(" + ENTRY_POINT_VALUE_REGEX + ")\"\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_BASE_TAG = Pattern.compile("<scout:base\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_VERSION_TAG = Pattern.compile("<scout:version\\s*/?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_UNKNOWN_TAG = Pattern.compile("<scout:(\"[^\"]*\"|[^>]*?)*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_KEY_VALUE = Pattern.compile("(\\S+)=\"([^\"]*)\"");
  protected static final Pattern PATTERN_BODY_TAG = Pattern.compile("<body\\s*([^>]*)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  protected static final Pattern PATTERN_NONCE_ATTRIBUTE = Pattern.compile(Pattern.quote("nonce=\"scout:nonce\""));

  protected final HtmlDocumentParserParameters m_params;
  protected String m_cspNonce;
  protected String m_workingContent;

  public HtmlDocumentParser(HtmlDocumentParserParameters params) {
    m_cspNonce = "";
    m_params = params;
  }

  public byte[] parseDocument(byte[] document) {
    m_cspNonce = newNonce(); // create new nonce for each parse
    m_workingContent = new String(document, StandardCharsets.UTF_8);
    replaceAllTags();
    return m_workingContent.getBytes(StandardCharsets.UTF_8);
  }

  protected void replaceAllTags() {
    // the order of calls is important: first we must resolve all includes
    replaceIncludeTags();
    replaceBaseTags();
    replaceVersionTags();
    replaceMessageTags();
    replaceStylesheetsTags();
    replaceStylesheetTags();
    replaceScriptsTags();
    replaceScriptTags();
    replaceNonceAttributes();
    replaceBodyTag();
    stripUnknownTags();
  }

  protected String newNonce() {
    return SessionId.randomSessionId();
  }

  protected void replaceBodyTag() {
    m_workingContent = PATTERN_BODY_TAG.matcher(m_workingContent).replaceAll(m -> {
      String bodyTagContent = m.group(1);
      if (!StringUtility.hasText(bodyTagContent)) {
        return "<body data-scout-nonce=\"" + getCspNonce() + "\">";
      }
      return "<body " + bodyTagContent + " data-scout-nonce=\"" + getCspNonce() + "\">";
    });
  }

  public String getCspNonce() {
    return m_cspNonce;
  }

  protected void replaceNonceAttributes() {
    String nonceAttribute = "nonce=\"" + getCspNonce() + "\"";
    m_workingContent = PATTERN_NONCE_ATTRIBUTE.matcher(m_workingContent).replaceAll(nonceAttribute);
  }

  protected void replaceScriptTags(Pattern pattern, Supplier<IHtmlElement> tagFactory, String attributeName) {
    m_workingContent = pattern.matcher(m_workingContent)
        .replaceAll(r -> getWebResourceElementHtml(tagFactory, r.group(1), attributeName));
  }

  /**
   * Creates the external path of the given resource, including theme, fingerprint and '.min' extensions.
   */
  protected String createExternalPath(String internalPath) {
    String theme = UiThemeHelper.get().isDefaultTheme(m_params.getTheme()) ? null : m_params.getTheme();
    return new WebResourceLoader(m_params.isMinify(), false, theme)
        .resolveResource(internalPath)
        .map(WebResourceDescriptor::getResolvedPath)
        .orElse(internalPath);
  }

  protected IHtmlElement createScriptTagBuilder() {
    return HTML.tag("script")
        .addAttribute("nonce", getCspNonce());
  }

  protected IHtmlElement createStylesheetsTagBuilder() {
    return HTML.tag("link")
        .withRequireEndTag(false)
        .addAttribute("rel", "stylesheet")
        .addAttribute("type", "text/css");
  }

  protected void replaceStylesheetTags() {
    // <scout:stylesheet src="scout-all-macro.css" />
    replaceScriptTags(PATTERN_STYLESHEET_TAG, this::createStylesheetsTagBuilder, "href");
  }

  protected void replaceScriptTags() {
    // <scout:script src="scout-all-macro.css" />
    replaceScriptTags(PATTERN_SCRIPT_TAG, this::createScriptTagBuilder, "src");
  }

  protected void replaceScriptsTags() {
    // <scout:scripts entryPoint="entry-point-name"/>
    if (!m_params.isBrowserSupported()) {
      String unsupportedBrowserScriptTag = createScriptTagBuilder().addAttribute("src", LegacyBrowserScriptLoader.LEGACY_BROWSERS_SCRIPT).toHtml();
      m_workingContent = PATTERN_SCRIPTS_TAG.matcher(m_workingContent).replaceAll(unsupportedBrowserScriptTag);
      return;
    }
    replaceEntryPointTags(PATTERN_SCRIPTS_TAG, ".js", this::createScriptTagBuilder, "src");
  }

  protected void replaceStylesheetsTags() {
    // <scout:stylesheets entrypoint="entry-point-name"/>
    replaceEntryPointTags(PATTERN_STYLESHEETS_TAG, ".css", this::createStylesheetsTagBuilder, "href");
  }

  protected void replaceEntryPointTags(Pattern pattern, String fileSuffixFilter, Supplier<IHtmlElement> tagFactory, String attributeName) {
    m_workingContent = pattern.matcher(m_workingContent)
        .replaceAll(r -> buildScriptTagsForEntryPoint(r.group(1), fileSuffixFilter, tagFactory, attributeName));
  }

  protected String buildScriptTagsForEntryPoint(String entryPoint, String fileSuffixFilter, Supplier<IHtmlElement> tagFactory, String attributeName) {
    return getAssetsForEntryPoint(entryPoint)
        .filter(path -> path.toLowerCase().endsWith(fileSuffixFilter))
        .map(path -> getWebResourceElementHtml(tagFactory, path, attributeName))
        .collect(joining("\n"));
  }

  protected String getWebResourceElementHtml(Supplier<IHtmlElement> tagFactory, String internalPath, String attributeName) {
    return tagFactory.get().addAttribute(attributeName, createExternalPath(internalPath)).toHtml();
  }

  protected Stream<String> getAssetsForEntryPoint(String entryPoint) {
    return ScriptResourceIndexes.getAssetsForEntryPoint(entryPoint, m_params.isMinify(), m_params.isCacheEnabled()).stream();
  }

  protected void replaceBaseTags() {
    // <scout:base />
    String basePath = m_params.getBasePath();
    if (StringUtility.isNullOrEmpty(basePath)) {
      basePath = "/";
    }
    else if (basePath.charAt(basePath.length() - 1) != '/') {
      // add / at end of string (unless it already has a slash at the end)
      basePath += "/";
    }
    String baseTag = HTML.tag("base")
        .withRequireEndTag(false)
        .addAttribute("href", basePath)
        .toHtml();
    m_workingContent = PATTERN_BASE_TAG.matcher(m_workingContent).replaceAll(baseTag);
  }

  protected void replaceVersionTags() {
    // <scout:version />
    String version = CONFIG.getPropertyValue(ApplicationVersionProperty.class);
    String versionTag = HTML.tag("scout-version")
        .withRequireEndTag(false)
        .addAttribute("data-value", version)
        .toHtml();
    m_workingContent = PATTERN_VERSION_TAG.matcher(m_workingContent).replaceAll(versionTag);
  }

  protected void replaceIncludeTags() {
    // <scout:include template="no-script.html" />
    m_workingContent = PATTERN_INCLUDE_TAG.matcher(m_workingContent).replaceAll(r -> {
      String includeName = r.group(1);
      URL includeUrl = resolveInclude(includeName);
      if (includeUrl == null) {
        throw new ProcessingException("Could not resolve include '{}'.", includeName);
      }

      try {
        byte[] includeContent = IOUtility.readFromUrl(includeUrl);
        String replacement = new String(includeContent, StandardCharsets.UTF_8);
        LOG.trace("Resolved include '{}'", includeName);
        // Ensure exactly 1 newline before and after the replacement (to improve readability in resulting document)
        return "\n" + replacement.trim() + "\n";
      }
      catch (IOException e) {
        throw new ProcessingException("Error reading include '{}'.", includeName, e);
      }
    });
  }

  protected URL resolveInclude(String includeName) {
    return WebResources
        .resolveWebResource(includeName, m_params.isMinify(), m_params.isCacheEnabled())
        .map(WebResourceDescriptor::getUrl)
        .orElse(null);
  }

  protected void replaceMessageTags() {
    // <scout:message key="ui.JavaScriptDisabledTitle" />
    m_workingContent = PATTERN_MESSAGE_TAG.matcher(m_workingContent).replaceAll(this::replaceMessageTag);
  }

  protected String replaceMessageTag(MatchResult r) {
    HtmlHelper htmlHelper = BEANS.get(HtmlHelper.class);
    Matcher m2 = PATTERN_KEY_VALUE.matcher(r.group(1));
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
        case "javascript" -> {
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
        }
        case "plain" ->
          // Plain normal replacement (only supports one key, because we don't know how to separate multiple keys)
            text = TEXTS.get(keys.getFirst());
        case "tag" -> {
          StringBuilder tags = new StringBuilder();
          for (String key : keys) {
            String scoutText = HTML.tag("scout-text")
                .withRequireEndTag(false)
                .addAttribute("data-key", key)
                .addAttribute("data-value", TEXTS.get(key))
                .toHtml();
            tags.append(scoutText).append("\n");
          }
          text = tags.toString();
        }
        default -> text = htmlHelper.escape(TEXTS.get(keys.getFirst()));
      }
    }
    return Matcher.quoteReplacement(text);
  }

  protected String toJavaScriptString(String text) {
    // escape single quotes
    text = text.replaceAll("'", "\\\\'");
    // escape new-lines
    text = text.replaceAll("(\r\n|\n)", "\\\\n");
    return "'" + text + "'";
  }

  protected void stripUnknownTags() {
    m_workingContent = PATTERN_UNKNOWN_TAG.matcher(m_workingContent).replaceAll(r -> {
      LOG.warn("Removing unknown or improperly formatted scout tag from '{}': {}", m_params.getHtmlPath(), r.group());
      return "";
    });
  }
}
