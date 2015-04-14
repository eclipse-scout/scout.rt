/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.browserfield.internal;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;

/**
 *
 */
public class SwingBrowserFieldPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField.class, org.eclipse.scout.rt.ui.swing.form.fields.browserfield.internal.SwingScoutBrowserField.class);
    }
  }

}
