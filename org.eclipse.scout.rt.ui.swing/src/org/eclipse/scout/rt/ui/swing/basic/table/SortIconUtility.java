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
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;

/**
 *
 */
public class SortIconUtility {

  private static final float BRIGHTEN_FACTOR = 1.6f;
  private static final Icon m_sortUpIcon = Activator.getIcon(SwingIcons.TableSortAsc);
  private static final Icon m_sortDownIcon = Activator.getIcon(SwingIcons.TableSortDesc);

  private SortIconUtility() {
  }

  /**
   * Brightens icons of type {@link ImageIcon} by a factor
   * 
   * @param icon
   *          the ImageIcon to brighten
   * @param factor
   *          the factor to adjust brightness
   * @return the brightened icon, if icon is not instance of {@link ImageIcon}, the input icon is returned.
   */
  private static Icon changeBrightness(Icon icon, float factor) {
    if (icon == null || !(icon instanceof ImageIcon)) {
      return icon;
    }
    ImageIcon imageIcon = (ImageIcon) icon;

    ImageIcon ret = new ImageIcon();

    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    image.getGraphics().drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());

    RescaleOp op = new RescaleOp(factor, 0, null);
    image = op.filter(image, image);

    ret.setImage(image);
    return ret;
  }

  /**
   * Create a sort icon with gray scales according sort order
   * 
   * @param column
   *          the column the icon is intended for
   * @param sortColumns
   *          all sortco
   * @param ascending
   * @return
   */
  public static Icon createSortIcon(IColumn column, IColumn[] sortColumns, boolean ascending) {
    if (column == null || sortColumns == null) {
      return null;
    }

    float count = sortColumns.length;
    float factor = 1 + (column.getSortIndex() / count) * BRIGHTEN_FACTOR;

    Icon icon = m_sortDownIcon;
    if (ascending) {
      icon = m_sortUpIcon;
    }

    return changeBrightness(icon, factor);
  }

}
