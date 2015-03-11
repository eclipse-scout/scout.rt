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
package org.eclipse.scout.rt.platform.cdi.internal.scan;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.internal.scan.fixture.TestingBean;
import org.junit.Assert;
import org.junit.Test;

public class BeanScannerTest {

  @Test
  public void testReflection() throws Exception {
    testFinderImpl(new BeanFinderWithReflection());
  }

  @Test
  public void testJandex() throws Exception {
    testFinderImpl(new BeanFinderWithJandex());
  }

  private void testFinderImpl(AbstractBeanFinder finder) throws Exception {
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
        basePath + "$M1",
        basePath + "$I1",
        basePath + "$I2",
        basePath + "$E1",
        basePath + "$A1",
    }) {
      finder.handleClass(path.replace('/', '.'), TestingBean.class.getClassLoader().getResource(path + ".class"));
    }
    Collection<Class> actual = finder.finish();

    HashSet<Class> expected = new HashSet<Class>();
    expected.add(org.eclipse.scout.rt.platform.cdi.internal.scan.fixture.TestingBean.class);
    expected.add(org.eclipse.scout.rt.platform.cdi.internal.scan.fixture.TestingBean.S1.class);
    expected.add(Class.forName(TestingBean.class.getName() + "$S2"));
    expected.add(org.eclipse.scout.rt.platform.cdi.internal.scan.fixture.TestingBean.I1.class);

    Assert.assertEquals(expected, actual);
  }
}
