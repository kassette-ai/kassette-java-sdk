package ai.kassette.sdk.analytics.service;

import ai.kassette.sdk.analytics.AnalyticsVersion;
import ai.kassette.sdk.analytics.Beta;
import ai.kassette.sdk.analytics.gson.AutoValueAdapterFactory;
import ai.kassette.sdk.analytics.gson.ISO8601DateAdapter;
import ai.kassette.sdk.analytics.http.KassetteService;
import ai.kassette.sdk.analytics.messages.Message;
import ai.kassette.sdk.analytics.messages.MessageBuilder;
import ai.kassette.sdk.analytics.service.internal.AnalyticsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.Nullable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * The entry point into the Rudder for Java library.
 *
 * <p>The idea is simple: one pipeline for all your data. Rudder is the single hub to collect,
 * translate and route your data with the flip of a switch.
 *
 * <p>Analytics for Java will automatically batch events and upload it periodically to Rudder's
 * servers for you. You only need to instrument Rudder once, then flip a switch to install new
 * tools.
 *
 * <p>This class is the main entry point into the client API. Use {@link #builder} to construct your
 * own instances.
 *
 * @see <a href="https://Rudder/">Rudder</a>
 */
public class KassetteAnalytics {
  private final AnalyticsClient client;
  private final List<MessageTransformer> messageTransformers;
  private final List<MessageInterceptor> messageInterceptors;
  private final Log log;

  KassetteAnalytics(
      AnalyticsClient client,
      List<MessageTransformer> messageTransformers,
      List<MessageInterceptor> messageInterceptors,
      Log log) {
    this.client = client;
    this.messageTransformers = messageTransformers;
    this.messageInterceptors = messageInterceptors;
    this.log = log;
  }

  /**
   * Start building an {@link KassetteAnalytics} instance.
   *
   * @param writeKey Your project write key available on the Rudder dashboard.
   */
  public static Builder builder(String writeKey) {
    return new Builder(writeKey);
  }

  /** Enqueue the given message to be uploaded to Rudder's servers. */
  public void enqueue(MessageBuilder builder) {
    Message message = buildMessage(builder);
    if (message == null) {
      return;
    }
    client.enqueue(message);
  }

  /**
   * Inserts the message into queue if it is possible to do so immediately without violating
   * capacity restrictions, returning {@code true} upon success and {@code false} if no space is
   * currently available.
   *
   * @param builder
   */
  public boolean offer(MessageBuilder builder) {
    Message message = buildMessage(builder);
    if (message == null) {
      return false;
    }
    return client.offer(message);
  }

  /** Flush events in the message queue. */
  public void flush() {
    client.flush();
  }

  /** Stops this instance from processing further requests. */
  public void shutdown() {
    client.shutdown();
  }

  /**
   * Helper method to build message
   *
   * @param builder
   * @return Instance of Message if valid message can be build null if skipping this message
   */
  private Message buildMessage(MessageBuilder builder) {
    for (MessageTransformer messageTransformer : messageTransformers) {
      boolean shouldContinue = messageTransformer.transform(builder);
      if (!shouldContinue) {
        log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
        return null;
      }
    }
    Message message = builder.build();
    for (MessageInterceptor messageInterceptor : messageInterceptors) {
      message = messageInterceptor.intercept(message);
      if (message == null) {
        log.print(Log.Level.VERBOSE, "Skipping message %s.", builder);
        return null;
      }
    }
    return message;
  }

  /** Fluent API for creating {@link KassetteAnalytics} instances. */
  public static class Builder {
    private static final String DEFAULT_ENDPOINT = "https://hosted.rudderlabs.com/";
    private static final String DEFAULT_PATH = "v1/batch";
    private static final String DEFAULT_USER_AGENT = "analytics-java/" + AnalyticsVersion.get();
    private static final int MESSAGE_QUEUE_MAX_BYTE_SIZE = 1024 * 500;

    private final String writeKey;
    private OkHttpClient client;
    private Log log;
    /**
     * &#064;Deprecated  use {@link #getDataPlaneUrl()}
     */
    @Deprecated public HttpUrl endpoint;
    public HttpUrl uploadURL;
    private String userAgent = DEFAULT_USER_AGENT;
    private List<MessageTransformer> messageTransformers;
    private List<MessageInterceptor> messageInterceptors;
    private ExecutorService networkExecutor;
    private ThreadFactory threadFactory;
    private int flushQueueSize;
    private int maximumFlushAttempts;
    private int maximumQueueSizeInBytes;
    private long flushIntervalInMillis;
    private List<Callback> callbacks;
    private int queueCapacity;
    private boolean forceTlsV1 = false;
    private boolean gzip = true;

    Builder(String writeKey) {
      if (writeKey == null || writeKey.trim().length() == 0) {
        throw new NullPointerException("writeKey cannot be null or empty.");
      }
      this.writeKey = writeKey;
    }

    /** Set a custom networking client. */
    public Builder client(OkHttpClient client) {
      if (client == null) {
        throw new NullPointerException("Null client");
      }
      this.client = client;
      return this;
    }

    /** Disable the GZIP client. */
    public Builder setGZIP(boolean gzip) {
      this.gzip = gzip;
      return this;
    }

    /** Configure debug logging mechanism. By default, nothing is logged. */
    public Builder log(Log log) {
      if (log == null) {
        throw new NullPointerException("Null log");
      }
      this.log = log;
      return this;
    }

    /**
     * Set an endpoint (host only) that this client should upload events to. Uses {@code
     * http://hosted.rudderlabs.com} by default.
     *
     * &#064;Deprecated  use {@link #setDataPlaneUrl(String)}
     */
    @Deprecated
    public Builder endpoint(String endpoint) {
      return setDataPlaneUrl(endpoint);
    }

    /**
     * Set a dataPlaneUrl that this client should upload events to.
     */
    public Builder setDataPlaneUrl(String dataPlaneUrl) {
      if (dataPlaneUrl == null || dataPlaneUrl.trim().length() == 0) {
        throw new NullPointerException("dataPlaneUrl cannot be null or empty.");
      }
      if (! dataPlaneUrl.endsWith("/")){
        dataPlaneUrl += "/";
      }
      this.endpoint = HttpUrl.parse(dataPlaneUrl + DEFAULT_PATH);
      return this;
    }

    /**
     *
     * @return dataPlaneUrl (your data-plane url)
     */
    @Nullable
    public HttpUrl getDataPlaneUrl() {
      return this.endpoint;
    }

    /**
     * Set an endpoint (host and prefix) that this client should upload events to. Uses {@code
     *https://hosted.rudderlabs.com/v1} by default.
     *
     * Segment compatibility
     */
    public Builder setUploadURL(String uploadURL) {
      if (uploadURL == null || uploadURL.trim().length() == 0) {
        throw new NullPointerException("Upload URL cannot be null or empty.");
      }
      this.uploadURL = HttpUrl.parse(uploadURL);
      return this;
    }

    /** Sets a user agent for HTTP requests. */
    public Builder userAgent(String userAgent) {
      if (userAgent == null || userAgent.trim().length() == 0) {
        throw new NullPointerException("userAgent cannot be null or empty.");
      }
      this.userAgent = userAgent;
      return this;
    }

    /** Add a {@link MessageTransformer} for transforming messages. */
    @Beta
    public Builder messageTransformer(MessageTransformer transformer) {
      if (transformer == null) {
        throw new NullPointerException("Null transformer");
      }
      if (messageTransformers == null) {
        messageTransformers = new ArrayList<>();
      }
      if (messageTransformers.contains(transformer)) {
        throw new IllegalStateException("MessageTransformer is already registered.");
      }
      messageTransformers.add(transformer);
      return this;
    }

    /** Add a {@link MessageInterceptor} for intercepting messages. */
    @Beta
    public Builder messageInterceptor(MessageInterceptor interceptor) {
      if (interceptor == null) {
        throw new NullPointerException("Null interceptor");
      }
      if (messageInterceptors == null) {
        messageInterceptors = new ArrayList<>();
      }
      if (messageInterceptors.contains(interceptor)) {
        throw new IllegalStateException("MessageInterceptor is already registered.");
      }
      messageInterceptors.add(interceptor);
      return this;
    }

    /** Set queue capacity */
    public Builder queueCapacity(int capacity) {
      if (capacity <= 0) {
        throw new IllegalArgumentException("capacity should be positive.");
      }
      this.queueCapacity = capacity;
      return this;
    }
    /** Set the queueSize at which flushes should be triggered. */
    @Beta
    public Builder flushQueueSize(int flushQueueSize) {
      if (flushQueueSize < 1) {
        throw new IllegalArgumentException("flushQueueSize must not be less than 1.");
      }
      this.flushQueueSize = flushQueueSize;
      return this;
    }

    /** Set the queueSize at which flushes should be triggered. */
    @Beta
    public Builder maximumQueueSizeInBytes(int bytes) {
      if (bytes < 1) {
        throw new IllegalArgumentException("maximumQueueSizeInBytes must not be less than 1.");
      }

      this.maximumQueueSizeInBytes = bytes;
      return this;
    }

    /** Set the interval at which the queue should be flushed. */
    @Beta
    public Builder flushInterval(long flushInterval, TimeUnit unit) {
      long flushIntervalInMillis = unit.toMillis(flushInterval);
      if (flushIntervalInMillis < 1000) {
        throw new IllegalArgumentException("flushInterval must not be less than 1 second.");
      }
      this.flushIntervalInMillis = flushIntervalInMillis;
      return this;
    }

    /** Set how many retries should happen before getting exhausted */
    public Builder retries(int maximumRetries) {
      if (maximumRetries < 1) {
        throw new IllegalArgumentException("retries must be at least 1");
      }
      this.maximumFlushAttempts = maximumRetries;
      return this;
    }

    /** Set the {@link ExecutorService} on which all HTTP requests will be made. */
    public Builder networkExecutor(ExecutorService networkExecutor) {
      if (networkExecutor == null) {
        throw new NullPointerException("Null networkExecutor");
      }
      this.networkExecutor = networkExecutor;
      return this;
    }

    /** Set the {@link ThreadFactory} used to create threads. */
    @Beta
    public Builder threadFactory(ThreadFactory threadFactory) {
      if (threadFactory == null) {
        throw new NullPointerException("Null threadFactory");
      }
      this.threadFactory = threadFactory;
      return this;
    }

    /** Add a {@link Callback} to be notified when an event is processed. */
    public Builder callback(Callback callback) {
      if (callback == null) {
        throw new NullPointerException("Null callback");
      }
      if (callbacks == null) {
        callbacks = new ArrayList<>();
      }
      if (callbacks.contains(callback)) {
        throw new IllegalStateException("Callback is already registered.");
      }
      callbacks.add(callback);
      return this;
    }

    /** Use a {@link Plugin} to configure the builder. */
    @Beta
    public Builder plugin(Plugin plugin) {
      if (plugin == null) {
        throw new NullPointerException("Null plugin");
      }
      plugin.configure(this);
      return this;
    }

    /** Specify if need TlsV1 */
    public Builder forceTlsVersion1() {
      forceTlsV1 = true;
      return this;
    }

    /** Create a {@link KassetteAnalytics} client. */
    public KassetteAnalytics build() {
      Gson gson =
          new GsonBuilder() //
              .registerTypeAdapterFactory(new AutoValueAdapterFactory()) //
              .registerTypeAdapter(Date.class, new ISO8601DateAdapter()) //
              .create();

      if (endpoint == null) {
        if (uploadURL != null) {
          endpoint = uploadURL;
        } else {
          endpoint = HttpUrl.parse(DEFAULT_ENDPOINT + DEFAULT_PATH);
        }
      }

      if (client == null) {
        client = Platform.get().defaultClient(this.gzip);
      }

      if (log == null) {
        log = Log.NONE;
      }
      if (flushIntervalInMillis == 0) {
        flushIntervalInMillis = Platform.get().defaultFlushIntervalInMillis();
      }
      if (queueCapacity == 0) {
        queueCapacity = Integer.MAX_VALUE;
      }
      if (flushQueueSize == 0) {
        flushQueueSize = Platform.get().defaultFlushQueueSize();
      }
      if (maximumQueueSizeInBytes == 0) {
        maximumQueueSizeInBytes = MESSAGE_QUEUE_MAX_BYTE_SIZE;
      }
      if (maximumFlushAttempts == 0) {
        maximumFlushAttempts = 3;
      }
      if (messageTransformers == null) {
        messageTransformers = Collections.emptyList();
      } else {
        messageTransformers = Collections.unmodifiableList(messageTransformers);
      }
      if (messageInterceptors == null) {
        messageInterceptors = Collections.emptyList();
      } else {
        messageInterceptors = Collections.unmodifiableList(messageInterceptors);
      }
      if (networkExecutor == null) {
        networkExecutor = Platform.get().defaultNetworkExecutor();
      }
      if (threadFactory == null) {
        threadFactory = Platform.get().defaultThreadFactory();
      }
      if (callbacks == null) {
        callbacks = Collections.emptyList();
      } else {
        callbacks = Collections.unmodifiableList(callbacks);
      }

      HttpLoggingInterceptor interceptor =
          new HttpLoggingInterceptor(
              new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                  log.print(Log.Level.VERBOSE, "%s", message);
                }
              });

      interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

      OkHttpClient.Builder builder =
          client
              .newBuilder()
              .addInterceptor(new AnalyticsRequestInterceptor(writeKey, userAgent))
              .addInterceptor(interceptor);


      client = builder.build();

      Retrofit restAdapter =
          new Retrofit.Builder()
              .addConverterFactory(GsonConverterFactory.create(gson))
              .baseUrl(DEFAULT_ENDPOINT)
              .client(client)
              .build();

      KassetteService rudderService = restAdapter.create(KassetteService.class);

      AnalyticsClient analyticsClient =
          AnalyticsClient.create(
              endpoint,
              rudderService,
              queueCapacity,
              flushQueueSize,
              flushIntervalInMillis,
              maximumFlushAttempts,
              maximumQueueSizeInBytes,
              log,
              threadFactory,
              networkExecutor,
              callbacks);

      return new KassetteAnalytics(analyticsClient, messageTransformers, messageInterceptors, log);
    }
  }
}
