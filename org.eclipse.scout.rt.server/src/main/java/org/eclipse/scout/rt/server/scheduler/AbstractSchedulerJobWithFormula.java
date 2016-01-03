/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.scheduler;

import java.text.ParseException;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.util.VerboseUtility;
import org.eclipse.scout.rt.server.scheduler.internal.FormulaParser;
import org.eclipse.scout.rt.server.scheduler.internal.SchedulingFormula;

/**
 * Special job with a formula that defines the pattern interval
 *
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public abstract class AbstractSchedulerJobWithFormula extends AbstractSchedulerJob {
  private ISchedulingFormula m_formulaCompiled;
  private String m_action;
  private Object[] m_args;

  /**
   * see {@link AbstractSchedulerJobWithFormula(String, String, String, String, Object[])}
   */
  public AbstractSchedulerJobWithFormula(String groupId, String jobId, String formula) {
    this(groupId, jobId, formula, null);
  }

  /**
   * @param formula
   *          see syntax in {@link com.bsiag.scheduler.formula.FormulaParser} FORMULA is a boolean expression that can
   *          use the following variables: second (0..59) minute (0..59) hour (0..23) day (1..31) week (1..52) month
   *          (1..12) year (1900..9999) dayOfWeek (1..7) 1 is monday dayOfMonthReverse (1..31) 1 is last day of month
   *          dayOfYear (1..365) arg0....argN where arg0 is the value of param args[0],... Examples of a formula:
   *          (second==0) && (minute % 2 == 0) //every even minute (second==0) && (minute==0) && (hour==13) && (day==1)
   *          //every first day of each month at 13:00:00
   */
  public AbstractSchedulerJobWithFormula(String groupId, String jobId, String formula, String action, Object... args) {
    super(groupId, jobId);
    m_action = action;
    m_args = args;
    m_formulaCompiled = createFormula(formula);
  }

  public ISchedulingFormula getFormula() {
    return m_formulaCompiled;
  }

  public String getAction() {
    return m_action;
  }

  public Object[] getArgs() {
    return m_args;
  }

  @Override
  public final boolean acceptTick(TickSignal signal) {
    return m_formulaCompiled.eval(signal, getArgs());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getGroupId() + "." + getJobId() + " formula=" + m_formulaCompiled + ", action=" + m_action + ", args=" + VerboseUtility.dumpObjects(m_args) + "]";
  }

  public static ISchedulingFormula createFormula(String formula) {
    try {
      return new SchedulingFormula(new FormulaParser().parse(formula));
    }
    catch (ParseException e) {
      throw new ProcessingException("invalid formula: " + formula, e);
    }
  }
}
