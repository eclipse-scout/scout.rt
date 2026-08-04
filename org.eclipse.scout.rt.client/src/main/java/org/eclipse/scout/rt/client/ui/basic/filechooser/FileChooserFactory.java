/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.filechooser;

import java.util.Collection;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public class FileChooserFactory {

  public IFileChooser createFileChooser() {
    return new FileChooser();
  }

  public IFileChooser createFileChooser(boolean multiSelect) {
    return new FileChooser(multiSelect);
  }

  public IFileChooser createFileChooser(Collection<String> fileExtensions) {
    return new FileChooser(fileExtensions);
  }

  public IFileChooser createFileChooser(Collection<String> fileExtensions, boolean multiSelect) {
    return new FileChooser(fileExtensions, multiSelect);
  }
}
