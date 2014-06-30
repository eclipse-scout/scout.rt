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
package org.eclipse.scout.rt.client.officeonline;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.scout.rt.client.officeonline.IOfficeWebAppsService;
import org.eclipse.scout.service.SERVICES;

public class WopiTestApplication implements IApplication {

  @Override
  public Object start(IApplicationContext context) throws Exception {

    IOfficeWebAppsService owas = SERVICES.getService(IOfficeWebAppsService.class);

    TestingWopiContentProvider wopiSrc = SERVICES.getService(TestingWopiContentProvider.class);
    String url1 = owas.createIFrameUrl(
        IOfficeWebAppsService.Zone.InternalHttps,
        IOfficeWebAppsService.App.Word,
        IOfficeWebAppsService.Action.View,
        owas.wopiEmbeddedUrl() + "/hello.docx",
        wopiSrc.issueAccessToken("hello.docx", "imo")
        );
    System.out.println("Embedded document URL (view only): " + url1);

    //owas.getCobaltDocument("h1.docx", true);

    owas.putDocument("h2.docx", wopiSrc.getFileContent("hello.docx"));
    String url2 = owas.createIFrameUrl(
        IOfficeWebAppsService.Zone.InternalHttps,
        IOfficeWebAppsService.App.Word,
        IOfficeWebAppsService.Action.Edit,
        owas.bsiWopiServiceUrl() + "/h2.docx",
        "0"
        );
    System.out.println("Cobalt document URL (edit): " + url2);
    System.out.println("IMO Test aspx: WordEditorFrame_imo.aspx");

    return EXIT_OK;
  }

  @Override
  public void stop() {
  }

}
