import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;

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
