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
package org.eclipse.scout.rt.ui.swt.util.listener;

import java.util.EventListener;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public abstract class PartListener implements IPartListener2, EventListener {

  public void partActivated(IWorkbenchPartReference partRef) {
  }

  public void partBroughtToTop(IWorkbenchPartReference partRef) {
  }

  public void partClosed(IWorkbenchPartReference partRef) {
  }

  public void partDeactivated(IWorkbenchPartReference partRef) {
  }

  public void partHidden(IWorkbenchPartReference partRef) {
  }

  public void partInputChanged(IWorkbenchPartReference partRef) {
  }

  public void partOpened(IWorkbenchPartReference partRef) {
  }

  public void partVisible(IWorkbenchPartReference partRef) {
  }

}
