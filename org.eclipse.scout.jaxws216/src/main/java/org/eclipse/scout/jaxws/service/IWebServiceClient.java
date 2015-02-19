/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.service;

import java.net.URL;

import org.eclipse.scout.service.IService;

public interface IWebServiceClient extends IService {

  String getUsername();

  void setUsername(String username);

  String getPassword();

  void setPassword(String password);

  URL getWsdlLocation();

  void setWsdlLocation(URL wsdlLocation);

  String getTargetNamespace();

  void setTargetNamespace(String targetNamespace);

  String getServiceName();

  void setServiceName(String serviceName);

  String getUrl();

  void setUrl(String url);

  Integer getRequestTimeout();

  void setRequestTimeout(Integer requestTimeout);

  Integer getConnectTimeout();

  void setConnectTimeout(Integer connectTimeout);
}
