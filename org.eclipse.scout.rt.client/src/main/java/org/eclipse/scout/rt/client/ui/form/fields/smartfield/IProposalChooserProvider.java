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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

/**
 * Responsible for creating {@link IProposalChooser}.
 *
 * @since 6.0.0
 */
public interface IProposalChooserProvider<LOOKUP_KEY> {

  IProposalChooser<?, LOOKUP_KEY> createProposalChooser(IContentAssistField<?, LOOKUP_KEY> smartField, boolean allowCustomText);

}
