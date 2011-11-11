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
package org.eclipse.scout.jaxws216.service;

import java.net.URL;

import org.eclipse.scout.service.IService;

public interface IWebServiceClient extends IService {

  public String getUsername();

  public void setUsername(String username);

  public String getPassword();

  public void setPassword(String password);

  public URL getWsdlLocation();

  public void setWsdlLocation(URL wsdlLocation);

  public String getTargetNamespace();

  public void setTargetNamespace(String targetNamespace);

  public String getServiceName();

  public void setServiceName(String serviceName);

  public String getUrl();

  public void setUrl(String url);

  public Integer getRequestTimeout();

  public void setRequestTimeout(Integer requestTimeout);

  public Integer getConnectTimeout();

  public void setConnectTimeout(Integer connectTimeout);
}
