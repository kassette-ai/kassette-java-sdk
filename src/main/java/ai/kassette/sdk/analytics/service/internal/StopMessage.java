package ai.kassette.sdk.analytics.service.internal;


import ai.kassette.sdk.analytics.messages.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;


class StopMessage implements Message {
    static final StopMessage STOP = new StopMessage();

    private StopMessage() {}

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

    @NotNull
    @Override
    public Map<String, ?> context() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String anonymousId() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String userId() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Map<String, Object> integrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "StopMessage{}";
    }
}
