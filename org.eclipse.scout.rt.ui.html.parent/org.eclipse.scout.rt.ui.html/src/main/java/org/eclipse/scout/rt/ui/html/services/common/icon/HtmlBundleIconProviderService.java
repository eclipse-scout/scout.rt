/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.services.common.icon;

import org.eclipse.scout.rt.client.services.common.icon.IconProviderService;
import org.eclipse.scout.rt.ui.html.Activator;

// FIXME AWE: (resource loading) discuss with C.GU - wollen wir diesen service wirklich?
// Wir könnten auch ohne ihn leben prinzipiell - aber dann gibt es keine bitmap grafiken
// aus dem scout.rt.ui.html Bundle
public class HtmlBundleIconProviderService extends IconProviderService {

  public static final String FOLDER_NAME = "resources/icons/internal";

  public HtmlBundleIconProviderService() {
    setHostBundle(Activator.getDefault().getBundle());
    setFolderName(FOLDER_NAME);
  }

  @Override
  public void initializeService() {
    super.initializeService();
    // TODO NOOSGI
//    setHostBundle(Activator.getDefault().getBundle());
//    setFolderName(FOLDER_NAME);
  }

}
