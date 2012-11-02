/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop;

import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopExtension;
import org.eclipse.scout.rt.extension.client.Activator;
import org.eclipse.scout.rt.extension.client.IExtensibleScoutObject;

/**
 * Desktop supporting the following Scout extension features:
 * <ul>
 * <li>adding desktop extensions {@link IDesktopExtension}</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensibleDesktop extends AbstractDesktop implements IExtensibleScoutObject {

  @Override
  protected void injectDesktopExtensions(List<IDesktopExtension> desktopExtensions) {
    super.injectDesktopExtensions(desktopExtensions);
    List<IDesktopExtension> extensions = Activator.getDefault().getDesktopExtensionManager().getDesktopExtensions();
    for (IDesktopExtension e : extensions) {
      e.setCoreDesktop(this);
    }
    desktopExtensions.addAll(extensions);
  }
}
