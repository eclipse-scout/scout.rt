/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.ext;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public interface IDropDownFileUploadForPatch {

  /**
   * since tab list on parent does not work
   */
  boolean forceFocus();

  void fireSelectionEvent(SelectionEvent e);

  void addSelectionListener(SelectionListener listener);

  void removeSelectionListener(SelectionListener listener);

  void addFocusListener(FocusListener listener);

  void addMouseListener(MouseListener listener);

  Shell getShell();

  void setText(String text);

  String getText();

  void setImage(Image image);

  Image getImage();

  void setBackground(Color color);

  void setMenu(Menu menu);

  void setLayoutData(Object layoutData);

  void setData(String key, Object value);

  String getFileName();

  void submit(String url);

  Composite getParent();

  boolean isEnabled();

  boolean isVisible();

  void dispose();

  boolean isDisposed();

  void setDropdownEnabled(boolean enabled);

  boolean isDropdownEnabled();

  void setButtonEnabled(boolean enabled);

  boolean isButtonEnabled();

  void setEnabled(boolean enabled);

  Point computeSize(int hint, int hint2, boolean changed);

  void setBounds(Rectangle bounds);

  void setBounds(int x, int y, int width, int height);
}
