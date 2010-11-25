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
package org.eclipse.scout.rt.ui.swt.form.fields.groupbox;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ISwtScoutGroupBox</h3> ...
 * 
 * @since 1.0.0 28.03.2008
 */
public interface ISwtScoutGroupBox extends ISwtScoutFormField<IGroupBox> {

  String DATA_SCOUT_GROUPBOX = "scoutGroupBox";

  Composite getSwtGroupBox();

  Composite getSwtBodyPart();

}
