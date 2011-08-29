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
package org.eclipse.scout.rt.ui.swing.form.fields.imagebox.imageviewer;

/**
 * , Samuel Moser
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;

public class SwingImageViewer extends JComponent {
  public static final float EPS = 1E-6f;
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingImageViewer.class);

  private EventListenerList m_eventListenerList;

  private Image m_origImg;

  private double m_dx = 0;

  private double m_dy = 0;

  private double m_sx = 1;

  private double m_sy = 1;

  private double m_angle = 0;

  private Rectangle m_analysisRect;

  private Rectangle m_selectionRect;

  private boolean m_autoFit = false;

  private boolean m_focusVisible = false;

  private boolean m_insideValidateTreeProcess;

  public SwingImageViewer() {
    super();
    m_eventListenerList = new EventListenerList();
    setFocusable(true);
    addMouseListener(new MouseAdapter() {
      MouseClickedBugFix fix;

      @Override
      public void mousePressed(MouseEvent e) {
        fix = new MouseClickedBugFix(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (fix != null) {
          if(fix!=null) {
            fix.mouseReleased(this, e);
          }
        }
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (fix.mouseClicked()) {
          return;
        }
        requestFocus();
      }
    });
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        repaint();
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
        repaint();
      }
    });
  }

  public void revalidateAndRepaint() {
    if (!m_insideValidateTreeProcess) {
      revalidate();
      repaint();
    }
  }

  public void setAutoFit(boolean b) {
    m_autoFit = b;
    if (isAutoFit()) {
      doFitImage();
    }
  }

  public void doZoomRectangle(Rectangle rect) {
    m_selectionRect = (Rectangle) rect.clone();
    doFitImage();
  }

  public boolean isAutoFit() {
    return m_autoFit;
  }

  public void setFocusVisible(boolean b) {
    m_focusVisible = b;
    repaint();

  }

  public boolean isFocusVisible() {
    return m_focusVisible;
  }

  /**
   * This method calculates the current active transformation
   */
  private AffineTransform calculateCurrentTransformation() {
    AffineTransform af = new AffineTransform();
    af.translate(m_dx, m_dy);

    af.scale(m_sx, m_sy);

    if (m_selectionRect != null) {
      // rotate to the center of the selection
      af.translate(m_selectionRect.getCenterX(), m_selectionRect.getCenterY());

      af.rotate(m_angle);
      af.translate(-m_selectionRect.getCenterX(), -m_selectionRect.getCenterY());

    }
    else {
      // rotate to the center of image
      af.translate(m_origImg.getWidth(this) / 2, m_origImg.getHeight(this) / 2);

      af.rotate(m_angle);
      af.translate(-m_origImg.getWidth(this) / 2, -m_origImg.getHeight(this) / 2);

    }

    return af;
  }

  public void setImage(Object imgData) {
    Image img = null;
    if (imgData instanceof Image) {
      img = (Image) imgData;
    }
    else if (imgData instanceof byte[]) {
      byte[] b = (byte[]) imgData;
      if (b != null) {
        img = Toolkit.getDefaultToolkit().createImage(b);
      }
    }
    if (img != null) {
      // load image
      new ImageIcon(img).getIconWidth();
    }
    m_origImg = img;
    if (isAutoFit()) {
      doFitImage();
    }
    revalidateAndRepaint();
  }

  private void doZoomToSelection(Rectangle selectionRect) {

    int selectionWidth = (int) selectionRect.getWidth();
    int selectionHeight = (int) selectionRect.getHeight();

    Dimension frame = getSize();

    // calculate the scaling factor to fit one dimension of the selection to the
    // frame
    double wfactor = Math.max(0.001, 1.0 * frame.width / selectionWidth); // width
    // limiting
    double hfactor = Math.max(0.001, 1.0 * frame.height / selectionHeight); // height
    // limiting

    // choose min scale factor (only one factor for x ans y axle (aspect ratio))
    double fitScale = Math.min(hfactor, wfactor);

    // we have the scaling factor, create a new Transform with it! (remark: x
    // and y have the same scale factor - aspect ratio)
    AffineTransform newAT = AffineTransform.getScaleInstance(fitScale, fitScale);

    // transform the selection to the new scale
    Rectangle txSelectionRect = newAT.createTransformedShape(selectionRect).getBounds();

    // from now on we working in the transformed space

    // searching for the right translation

    int translateX;
    int translateY;

    // each alignment -> other shifting params
    if (getAlignmentX() == 0f) {
      translateX = -txSelectionRect.x;
    }
    else if (getAlignmentX() == 0.5f) {
      // calculate the center
      translateX = -txSelectionRect.x + (Math.max(0, frame.width - txSelectionRect.width) / 2);

    }
    else {
      // >0
      translateX = -txSelectionRect.x + Math.max(0, frame.width - txSelectionRect.width);
    }
    if (getAlignmentY() == 0f) {
      translateY = -txSelectionRect.y;
    }
    else if (getAlignmentY() == 0.5f) {
      // calculate the center
      translateY = -txSelectionRect.y + (Math.max(0, frame.height - txSelectionRect.height) / 2);
    }
    else {
      // >0
      translateY = -txSelectionRect.y + Math.max(0, frame.height - txSelectionRect.height);
    }

    m_dx = translateX;
    m_dy = translateY;
    m_sx = fitScale;
    m_sy = fitScale;

    revalidateAndRepaint();
    fireImageTransformChange();
  }

  public void doZoomToSelection(int x, int y, int width, int heigth) {
    doZoomToSelection(new Rectangle(x, y, width, heigth));
  }

  public void doFitImage() {
    if (m_origImg != null) {
      if (m_selectionRect != null) {
        doZoomToSelection(m_selectionRect);
      }
      else {
        doZoomToSelection(0, 0, m_origImg.getWidth(this), m_origImg.getHeight(this));
      }
    }
  }

  public void setAnalysisRect(Rectangle rect) {
    m_analysisRect = rect;
    revalidateAndRepaint();
  }

  public void setImageTransform(double dx, double dy, double sx, double sy, double angle) {
    m_dx = dx;
    m_dy = dy;
    m_sx = sx;
    m_sy = sy;
    m_angle = angle;
    revalidateAndRepaint();
  }

  @Override
  @SuppressWarnings("deprecation")
  public void reshape(int x, int y, int w, int h) {
    boolean changed = (getWidth() != w || getHeight() != h);
    super.reshape(x, y, w, h);
    if (changed && (isAutoFit() || (m_selectionRect != null))) {
      try {
        m_insideValidateTreeProcess = true;
        //
        doFitImage();
      }
      finally {
        m_insideValidateTreeProcess = false;
      }
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = new Dimension(0, 0);
    Image img = m_origImg;
    if (img != null) {
      d.width = Math.max(d.width, img.getWidth(this));
      d.height = Math.max(d.height, img.getHeight(this));
    }
    return d;
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension d;
    if (isAutoFit()) {
      d = new Dimension(0, 0);
    }
    else {
      d = getPreferredSize();
    }
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension d;
    Image img = m_origImg;
    if (img != null) {
      d = new Dimension(0, 0);
      d.width = Math.max(d.width, img.getWidth(this));
      d.height = Math.max(d.height, img.getHeight(this));
    }
    else {
      d = getPreferredSize();
    }
    return d;
  }

  @Override
  public void paintComponent(Graphics g) {
    if (isOpaque()) { // paint background
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    if (m_origImg != null) {
      Graphics2D g2 = (Graphics2D) g;
      AffineTransform oldAt = g2.getTransform();
      Color oldColor = g2.getColor();
      try {
        AffineTransform af = calculateCurrentTransformation();
        g2.transform(af);
        int x = 0;
        int y = 0;
        // XXX implement layout with autofit
        if (!isAutoFit()) {
          Rectangle clipingBounds = getBounds();
          int imgW = m_origImg.getWidth(this);
          int imgH = m_origImg.getHeight(this);
          if (imgW >= clipingBounds.width) {
            x = 0;
          }
          else if (Math.abs(getAlignmentX() - 0.5) < EPS) {
            x = (clipingBounds.width - imgW) / 2;
          }
          else if (getAlignmentX() > 0.5) {
            x = (clipingBounds.width - imgW);
          }
          else {
            x = 0;
          }
          if (imgH >= clipingBounds.height) {
            // top
            y = 0;
          }
          else if (Math.abs(getAlignmentY() - 0.5) < EPS) {
            y = (clipingBounds.height - imgH) / 2;
          }
          else if (getAlignmentY() > 0.5) {
            y = (clipingBounds.height - imgH);
          }
          else {
            y = 0;
          }
        }
        g2.drawImage(m_origImg, x, y, this);
        if (m_analysisRect != null) {
          g.setColor(getForeground());
          g2.drawRect(m_analysisRect.x, m_analysisRect.y, m_analysisRect.width, m_analysisRect.height);
        }
      }
      finally {
        g2.setTransform(oldAt);
        g2.setColor(oldColor);
      }
      // guide lines
      /*
       * XXX if(hasFocus()&&m_focusVisible){ int w=d.width; int h=d.height;
       * g.setColor(Color.lightGray); g2.drawLine(0, h/2, w, h/2);
       * g2.drawLine(w/2, 0, w/2, h); }
       */
    }
  }

  /**
   * private classes for key actions
   */

  private class P_SwingFit extends AbstractAction {

    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      doFitImage();
    }
  }// end private class

  /**
   * Image Transformation Observer
   */
  public void addImageTransformListener(ImageTransformListener listener) {
    m_eventListenerList.add(ImageTransformListener.class, listener);
  }

  public void removeImageTransformListener(ImageTransformListener listener) {
    m_eventListenerList.remove(ImageTransformListener.class, listener);
  }

  @Override
  public String toString() {

    StringBuffer buf = new StringBuffer();
    buf.append("dx = " + m_dx);
    buf.append(" dy = " + m_dy);
    buf.append(" sx = " + m_sx);
    buf.append(" sy = " + m_sy);
    buf.append(" angle = " + m_angle);
    if (m_origImg != null) {
      buf.append(" center = " + m_origImg.getWidth(this) / 2 + " " + m_origImg.getHeight(this) / 2);
    }

    return buf.toString();
  }

  protected void fireImageTransformChange() {
    fireImageTransformChange(m_dx, m_dy, m_sx, m_sy, m_angle);
  }

  /**
   * @param dx
   *          Translation x direction
   * @param dy
   *          Translation y direction
   * @param sx
   *          Scale x
   * @param sy
   *          Scale y
   * @param angle
   *          Rotation in Radian
   */
  protected void fireImageTransformChange(double dx, double dy, double sx, double sy, double angle) {
    EventListener[] eventlisters = m_eventListenerList.getListeners(ImageTransformListener.class);
    if (eventlisters != null && eventlisters.length > 0) {
      ImageTransformEvent event = new ImageTransformEvent(this, dx, dy, sx, sy, angle);
      for (int i = 0; i < eventlisters.length; i++) {
        ((ImageTransformListener) eventlisters[i]).transformChanged(event);
      }
    }
  }

}
