package org.eclipse.scout.rt.server.commons.cache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This service identifies the requesting client
 * 
 * @since 4.0.0
 */
public interface IClientIdentificationService {

  /**
   * returns the session id of the HTTP-Request. If no session id is set a new id will be generated and set.
   * 
   * @param req
   *          HttpServletRequest
   * @param res
   *          HttpServletResponse
   * @return the session id
   */
  String getClientId(HttpServletRequest req, HttpServletResponse res);
}
