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

import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IEvalVisitor;
import org.eclipse.scout.rt.server.scheduler.internal.visitor.IFormatVisitor;

/**
 * @deprecated will be removed in release 6.1; use {@link IJobManager} instead, which provides you support for triggered
 *             execution via Quartz schedule plans; see {@link JobInput#withExecutionTrigger(...)};
 */
@SuppressWarnings("deprecation")
@Deprecated
public final class Op {

  private Op() {
  }

  public static class And extends AbstractOperation {

    public And(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("&&");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toBoolean(a) && v.toBoolean(b);
    }
  }

  public static class Or extends AbstractOperation {

    public Or(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("||");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      if ((a instanceof String) || (b instanceof String)) {
        return StringUtility.emptyIfNull(a) + StringUtility.emptyIfNull(b);
      }
      else {
        return v.toBoolean(a) || v.toBoolean(b);
      }
    }
  }

  public static class Equal extends AbstractOperation {

    public Equal(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("==");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return CompareUtility.equals(a, b);
    }
  }

  public static class NotEqual extends AbstractOperation {

    public NotEqual(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("!=");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return !CompareUtility.equals(a, b);
    }
  }

  public static class LessThanOrEqual extends AbstractOperation {
    public LessThanOrEqual(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("<=");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) <= v.toInt(b);
    }
  }

  public static class GreaterThanOrEqual extends AbstractOperation {
    public GreaterThanOrEqual(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print(">=");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) >= v.toInt(b);
    }
  }

  public static class LessThan extends AbstractOperation {
    public LessThan(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("<");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) < v.toInt(b);
    }
  }

  public static class GreaterThan extends AbstractOperation {
    public GreaterThan(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print(">");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) > v.toInt(b);
    }
  }

  public static class Add extends AbstractOperation {
    public Add(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("+");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) + v.toInt(b);
    }
  }

  public static class Sub extends AbstractOperation {
    public Sub(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("-");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) - v.toInt(b);
    }
  }

  public static class Mul extends AbstractOperation {
    public Mul(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("*");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) * v.toInt(b);
    }
  }

  public static class Div extends AbstractOperation {
    public Div(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("/");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) / v.toInt(b);
    }
  }

  public static class Mod extends AbstractOperation {
    public Mod(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("%");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) % v.toInt(b);
    }
  }

  public static class BitXor extends AbstractOperation {
    public BitXor(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("^");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) ^ v.toInt(b);
    }
  }

  public static class BitOr extends AbstractOperation {
    public BitOr(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("|");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) | v.toInt(b);
    }
  }

  public static class BitAnd extends AbstractOperation {
    public BitAnd(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("&");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) & v.toInt(b);
    }
  }

  public static class BitShiftLeft extends AbstractOperation {
    public BitShiftLeft(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print("<<");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) << v.toInt(b);
    }
  }

  public static class BitShiftRight extends AbstractOperation {
    public BitShiftRight(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print(">>");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) >> v.toInt(b);
    }
  }

  public static class BitShiftRightZeroExtending extends AbstractOperation {
    public BitShiftRightZeroExtending(INode leftCmd, INode rightCmd) {
      super(leftCmd, rightCmd);
    }

    @Override
    public void formatOpImpl(IFormatVisitor v) {
      v.print(">>>");
    }

    @Override
    public Object evalImpl(IEvalVisitor v, Object a, Object b) {
      return v.toInt(a) >>> v.toInt(b);
    }
  }
}
