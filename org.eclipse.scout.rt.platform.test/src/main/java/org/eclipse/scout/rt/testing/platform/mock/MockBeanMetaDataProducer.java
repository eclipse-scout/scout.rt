/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
