/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.IUTCDateFieldExtension;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.date.UTCDate;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractUTCFieldData;

/**
 * Date field with override on {@link #getHolderType()} to {@link UTCDate}
 */
@ClassId("97712b93-4633-4a47-ae4f-ad891d30183a")
@FormData(value = AbstractUTCFieldData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractUTCDateField extends AbstractDateField implements IUTCDateField {

  public AbstractUTCDateField() {
    this(true);
  }

  public AbstractUTCDateField(boolean callInitializer) {
    super(callInitializer);
  }

  protected static class LocalUTCDateFieldExtension<OWNER extends AbstractUTCDateField> extends LocalDateFieldExtension<OWNER> implements IUTCDateFieldExtension<OWNER> {

    public LocalUTCDateFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IUTCDateFieldExtension<? extends AbstractUTCDateField> createLocalExtension() {
    return new LocalUTCDateFieldExtension<>(this);
  }
}
