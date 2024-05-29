/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.junit.Test;

public class InspectorInfoTest {

  @Test
  public void testPutUuidProperty() {
    InspectorInfo inspectorInfo = BEANS.get(InspectorInfo.class);
    assertEquals("1ikng49ck6ttpbtc4cdjj8htojd7grnvg53s9ppbj03ngmnusi5r", inspectorInfo.prepareClassId("a"));
    assertEquals("1e9a0lhs8fdtp24lagft4d0taj04dn3e8do88fn64r1t3upsf6qk", inspectorInfo.prepareClassId("org.eclipse.scout.rt.ui.html.json.InspectorInfoTest"));
    assertEquals("ac18d702-e700-4881-b977-ea57a5117c00", inspectorInfo.prepareClassId("ac18d702-e700-4881-b977-ea57a5117c00"));
    assertEquals("5aon3l8jsp5bs5jbu4rf31djdqle0cfm25mtrm7mo9s5vd5mmkb", inspectorInfo.prepareClassId("ac18d702-e700-4881-b977-ea57a5117c00_org.eclipse.scout.rt.Test"));
    assertEquals("ac18d702-e700-4881-b977-ea57a5117c00_7fbc38c1-5432-490c-88ba-1e528c37a240",
        inspectorInfo.prepareClassId("ac18d702-e700-4881-b977-ea57a5117c00" + ITypeWithClassId.ID_CONCAT_SYMBOL + "7fbc38c1-5432-490c-88ba-1e528c37a240"));
    assertEquals("14dij01oj6j3secq1i2igv8hgr23olvgl2q4g9ebgfkvg5pf3uk", inspectorInfo.prepareClassId("ac18d702-e700-488x-b977-ea57a5117c00" + ITypeWithClassId.ID_CONCAT_SYMBOL + "7fbc38c1-5432-490c-88ba-1e528c37a240"));
    assertEquals("c2ca7cb6-7a3f-44a5-a210-684918923e78_e08859b4-1df0-47e8-be2d-bc46f4517762_f2e4e1b0-2c06-4119-bf04-a797c7415d02",
        inspectorInfo.prepareClassId("c2ca7cb6-7a3f-44a5-a210-684918923e78" + ITypeWithClassId.ID_CONCAT_SYMBOL + "e08859b4-1df0-47e8-be2d-bc46f4517762" + ITypeWithClassId.ID_CONCAT_SYMBOL + "f2e4e1b0-2c06-4119-bf04-a797c7415d02"));
  }
}
