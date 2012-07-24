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
package org.eclipse.scout.rt.ui.rap.mobile;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class MobileFormToolkit extends FormToolkit {

  public MobileFormToolkit(Display display) {
    super(display);
  }

  /**
   * The default method in FormToolkit registers a mouse down listener on the composite which sets the focus on the
   * first field. This is annoying on touchscrens therefore it's removed.
   */
  @Override
  public void adapt(Composite composite) {
    composite.setBackground(getColors().getBackground());
    if (composite.getParent() != null) {
      composite.setMenu(composite.getParent().getMenu());
    }
  }

}
