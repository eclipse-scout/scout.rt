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
package org.eclipse.scout.rt.ui.rap.window.desktop;

import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.8.0
 */
public interface IViewArea {
  void updateSashPositionForViewStack(IRwtScoutViewStack stack);

  IRwtScoutViewStack getStackForForm(IForm form);

  void layout();
}
