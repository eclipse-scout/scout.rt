/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.file;

import java.io.File;
import java.util.Locale;

import org.eclipse.scout.rt.platform.service.IService;

public interface IFileService extends IService {

  /**
   * never returns null. Use {@link File#exists()} to check
   */
  File getLocalFile(String dir, String simpleName);

  File getRemoteFile(String dir, String simpleName);

  File getRemoteFile(String dir, String simpleName, Locale locale);

  File getRemoteFile(String dir, String simpleName, Locale locale, boolean checkCache);

  /**
   * @since 21.10.2009
   */
  File getLocalFileLocation(String dir, String name);

  /**
   * @since 21.10.2009
   */
  File getRemoteFileLocation(String dir, String name);
}
