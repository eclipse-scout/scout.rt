import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;
import org.eclipse.scout.rt.shared.ISession;

/**
 * {@link IAccessControlService} service that uses {@link ISession#getUserId()} as internal cache key required by
 * {@link AbstractAccessControlService} implementation.
 * <p>
 * Replace this service at server side to load permission collection. It is <b>not</b> required to implement
 * {@link #execLoadPermissions(String)} at client side.
 *
 * @author ${userName}
 */
public class AccessControlService extends AbstractAccessControlService<String> {

  @Override
  protected String getCurrentUserCacheKey() {
    return Users.CURRENT.get();
  }

  @Override
  protected IPermissionCollection execLoadPermissions(String userId) {
    return null;
  }
}
