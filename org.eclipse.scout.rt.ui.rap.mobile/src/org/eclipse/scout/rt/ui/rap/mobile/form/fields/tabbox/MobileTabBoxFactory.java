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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tabbox;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.extension.IFormFieldFactory;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.tabbox.IRwtScoutTabBox;
import org.eclipse.scout.rt.ui.rap.form.fields.tabbox.RwtScoutTabBox;
import org.eclipse.scout.rt.ui.rap.util.DeviceUtility;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.9.0
 */
public class MobileTabBoxFactory implements IFormFieldFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MobileTabBoxFactory.class);

  private IRwtEnvironment m_uiEnvironment;

  @Override
  public IRwtScoutFormField<?> createUiFormField(Composite parent, IFormField model, IRwtEnvironment uiEnvironment) {
    m_uiEnvironment = uiEnvironment;
    IRwtScoutTabBox field;
    ITabBox formField = (ITabBox) model;

    if (DeviceUtility.isMobileOrTabletDevice() && acceptMobileTabBoxTransformation(formField)) {
      field = new RwtScoutMobileTabBox();
    }
    else {
      field = new RwtScoutTabBox();
    }

    field.createUiField(parent, formField, uiEnvironment);

    return field;
  }

  private boolean acceptMobileTabBoxTransformation(final ITabBox tabBox) {
    final BooleanHolder accepted = new BooleanHolder(false);
    ClientSyncJob job = new ClientSyncJob("Getting permission to create mobile tabbox.", m_uiEnvironment.getClientSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        accepted.setValue(SERVICES.getService(IDeviceTransformationService.class).getDeviceTransformer().acceptMobileTabBoxTransformation(tabBox));
      }

    };
    job.schedule();
    try {
      job.join(5000);
    }
    catch (InterruptedException e) {
      LOG.warn("Failed to getting permission to create mobile tabbox.", e);
    }

    return accepted.getValue();

  }

}
