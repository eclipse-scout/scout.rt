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
package org.eclipse.scout.rt.ui.html;

import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.BinaryContent;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;

public class ImageUrlUtility {

  /**
   * Returns a relative URL for a configured logical icon-name or a font-based icon. For instance:
   * <ul>
   * <li>input: <code>"bookmark"</code>, output: <code>"icon/bookmark.png"</code> (the file extension is included to
   * support auto-detection of the MIME type without looking at the file contents)</li>
   * <li>input: <code>"font:X"</code>, output: <code>"font:X"</code></li>
   * </ul>
   * The file extension is included to be able to auto-detect the MIME type based on it.
   * <p>
   * Use this method for image-files located in the /resource/icons directory.
   */
  public static String createIconUrl(AbstractJsonAdapter<?> jsonAdapter, String iconName) {
    if (iconName == null) {
      return null;
    }
    if (iconName.startsWith("font:")) {
      return iconName;
    }
    IIconLocator iconLocator = jsonAdapter.getJsonSession().getClientSession().getIconLocator();
    IconSpec iconSpec = iconLocator.getIconSpec(iconName);
    if (iconSpec != null) {
      return "icon/" + iconSpec.getName(); // includes file extension
    }
    return null; // may happen, when no icon is available for the requested iconName
  }

  /**
   * Returns a relative URL for an image which belongs to an adapter. The file extension is appended from the given
   * 'image' argument to support auto-detection of the MIME type without looking at the file contents.
   */
  public static String createImageUrl(AbstractJsonAdapter<?> jsonAdapter, BinaryContent image) {
    if (image == null) {
      return null;
    }
    return "image/" + jsonAdapter.getId() + "." + image.getFileExtension();
  }
}
