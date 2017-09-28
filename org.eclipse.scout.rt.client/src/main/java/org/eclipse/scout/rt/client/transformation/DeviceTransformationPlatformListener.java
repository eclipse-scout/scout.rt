/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;

public class DeviceTransformationPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.PlatformStarted) {
      BEANS.get(IExtensionRegistry.class).register(DesktopExtension.class);
      BEANS.get(IExtensionRegistry.class).register(OutlineExtension.class);
      BEANS.get(IExtensionRegistry.class).register(PageExtension.class);
      BEANS.get(IExtensionRegistry.class).register(PageWithTableExtension.class);
      BEANS.get(IExtensionRegistry.class).register(FormExtension.class);
      BEANS.get(IExtensionRegistry.class).register(FormFieldExtension.class);
    }
  }

}
