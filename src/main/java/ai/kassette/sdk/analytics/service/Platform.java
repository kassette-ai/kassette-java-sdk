package ai.kassette.sdk.analytics.service;

import static java.lang.Thread.MIN_PRIORITY;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

class Platform {
  static final String THREAD_NAME = "Analytics";

  private static final Platform PLATFORM = findPlatform();

  static Platform get() {
    return PLATFORM;
  }

  private static Platform findPlatform() {
    return new Platform();
  }

  OkHttpClient defaultClient(boolean enableGZIP) {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS);

    if (enableGZIP) {
      builder.addInterceptor(new GzipRequestInterceptor());
    }
    OkHttpClient client = builder.build();
    return client;
  }

  ExecutorService defaultNetworkExecutor() {
    return Executors.newSingleThreadExecutor(defaultThreadFactory());
  }

  ThreadFactory defaultThreadFactory() {
    return new ThreadFactory() {
      @Override
      public Thread newThread(final Runnable r) {
        return new Thread(
            new Runnable() {
              @Override
              public void run() {
                Thread.currentThread().setPriority(MIN_PRIORITY);
                r.run();
              }
            },
            THREAD_NAME);
      }
    };
  }

  public long defaultFlushIntervalInMillis() {
    return 10 * 1000; // 10s
  }

  public int defaultFlushQueueSize() {
    return 250;
  }
}
