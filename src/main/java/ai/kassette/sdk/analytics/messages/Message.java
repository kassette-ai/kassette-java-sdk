package ai.kassette.sdk.analytics.messages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Date;
import java.util.Map;


/**
 */
public interface Message {
    @NotNull
    Type type();

    @NotNull
    String messageId();

    @Nullable
    Date sentAt();

    @NotNull
    Date timestamp();

    @Nullable
    Map<String, ?> context();

    @Nullable
    String anonymousId();

    @Nullable
    String userId();

    @Nullable
    Map<String, Object> integrations();

    enum Type {
        audit,
        reporting,
        alias,

    }
}
