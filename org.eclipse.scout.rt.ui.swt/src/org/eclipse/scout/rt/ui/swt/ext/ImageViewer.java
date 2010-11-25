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
package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ImageViewer extends Canvas {

  private int m_xAglin = SWT.CENTER;
  private int m_yAglin = SWT.CENTER;
  private boolean m_autoFit = false;

  private Image m_image;
  private Image m_scaledImage;

  public ImageViewer(Composite parent) {
    super(parent, SWT.NONE);
    addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        handleSwtPaintEvent(e.gc);
      }
    });
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
  }

  private void freeResources() {
    if (m_scaledImage != null && !m_scaledImage.isDisposed()) {
      m_scaledImage.dispose();
      m_scaledImage = null;
    }
  }

  private Image scaleImage(Image img) {
    freeResources();
    if (m_autoFit && img != null) {
      Point swtFieldSize = getSize();
      Rectangle imageBounds = img.getBounds();
      double scaleFactor = (double) swtFieldSize.x / (double) imageBounds.width;
      scaleFactor = Math.min(scaleFactor, (double) swtFieldSize.y / (double) img.getBounds().height);
      m_scaledImage = new Image(getDisplay(), img.getImageData().scaledTo((int) (scaleFactor * img.getBounds().width), (int) (scaleFactor * img.getBounds().height)));
      return m_scaledImage;
    }
    else {
      return img;
    }
  }

  protected void handleSwtPaintEvent(GC gc) {
    Image img = getImage();
    Rectangle bounds = gc.getClipping();
    if (img != null) {
      // scale img
      if (isAutoFit()) {
        img = scaleImage(img);
      }
      Rectangle imgBounds = img.getBounds();
      int x = 0;
      if (imgBounds.width <= bounds.width) {
        switch (getAlignmentX()) {
          case SWT.CENTER:
            x = (bounds.width - imgBounds.width) / 2;
            break;
          case SWT.RIGHT:
            x = bounds.width - imgBounds.width;
            break;
          default:
            x = 0;
            break;
        }
      }
      int y = 0;
      if (imgBounds.height <= bounds.height) {
        switch (getAlignmentY()) {
          case SWT.CENTER:
            y = (bounds.height - imgBounds.height) / 2;
            break;
          case SWT.RIGHT:
            y = bounds.height - imgBounds.height;
            break;
          default:
            y = 0;
            break;
        }
      }
      // draw
      gc.drawImage(img, x, y);
    }
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    Point size = super.computeSize(hint, hint2, changed);
    if (getImage() != null) {
      Rectangle imgBounds = getImage().getBounds();
      size.x = imgBounds.width;
      size.y = imgBounds.height;
    }
    return size;
  }

  public void setAlignmentX(int alignment) {
    m_xAglin = alignment;
  }

  public int getAlignmentX() {
    return m_xAglin;
  }

  public void setAlignmentY(int alignment) {
    m_yAglin = alignment;
  }

  public int getAlignmentY() {
    return m_yAglin;
  }

  public boolean isAutoFit() {
    return m_autoFit;
  }

  public void setAutoFit(boolean autoFit) {
    m_autoFit = autoFit;
  }

  public void setImage(Image img) {
    m_image = img;
  }

  public Image getImage() {
    return m_image;
  }

}
