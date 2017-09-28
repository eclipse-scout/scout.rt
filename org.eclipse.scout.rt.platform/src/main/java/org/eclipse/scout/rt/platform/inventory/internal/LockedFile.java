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
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockedFile implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(LockedFile.class);

  private final File m_unsafeFile;
  private final RandomAccessFile m_rwFile;

  public LockedFile(File f) throws IOException {
    m_unsafeFile = f;
    m_rwFile = new RandomAccessFile(f, "rw");
    acquireLock();
  }

  private FileLock acquireLock() throws IOException {
    //timeout after 30 seconds
    long timeoutSeconds = 30;
    long timeoutEvent = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
    while (true) {
      try {
        FileLock lock = m_rwFile.getChannel().tryLock();
        //for locks in another vm
        if (lock != null) {
          return lock;
        }
      }
      catch (OverlappingFileLockException e) {//NOSONAR
        //for locks in same vm
      }
      if (timeoutEvent > 0L && System.currentTimeMillis() > timeoutEvent) {
        timeoutEvent = 0L;
        LOG.warn("File '{}' is still locked after {} seconds.", m_unsafeFile.getAbsolutePath(), timeoutSeconds);
      }
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * @return true if the {@link LockedFile} has {@link #length()} &gt; 0
   * @throws IOException
   */
  public boolean exists() throws IOException {
    return length() > 0L;
  }

  public long length() throws IOException {
    return m_rwFile.length();
  }

  public void flush() throws IOException {
    m_rwFile.getChannel().force(true);
  }

  public boolean setLastModified(long t) throws IOException {
    flush();
    return m_unsafeFile.setLastModified(t);
  }

  public long lastModified() throws IOException {
    flush();
    return m_unsafeFile.lastModified();
  }

  /**
   * @return a {@link InputStream} to this locked file, do NOT close this {@link InputStream} unless you want to close
   *         the {@link LockedFile} and release the lock
   * @throws IOException
   */
  public InputStream newInputStream() throws IOException {
    m_rwFile.seek(0);
    return Channels.newInputStream(m_rwFile.getChannel());
  }

  /**
   * @return a {@link OutputStream} to this locked file, do NOT close this {@link OutputStream} unless you want to close
   *         the {@link LockedFile} and release the lock
   * @throws IOException
   */
  public OutputStream newOutputStream() throws IOException {
    m_rwFile.seek(0);
    m_rwFile.setLength(0);
    return Channels.newOutputStream(m_rwFile.getChannel());
  }

  @Override
  public void close() throws IOException {
    m_rwFile.close();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_unsafeFile + "]";
  }
}
