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
package org.eclipse.scout.rt.shared.services.common.test;

import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingStatus;

/**
 * {@link IProcessingStatus} object with additional properties "product" and
 * "subTitle" and "duration". <br>
 * The property "subTitle" is the test optional sub title. <br>
 * The property "severity" is the test result. <br>
 * The property "message" is the (optional) test result message. <br>
 * <p>
 * see {@link ITest} for details
 */
public class TestStatus extends ProcessingStatus {
  private static final long serialVersionUID = 1L;

  private String m_product;
  private String m_subTitle;
  private long m_duration = -1;

  public TestStatus(String product, String title, String subTitle) {
    super("", INFO);
    setProduct(product);
    setTitle(title);
    setSubTitle(subTitle);
  }

  public String getProduct() {
    return m_product;
  }

  public void setProduct(String s) {
    m_product = s;
  }

  public String getSubTitle() {
    return m_subTitle;
  }

  public void setSubTitle(String s) {
    m_subTitle = s;
  }

  public long getDuration() {
    return m_duration;
  }

  public void setDuration(long d) {
    m_duration = d;
  }

  @Override
  public void setMessage(String message) {
    super.setMessage(message != null ? message : "");
  }

  public static String getSeverityAsText(int severity) {
    if (severity == TestStatus.INFO) {
      return "OK";
    }
    else if (severity == TestStatus.WARNING) {
      return "WARNING";
    }
    else if (severity == TestStatus.ERROR) {
      return "ERROR";
    }
    else if (severity == TestStatus.FATAL) {
      return "FATAL";
    }
    else {
      return "UNKNOWN[" + severity + "]";
    }
  }
}
