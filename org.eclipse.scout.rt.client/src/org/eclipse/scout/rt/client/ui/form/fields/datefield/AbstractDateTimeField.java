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

import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * convenience subclass of {@link AbstractDateField} with hasDate=true and hasTime=true
 */
public abstract class AbstractDateTimeField extends AbstractDateField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDateTimeField.class);

  public AbstractDateTimeField() {
  }

  @ConfigPropertyValue("true")
  @Override
  protected boolean getConfiguredHasTime() {
    return true;
  }
}
