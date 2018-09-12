/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Replace
public class SharedBeanTestingHelper extends BeanTestingHelper {

  private static final Logger LOG = LoggerFactory.getLogger(SharedBeanTestingHelper.class);

  /**
   * If the underlying instance is mocked, any {@link TunnelToServer} annotation is removed.
   */
  @Override
  protected void interceptRegisterBean(BeanMetaData beanData) {
    super.interceptRegisterBean(beanData);

    if (Mockito.mockingDetails(beanData.getInitialInstance()).isMock() && beanData.getBeanAnnotation(TunnelToServer.class) != null) {
      LOG.info("removing TunnelToServer annotation on mocked bean: {}", beanData.getBeanClazz());
      beanData.withoutAnnotation(TunnelToServer.class);
    }
  }
}
