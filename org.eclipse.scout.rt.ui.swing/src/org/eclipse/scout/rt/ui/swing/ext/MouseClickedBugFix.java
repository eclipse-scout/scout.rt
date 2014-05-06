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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.JTree;

/**
 * bug fix for
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do;jsessionid=eaf146387466956f9693eedca1?bug_id=6578074">bug</a>
 * <p>
 * 
 * <pre>
 * From awt_Component.cpp :
 *   if (<b>!m_firstDragSent</b>) {
 *      SendMouseEvent(java_awt_event_MouseEvent_MOUSE_CLICKED,
 *          nowMillisUTC(), x, y, GetJavaModifiers(),
 *          clickCount, JNI_FALSE, GetButton(button));
 * <p>
 * Usage example:
 * new MouseAdapter(){
 *   MouseClickedBugFix fix;
 * 
 *   public void mousePressed(MouseEvent e){
 *     fix=new MouseClickedBugFix(e);
 *     ...
 *   }
 * 
 *   public void mouseReleased(MouseEvent e){
 *     try{
 *       ...
 *     }
 *     finally{
 *       if(fix!=null) fix.mouseReleased(this,e);
 *     }
 *   }
 * 
 *   public void mouseClicked(MouseEvent e){
 *     if(fix.mouseClicked()) return;
 *     ...
 *   }
 * }
 * </pre>
 */
public class MouseClickedBugFix {
  private Point m_pressedLocation;
  private boolean m_clickConsumed;

  public MouseClickedBugFix(MouseEvent e) {
    m_pressedLocation = e.getPoint();
  }

  public void mouseReleased(MouseListener listener, MouseEvent e) {
    Point p = e.getPoint();
    boolean generateClick = false;
    if (!m_clickConsumed) {
      if (!m_pressedLocation.equals(p)) {
        Component c = e.getComponent();
        if (c == null || c.getBounds().contains(p)) {
          if (c instanceof JTree || c instanceof JTable) {
            //only allow 8 pixel in order not to interfere with drag and drop
            if (Math.max(Math.abs(m_pressedLocation.x - p.x), Math.abs(m_pressedLocation.y - p.y)) <= 8) {
              generateClick = true;
            }
          }
          else {
            generateClick = true;
          }
        }
      }
    }
    if (generateClick) {
      listener.mouseClicked(e);
    }
  }

  /**
   * @return true if mouseClicked was already handled and can be returned immediately
   */
  public boolean mouseClicked() {
    if (m_clickConsumed) {
      return true;
    }
    else {
      m_clickConsumed = true;
      return false;
    }
  }
}
