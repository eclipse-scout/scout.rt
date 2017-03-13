/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.filechooserfield;

import org.eclipse.scout.rt.client.ui.basic.filechooser.FileChooser;

public interface IFileChooserFieldUIFacade {

  void parseAndSetValueFromUI(String value);

  /**
   * @deprecated The UI will display the native file chooser dialog on click. There is no {@link FileChooser} created on
   *             java side anymore. Will be removed with 7.0
   */
  @Deprecated
  void startFileChooserFromUI();
}
