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
package org.eclipse.scout.rt.client.mobile.services;

import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.services.ClientServiceFactory;
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Accepts the service only if the current device is a mobile or tablet.
 * 
 * @since 3.9.0
 * @see {@link UserAgent}
 */
public class MobileClientServiceFactory extends ClientServiceFactory {

  public MobileClientServiceFactory(Class<?> serviceClass) {
    super(serviceClass);
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    IUiDeviceType uiDeviceType = ClientJob.getCurrentSession().getUserAgent().getUiDeviceType();
    if (!UiDeviceType.MOBILE.equals(uiDeviceType) && !UiDeviceType.TABLET.equals(uiDeviceType)) {
      return ServiceUtility.NULL_SERVICE;
    }

    return super.getService(bundle, registration);
  }
}
