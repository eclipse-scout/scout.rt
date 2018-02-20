package org.eclipse.scout.rt.client.ui;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeTraversals;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 7.1
 */
public abstract class AbstractWidget extends AbstractPropertyObserver implements IWidget {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractWidget.class);

  public AbstractWidget() {
    this(true);
  }

  public AbstractWidget(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    if (isInitConfigDone()) {
      return;
    }
    initConfigInternal();
    setInitConfigDone(true);
  }

  /**
   * Will be called by {@link #callInitializer()} but only if {@link #isInitConfigDone()} returns false.
   */
  protected void initConfigInternal() {
    initConfig();
  }

  protected void initConfig() {
    setCssClass(getConfiguredCssClass());
  }

  @Override
  public boolean isInitConfigDone() {
    return propertySupport.getPropertyBool(PROP_INIT_CONFIG_DONE);
  }

  protected void setInitConfigDone(boolean initConfigDone) {
    propertySupport.setPropertyBool(PROP_INIT_CONFIG_DONE, initConfigDone);
  }

  @Override
  public final void init() {
    if (isInitDone()) {
      return;
    }
    doInit(true);
  }

  private void doInit(boolean recursive) {
    initInternal();
    if (recursive) {
      initChildren();
    }

    setInitDone(true);
    setDisposeDone(false);
  }

  protected void initChildren() {
    initChildren(getChildren());
  }

  protected void initChildren(List<? extends IWidget> widgets) {
    for (IWidget w : widgets) {
      w.init();
    }
  }

  /**
   * Will be called by {@link #init()} but only if {@link #isInitDone()} returns false.<br>
   * This method should initialize this instance and non-widget-children only. Child widgets will be initialized in
   * {@link #initChildren()}.
   */
  protected void initInternal() {
    // nop
  }

  @Override
  public boolean isInitDone() {
    return propertySupport.getPropertyBool(PROP_INIT_DONE);
  }

  protected void setInitDone(boolean initDone) {
    propertySupport.setPropertyBool(PROP_INIT_DONE, initDone);
  }

  @Override
  public void reinit() {
    setInitDone(false);
    doInit(false);
    for (IWidget w : getChildren()) {
      w.reinit();
    }
  }

  @Override
  public final void dispose() {
    if (isDisposeDone()) {
      return;
    }

    disposeChildren();

    try {
      disposeInternal();
    }
    catch (RuntimeException t) {
      LOG.warn("Could not dispose widget '{}'.", this, t);
    }

    setDisposeDone(true);
    setInitDone(false);
  }

  protected void disposeChildren() {
    disposeChildren(getChildren());
  }

  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    for (IWidget w : widgetsToDispose) {
      try {
        w.dispose();
      }
      catch (RuntimeException t) {
        LOG.warn("Could not dispose widget '{}'.", w, t);
      }
    }
  }

  /**
   * Will be called by {@link #dispose()} but only if {@link #isDisposeDone()} returns false.<br>
   * This method should only dispose this single instance and non-widget children. Widget children are disposed
   * automatically in {@link #disposeChildren()}.
   */
  protected void disposeInternal() {
    // nop
  }

  @Override
  public boolean isDisposeDone() {
    return propertySupport.getPropertyBool(PROP_DISPOSE_DONE);
  }

  protected void setDisposeDone(boolean disposeDone) {
    propertySupport.setPropertyBool(PROP_DISPOSE_DONE, disposeDone);
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type) {
    return visit(new WidgetVisitorTypeAdapter<T>(visitor, type));
  }

  @Override
  public <T extends IWidget> void visit(Consumer<T> visitor, Class<T> type) {
    assertNotNull(visitor);
    visit(new WidgetVisitorTypeAdapter<>(widget -> {
      visitor.accept(widget);
      return TreeVisitResult.CONTINUE;
    }, type));
  }

  @Override
  public void visit(Consumer<IWidget> visitor) {
    assertNotNull(visitor);
    visit(widget -> {
      visitor.accept(widget);
      return TreeVisitResult.CONTINUE;
    });
  }

  @Override
  public TreeVisitResult visit(Function<IWidget, TreeVisitResult> visitor) {
    return visit(WidgetVisitorTypeAdapter.functionToVisitor(visitor));
  }

  @Override
  public <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Class<T> type) {
    return visit(new WidgetVisitorTypeAdapter<>(visitor, type));
  }

  @Override
  public TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor) {
    return visit(visitor, IWidget::getChildren);
  }

  @SuppressWarnings("unchecked")
  protected <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Function<T, List<? extends IWidget>> childrenSupplier, Class<T> type) {
    IDepthFirstTreeVisitor<IWidget> widgetVisitorTypeAdapter = new WidgetVisitorTypeAdapter<T>(visitor, type);
    Function<IWidget, List<? extends IWidget>> cs = widget -> {
      if (type.isAssignableFrom(widget.getClass())) {
        childrenSupplier.apply((T) widget);
      }
      return widget.getChildren();
    };
    return visit(widgetVisitorTypeAdapter, cs);
  }

  protected TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor, Function<IWidget, List<? extends IWidget>> childrenSupplier) {
    return TreeTraversals.create(visitor, childrenSupplier).traverse(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IWidget> TreeVisitResult visit(IBreadthFirstTreeVisitor<T> visitor, Class<T> type) {
    return visit((widget, level, index) -> {
      if (type.isAssignableFrom(widget.getClass())) {
        return visitor.visit((T) widget, level, index);
      }
      return TreeVisitResult.CONTINUE;
    });
  }

  @Override
  public TreeVisitResult visit(IBreadthFirstTreeVisitor<IWidget> visitor) {
    return TreeTraversals.create(visitor, IWidget::getChildren).traverse(this);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.emptyArrayList(); // by default a widget has no children
  }

  /**
   * Configures the css class(es) of this widget.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(55)
  protected String getConfiguredCssClass() {
    return null;
  }

  @Override
  public String getCssClass() {
    return propertySupport.getPropertyString(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    propertySupport.setPropertyString(PROP_CSS_CLASS, cssClass);
  }
}
