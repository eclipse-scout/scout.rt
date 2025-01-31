/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockedFile implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(LockedFile.class);

  private final File m_unsafeFile;
  private final RandomAccessFile m_raFile;
  private final boolean m_isReadOnly;

  public LockedFile(File f) throws IOException {
    m_unsafeFile = f;
    m_isReadOnly = f.exists() && !f.canWrite(); // non existing file has canWrite=false. But it can be created. Therefore not read only
    m_raFile = new RandomAccessFile(f, m_isReadOnly ? "r" : "rw");
    acquireLock();
  }

  private FileLock acquireLock() throws IOException {
    //timeout after 30 seconds
    long timeoutSeconds = 30;
    long timeoutEvent = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
    while (true) {
      try {
        //noinspection resource
        FileLock lock = m_raFile.getChannel().tryLock(0, Long.MAX_VALUE, isReadOnly());
        //for locks in another vm
        if (lock != null) {
          return lock;
        }
      }
      catch (OverlappingFileLockException e) {//NOSONAR
        //for locks in same vm
      }
      if (System.currentTimeMillis() > timeoutEvent) {
        LOG.warn("File '{}' is still locked after {} seconds.", m_unsafeFile.getAbsolutePath(), timeoutSeconds);
        return null;
      }
      SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * @return {@code true} if this {@link LockedFile} is read only.
   */
  public boolean isReadOnly() {
    return m_isReadOnly;
  }

  /**
   * @return true if the {@link LockedFile} has {@link #length()} &gt; 0
   */
  public boolean exists() throws IOException {
    return length() > 0L;
  }

  public long length() throws IOException {
    return m_raFile.length();
  }

  /**
   * See {@link FileChannel#force(boolean)}. This method has no effect if this {@link LockedFile}
   * {@link LockedFile#isReadOnly()}
   */
  public void flush() throws IOException {
    if (isReadOnly()) {
      return;
    }
    //noinspection resource
    m_raFile.getChannel().force(true);
  }

  /**
   * See {@link File#setLastModified(long)}. This method has no effect if this {@link LockedFile}
   * {@link LockedFile#isReadOnly()}
   *
   * @param t
   *     The new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
   * @return {@code true} if and only if the operation succeeded.
   */
  public boolean setLastModified(long t) throws IOException {
    if (isReadOnly()) {
      return false;
    }
    flush();
    return m_unsafeFile.setLastModified(t);
  }

  public long lastModified() throws IOException {
    flush();
    return m_unsafeFile.lastModified();
  }

  /**
   * @return a {@link InputStream} to this locked file, do NOT close this {@link InputStream} unless you want to close
   * the {@link LockedFile} and release the lock
   */
  public InputStream newInputStream() throws IOException {
    m_raFile.seek(0);
    return Channels.newInputStream(m_raFile.getChannel());
  }

  /**
   * @return an {@link OutputStream} to this {@link LockedFile}. Do NOT close this {@link OutputStream} unless you want
   * to close the {@link LockedFile} and release the lock! This method fails if this {@link LockedFile}
   * {@link #isReadOnly()}.
   */
  public OutputStream newOutputStream() throws IOException {
    m_raFile.seek(0);
    m_raFile.setLength(0);
    return Channels.newOutputStream(m_raFile.getChannel());
  }

  @Override
  public void close() throws IOException {
    m_raFile.close();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + m_unsafeFile + "]";
  }
}
