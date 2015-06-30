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
