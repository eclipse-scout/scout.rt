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
package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractProposalField;

public abstract class AbstractProposalFieldExtension<LOOKUP_KEY, OWNER extends AbstractProposalField<LOOKUP_KEY>> extends AbstractContentAssistFieldExtension<String, LOOKUP_KEY, OWNER> implements IProposalFieldExtension<LOOKUP_KEY, OWNER> {

  public AbstractProposalFieldExtension(OWNER owner) {
    super(owner);
  }
}
