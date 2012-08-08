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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.mobile.transformation.IDeviceTransformationService;
import org.eclipse.scout.rt.ui.rap.window.desktop.RwtScoutViewStack;
import org.eclipse.scout.rt.ui.rap.window.desktop.viewarea.ViewArea;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;

/**
 * @since 3.9.0
 */
public class MobileViewArea extends ViewArea {

  private static final long serialVersionUID = 1L;

  public MobileViewArea(Composite parent) {
    super(parent);
  }

  @Override
  protected RwtScoutViewStack createRwtScoutViewStack(Composite parent) {
    return new RwtScoutMobileViewStack(parent, getUiEnvironment(), this);
  }

  @Override
  protected Sash createSash(Composite parent, int style) {
    SimpleSash simpleSash = new SimpleSash(parent, style);
    return simpleSash;
  }

  @Override
  protected int getSashWidth() {
    return 1;
  }

  @Override
  protected boolean acceptViewId(final String viewId) {
    final BooleanHolder accepted = new BooleanHolder(true);

    ClientSyncJob job = new ClientSyncJob("Adapting form header left menus", getUiEnvironment().getClientSession()) {

      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        IDeviceTransformationService service = SERVICES.getService(IDeviceTransformationService.class);
        if (service != null && service.getDeviceTransformer() != null) {
          List<String> acceptedViewIds = service.getDeviceTransformer().getAcceptedViewIds();

          //Accept all if null is returned.
          if (acceptedViewIds != null) {
            accepted.setValue(acceptedViewIds.contains(viewId));
          }
        }
      }

    };
    job.runNow(new NullProgressMonitor());

    return accepted.getValue();
  }
}
