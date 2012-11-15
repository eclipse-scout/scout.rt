/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.workbench.util.listener;

import java.util.EventListener;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public abstract class PartListener implements IPartListener2, EventListener {

  @Override
  public void partActivated(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partBroughtToTop(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partClosed(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partDeactivated(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partHidden(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partInputChanged(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partOpened(IWorkbenchPartReference partRef) {
  }

  @Override
  public void partVisible(IWorkbenchPartReference partRef) {
  }

}
