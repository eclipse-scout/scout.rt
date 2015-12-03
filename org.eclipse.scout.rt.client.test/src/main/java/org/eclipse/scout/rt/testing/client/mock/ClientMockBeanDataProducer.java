/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.client.mock;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.testing.platform.mock.IBeanAnnotationMetaDataProducer;
import org.eclipse.scout.rt.testing.platform.mock.MockBeanInstanceProducer;

@Order(550)
public class ClientMockBeanDataProducer implements IBeanAnnotationMetaDataProducer {
  public static final int MOCK_BEAN_ORDER = -10000;

  @Override
  public BeanMetaData produce(Class<?> type) {
    return new BeanMetaData(type)
        .withProducer(new MockBeanInstanceProducer())
        .withOrder(MOCK_BEAN_ORDER)
        .withoutAnnotation(TunnelToServer.class);
  }

}
