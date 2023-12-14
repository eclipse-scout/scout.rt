/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;
import org.eclipse.scout.rt.dataobject.lookup.LookupResponse;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Helper class to perform any kind of lookup call
 */
@ApplicationScoped
public class LookupClientHelper {

  /**
   * Performs lookup call to specified {@code resourcePath} using context path {@code path} and specified
   * {@code restriction} object.
   *
   * @param <LOOKUP_ROW>
   *          Lookup row type
   * @param <ID>
   *          Lookup row key type
   * @param <RESTRICTION>
   *          Restriction object type
   */
  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>> LookupResponse<LOOKUP_ROW> lookup(Class<? extends IRestClientHelper> clientHelperType, String resourcePath,
      String path, RESTRICTION restriction) {
    IRestClientHelper clientHelper = BEANS.get(clientHelperType);
    return lookup(clientHelper, resourcePath, path, restriction);
  }

  public <LOOKUP_ROW extends AbstractLookupRowDo<ID>, ID, RESTRICTION extends AbstractLookupRestrictionDo<ID>> LookupResponse<LOOKUP_ROW> lookup(IRestClientHelper clientHelper, String resourcePath, String path,
      RESTRICTION restriction) {

    WebTarget target = clientHelper.target(resourcePath)
        .path(path);

    //noinspection Convert2Diamond
    return target.request()
        .accept(MediaType.APPLICATION_JSON)
        .post(
            Entity.json(restriction),
            new GenericType<LookupResponse<LOOKUP_ROW>>() {
            });
  }
}
