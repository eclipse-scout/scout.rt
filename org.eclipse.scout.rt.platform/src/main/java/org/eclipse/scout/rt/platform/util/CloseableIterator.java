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

import java.util.Iterator;

/**
 * Iterator that must be closed.
 */
public interface CloseableIterator<E> extends Iterator<E>, AutoCloseable {

  /**
   * Closes any resources allocated by this iterator. This method may be invoked multiple times.
   */
  @Override
  void close();
}
