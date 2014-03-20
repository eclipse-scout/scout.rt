///*******************************************************************************
// * Copyright (c) 2010 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.rt.ui.json;
//
//import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
//import org.eclipse.scout.rt.ui.json.fixtures.JsonSessionMock;
//import org.eclipse.scout.rt.ui.json.testing.JsonTestUtility;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.mockito.Mockito;
//
//public class JsonDesktopTreeTestUtil {
//
//  public static JsonDesktopTree createJsonDesktopTreeWithMocks(IOutline outline) {
//    JsonSessionMock jsonSession = new JsonSessionMock();
//    JsonDesktop jsonDesktop = Mockito.mock(JsonDesktop.class);
//
//    JsonDesktopTree jsonDesktopTree = new JsonDesktopTree(jsonDesktop, outline, jsonSession);
//    jsonDesktopTree.init();
//
//    //init treeNode map
//    jsonDesktopTree.toJson();
//
//    return jsonDesktopTree;
//  }
//
//  public static JsonEvent createJsonSelectedEvent(String nodeId) throws JSONException {
//    JsonEvent event = JsonTestUtility.createJsonEvent(JsonDesktopTree.EVENT_NODES_SELECTED);
//    JSONArray nodeIds = new JSONArray();
//    nodeIds.put(nodeId);
//    event.getEventObject().put(JsonDesktopTree.PROP_NODE_IDS, nodeIds);
//    return event;
//  }
//}
