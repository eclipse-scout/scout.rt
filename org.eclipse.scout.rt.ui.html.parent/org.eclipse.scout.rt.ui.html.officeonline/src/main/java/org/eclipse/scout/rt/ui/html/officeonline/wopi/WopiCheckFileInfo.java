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
package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.json.JSONObject;

public class WopiCheckFileInfo {
  public boolean AllowExternalMarketplace;
  public String BaseFileName;
  public String BreadcrumbBrandName;
  public String BreadcrumbBrandUrl;
  public String BreadcrumbDocName;
  public String BreadcrumbDocUrl;
  public String BreadcrumbFolderName;
  public String BreadcrumbFolderUrl;
  public String ClientUrl;
  public boolean CloseButtonClosesWindow;
  public String CloseUrl;
  public boolean DisableBrowserCachingOfUserContent;
  public boolean DisablePrint;
  public boolean DisableTranslation;
  public String DownloadUrl;
  public String FileSharingUrl;
  public String FileUrl;
  public String HostAuthenticationId;
  public String HostEditUrl;
  public String HostEmbeddedEditUrl;
  public String HostEmbeddedViewUrl;
  public String HostName;
  public String HostNotes;
  public String HostRestUrl;
  public String HostViewUrl;
  public String IrmPolicyDescription;
  public String IrmPolicyTitle;
  public String OwnerId;
  public String PresenceProvider;
  public String PresenceUserId;
  public String PrivacyUrl;
  public boolean ProtectInClient;
  public boolean ReadOnly;
  public boolean RestrictedWebViewOnly;
  public String SHA256;
  public String SignoutUrl;
  public long Size;
  public boolean SupportsCoauth;
  public boolean SupportsCobalt;
  public boolean SupportsFolders;
  public boolean SupportsLocks;
  public boolean SupportsScenarioLinks;
  public boolean SupportsSecureStore;
  public boolean SupportsUpdate;
  public String TenantId;
  public String TermsOfUseUrl;
  public String TimeZone;
  public boolean UserCanAttend;
  public boolean UserCanNotWriteRelative;
  public boolean UserCanPresent;
  public boolean UserCanWrite;
  public String UserFriendlyName;
  public String UserId;
  public String Version;
  public boolean WebEditingDisabled;

  public String toJson() throws IOException {
    JSONObject j = new JSONObject();
    for (Field f : WopiCheckFileInfo.class.getFields()) {
      if (!Modifier.isPublic(f.getModifiers())) {
        continue;
      }
      try {
        Object value = f.get(this);
        if (value != null) {
          j.put(f.getName(), value);
        }
      }
      catch (Exception e) {
        throw new IOException("setting " + f.getName(), e);
      }
    }
    return j.toString();
  }
}
