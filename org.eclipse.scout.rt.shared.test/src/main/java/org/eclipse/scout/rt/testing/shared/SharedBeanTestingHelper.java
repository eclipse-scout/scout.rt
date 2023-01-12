/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
