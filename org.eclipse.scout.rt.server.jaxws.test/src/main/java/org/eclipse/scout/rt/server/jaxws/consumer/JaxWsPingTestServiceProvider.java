/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import javax.jws.WebService;

import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.JaxWsPingTestServicePortType;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.PingRequest;
import org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.PingResponse;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web service used for testing purposes.
 *
 * @since 6.0.300
 */
@WebService(
    serviceName = "JaxWsPingTestService",
    portName = "JaxWsPingTestServicePort",
    targetNamespace = "http://consumer.jaxws.scout.eclipse.org/JaxWsPingTestService/",
    endpointInterface = "org.eclipse.scout.jaxws.consumer.jaxwspingtestservice.JaxWsPingTestServicePortType",
    wsdlLocation = "WEB-INF/wsdl/JaxWsPingTestService.wsdl")
public class JaxWsPingTestServiceProvider implements JaxWsPingTestServicePortType {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsPingTestServiceProvider.class);

  @Override
  public PingResponse ping(PingRequest req) {
    Assertions.assertNotNull(req);
    LOG.info("echo '{}'", req.getMessage());
    PingResponse resp = new PingResponse();
    resp.setMessage(req.getMessage());
    return resp;
  }
}
