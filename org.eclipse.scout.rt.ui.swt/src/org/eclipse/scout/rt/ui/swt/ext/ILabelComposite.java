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

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.swt.graphics.Color;

/**
 * <h3>ILabelComposite</h3> ...
 * 
 * @since 1.0.0 15.12.2009
 */
public interface ILabelComposite {
  Object getLayoutData();

  void setLayoutData(Object layoutData);

  boolean getEnabled();

  void setEnabled(boolean enabled);

  Color getForeground();

  void setForeground(Color color);

  void setLayoutWidthHint(int w);

  boolean setMandadatory(boolean b);

  void setStatus(IProcessingStatus status);

  String getText();

  void setText(String text);

  boolean getVisible();

  void setVisible(boolean b);
}
