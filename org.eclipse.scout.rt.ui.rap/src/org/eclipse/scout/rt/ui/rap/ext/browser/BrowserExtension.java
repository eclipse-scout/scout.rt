/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext.browser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.html.HtmlAdapter;
import org.eclipse.scout.rt.ui.rap.html.IHyperlinkProcessor;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationListener;

/**
 * Adds hyperlink callback support as in normal swt to the rwt browser<br/>
 * Adds support for registering/unregistering (publishing) local resources.
 * 
 * @since 3.8.0
 */
public class BrowserExtension {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BrowserExtension.class);
  public static final String HYPERLINK_FUNCTION_NAME = "scoutActivateUrl";
  public static final String HYPERLINK_FUNCTION_RETURN_TYPE = "void";

  private final Browser m_browser;
  private final HashMap<String, String> m_hyperlinkMap;
  private BrowserFunction m_hyperlinkBrowserFunction;
  private IHyperlinkCallback m_hyperlinkCallback;
  private String m_resourceFolderId;
  private IRwtEnvironment m_uiEnvironment;
  private HyperlinkProcessor m_hyperlinkProcessor;
  private HashSet<String> m_tempFileNames;

  public BrowserExtension(Browser b, IRwtEnvironment uiEnvironment, IHyperlinkCallback hyperlinkCallback) {
    m_browser = b;
    m_hyperlinkCallback = hyperlinkCallback;
    m_hyperlinkMap = new HashMap<String, String>();
    m_resourceFolderId = UUID.randomUUID().toString();
    m_uiEnvironment = uiEnvironment;
    m_tempFileNames = new HashSet<String>();
    m_hyperlinkProcessor = createHyperlinkProcessor();
    m_hyperlinkProcessor.setGeneratedMappings(m_hyperlinkMap);
    m_hyperlinkProcessor.setHyperlinkFunctionName(getHyperlinkFunctionName());
    m_hyperlinkProcessor.setHyperlinkFunctionReturnType(getHyperlinkFunctionReturnType());
  }

  public void attach() {
    if (m_hyperlinkBrowserFunction == null) {
      m_hyperlinkBrowserFunction = createLocalHyperlinkFunction();
    }
  }

  private BrowserFunction createLocalHyperlinkFunction() {
    return new BrowserFunction(m_browser, getHyperlinkFunctionName()) {
      @Override
      public Object function(Object[] arguments) {
        String localUrl = m_hyperlinkMap.get(arguments[0]);
        if (localUrl == null) {
          LOG.error("Hyperlink could not be activated. No url specified.");
          return null;
        }
        if (m_hyperlinkCallback == null) {
          LOG.error("Hyperlink could not be activated. Please specify the runnable to be executed.");
          return null;
        }

        m_hyperlinkCallback.execute(localUrl);

        return null;
      }
    };
  }

  public void detach() {
    if (m_hyperlinkBrowserFunction != null) {
      m_hyperlinkBrowserFunction.dispose();
      m_hyperlinkBrowserFunction = null;
    }
    clearLocalHyperlinkCache();
    clearResourceCache();
  }

  /**
   * Registers a text resource in the {@link ResourceManager}.
   * 
   * @param content
   *          the content of the resource to add.
   * @return the web url of the resource valid for calls from outside
   */
  public String addResource(String name, InputStream content) {
    name = name.replaceAll("\\\\", "/");
    if (name == null || name.length() == 0) {
      return null;
    }
    if (!name.startsWith("/")) {
      name = "/" + name;
    }
    String uniqueName = m_resourceFolderId + name;
    m_tempFileNames.add(uniqueName);
    ResourceManager resourceManager = RWT.getResourceManager();
    resourceManager.register(uniqueName, content);
    return resourceManager.getLocation(uniqueName);
  }

  public void clearResourceCache() {
    ResourceManager resourceManager = RWT.getResourceManager();
    try {
      for (String name : m_tempFileNames) {
        resourceManager.unregister(name);
      }
    }
    finally {
      m_tempFileNames.clear();
    }
  }

  /**
   * Replaces the links in the given html with calls to a javascript callback function.
   * 
   * @see {@link HyperlinkProcessor}
   */
  public String adaptHyperlinks(String html) {
    HtmlAdapter htmlAdapter = getUiEnvironment().getHtmlAdapter();
    if (htmlAdapter == null) {
      return html;
    }
    return htmlAdapter.processHyperlinks(html, m_hyperlinkProcessor);
  }

  protected HyperlinkProcessor createHyperlinkProcessor() {
    return new HyperlinkProcessor();
  }

  public void clearLocalHyperlinkCache() {
    m_hyperlinkMap.clear();
  }

  protected String getHyperlinkFunctionName() {
    return HYPERLINK_FUNCTION_NAME;
  }

  protected String getHyperlinkFunctionReturnType() {
    return HYPERLINK_FUNCTION_RETURN_TYPE;
  }

  public IRwtEnvironment getUiEnvironment() {
    return m_uiEnvironment;
  }

  public String getDefaultHyperlinkTarget() {
    return m_hyperlinkProcessor.getDefaultTarget();
  }

  public void setDefaultHyperlinkTarget(String defaultTarget) {
    m_hyperlinkProcessor.setDefaultTarget(defaultTarget);
  }

  public boolean isConvertExternalUrlsEnabled() {
    return m_hyperlinkProcessor.isConvertExternalUrlsEnabled();
  }

  public void setConvertExternalUrlsEnabled(boolean convertExternalUrls) {
    m_hyperlinkProcessor.setConvertExternalUrlsEnabled(convertExternalUrls);
  }

  /**
   * Replaces the links in the given html with calls to a javascript callback function. Considers &lt;a&gt; and
   * &lt;area&gt; tags.
   * <p>
   * This is necessary because the RAP Browser widget does NOT send
   * {@link LocationListener#changed(org.eclipse.swt.browser.LocationEvent)} and
   * {@link LocationListener#changing(org.eclipse.swt.browser.LocationEvent)} if a link gets clicked.
   */
  public static class HyperlinkProcessor implements IHyperlinkProcessor {
    private Map<String /*externalKey*/, String /*url*/> m_generatedMappings;
    private String m_hyperlinkFunctionName;
    private String m_hyperlinkFunctionReturnType;
    private String m_defaultTarget;
    private boolean m_convertExternalUrlsEnabled;

    public Map<String, String> getGeneratedMappings() {
      return m_generatedMappings;
    }

    public void setGeneratedMappings(Map<String, String> generatedMappings) {
      m_generatedMappings = generatedMappings;
    }

    public String getHyperlinkFunctionName() {
      return m_hyperlinkFunctionName;
    }

    public void setHyperlinkFunctionName(String hyperlinkFunctionName) {
      m_hyperlinkFunctionName = hyperlinkFunctionName;
    }

    public String getHyperlinkFunctionReturnType() {
      return m_hyperlinkFunctionReturnType;
    }

    public void setHyperlinkFunctionReturnType(String hyperlinkFunctionReturnType) {
      m_hyperlinkFunctionReturnType = hyperlinkFunctionReturnType;
    }

    public String getDefaultTarget() {
      return m_defaultTarget;
    }

    public void setDefaultTarget(String defaultTarget) {
      m_defaultTarget = defaultTarget;
    }

    public boolean isConvertExternalUrlsEnabled() {
      return m_convertExternalUrlsEnabled;
    }

    public void setConvertExternalUrlsEnabled(boolean convertExternalUrls) {
      m_convertExternalUrlsEnabled = convertExternalUrls;
    }

    @Override
    public String processUrl(String url, boolean local) {
      if (url == null) {
        return null;
      }
      if (!local && !isConvertExternalUrlsEnabled()) {
        return url;
      }

      String externalKey = "" + m_generatedMappings.size();
      String callableURL = "javascript:" + (StringUtility.hasText(m_hyperlinkFunctionReturnType) ? m_hyperlinkFunctionReturnType + " " : "") + m_hyperlinkFunctionName + "('" + externalKey + "');";

      m_generatedMappings.put(externalKey, url);
      return callableURL;
    }

    @Override
    public String processTarget(String target, boolean local) {
      if (local || !StringUtility.isNullOrEmpty(target)) {
        return target;
      }

      return getDefaultTarget();
    }
  }
}
