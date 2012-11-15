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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.IRwtScoutPart;

/**
 * @since 3.8.0
 */
public interface IRwtScoutViewStack {

  IRwtEnvironment getUiEnvironment();

  IRwtScoutPart addForm(IForm form);

  boolean isPartVisible(IRwtScoutPart part);

  void setPartVisible(IRwtScoutPart part);

  int getHeightHint();

  void setHeightHint(int heightHint);

  int getWidthHint();

  void setWidthHint(int widthHint);

}
