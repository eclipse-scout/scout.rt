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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.util.Date;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractUTCFieldData;

/**
 * A Value field for date (and time) values dependent on time zone.
 * <p>
 * It uses {@link org.eclipse.scout.rt.shared.data.form.fields.AbstractUTCFieldData AbstractUTCFieldData} with a
 * {@link org.eclipse.scout.commons.UTCDate UTCDate} in order to provide a time zone dependent date object as field
 * data.
 * </p>
 * <p>
 * <strong>Note:</strong> By default, all objects of type {@link java.util.Date} - except
 * {@link org.eclipse.scout.commons.UTCDate UTCDate} - are converted to
 * {@link org.eclipse.scout.rt.shared.servicetunnel.StaticDate StaticDate} during serialization and converted back to
 * <code>Date</code> objects during de-serialization in order to be independent of time zone and daylight saving time.
 * </p>
 * 
 * @see org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField AbstractDateField
 * @see org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelObjectReplacer ServiceTunnelObjectReplacer
 */
@FormData(value = AbstractUTCFieldData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractUTCDateField extends AbstractDateField implements IUTCDateField {

  public AbstractUTCDateField() {
    this(true);
  }

  public AbstractUTCDateField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public Class<Date> getHolderType() {
    return super.getHolderType();
  }

}
