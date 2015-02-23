/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.action.menu;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The context menu of a control is located at the mouse position. Now when the mouse is not on the control even worse
 * on a other screen and the user triggers the context menu over the keyboard. The context menu gets showed on the
 * second screen.
 * This listener is to ensure the context menu gets always located on/over the owner control. If the mouse is over the
 * control the menu will be shown at the mouse position.
 * <p>
 *
 * @see(<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=431030">BUG 431030</a>)
 */
public class MenuPositionCorrectionListener implements Listener {

  public final static int HORIZONTAL_LEFT = 1;
  public final static int HORIZONTAL_CENTER = 1 << 1;
  public final static int HORIZONTAL_RIGHT = 1 << 2;
  public final static int VERTICAL_TOP = 1 << 3;
  public final static int VERTICAL_CENTER = 1 << 4;
  public final static int VERTICAL_BOTTOM = 1 << 5;

  private final Control m_owner;
  private final int m_position;

  public MenuPositionCorrectionListener(Control owner) {
    this(owner, HORIZONTAL_LEFT | VERTICAL_BOTTOM);
  }

  /**
   * @param owner
   * @param position
   *          a bitmask of the position. The position is always the top-left corner of the menu.
   */
  public MenuPositionCorrectionListener(Control owner, int position) {
    m_owner = owner;
    m_position = position;
  }

  @Override
  public void handleEvent(Event event) {
    Rectangle ownerBounds = getOwner().getBounds();
    if (!ownerBounds.contains(getOwner().getParent().toControl(event.x, event.y))) {
      // horizontal
      Point newLocation = new Point(0, 0);
      if ((getPosition() & HORIZONTAL_RIGHT) != 0) {
        newLocation.x = ownerBounds.x + ownerBounds.width;
      }
      else if ((getPosition() & HORIZONTAL_CENTER) != 0) {
        newLocation.x = ownerBounds.x + (ownerBounds.width / 2);
      }
      else {
        newLocation.x = ownerBounds.x;
      }
      // vertical
      if ((getPosition() & VERTICAL_TOP) != 0) {
        newLocation.y = ownerBounds.y;
      }
      else if ((getPosition() & VERTICAL_CENTER) != 0) {
        newLocation.y = ownerBounds.y + (ownerBounds.height / 2);
      }
      else {
        newLocation.y = ownerBounds.y + ownerBounds.height;
      }
      newLocation = getOwner().toDisplay(newLocation);
      event.x = newLocation.x;
      event.y = newLocation.y;
    }
    else {
      // Do a one pixel move to ensure the position of the event getting changed.
      // In case of no change the event provider does not update the menu location.
      event.x += 1;
    }
  }

  public Control getOwner() {
    return m_owner;
  }

  public int getPosition() {
    return m_position;
  }
}
