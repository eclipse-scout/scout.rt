package org.eclipse.scout.rt.client.extension.ui.basic.activitymap;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.activitymap.AbstractActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.MajorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.MinorTimeColumn;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ActivityMapChains {

  private ActivityMapChains() {
  }

  protected abstract static class AbstractActivityMapChain<RI, AI> extends AbstractExtensionChain<IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> {

    public AbstractActivityMapChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions, IActivityMapExtension.class);
    }
  }

  public static class ActivityMapDecorateMinorTimeColumnChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapDecorateMinorTimeColumnChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateMinorTimeColumn(final TimeScale scale, final MajorTimeColumn majorColumn, final MinorTimeColumn minorColumn) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execDecorateMinorTimeColumn(ActivityMapDecorateMinorTimeColumnChain.this, scale, majorColumn, minorColumn);
        }
      };
      callChain(methodInvocation, scale, majorColumn, minorColumn);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapActivityCellSelectedChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapActivityCellSelectedChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execActivityCellSelected(final ActivityCell<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execActivityCellSelected(ActivityMapActivityCellSelectedChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapDisposeActivityMapChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapDisposeActivityMapChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDisposeActivityMap() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execDisposeActivityMap(ActivityMapDisposeActivityMapChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapCreateTimeScaleChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapCreateTimeScaleChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public TimeScale execCreateTimeScale() throws ProcessingException {
      MethodInvocation<TimeScale> methodInvocation = new MethodInvocation<TimeScale>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          setReturnValue(next.execCreateTimeScale(ActivityMapCreateTimeScaleChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class ActivityMapDecorateActivityCellChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapDecorateActivityCellChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateActivityCell(final ActivityCell<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execDecorateActivityCell(ActivityMapDecorateActivityCellChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapInitActivityMapChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapInitActivityMapChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execInitActivityMap() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execInitActivityMap(ActivityMapInitActivityMapChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapCellActionChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapCellActionChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execCellAction(final RI resourceId, final MinorTimeColumn column, final ActivityCell<RI, AI> activityCell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execCellAction(ActivityMapCellActionChain.this, resourceId, column, activityCell);
        }
      };
      callChain(methodInvocation, resourceId, column, activityCell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class ActivityMapDecorateMajorTimeColumnChain<RI, AI> extends AbstractActivityMapChain<RI, AI> {

    public ActivityMapDecorateMajorTimeColumnChain(List<? extends IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateMajorTimeColumn(final TimeScale scale, final MajorTimeColumn columns) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IActivityMapExtension<RI, AI, ? extends AbstractActivityMap<RI, AI>> next) throws ProcessingException {
          next.execDecorateMajorTimeColumn(ActivityMapDecorateMajorTimeColumnChain.this, scale, columns);
        }
      };
      callChain(methodInvocation, scale, columns);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
