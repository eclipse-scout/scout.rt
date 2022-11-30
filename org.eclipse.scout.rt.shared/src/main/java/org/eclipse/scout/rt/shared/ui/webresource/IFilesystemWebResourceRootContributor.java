/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.ui.webresource;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

@ApplicationScoped
public interface IFilesystemWebResourceRootContributor {

  List<Path> getRoots();
}
