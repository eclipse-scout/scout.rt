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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * Creates JSON output for a Scout form-field object.
 * 
 * @param <T>
 *          Type of Scout form-field
 */
public interface IJsonFormField<T extends IFormField> extends IJsonAdapter<T> {

  String PROP_GRID_DATA = "gridData";

}
