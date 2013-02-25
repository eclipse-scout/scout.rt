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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

/**
 * <h3>BrowserSupport</h3> adding hyperlink callback support as in normal swt to the rwt browser
 * <p>
 * Adding support for registering/unregistering (publishing) local resources.
 * 
 * @since 3.8.0
 */
public class BrowserExtension {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BrowserExtension.class);
  private static final Pattern LOCAL_URL_PATTERN = Pattern.compile("(['\"])(http://local[?/][^'\"]*)(['\"])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final String HYPERLINK_FUNCTION_NAME = "scoutActivateLocalUrl";

  private final Browser m_browser;
  private final HashMap<String, String> m_hyperlinkMap;
  private BrowserFunction m_hyperlinkBrowserFunction;
  private IHyperlinkCallback m_hyperlinkCallback;
  private String m_resourceFolderId;
  //
  private HashSet<String> m_tempFileNames = new HashSet<String>();

  public BrowserExtension(Browser b, IHyperlinkCallback hyperlinkCallback) {
    m_browser = b;
    m_hyperlinkCallback = hyperlinkCallback;
    m_hyperlinkMap = new HashMap<String, String>();
    m_resourceFolderId = UUID.randomUUID().toString();
  }

  public void attach() {
    if (m_hyperlinkBrowserFunction == null) {
      m_hyperlinkBrowserFunction = createLocalHyperlinkFunction();
    }
  }

  private BrowserFunction createLocalHyperlinkFunction() {
    return new BrowserFunction(m_browser, getLocalHyperlinkFunctionName()) {
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
   * Replaces all http://local/... urls with a call to a javascript callback function named
   * {@link #getLocalHyperlinkFunctionName()}.
   * 
   * @see {@link BrowserFunction}
   */
  public String adaptLocalHyperlinks(String html) {
    return rewriteLocalHyperlinks(html, m_hyperlinkMap, getLocalHyperlinkFunctionName());
  }

  public void clearLocalHyperlinkCache() {
    m_hyperlinkMap.clear();
  }

  protected String getLocalHyperlinkFunctionName() {
    return HYPERLINK_FUNCTION_NAME;
  }

  /**
   * Replaces all href="http://local/... references in the html file with a javascript callback function
   * 
   * @param html
   * @param generatedMappings
   *          is being filled up with the generated mappings
   * @return the rewritten html
   */
  private static String rewriteLocalHyperlinks(String html, Map<String /*externalKey*/, String /*url*/> generatedMappings, String callbackFuncName) {
    if (html == null) {
      return html;
    }
    StringBuilder buf = new StringBuilder();
    Matcher m = LOCAL_URL_PATTERN.matcher(html);
    int nextFind = 0;
    while (m.find(nextFind)) {
      String localUrl = m.group(2);
      String externalKey = "" + generatedMappings.size();
      String callableURL = "javascript:" + callbackFuncName + "('" + externalKey + "');";

      buf.append(html.substring(nextFind, m.start()));
      buf.append(m.group(1));
      buf.append(callableURL);
      buf.append(m.group(3));
      //register
      generatedMappings.put(externalKey, localUrl);
      //next
      nextFind = m.end();
    }
    if (nextFind == 0) {
      return html;
    }
    if (nextFind < html.length()) {
      buf.append(html.substring(nextFind));
    }
    return buf.toString();
  }

}
