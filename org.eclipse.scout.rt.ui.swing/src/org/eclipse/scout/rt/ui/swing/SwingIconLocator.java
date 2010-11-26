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
package org.eclipse.scout.rt.ui.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.shared.AbstractIcons;

/**
 * Looks for icons in the resources/icons folder of bundles
 */
public class SwingIconLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingIconLocator.class);

  private final Object m_cacheLock = new Object();
  private final HashMap<String, Image> m_imagesByNameCache = new HashMap<String, Image>();

  private final IIconLocator m_iconLocator;

  public SwingIconLocator(IIconLocator iconLocator) {
    m_iconLocator = iconLocator;
  }

  /**
   * Find icon in plugin dependency path starting with root bundle {@link Platform#getProduct#getDefiningBundle}
   */
  public Icon getIcon(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }
    Image img = getImage(name);
    if (img != null) {
      return new ImageIcon(img, name);
    }
    else {
      return null;
    }
  }

  public Image getImage(String name) {
    if (name.length() == 0) {
      return null;
    }
    Image img;
    synchronized (m_cacheLock) {
      img = m_imagesByNameCache.get(name);
      if (img == null && !m_imagesByNameCache.containsKey(name)) {
        img = createImageImpl(name);
        m_imagesByNameCache.put(name, img);
        if (LOG.isDebugEnabled()) LOG.debug("load image '" + name + "' as " + img);
        if (img == null) {
          warnImageNotFound(name);
        }
      }
    }
    return img;
  }

  protected void warnImageNotFound(String name) {
    if (name.equals("window")) {
      //optional image, maybe the new style window16, window256 etc were specified
      return;
    }
    if (name.matches(".*(_active|_disabled|_mouse|_open|_over|_pressed|_rollover|_selected)")) {
      //optional "sub" images
      return;
    }
    LOG.warn("could not find image '" + name + "'");
  }

  private Image createImageImpl(String name) {
    IconSpec iconSpec = m_iconLocator.getIconSpec(name);
    if (iconSpec != null) {
      Image img = Toolkit.getDefaultToolkit().createImage(iconSpec.getContent());
      if (img != null) {
        //decorate window icon in development mode
        if (Platform.inDevelopmentMode() && name != null && name.matches("^(window\\d+|tray)$")) {
          img = decorateForDevelopment(img);
        }
      }
      return img;
    }
    return null;
  }

  protected Image decorateForDevelopment(Image img) {
    if (img == null) return img;
    try {
      ImageIcon icon = new ImageIcon(img);
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();
      BufferedImage devImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics g = devImg.getGraphics();
      icon.paintIcon(null, g, 0, 0);
      // Convert icon to gray scale
      ColorConvertOp grayOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      grayOp.filter(devImg, devImg);
      g.dispose();
      return devImg;
    }
    catch (Throwable t) {
      return img;
    }
  }

}
