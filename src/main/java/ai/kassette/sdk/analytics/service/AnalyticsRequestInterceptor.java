package ai.kassette.sdk.analytics.service;

import java.io.IOException;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

class AnalyticsRequestInterceptor implements Interceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private final @NotNull String writeKey;
    private final @NotNull String userAgent;

    AnalyticsRequestInterceptor(@NotNull String writeKey, @NotNull String userAgent) {
        this.writeKey = writeKey;
        this.userAgent = userAgent;
    }

    @NotNull
    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request newRequest =
                request
                        .newBuilder()
                        .addHeader(AUTHORIZATION_HEADER, Credentials.basic(writeKey, ""))
                        .addHeader(USER_AGENT_HEADER, userAgent)
                        .build();

        return chain.proceed(newRequest);
    }
}