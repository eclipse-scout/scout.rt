package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

/**
 * Represents a strategy to fetch lookup rows.
 */
public interface ILookupRowProvider<LOOKUP_KEY> {

  /**
   * Method invoked before fetching lookup rows.
   */
  void beforeProvide(ILookupCall<LOOKUP_KEY> lookupCall);

  /**
   * Method invoked after fetching lookup rows, but before the result is returned.
   */
  void afterProvide(ILookupCall<LOOKUP_KEY> lookupCall, List<ILookupRow<LOOKUP_KEY>> result);

  /**
   * Invoke to load lookup rows. This method must be called from within a session aware {@link ClientRunContext}.
   */
  List<ILookupRow<LOOKUP_KEY>> provide(ILookupCall<LOOKUP_KEY> lookupCall);

  /**
   * For legacy reasons<br>
   * Invoke to load lookup rows synchronously in the current thread. This method must be called from within a session
   * aware {@link ClientRunContext}.
   * <p>
   * Upon loading finished, the given callback is notified, either in the current thread if being the model thread, or
   * in the model thread as specified by the current {@link ClientRunContext}.
   */
  void provideSync(ILookupCall<LOOKUP_KEY> lookupCall, ILookupRowFetchedCallback<LOOKUP_KEY> callback);

  /**
   * For legacy reasons<br>
   * Invoke to load lookup rows asynchronously. Upon loading finished, the given callback is notified in the model
   * thread as specified by the given session aware {@link ClientRunContext}.
   */
  IFuture<List<? extends ILookupRow<LOOKUP_KEY>>> provideAsync(ILookupCall<LOOKUP_KEY> lookupCall, ILookupRowFetchedCallback<LOOKUP_KEY> callback, ClientRunContext clientRunContext);
}
