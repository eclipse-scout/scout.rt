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
package org.eclipse.scout.rt.ui.swt.form.fields.datefield;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swt.ext.ButtonEx;
import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;
import org.eclipse.swt.custom.StyledText;

/**
 * <h3>ISwtScoutDateField</h3> ...
 * 
 * @since 1.0.0 12.04.2008
 */
public interface ISwtScoutDateField extends ISwtScoutFormField<IDateField> {

  ButtonEx getDateChooserButton();

  ButtonEx getTimeChooserButton();

  StyledText getSwtField();
}
