package ai.kassette.sdk.analytics.service.internal;


import ai.kassette.sdk.analytics.messages.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;


class FlushMessage implements Message {
    static final FlushMessage POISON = new FlushMessage();

    private FlushMessage() {}

    @NotNull
    @Override
    public Type type() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String messageId() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Date sentAt() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public String channel() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Date timestamp() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Map<String, ?> context() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String anonymousId() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String userId() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Map<String, Object> integrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "FlushMessage{}";
    }
}
