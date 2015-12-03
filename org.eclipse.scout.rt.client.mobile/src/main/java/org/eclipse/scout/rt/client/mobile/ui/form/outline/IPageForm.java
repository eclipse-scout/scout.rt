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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IPageForm extends IForm {

  IPage<?> getPage();

  PageFormConfig getPageFormConfig();

  void pageSelectedNotify();

  /**
   * returns true if either the detail form or the table of the page has changed, which makes a recreation of the form
   * necessary.
   */
  boolean isDirty();
}
