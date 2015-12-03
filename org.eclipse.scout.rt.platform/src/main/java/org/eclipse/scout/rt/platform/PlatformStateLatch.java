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
package org.eclipse.scout.rt.platform;

import java.util.concurrent.CountDownLatch;

public class PlatformStateLatch {

  private final CountDownLatch m_countDownLatch = new CountDownLatch(1);

  public void await() throws InterruptedException {
    m_countDownLatch.await();
  }

  public void release() {
    m_countDownLatch.countDown();
  }
}
