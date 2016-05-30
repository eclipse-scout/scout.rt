package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;

public class DeviceTransformationPlatformListener implements IPlatformListener {

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == IPlatform.State.PlatformStarted) {
      BEANS.get(IExtensionRegistry.class).register(DesktopExtension.class);
      BEANS.get(IExtensionRegistry.class).register(OutlineExtension.class);
      BEANS.get(IExtensionRegistry.class).register(PageExtension.class);
      BEANS.get(IExtensionRegistry.class).register(PageWithTableExtension.class);
      BEANS.get(IExtensionRegistry.class).register(FormExtension.class);
      BEANS.get(IExtensionRegistry.class).register(FormFieldExtension.class);
    }
  }

}
