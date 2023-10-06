package ai.kassette.sdk.analytics.messages;

import ai.kassette.sdk.analytics.gson.AutoGson;
import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;

@AutoValue
@AutoGson //
public abstract class AuditMessage implements Message {

    /**
     * Start building an {@link AuditMessage} instance.
     *
     * @param event The event is the name of the action that a user has performed.
     * @throws IllegalArgumentException if the event name is null or empty
     * @see <a href="https://rudder.com/docs/spec/track/#event">Track Event</a>
     */
    public static Builder builder(String event) {
        return new Builder(event);
    }

    public abstract String event();

    @Nullable
    public abstract Map<String, ?> properties();

    public Builder toBuilder() {
        return new Builder(this);
    }

    /** Fluent API for creating {@link AuditMessage} instances. */
    public static class Builder extends MessageBuilder<AuditMessage, Builder> {
        private String event;
        private Map<String, ?> properties;

        private Builder(AuditMessage track) {
            super(track);
            event = track.event();
            properties = track.properties();
        }

        private Builder(String event) {
            super(Type.audit);
            if (isNullOrEmpty(event)) {
                throw new IllegalArgumentException("event cannot be null or empty.");
            }
            this.event = event;
        }

        /**
         * Set a map of information that describe the event. These can be anything you want.
         *
         * @see <a href="https://rudder.com/docs/spec/track/#properties">Properties</a>
         */
        public Builder properties(Map<String, ?> properties) {
            if (properties == null) {
                throw new NullPointerException("Null properties");
            }
            this.properties = ImmutableMap.copyOf(properties);
            return this;
        }

        @Override
        Builder self() {
            return this;
        }

        @Override
        protected AuditMessage realBuild(
                Type type,
                String messageId,
                Date sentAt,
                String channel,
                Date timestamp,
                Map<String, ?> context,
                String anonymousId,
                String userId,
                Map<String, Object> integrations) {
            return new AutoValue_AuditMessage(
                    type,
                    messageId,
                    sentAt,
                    timestamp,
                    context,
                    anonymousId,
                    userId,
                    integrations,
                    event,
                    properties);
        }
    }
}
