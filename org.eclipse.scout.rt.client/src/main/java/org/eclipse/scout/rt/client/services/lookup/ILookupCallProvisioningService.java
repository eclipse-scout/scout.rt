/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

/**
 * This service creates lookup call instances from lookup call templates.
 * <p>
 * Callers are fields and components that deal with getConfiguredLookupCall and getConfiguredCodeType and create new
 * instances usign copy().
 * <p>
 * Known are {@link ISmartField}, {@link IListBox}, {@link ITreeBox}, {@link IRadioButtonGroup}, {@link ISmartColumn}
 * <p>
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388242
 *
 * @since 3.8.1
 */
public interface ILookupCallProvisioningService extends IService {

  /**
   * @return a new cloned and provisioned instance of the lookup call template.
   */
  <T> ILookupCall<T> newClonedInstance(ILookupCall<T> templateCall, IProvisioningContext context);
}
