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
package org.eclipse.scout.rt.server.scheduler.internal.node;

import org.eclipse.scout.rt.server.scheduler.internal.visitor.IEvalVisitor;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IFormatVisitor;

/**
 * always returns the referenced value except the method getReference() return the reference itself
 */
public class SignalRef extends AbstractNode implements INode {
  public static final int SECOND = 1;
  public static final int MINUTE = 2;
  public static final int HOUR = 3;
  public static final int DAY = 4;
  public static final int WEEK = 5;
  public static final int MONTH = 6;
  public static final int YEAR = 7;
  public static final int DAY_OF_WEEK = 8;
  public static final int DAY_OF_MONTH_REVERSE = 9;
  public static final int DAY_OF_YEAR = 10;
  public static final int SECOND_OF_DAY = 11;

  private int m_field;

  public SignalRef(int field) {
    m_field = field;
  }

  public int getField() {
    return m_field;
  }

  @Override
  public void format(IFormatVisitor v) {
    switch (m_field) {
      case SECOND: {
        v.print("second");
        break;
      }
      case MINUTE: {
        v.print("minute");
        break;
      }
      case HOUR: {
        v.print("hour");
        break;
      }
      case DAY: {
        v.print("day");
        break;
      }
      case WEEK: {
        v.print("week");
        break;
      }
      case MONTH: {
        v.print("month");
        break;
      }
      case YEAR: {
        v.print("year");
        break;
      }
      case DAY_OF_WEEK: {
        v.print("dayOfWeek");
        break;
      }
      case DAY_OF_MONTH_REVERSE: {
        v.print("dayOfMonthReverse");
        break;
      }
      case DAY_OF_YEAR: {
        v.print("dayOfYear");
        break;
      }
      case SECOND_OF_DAY: {
        v.print("secondOfDay");
        break;
      }
    }
  }

  @Override
  public Object eval(IEvalVisitor v) {
    switch (m_field) {
      case SECOND: {
        return v.getSignal().getSecond();
      }
      case MINUTE: {
        return v.getSignal().getMinute();
      }
      case HOUR: {
        return v.getSignal().getHour();
      }
      case DAY: {
        return v.getSignal().getDay();
      }
      case WEEK: {
        return v.getSignal().getWeek();
      }
      case MONTH: {
        return v.getSignal().getMonth();
      }
      case YEAR: {
        return v.getSignal().getYear();
      }
      case DAY_OF_WEEK: {
        return v.getSignal().getDayOfWeek();
      }
      case DAY_OF_MONTH_REVERSE: {
        return v.getSignal().getDayOfMonthReverse();
      }
      case DAY_OF_YEAR: {
        return v.getSignal().getDayOfYear();
      }
      case SECOND_OF_DAY: {
        return v.getSignal().getSecondOfDay();
      }
    }
    throw new IllegalArgumentException("unexpected field " + m_field);
  }

}
