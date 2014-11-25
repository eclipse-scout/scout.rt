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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.ui.html.officeonline.wopi.IWopiContentProvider;
import org.eclipse.scout.service.IService;

/**
 * Access to Microsoft Office Web Apps services, see {@link OfficeWebAppsService}
 * <p>
 * Requires the config.ini parameter {@link #CONFIG_INI_DISCOVERY_URL}
 */
public interface IOfficeWebAppsService extends IService {
  /**
   * the url of the office web apps server registry to find out the app urls<br/>
   * for example https://office-web-apps-server/hosting/discovery
   */
  String CONFIG_INI_DISCOVERY_URL = "owas.discovery.url";

  /**
   * registry of office web apps urls, for example
   * https://office-web-apps-server/hosting/discovery
   *
   * @return the url defined by {@link #CONFIG_INI_DISCOVERY_URL}
   */
  String getDiscoveryUrl();

  /**
   * @param wopiSrcUrl
   *          is {@link IWopiContentProvider#getWopiBaseUrl()}/fileId
   * @return the iframe source url used to access the fileId with the accessToken.
   *         <p>
   *         see also {@link IWopiContentProvider}
   */
  String createIFrameUrl(Zone zone, App app, Action action, String wopiSrcUrl, String accessToken) throws ProcessingException;

  enum Zone {
    INTERNAL_HTTPS("internal-https"),
    EXTERNAL_HTTPS("external-https");

    private final String m_xmlName;

    private Zone(String xmlName) {
      m_xmlName = xmlName;
    }

    @Override
    public String toString() {
      return m_xmlName;
    }

    public static Zone parse(String name) {
      for (Zone elem : values()) {
        if (elem.m_xmlName.equals(name)) {
          return elem;
        }
      }
      return null;
    }
  }

  enum App {
    WORD("Word", "docx"),
    EXCEL("Excel", "xlsx"),
    POWERPOINT("PowerPoint", "pptx");

    private final String m_xmlName;
    private final String m_ext;

    private App(String xmlName, String ext) {
      m_xmlName = xmlName;
      m_ext = ext;
    }

    public String getFileExtension() {
      return m_ext;
    }

    @Override
    public String toString() {
      return m_xmlName;
    }

    public static App parse(String name) {
      for (App elem : values()) {
        if (elem.m_xmlName.equals(name)) {
          return elem;
        }
      }
      return null;
    }

  }

  enum Action {
    VIEW("view"),
    EMBED_VIEW("embedview"),
    EDIT("edit"),
    EDIT_NEW("editnew"),
    MOBILE_VIEW("mobileView");

    private final String m_xmlName;

    private Action(String xmlName) {
      m_xmlName = xmlName;
    }

    @Override
    public String toString() {
      return m_xmlName;
    }

    public static Action parse(String name) {
      for (Action elem : values()) {
        if (elem.m_xmlName.equals(name)) {
          return elem;
        }
      }
      return null;
    }

  }

}
