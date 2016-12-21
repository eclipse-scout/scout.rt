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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.EchoRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.EchoResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.GetHeaderRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.GetHeaderResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.JaxWsConsumerTestServicePortType;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SetHeaderRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SetHeaderResponse;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SleepRequest;
import org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.SleepResponse;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web service used for testing purposes. Among other, it provides operations for getting and setting HTTP request and
 * response headers, respectively.
 *
 * @since 6.0.300
 */
@WebService(
    serviceName = "JaxWsConsumerTestService",
    portName = "JaxWsConsumerTestServicePort",
    targetNamespace = "http://consumer.jaxws.scout.eclipse.org/JaxWsConsumerTestService/",
    endpointInterface = "org.eclipse.scout.jaxws.consumer.jaxwsconsumertestservice.JaxWsConsumerTestServicePortType",
    wsdlLocation = "WEB-INF/wsdl/JaxWsConsumerTestService.wsdl")
public class JaxWsConsumerTestServiceProvider implements JaxWsConsumerTestServicePortType {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsConsumerTestServiceProvider.class);

  @Resource
  WebServiceContext wsCtx;

  @Override
  public EchoResponse echo(EchoRequest req) {
    Assertions.assertNotNull(req);
    LOG.info("echo '{}'", req.getMessage());
    EchoResponse resp = new EchoResponse();
    resp.setMessage(req.getMessage());
    return resp;
  }

  @Override
  public SleepResponse sleep(SleepRequest req) {
    Assertions.assertNotNull(req);
    SleepResponse resp = new SleepResponse();
    if (req.getMillis() <= 0) {
      resp.setMessage("sleep time millis is <= 0");
    }
    else {
      SleepUtil.sleepSafe(req.getMillis(), TimeUnit.MILLISECONDS);
      resp.setMessage("slept...");
    }
    return resp;
  }

  @Override
  public GetHeaderResponse getHeader(GetHeaderRequest req) {
    Assertions.assertNotNull(req);
    GetHeaderResponse resp = new GetHeaderResponse();

    @SuppressWarnings("unchecked")
    Map<String, List<String>> httpRequestHeaderMap = (Map<String, List<String>>) wsCtx.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
    List<String> values = httpRequestHeaderMap.get(req.getHeaderName());
    if (CollectionUtility.isEmpty(values)) {
      resp.setHeaderSet(false);
    }
    else {
      StringBuilder sb = new StringBuilder();
      for (String s : values) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(s == null ? "<null>" : s);
      }
      resp.setHeaderSet(true);
      resp.setHeaderValue(sb.toString());
    }

    LOG.info("get header ['{}'='{}', headerSet={}]", req.getHeaderName(), resp.getHeaderValue(), resp.isHeaderSet());
    return resp;
  }

  @Override
  public SetHeaderResponse setHeader(SetHeaderRequest parameters) {
    Assertions.assertNotNull(parameters);
    final String headerName = parameters.getHeaderName();
    final String headerValue = parameters.getHeaderValue();
    LOG.info("set header ['{}'='{}']", headerName, headerValue);

    SetHeaderResponse resp = new SetHeaderResponse();

    @SuppressWarnings("unchecked")
    Map<String, List<String>> httpResonseHeaderMap = (Map<String, List<String>>) wsCtx.getMessageContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
    if (httpResonseHeaderMap != null) {
      httpResonseHeaderMap.put(headerName, CollectionUtility.arrayList(headerValue));
      resp.setMessage("ok");
    }
    else {
      resp.setMessage("nok -- response headers not available");
    }

    return resp;
  }
}
