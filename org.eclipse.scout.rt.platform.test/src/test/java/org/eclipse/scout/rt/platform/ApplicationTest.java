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
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.fixture.TestApplication;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {

  @Test
  public void test() {
    IPlatform p = Platform.get();
    try {
      Platform.setDefault();
      Platform.get().start(TestApplication.class);

      Assert.assertEquals(TestApplication.getInstance().getClass(), TestApplication.class);
    }
    finally {
      Platform.get().stop();
    }
    Assert.assertSame(Platform.get(), p);
  }

}
