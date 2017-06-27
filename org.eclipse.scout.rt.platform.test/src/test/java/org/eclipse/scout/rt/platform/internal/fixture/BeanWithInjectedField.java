/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal.fixture;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.InjectBean;
import org.eclipse.scout.rt.platform.service.IService;

@Bean
public class BeanWithInjectedField {

  @InjectBean
  private BeanWithInjections m_field;

  @InjectBean
  private BeanWithInjectedField(IService constructorArg) {
  }

  public void assertInit() {
    m_field.assertInit();
  }
}
