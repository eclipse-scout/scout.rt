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
package org.eclipse.scout.rt.ui.swing.basic;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public final class IconUtility {

  private IconUtility() {
  }

  /**
   * Add a color layer to the icon (not including alpha)
   */
  public static ImageIcon blendIcon(Icon icon, float iconWeight, int rgb, float rgbWeight) {
    if (icon == null || !(icon instanceof ImageIcon)) {
      return null;
    }
    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(((ImageIcon) icon).getImage(), 0, 0, null);
    g.dispose();
    float wsum = iconWeight + rgbWeight;
    if (wsum > 0) {
      iconWeight = iconWeight / wsum;
      rgbWeight = rgbWeight / wsum;
    }
    int rmask = (rgb >> 16) & 0xff;
    int gmask = (rgb >> 8) & 0xff;
    int bmask = rgb & 0xff;
    int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    for (int i = 0; i < pixels.length; i++) {
      int c = pixels[i];
      pixels[i] = (c & 0xff000000) | (mergeComponent((c >> 16) & 0xff, iconWeight, rmask, rgbWeight) << 16) | (mergeComponent((c >> 8) & 0xff, iconWeight, gmask, rgbWeight) << 8) | (mergeComponent(c & 0xff, iconWeight, bmask, rgbWeight));
    }
    image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
    return new ImageIcon(image);
  }

  private static int mergeComponent(int a, float wa, int b, float wb) {
    int c = (int) (wa * a + wb * b);
    if (c >= 0 && c <= 255) {
      return c;
    }
    if (c < 0) {
      return 0;
    }
    if (c > 255) {
      return 255;
    }
    return c;
  }

  /**
   * Make grascale using a {@link ColorConvertOp}
   */
  public static ImageIcon grayIcon(Icon icon) {
    if (icon == null || !(icon instanceof ImageIcon)) {
      return null;
    }
    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    g.drawImage(((ImageIcon) icon).getImage(), 0, 0, null);
    g.dispose();
    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    return new ImageIcon(op.filter(image, image));
  }

}
