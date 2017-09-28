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
package org.eclipse.scout.rt.ui.html.selenium.junit;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumDriver;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

/**
 * Catches browser log and prints it to STDOUT. Requires setup of selenium logging preferences, see
 * {@link SeleniumDriver}.
 */
public class BrowserLogRule extends TestWatcher {

  private final WebDriver m_driver;
  private Date m_start;

  public BrowserLogRule(AbstractSeleniumTest test) {
    m_driver = test.getDriver();
  }

  @Override
  protected void starting(Description description) {
    m_start = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    System.out.println(MessageFormat.format("v | {1}.{2} [{0}]",
        sdf.format(m_start), description.getClassName(), description.getMethodName()));
  }

  @Override
  protected void finished(Description description) {
    Date end = new Date();
    long duration = end.getTime() - m_start.getTime();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    LogEntries logEntries = m_driver.manage().logs().get(LogType.BROWSER);
    for (LogEntry logEntry : logEntries) {
      System.out.println(sdf.format(new Date(logEntry.getTimestamp())) + " " + logEntry.getLevel() + " " + logEntry.getMessage());
    }
    System.out.println(MessageFormat.format("^ | {1}.{2} [{0}] (took {3} ms)",
        sdf.format(end), description.getClassName(), description.getMethodName(), duration));
  }
}
