package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;

/**
 * Config property for {@link IDestination}s. For {@link IBiDestination}s (<i>request-reply messaging</i>), use
 * {@link AbstractBiDestinationConfigProperty}.
 * <p>
 * The expected format for config values is described here: {@link DestinationConfigPropertyParser}.
 * <p>
 * <h3>Example usage:</h3> <i>Application code:</i>
 *
 * <pre>
 * public class PersonChangesTopicConfig extends AbstractDestinationConfigProperty&lt;PersonBean&gt; {
 *
 *   &#64;Override
 *   public String getKey() {
 *     return "scout.mom.myMom.destination.personChangesTopic";
 *   }
 *
 *   &#64;Override
 *   protected IDestinationType getType() {
 *     return DestinationType.TOPIC;
 *   }
 * }
 *
 * ...
 *
 * // Usage
 * MOM.publish(MyMom.class, CONFIG.getPropertyValue(PersonChangesTopicConfig.class), thePerson);
 * </pre>
 *
 * <i>config.properties</i>:
 *
 * <pre>
 * scout.mom.myMom.destination.personChangesTopic=lookup:///myApp/topics/personChanges
 * </pre>
 *
 * @see IBiDestination
 * @see IDestination
 * @since 6.1
 */
public abstract class AbstractDestinationConfigProperty<DTO> extends AbstractConfigProperty<IDestination<DTO>> {

  // -----------------------------------------------------------------------------------
  // Implementation note: The same code exists in AbstractBiDestinationConfigProperty.
  // If you change something here, make sure to change it in the other class as well!
  // -----------------------------------------------------------------------------------

  @Override
  protected IDestination<DTO> parse(final String value) {
    final DestinationConfigPropertyParser p = BEANS.get(DestinationConfigPropertyParser.class).parse(value);
    return MOM.newDestination(p.getDestinationName(), getType(), p.getResolveMethod(), p.getParameters());
  }

  /**
   * @return The destination type (must not be <code>null</code>)
   */
  protected abstract IDestinationType getType();
}
