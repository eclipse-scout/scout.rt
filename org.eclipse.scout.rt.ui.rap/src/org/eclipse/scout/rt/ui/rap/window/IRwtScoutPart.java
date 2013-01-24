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
package org.eclipse.scout.rt.ui.rap.window;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;

public interface IRwtScoutPart {

  IForm getScoutObject();

  /**
   * @return the eclipse form inside the {@link #getUiContainer()}, may be null
   */
  Form getUiForm();

  /**
   * @return the top-level container
   */
  Composite getUiContainer();

  void showPart();

  void closePart();

  boolean isVisible();

  boolean isActive();

  void activate();

  boolean setStatusLineMessage(Image image, String message);

  /**
   * Mark the part as busy waiting
   */
  void setBusy(boolean b);
}
