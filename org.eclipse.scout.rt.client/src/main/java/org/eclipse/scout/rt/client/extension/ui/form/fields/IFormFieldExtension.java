/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields;

import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldAddSearchTermsChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldChangedMasterValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDisposeFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsEmptyChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsSaveNeededChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldMarkSavedChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public interface IFormFieldExtension<OWNER extends AbstractFormField> extends IExtension<OWNER> {

  void execDataChanged(FormFieldDataChangedChain chain, Object... dataTypes);

  void execAddSearchTerms(FormFieldAddSearchTermsChain chain, SearchFilter search);

  void execChangedMasterValue(FormFieldChangedMasterValueChain chain, Object newMasterValue);

  void execDisposeField(FormFieldDisposeFieldChain chain);

  void execInitField(FormFieldInitFieldChain chain);

  boolean execCalculateVisible(FormFieldCalculateVisibleChain chain);

  void execMarkSaved(FormFieldMarkSavedChain chain);

  boolean execIsEmpty(FormFieldIsEmptyChain chain);

  boolean execIsSaveNeeded(FormFieldIsSaveNeededChain chain);
}
