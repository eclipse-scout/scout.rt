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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public interface ILabelComposite {
  Object getLayoutData();

  void setLayoutData(Object layoutData);

  boolean getEnabled();

  void setEnabled(boolean enabled);

  Color getForeground();

  void setForeground(Color color);

  void setLayoutWidthHint(int w);

  boolean setMandatory(boolean b);

  void setStatus(IProcessingStatus status);

  String getText();

  void setText(String text);

  boolean getVisible();

  void setVisible(boolean b);

  /**
   * Changes the visibility of the status label part. Typically, the visibility is set to <code>false</code> for the
   * first field used in a {@link ISequenceBox}, because rendered as part of the box label.
   */
  void setStatusVisible(boolean b);

  /**
   * Changes the grabbing behavior of this compound label. By default, grabbing is <code>enabled</code>. Typically,
   * grabbing is disabled for fields used within a {@link ISequenceBox}.
   */
  public void setGrabHorizontalEnabled(boolean enabled);

  Object getData(String key);

  void setData(String key, Object value);

  Object getBackground();

  void setBackground(Color c);

  Font getFont();

  void setFont(Font f);
}
