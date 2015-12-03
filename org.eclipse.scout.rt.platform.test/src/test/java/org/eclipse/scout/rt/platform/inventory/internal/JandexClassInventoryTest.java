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
package org.eclipse.scout.rt.platform.inventory.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.internal.BeanFilter;
import org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean;
import org.jboss.jandex.Indexer;
import org.junit.Assert;
import org.junit.Test;

public class JandexClassInventoryTest {

  @Test
  public void testJandex() throws Exception {
    Indexer indexer = new Indexer();
    String basePath = TestingBean.class.getName().replace('.', '/');
    for (String path : new String[]{
        Bean.class.getName().replace('.', '/'),
        ApplicationScoped.class.getName().replace('.', '/'),
        basePath,
        basePath + "2",
        basePath + "$S1",
        basePath + "$S2",
        basePath + "$S3",
        basePath + "$S4",
        basePath + "$S1Sub1",
        basePath + "$M1",
        basePath + "$I1",
        basePath + "$I2",
        basePath + "$E1",
        basePath + "$A1",
    }) {
      indexer.index(TestingBean.class.getClassLoader().getResource(path + ".class").openStream());
    }
    JandexClassInventory classInventory = new JandexClassInventory(indexer.complete());
    final Set<Class> actual = new BeanFilter().collect(classInventory);

    Set<Class> expected = new HashSet<>();
    expected.add(org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.class);
    expected.add(org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.S1.class);
    expected.add(org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.S1Sub1.class);

    Assert.assertEquals(expected, actual);
  }
}
