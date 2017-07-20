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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

/**
 * UI facade for proposal fields.
 *
 * @since 7.0
 */
public interface IProposalField2UIFacade<VALUE> extends ISmartField2UIFacade<VALUE> {

  void setValueAsStringFromUI(String value);

}
