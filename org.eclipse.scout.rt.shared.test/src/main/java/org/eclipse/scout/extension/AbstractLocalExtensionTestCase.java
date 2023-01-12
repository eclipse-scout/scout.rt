/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.extension;

import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.shared.extension.ExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * use this superclass for test requiring a local extension registry ({@link IExtensionRegistry}). Extensions can be
 * added in every test method of subclasses and will be available only for that test method.
 */
@RunWith(PlatformTestRunner.class)
public abstract class AbstractLocalExtensionTestCase {

  private List<IBean<?>> m_localServiceRegistrations;

  @Before
  public void registerLocalRegistry() {
    m_localServiceRegistrations = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(ExtensionRegistry.class)
            .withApplicationScoped(true));
  }

  @After
  public void unregisterLocalRegistry() {
    BeanTestingHelper.get().unregisterBeans(m_localServiceRegistrations);
  }
}
