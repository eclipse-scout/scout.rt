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
package org.eclipse.scout.rt.client.ui.form.fields.treefield;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.shared.data.form.fields.treefield.AbstractTreeFieldData;

/**
 * FIXME imo scout is missing this formdata annotation
 */
@FormData(value = AbstractTreeFieldData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public class AbstractTreeField5 extends AbstractTreeField {

}
