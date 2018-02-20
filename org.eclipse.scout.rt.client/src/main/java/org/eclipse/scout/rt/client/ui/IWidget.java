package org.eclipse.scout.rt.client.ui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.IDepthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

/**
 * Root interface for Scout UI components.
 *
 * @since 7.1
 */
public interface IWidget extends IPropertyObserver, IStyleable {

  String PROP_INIT_CONFIG_DONE = "initConfigDone";
  String PROP_INIT_DONE = "initDone";
  String PROP_DISPOSE_DONE = "disposeDone";

  /**
   * @return {@code true} if {@link #initConfig()} has been called, {@code false} if not.
   */
  boolean isInitConfigDone();

  /**
   * Init is supposed to be called after the widget has been created. This is usually done by the framework itself. E.g.
   * if the widgets are used on a form, the form will initialize all the widgets on form startup.
   * <p>
   * After the execution of this method, {@link #isInitDone()} will return true. A repeated execution of {@link #init()}
   * will do nothing unless initDone would be set to false again which is done by {@link #reinit()} and
   * {@link #dispose()}. This means a widget may be initialized again after it has been disposed.
   * <p>
   * This method initializes this widget and all child widgets recursively.
   */
  void init();

  /**
   * @return {@code true} if {@link #init()} has been called, {@code false} if not. Also returns false if the widget has
   *         been disposed.
   */
  boolean isInitDone();

  /**
   * Re initializes the widget all children by setting {@link #isInitDone()} to {@code false} and calling
   * {@link #init()} again.
   */
  void reinit();

  /**
   * Dispose needs to be called if the widget should not be used anymore in order to release any bound resources. This
   * is usually done by the framework itself. E.g. if the widgets are used on a form, the form will dispose all the
   * widgets when the form closes.
   * <p>
   * After the execution of this method, {@link #isDisposeDone()} will return true. A repeated execution of
   * {@link #dispose()} will do nothing unless the disposeDone would be set to false which is done by {@link #init()}.
   * This means a widget may be initialized again after it has been disposed.
   * <p>
   * By calling this method the complete widget tree is disposed recursively.
   */
  void dispose();

  /**
   * @return {@code true} if {@link #dispose()} has been called, {@code false} if not. Also returns {@code false} if the
   *         widget has been initialized again after it was disposed.
   */
  boolean isDisposeDone();

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *          The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Consumer} is never
   *          {@code null}.
   * @throws AssertionException
   *           if the visitor is {@code null}.
   */
  void visit(Consumer<IWidget> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once. The specified visitor is only
   * called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *          The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Consumer} is never
   *          {@code null}.
   * @param <T>
   *          The type of widget that should be visited.
   * @param type
   *          The type of widget that should be visited.
   * @throws AssertionException
   *           if one of the arguments is {@code null}.
   */
  <T extends IWidget> void visit(Consumer<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively according to the
   * {@link TreeVisitResult} returned by the specified {@link Function}.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *          The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Function} is never
   *          {@code null}. The {@link Function} returns a {@link TreeVisitResult} to control the traversal.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws AssertionException
   *           if the visitor is {@code null}.
   * @see TreeVisitResult
   */
  TreeVisitResult visit(Function<IWidget, TreeVisitResult> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively according to the
   * {@link TreeVisitResult} returned by the specified {@link Function}.
   * <p>
   * The visit performs a pre-order traversal. Every {@link IWidget} is only visited once. The specified visitor is only
   * called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *          The visitor to call. Must not be {@code null}. The {@link IWidget} passed to the {@link Function} is never
   *          {@code null}. The {@link Function} returns a {@link TreeVisitResult} to control the traversal.
   * @param type
   *          The type of widget that should be visited.
   * @param <T>
   *          The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link Function}.
   * @throws AssertionException
   *           if one of the arguments is {@code null}.
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(Function<T, TreeVisitResult> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The specified {@link IDepthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IDepthFirstTreeVisitor#preVisit(T, int, int)}. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *          The {@link IDepthFirstTreeVisitor} to call. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IDepthFirstTreeVisitor}.
   * @throws AssertionException
   *           if the visitor is {@code null}.
   * @see IDepthFirstTreeVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IDepthFirstTreeVisitor<IWidget> visitor);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively.
   * <p>
   * The specified {@link IDepthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IDepthFirstTreeVisitor#preVisit(T, int, int)}. Every {@link IWidget} is only visited once. The
   * specified visitor is only called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *          The {@link IDepthFirstTreeVisitor} to call. Must not be {@code null}.
   * @param type
   *          The type of widget that should be visited.
   * @param <T>
   *          The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IDepthFirstTreeVisitor}.
   * @throws AssertionException
   *           if one of the arguments is {@code null}.
   * @see IDepthFirstTreeVisitor
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(IDepthFirstTreeVisitor<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively using a level-order strategy.
   * <p>
   * The specified {@link IBreadthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IBreadthFirstTreeVisitor#visit(T, int, int)}. Every {@link IWidget} is only visited once. The
   * specified visitor is only called for {@link IWidget}s that are of the specified type.
   *
   * @param visitor
   *          The {@link IBreadthFirstTreeVisitor} to use. Must not be {@code null}.
   * @param type
   *          The type of widget that should be visited.
   * @param <T>
   *          The type of widget that should be visited.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IBreadthFirstTreeVisitor}.
   * @throws AssertionException
   *           if one of the arguments is {@code null}.
   * @see IBreadthFirstTreeVisitor
   * @see TreeVisitResult
   */
  <T extends IWidget> TreeVisitResult visit(IBreadthFirstTreeVisitor<T> visitor, Class<T> type);

  /**
   * Visits this {@link IWidget} and all of its child {@link IWidget}s recursively using a level-order strategy.
   * <p>
   * The specified {@link IBreadthFirstTreeVisitor} controls the visited elements using the {@link TreeVisitResult}
   * returned by {@link IBreadthFirstTreeVisitor#visit(T, int, int)}. Every {@link IWidget} is only visited once.
   *
   * @param visitor
   *          The {@link IBreadthFirstTreeVisitor} to use. Must not be {@code null}.
   * @return The {@link TreeVisitResult} of the last call to the specified {@link IBreadthFirstTreeVisitor}.
   * @throws AssertionException
   *           if the visitor is {@code null}.
   * @see IBreadthFirstTreeVisitor
   * @see TreeVisitResult
   */
  TreeVisitResult visit(IBreadthFirstTreeVisitor<IWidget> visitor);

  /**
   * @return All child {@link IWidget}s. The resulting {@link List} is never {@code null} and does not contain any
   *         {@code null} elements.
   */
  List<? extends IWidget> getChildren();
}
