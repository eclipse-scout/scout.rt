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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import org.eclipse.scout.commons.CompareUtility;

/**
 * analog clock with mouse-over and pick facility
 * 
 * @author imo
 */
public class JClock extends JComponent {
  private static final long serialVersionUID = 1L;

  private final boolean m_h24Mode;
  private boolean m_am;
  private int m_time;
  /**
   * mark at every quarter hour 0...12*4
   */
  private int m_higlightedMark = -1;
  /**
   * minutes in range 0...24*60
   */
  private Integer m_temporaryTime;

  public JClock(boolean h24Mode) {
    m_h24Mode = h24Mode;
    Dimension d = new Dimension(180, 180);
    setMinimumSize(d);
    setPreferredSize(d);
    setMaximumSize(d);
    setForeground(new Color(0x67A8CE));
    setOpaque(false);
    //TODO [awe] add to synth L&F config xml
    setName("Synth.Clock");
  }

  public boolean isAM() {
    return m_am;
  }

  public void setAM(boolean am) {
    m_am = am;
  }

  /**
   * @return time in minutes in the range 0...24*60
   */
  public int getTime() {
    return m_time;
  }

  /**
   * valid range is 0...24*60
   */
  public void setTime(int time) {
    time = (time / 15) * 15;
    if (m_time == time) {
      return;
    }
    m_time = time;
    repaint();
  }

  public Integer getTimeAt(Point p) {
    int w = getWidth();
    int h = getHeight();
    int x = p.x - w / 2;
    int y = p.y - h / 2;
    //
    int minutes = (int) ((Math.atan2(x, -y) + 2.0 * Math.PI) * 12.0 * 60.0 / Math.PI / 2.0);
    minutes = minutes % (12 * 60);
    if (!isAM()) minutes = minutes + 12 * 60;
    //snap
    minutes = ((minutes + 7) / 15) * 15;
    return minutes;
  }

  public void setTemporaryTime(Integer temporaryTime) {
    temporaryTime = temporaryTime != null ? (temporaryTime / 15) * 15 : null;
    if (CompareUtility.equals(m_temporaryTime, temporaryTime)) {
      return;
    }
    m_temporaryTime = temporaryTime;
    m_higlightedMark = -1;
    if (m_temporaryTime != null) {
      m_higlightedMark = (m_temporaryTime.intValue() % (12 * 60)) / 15;
    }
    else {
      m_higlightedMark = -1;
    }
    repaint();
  }

  public Integer getTemporaryTime() {
    return m_temporaryTime;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Graphics2D sub = (Graphics2D) g2d.create();
    paintMarks(sub, getForeground(), getForeground());
    sub.dispose();
    //
    sub = (Graphics2D) g2d.create();
    paintLabels(sub, Color.black);
    sub.dispose();
    //
    int t = getTime();
    sub = (Graphics2D) g2d.create();
    paintHands(sub, getForeground(), getForeground(), t);
    sub.dispose();
  }

  protected void paintMarks(Graphics2D g, Color col, Color highlightedCol) {
    int w = getWidth();
    int h = getHeight();
    g.translate(w / 2, h / 2);
    for (int i = 0; i < 48; i++) {
      if (i == m_higlightedMark) {
        paintHighlightedMark(g, w, h, i, highlightedCol);
      }
      else {
        paintMark(g, w, h, i, col);
      }
      //
      g.rotate(Math.PI * 2.0 / 48.0);
    }
  }

  protected void paintMark(Graphics2D g, int w, int h, int i, Color col) {
    g.setColor(col);
    if (i % 4 == 0) {
      g.fillRect(0, -h / 2, 1, 5);
    }
    else {
      g.fillOval(-1, -h / 2 + 3, 2, 2);
    }
  }

  protected void paintHighlightedMark(Graphics2D g, int w, int h, int i, Color col) {
    g.setColor(col);
    if (i % 4 == 0) {
      g.fillRect(-3, -h / 2 - 2, 6, 9);
    }
    else {
      g.fillOval(-4, -h / 2 - 1, 8, 8);
    }
  }

  protected void paintLabels(Graphics2D g, Color col) {
    int w = getWidth();
    int h = getHeight();
    g.setColor(col);
    g.setFont(Font.decode("Arial-BOLD-10"));
    FontMetrics fm = g.getFontMetrics();
    g.translate(w / 2, h / 2);
    double ex = 0;
    double ey = -h / 2 + 8 + fm.getAscent() / 2;
    double phi = Math.PI * 2.0 / 12.0;
    for (int i = 0; i < 12; i++) {
      String s = "" + (m_am || !m_h24Mode ? i : i + 12);
      int slen = fm.stringWidth(s);
      g.drawString(s, (int) ex - slen / 2, (int) ey + fm.getAscent() / 2 - 1);
      //rotate
      double tmp = ex * Math.cos(phi) - ey * Math.sin(phi);
      ey = ex * Math.sin(phi) + ey * Math.cos(phi);
      ex = tmp;
    }
  }

  protected void paintHands(Graphics2D g, Color hourCol, Color minuteCol, int t) {
    int w = getWidth();
    int h = getHeight();
    g.translate(w / 2, h / 2);
    //minute hand
    if (minuteCol != null) {
      g.setPaint(minuteCol);
      double beta = Math.PI * 2.0 * t / 60.0;
      g.rotate(beta);
      int ascent = 8 + 12 + 2;
      int descent = 6;
      g.fillRect(-1, -h / 2 + ascent, 2, h / 2 - ascent + descent);
      g.rotate(-beta);
    }
    //hour hand
    if (hourCol != null) {
      g.setPaint(hourCol);
      double alpha = Math.PI * 2.0 * t / 60.0 / 12.0;
      g.rotate(alpha);
      int ascent = 8 + 28 + 2;
      int descent = 6;
      g.fillRect(-2, -h / 2 + ascent, 4, h / 2 - ascent + descent);
    }
  }

  private static final int SOFT_REPAINT_ALPHA_DELTA = 8;
  private static final int SOFT_REPAINT_INTERVAL = 24;

}
