package ai.kassette.sdk.analytics.service;

import ai.kassette.sdk.analytics.Beta;

/**
 * Plugins configure an {@link KassetteAnalytics.Builder} instance. Plugins can be used to consolidate logic
 * around building an analytics client into a single class.
 */
@Beta
public interface Plugin {
  void configure(KassetteAnalytics.Builder builder);
}
