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
package org.eclipse.scout.rt.ui.swing.ext;

/**
 * Support for wait cursor property
 * <p>
 * XXX move to root pane and remove on JFrameEx, JDialogEx, JWindowEx, ...
 */
public interface IWaitSupport {

  boolean isWaitCursor();

  void setWaitCursor(boolean b);

}
