package ai.kassette.sdk.analytics.service;


import ai.kassette.sdk.analytics.Beta;
import ai.kassette.sdk.analytics.messages.*;


/** Intercept every message before it is built in order to add additional data. */
@Beta
public interface MessageTransformer {
    /**
     * Called for every builder. This will be called on the same thread the request was made and
     * before any {@link MessageInterceptor}'s are called. Returning {@code false} will skip
     * processing this message any further.
     */
    boolean transform(MessageBuilder builder);

    /**
     * A {@link MessageTransformer} that lets you implement more strongly typed methods and add
     * transformations specific to the event type.
     */
    abstract class Typed implements MessageTransformer {

        @Override
        public final boolean transform(MessageBuilder builder) {
            // todo: non final so messages can be filtered without duplicating logic?
            Message.Type type = builder.type();
            switch (type) {
                case alias:
                    return alias((AliasMessage.Builder) builder);
                case reporting:
                    return reporting((ReportingMessage.Builder) builder);
                case audit:
                    return audit((AuditMessage.Builder) builder);

                default:
                    throw new IllegalArgumentException("Unknown payload type: " + type);
            }
        }

        /** Called for every {@link AliasMessage}. */
        abstract boolean alias(AliasMessage.Builder builder);

        /** Called for every {@link AuditMessage}. */
        abstract boolean audit(AuditMessage.Builder builder);

        /** Called for every {@link ReportingMessage}. */
        abstract boolean reporting(ReportingMessage.Builder builder);


    }
}
