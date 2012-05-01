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
import org.eclipse.scout.rt.shared.ui.IUiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.service.DefaultServiceFactory;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * @since 3.8.0
 */
public class MobileServiceFactory extends DefaultServiceFactory {

  public MobileServiceFactory(Class<?> serviceClass) {
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
