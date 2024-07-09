/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.opentelemetry.TracingActionExtension;
import org.eclipse.scout.rt.client.opentelemetry.TracingButtonExtension;
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

      // Observability
      BEANS.get(IExtensionRegistry.class).register(TracingActionExtension.class);
      BEANS.get(IExtensionRegistry.class).register(TracingButtonExtension.class);
    }
  }

}
