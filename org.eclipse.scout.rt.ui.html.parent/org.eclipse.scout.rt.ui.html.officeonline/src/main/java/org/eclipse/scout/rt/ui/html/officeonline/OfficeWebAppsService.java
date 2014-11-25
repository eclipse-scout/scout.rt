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
package org.eclipse.scout.rt.ui.html.officeonline;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.ui.html.officeonline.internal.Activator;
import org.eclipse.scout.rt.ui.html.officeonline.wopi.WopiUtility;
import org.eclipse.scout.service.AbstractService;

@Priority(1)
public class OfficeWebAppsService extends AbstractService implements IOfficeWebAppsService {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OfficeWebAppsService.class);

  //wopi discovery cache expires after 30 minutes
  private long m_mappingsNextUpdate = 0L;
  private HashMap<String/*Zone_App_Action*/, String/*URL*/> m_mappings = new HashMap<>();

  @Override
  public String getDiscoveryUrl() {
    String discoveryUrl = Activator.getBundleContext().getProperty(CONFIG_INI_DISCOVERY_URL);
    if (discoveryUrl == null) {
      LOG.warn("Missing config.ini parameter " + CONFIG_INI_DISCOVERY_URL + "; Office Web Apps Server will not be available");
    }
    return discoveryUrl;
  }

  @Override
  public String createIFrameUrl(Zone zone, App app, Action action, String wopiSrcUrl, String accessToken) throws ProcessingException {
    String discoveryUrl = getDiscoveryUrl();
    if (discoveryUrl == null) {
      throw new ProcessingException("Missing config.ini parameter " + CONFIG_INI_DISCOVERY_URL);
    }
    String mapKey = createMapLookupKey(zone, app, action);
    String locale = WopiUtility.createWopiLocale(LocaleThreadLocal.get());
    try {
      ensureWopiConfigLoaded();
      String urlsrc = m_mappings.get(mapKey);
      if (urlsrc == null) {
        return null;
      }
      urlsrc = urlsrc.replace("{LOCALE}", encodeURIComponent(locale));
      urlsrc = urlsrc + "WOPISrc=" + encodeURIComponent(wopiSrcUrl) + "&access_token=" + encodeURIComponent(accessToken) + "&access_token_ttl=0";
      return urlsrc;
    }
    catch (IOException io) {
      throw new ProcessingException("wopiSrcUrl=" + wopiSrcUrl + ", mapkey=" + mapKey + ", wopi-locale=" + locale, io);
    }
  }

  protected void ensureWopiConfigLoaded() throws IOException {
    if (m_mappingsNextUpdate > System.currentTimeMillis()) {
      return;
    }
    HashMap<String, String> newMap = new HashMap<String, String>();
    SimpleXmlElement document = new SimpleXmlElement();
    document.parseStream(new URL(getDiscoveryUrl()).openStream());
    parseDiscoveryXml(document, newMap);
    m_mappings = newMap;
    m_mappingsNextUpdate = System.currentTimeMillis() + 30L * 60L * 1000L;//30 minutes
  }

  protected void parseDiscoveryXml(SimpleXmlElement document, Map<String, String> out) throws IOException {
    for (SimpleXmlElement zoneNode : document.getRoot().getChildren("net-zone")) {
      Zone zone = Zone.parse(zoneNode.getStringAttribute("name"));
      if (zone == null) {
        continue;
      }
      for (SimpleXmlElement appNode : zoneNode.getChildren("app")) {
        App app = App.parse(appNode.getStringAttribute("name"));
        if (app == null) {
          continue;
        }
        for (SimpleXmlElement actionNode : appNode.getChildren("action")) {
          Action action = Action.parse(actionNode.getStringAttribute("name"));
          if (action == null) {
            continue;
          }
          String ext = actionNode.getStringAttribute("ext");
          if (!app.getFileExtension().equals(ext)) {
            continue;
          }
          String urlsrc = actionNode.getStringAttribute("urlsrc");
          //prepare url
          urlsrc = urlsrc.replaceAll("<rs=DC_LLCC\\&>", "rs={LOCALE}&");
          urlsrc = urlsrc.replaceAll("<ui=UI_LLCC\\&>", "ui={LOCALE}&");
          urlsrc = urlsrc.replaceAll("<[^>]+>", "");
          String mapKey = createMapLookupKey(zone, app, action);
          if (!out.containsKey(mapKey)) {
            out.put(mapKey, urlsrc);
          }
        }
      }
    }
  }

  protected String createMapLookupKey(Zone zone, App app, Action action) {
    return zone + "_" + app + "_" + action;
  }

  protected String encodeURIComponent(String s) throws IOException {
    return URLEncoder.encode(s, "UTF-8");
  }
}
