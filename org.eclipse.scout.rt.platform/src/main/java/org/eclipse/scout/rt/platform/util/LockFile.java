/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of OS layer lock file
 *
 * @since 9.0
 */
public class LockFile {
  private static final Logger LOG = LoggerFactory.getLogger(LockFile.class);

  private final File m_file;

  private FileOutputStream m_stream;
  private FileChannel m_channel;
  private FileLock m_lock;

  public LockFile(File file) {
    Assertions.assertNotNull(file);
    m_file = file;
  }

  public File getFile() {
    return m_file;
  }

  /**
   * Call using try-finally:
   *
   * <pre>
   * try {
   *   lockFile.lock();
   *   ...
   * }
   * catch (TimedOutError e){
   *   // handle
   * }
   * finally {
   *   lockFile.unlock();
   * }
   * </pre>
   * <p>
   * Returns with lock acquired.
   *
   * @throws TimedOutError
   *     if the lock could not be acquired within timeout
   * @throws ProcessingException
   *     if an error occurred while acquiring the lock
   */
  public synchronized void lock(int timeout, TimeUnit unit) {
    long waitUntilNanoTime = System.nanoTime() + unit.toNanos(timeout);
    try {
      if (!m_file.exists()) {
        m_file.getParentFile().mkdirs();
      }
      m_stream = new FileOutputStream(m_file);
      m_channel = m_stream.getChannel();
      while (true) {
        try {//NOSONAR
          m_lock = m_channel.tryLock();
          //locks in another vm yield null
          if (m_lock != null) {
            break;
          }
        }
        catch (OverlappingFileLockException e) {//NOSONAR
          //locks in same vm throw an exception
        }
        if (waitUntilNanoTime < System.nanoTime()) {
          throw new TimedOutError("waiting for lock '{}' timed out after {} {}", m_file, timeout, unit);
        }
        SleepUtil.sleepElseThrow(40, TimeUnit.MILLISECONDS);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("lock '{}'", m_file, e);
    }
    finally {
      if (m_lock == null) {
        safeClose(m_channel);
        safeClose(m_stream);
        m_stream = null;
        m_channel = null;
      }
    }
  }

  /**
   * Can be called multiple times, does nothing if not owning the {@link FileLock}
   */
  public synchronized void unlock() {
    if (m_lock != null) {
      safeClose(m_lock);
      safeClose(m_channel);
      safeClose(m_stream);
      m_lock = null;
      m_channel = null;
      m_stream = null;
    }
  }

  protected void safeClose(AutoCloseable c) {
    if (c == null) {
      return;
    }
    try {
      c.close();
    }
    catch (Exception e) {
      LOG.info("Failed closing {}", c.getClass(), e);
    }
  }
}
