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
package org.eclipse.scout.rt.server.services.common.test;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

public class DefaultLookupServicesTest extends AbstractServerTest {

  @Override
  public void run() throws Throwable {
    ILookupService[] services = SERVICES.getServices(ILookupService.class);
    for (ILookupService s : services) {
      testMethodInvocation(s, "getDataByKey");
      testMethodInvocation(s, "getDataByText");
      testMethodInvocation(s, "getDataByAll");
      testMethodInvocation(s, "getDataByRec");
    }
  }

  protected void testMethodInvocation(ILookupService s, String methodName) {
    // test
    setSubTitle(s.getClass().getSimpleName() + "." + methodName);
    try {
      //
      LookupCall call = createLookupCall(s, methodName);
      Method m = s.getClass().getMethod(methodName, LookupCall.class);
      @SuppressWarnings("unused")
      LookupRow[] data = (LookupRow[]) m.invoke(s, call);
      addOkStatus();
    }
    catch (Throwable t) {
      addErrorStatus(t);
    }
  }

  protected LookupCall createLookupCall(ILookupService s, String methodName) throws Throwable {
    Bundle serverBundle = ThreadContext.getServerSession().getBundle();
    String groupName = serverBundle.getSymbolicName().replace("\\.server$", "");
    String lookupCallClassName = groupName + ".shared.services.lookup." + s.getClass().getSimpleName().replaceAll("LookupService$", "") + "LookupCall";
    LookupCall call = (LookupCall) serverBundle.loadClass(lookupCallClassName).newInstance();
    call.setKey(1L);
    call.setText("XXX");
    call.setAll("XXX");
    call.setRec(1L);
    call.setMaster(1L);
    call.setMaxRowCount(1);
    return call;
  }

  @Override
  protected String getConfiguredTitle() {
    return "Lookup Services";
  }

}
