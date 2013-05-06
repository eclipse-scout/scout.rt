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
package org.eclipse.scout.rt.ui.swt.util;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IconSpec;
import org.eclipse.scout.rt.client.ui.IIconLocator;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Looks for icons in the resources/icons folder of bundles
 */
public class SwtIconLocator implements ISwtIconLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwtIconLocator.class);

  private final ImageRegistry imageRegistry;
  private final Set<String> m_missingImages = new HashSet<String>();

  private final IIconLocator m_iconLocator;

  public SwtIconLocator(IIconLocator iconLocator) {
    m_iconLocator = iconLocator;
    imageRegistry = new ImageRegistry();
    imageRegistry.put("close_a_16", getFalse());
  }

  @Override
  public ImageDescriptor getImageDescriptor(String name) {
    if (name == null) {
      return null;
    }

    if (m_missingImages.contains(name)) {
      return null;
    }
    ImageDescriptor desc = imageRegistry.getDescriptor(name);
    if (desc == null) {
      desc = createImageDescriptor(name);
      if (desc == null) {
        m_missingImages.add(name);
      }
      else {
        imageRegistry.put(name, desc);
      }
    }
    return desc;

  }

  /**
   * Find icon in plugin dependency path starting with root bundle {@link Platform#getProduct#getDefiningBundle}
   */
  @Override
  public Image getIcon(String name) {
    if (name == null || AbstractIcons.Null.equals(name)) {
      return null;
    }

    if (m_missingImages.contains(name)) {
      return null;
    }
    Image image = imageRegistry.get(name);
    if (image == null) {
      ImageDescriptor desc = createImageDescriptor(name);
      if (desc != null) {
        imageRegistry.put(name, desc);
        image = imageRegistry.get(name);
        if (LOG.isDebugEnabled()) {
          LOG.debug("image found '" + name + "'.");
        }
      }
      else {
        LOG.warn("image '" + name + "' could not be found!");
        m_missingImages.add(name);
      }
    }
    return image;
  }

  protected ImageDescriptor createImageDescriptor(String name) {
    ImageDescriptor desc = null;
    IconSpec spec = m_iconLocator.getIconSpec(name);
    if (spec != null) {
      desc = ImageDescriptor.createFromImageData(new ImageData(new ByteArrayInputStream(spec.getContent())));
      if (LOG.isDebugEnabled()) {
        LOG.debug("image found '" + name + "'.");
      }
    }
    else {
      LOG.warn("image '" + name + "' could not be found!");
      m_missingImages.add(name);
    }
    return desc;
  }

  @Override
  public void dispose() {
    imageRegistry.dispose();
  }

  private Image getFalse() {
    Display display = PlatformUI.getWorkbench().getDisplay();
    Image img = new Image(display, 16, 16);
    GC gc = null;
    try {
      gc = new GC(img);
      gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
      gc.drawRectangle(2, 2, 12, 12);
    }
    finally {
      if (gc != null) {
        gc.dispose();
      }
    }
    return img;
  }

}
