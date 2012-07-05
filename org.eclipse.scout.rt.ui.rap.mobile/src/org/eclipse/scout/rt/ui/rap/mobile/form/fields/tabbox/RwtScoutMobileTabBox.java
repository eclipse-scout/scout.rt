/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tabbox;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.tabbox.MobileTabBoxGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.LogicalGridLayout;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.scout.rt.ui.rap.form.fields.RwtScoutFieldComposite;
import org.eclipse.scout.rt.ui.rap.form.fields.tabbox.IRwtScoutTabBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RwtScoutMobileTabBox extends RwtScoutFieldComposite<ITabBox> implements IRwtScoutTabBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutMobileTabBox.class);

  @Override
  protected void initializeUi(Composite parent) {
    Composite container = getUiEnvironment().getFormToolkit().createComposite(parent, SWT.TOP);
    container.setLayout(new LogicalGridLayout(0, 0));

    IGroupBox groupBox = wrapTabBox();
    IRwtScoutFormField group = getUiEnvironment().createFormField(container, groupBox);

    setUiLabel(null);
    setUiField(group.getUiContainer());
    setUiContainer(container);
  }

  private IGroupBox wrapTabBox() {
    final Holder<IGroupBox> holder = new Holder<IGroupBox>(IGroupBox.class);

    ClientSyncJob job = new ClientSyncJob("", getUiEnvironment().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        MobileTabBoxGroupBox groupBox = new MobileTabBoxGroupBox(getScoutObject());
        groupBox.initField();

        holder.setValue(groupBox);
      }
    };

    job.schedule();
    try {
      job.join();
    }
    catch (InterruptedException e) {
      LOG.error("TabBox wrapping interrupted. ", e);
    }

    return holder.getValue();
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    // void here
  }

}
