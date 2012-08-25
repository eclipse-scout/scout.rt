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
package org.eclipse.scout.rt.ui.swt.form.fields.browserfield;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ISwtScoutHtmlField</h3> ...
 * 
 * @since 1.0.0 15.04.2008
 */
public interface ISwtScoutBrowserField extends ISwtScoutFormField<IBrowserField> {

  @Override
  Composite getSwtField();
}
