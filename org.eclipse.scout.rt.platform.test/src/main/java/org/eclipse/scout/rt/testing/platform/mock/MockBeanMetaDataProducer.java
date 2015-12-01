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
package org.eclipse.scout.rt.testing.platform.mock;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.Order;

/**
 * Creates {@link BeanMetaData} for a class using {@link MockBeanInstanceProducer}.
 */
@Order(5100)
public class MockBeanMetaDataProducer implements IBeanAnnotationMetaDataProducer {
  public static final int MOCK_BEAN_ORDER = -10000;

  @Override
  public BeanMetaData produce(Class<?> type) {
    return new BeanMetaData(type)
        .withProducer(new MockBeanInstanceProducer())
        .withOrder(MOCK_BEAN_ORDER);
  }

}
