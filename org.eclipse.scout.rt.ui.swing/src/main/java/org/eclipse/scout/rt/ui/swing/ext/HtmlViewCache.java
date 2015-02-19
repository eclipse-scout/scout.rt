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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Color;
import java.util.HashMap;
import java.util.zip.Adler32;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

/**
 * Since html rendering is very expensive in swing and tables render views every time they are repainted, this cache
 * supports for re-using html views based on the crc of a string.
 * <p>
 * To use on JLabels you must perform the following actions:
 * <ul>
 * <li>Disable automatic html on the field: label.putClientProperty("html.disable",true);</li>
 * <li>Whenever {@link JLabel#setText(String)} is called, call {@link #updateHtmlView(JLabel)} just after it.</li>
 * </ul>
 */
public class HtmlViewCache {
  private HashMap<Object, View> m_viewMap;

  public HtmlViewCache() {
    m_viewMap = new HashMap<Object, View>();
  }

  /**
   * update html view using the cache
   */
  public void updateHtmlView(JLabel label, boolean customForeground) {
    String text = label.getText();
    View value = null;
    if (BasicHTML.isHTMLString(text)) {
      if (!customForeground) {
        label.setForeground(label.isEnabled() ? UIManager.getDefaults().getColor("TextField.foreground") : UIManager.getDefaults().getColor("TextField.inactiveForeground"));
      }

      Adler32 crc = new Adler32();
      crc.update(text.getBytes());
      Color fg = label.getForeground();
      Object key = "" + crc.getValue() + "." + label.getFont() + "." + (fg != null ? fg.getRGB() : 0);
      value = m_viewMap.get(key);
      if (value == null) {
        value = BasicHTML.createHTMLView(label, text);
        if (value != null) {
          m_viewMap.put(key, value);
        }
      }
    }
    label.putClientProperty(BasicHTML.propertyKey, value);
  }
}
