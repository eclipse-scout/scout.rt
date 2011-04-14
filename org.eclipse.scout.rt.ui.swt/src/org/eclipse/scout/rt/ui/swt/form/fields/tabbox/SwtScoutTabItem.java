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
package org.eclipse.scout.rt.ui.swt.form.fields.tabbox;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.swt.form.fields.groupbox.SwtScoutGroupBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>SwtScoutTabItem</h3> ...
 * 
 * @since 1.0.0 09.04.2008
 */
public class SwtScoutTabItem extends SwtScoutGroupBox implements ISwtScoutTabItem {
  private CTabItem m_tabItem;
  private Image m_tabImage;
  private boolean m_uiFocus;

  @Override
  protected void initializeSwt(Composite parent) {
    CTabFolder folder = (CTabFolder) parent;

    folder.setLayout(new FillLayout());
    m_tabItem = new CTabItem(folder, SWT.NONE);
    m_tabItem.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });

    super.initializeSwt(folder);
    m_tabItem.setControl(getSwtContainer());
    updateImage();
  }

  protected void freeResources() {
    if (m_tabImage != null && !m_tabImage.isDisposed()) {
      m_tabImage.dispose();
      m_tabImage = null;
    }
  }

  protected CTabItem getTabItem() {
    return m_tabItem;
  }

  @Override
  protected void setSaveNeededFromScout(boolean b) {
    updateImage();
  }

  protected void setEmptyFromScout(boolean b) {
    updateImage();
  }

  @Override
  protected void setLabelFromScout(String s) {
    updateImage();
  }

  @Override
  protected void setFontFromScout(FontSpec scoutFont) {
    super.setFontFromScout(scoutFont);
    m_tabItem.setFont(getEnvironment().getFont(scoutFont, m_tabItem.getFont()));
  }

  public void setUiFocus(boolean b) {
    m_uiFocus = b;
    if (!isDisposed()) {
      updateImage();
    }
  }

  private void updateImage() {
    GC gc = null;
    Point imageSize = new Point(15, 100);
    String label = getScoutObject().getLabel();
    if (label == null) {
      label = "";
    }
    try {
      gc = new GC(getSwtContainer());
      gc.setFont(getEnvironment().getFont(getScoutObject().getFont(), getTabItem().getFont()));
      Point stringSize = gc.stringExtent(label);
      imageSize.x = stringSize.x + 10;
      imageSize.y = stringSize.y + 3;
    }
    finally {
      if (gc != null) {
        gc.dispose();
      }
    }
    Image img = new Image(getTabItem().getDisplay(), imageSize.x, imageSize.y);
    ImageData data = img.getImageData();
    data.transparentPixel = data.palette.getPixel(new RGB(255, 255, 255));
    try {
      gc = new GC(img);
      gc.setAdvanced(true);
      gc.setBackground(getSwtContainer().getDisplay().getSystemColor(SWT.COLOR_WHITE));
      gc.fillRectangle(0, 0, imageSize.x, imageSize.y);
      // focus handling
      if (m_uiFocus) {
        gc.setBackground(getSwtContainer().getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        gc.setLineDash(new int[]{2, 1});
        gc.drawRoundRectangle(0, 0, imageSize.x - 1, imageSize.y - 1, 2, 2);
      }
      // underline
      boolean isUnderline = false;
      if (getScoutObject().getForm() instanceof ISearchForm && getScoutObject().isSaveNeeded()) {
        isUnderline = true;
      }
      else if (!(getScoutObject().getForm() instanceof ISearchForm) && !getScoutObject().isEmpty()) {
        isUnderline = true;
      }
      if (isUnderline) {
        gc.setForeground(getSwtContainer().getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.setBackground(getSwtContainer().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
        gc.fillGradientRectangle(4, imageSize.y - 5, imageSize.x - 8, imageSize.y, true);
      }
      // text
      gc.setFont(getEnvironment().getFont(getScoutObject().getFont(), getTabItem().getFont()));
      gc.setForeground(getSwtContainer().getDisplay().getSystemColor(SWT.COLOR_BLACK));
      gc.drawText(label, 5, 0, true);

    }
    finally {
      if (gc != null && !gc.isDisposed()) {
        gc.dispose();
      }
    }
    getTabItem().setImage(img);
    if (m_tabImage != null && !m_tabImage.isDisposed()) {
      m_tabImage.dispose();
    }
    m_tabImage = img;
  }
}
