package ai.kassette.sdk.analytics.messages;

import ai.kassette.sdk.analytics.gson.AutoGson;
import com.google.auto.value.AutoValue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@AutoValue
@AutoGson
public abstract class Batch {
    private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();

    public static Batch create(Map<String, ?> context, List<Message> batch) {
        Message sentAtNull =
                batch.stream().filter(message -> message.sentAt() != null).findAny().orElse(null);

        return new AutoValue_Batch(
                batch,
                sentAtNull == null ? new Date() : sentAtNull.sentAt(),
                context,
                SEQUENCE_GENERATOR.incrementAndGet());
    }

    public abstract List<Message> batch();

    public abstract Date sentAt();

    public abstract Map<String, ?> context();

    public abstract int sequence();
}