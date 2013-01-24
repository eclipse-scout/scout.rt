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
package org.eclipse.scout.rt.ui.swt.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.basic.ISwtScoutComposite;
import org.eclipse.scout.rt.ui.swt.ext.ILabelComposite;

/**
 * <h3>ISwtScoutFormField</h3> ...
 * 
 * @since 1.0.0 10.03.2008
 */
public interface ISwtScoutFormField<T extends IFormField> extends ISwtScoutComposite<T> {
  String CLIENT_PROPERTY_SCOUT_OBJECT = "org.eclipse.scout.rt.object";

  ILabelComposite getSwtLabel();

}
