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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;

public class BinaryResourceUrlUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BinaryResourceUrlUtility.class);

  /**
   * @return a relative URL for a configured logical icon-name or a font-based icon. For instance:
   *         <ul>
   *         <li>input: <code>"bookmark"</code>, output: <code>"icon/bookmark.png"</code> (the file extension is
   *         included to support auto-detection of the MIME type without looking at the file contents)</li>
   *         <li>input: <code>"font:X"</code>, output: <code>"font:X"</code></li>
   *         </ul>
   *         The file extension is included to be able to auto-detect the MIME type based on it.
   *         <p>
   *         Use this method for image-files located in the /resource/icons directories of all jars on the classpath.
   */
  public static String createIconUrl(IJsonAdapter<?> jsonAdapter, String iconId) {
    if (jsonAdapter == null) {
      return null;
    }
    if (iconId == null) {
      return null;
    }
    if (iconId.startsWith("font:")) {
      return iconId;
    }
    IconSpec iconSpec = IconLocator.instance().getIconSpec(iconId);
    if (iconSpec != null) {
      return "icon/" + iconSpec.getName() + "?" + JsonRequest.PROP_JSON_SESSION_ID + "=" + jsonAdapter.getJsonSession().getJsonSessionId(); // includes file extension
    }
    return null; // may happen, when no icon is available for the requested iconName
  }

  /**
   * @return a relative URL for a resource handled by an adapter, see
   *         {@link StaticResourceRequestInterceptor#loadDynamicAdapterResource(javax.servlet.http.HttpServletRequest, String)}
   *         <p>
   *         The calling adapter must implement {@link IBinaryResourceProvider}
   */
  public static String createCallbackUrl(IJsonAdapter<?> jsonAdapter, String filename) {
    if (jsonAdapter == null) {
      return null;
    }
    if (!(jsonAdapter instanceof IBinaryResourceProvider)) {
      LOG.warn("adapter " + jsonAdapter + " is not implementing " + IBinaryResourceProvider.class);
      return null;
    }
    if (filename == null) {
      return null;
    }
    return "tmp/" + filename + "?" + JsonRequest.PROP_JSON_SESSION_ID + "=" + jsonAdapter.getJsonSession().getJsonSessionId() + "&adapterId=" + jsonAdapter.getId();
  }
}
