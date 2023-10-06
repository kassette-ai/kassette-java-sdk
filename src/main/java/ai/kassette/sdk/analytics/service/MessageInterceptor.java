package ai.kassette.sdk.analytics.service;


import ai.kassette.sdk.analytics.Beta;
import ai.kassette.sdk.analytics.messages.AliasMessage;
import ai.kassette.sdk.analytics.messages.AuditMessage;
import ai.kassette.sdk.analytics.messages.Message;
import ai.kassette.sdk.analytics.messages.ReportingMessage;

/** Intercept every message after it is built to process it further. */
@Beta
public interface MessageInterceptor {
    /**
     * Called for every message. This will be called on the same thread the request was made and after
     * all {@link MessageTransformer}'s have been called. Returning {@code null} will skip processing
     * this message any further.
     */
    Message intercept(Message message);

    /**
     * A {@link MessageInterceptor} that lets you implement more strongly typed methods and add
     * transformations specific to the event type.
     */
    abstract class Typed implements MessageInterceptor {
        @Override
        public final Message intercept(Message message) {
            // todo: non final so messages can be filtered without duplicating logic?
            Message.Type type = message.type();
            switch (type) {
                case alias:
                    return alias((AliasMessage) message);
                case reporting:
                    return reporting((ReportingMessage) message);
                case audit:
                    return audit((AuditMessage) message);

                default:
                    throw new IllegalArgumentException("Unknown payload type: " + type);
            }
        }

        /** Called for every {@link AliasMessage}. */
        abstract AliasMessage alias(AliasMessage message);

        /** Called for every {@link AuditMessage}. */
        abstract AuditMessage audit(AuditMessage message);

        /** Called for every {@link ReportingMessage}. */
        abstract ReportingMessage reporting(ReportingMessage message);


    }
}
