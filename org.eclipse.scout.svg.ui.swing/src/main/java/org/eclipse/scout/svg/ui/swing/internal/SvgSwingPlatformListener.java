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
package org.eclipse.scout.svg.ui.swing.internal;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.PlatformException;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;

/**
 *
 */
public class SvgSwingPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) throws PlatformException {
    if (event.getState() == IPlatform.State.BeanManagerPrepared) {
      FormFieldExtensions.INSTANCE.put(org.eclipse.scout.svg.client.svgfield.ISvgField.class, org.eclipse.scout.svg.ui.swing.svgfield.SwingScoutSvgField.class);
    }
  }

}
