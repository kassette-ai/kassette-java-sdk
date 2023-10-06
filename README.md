# What is Kassette?

[Kassette](https://kassette.ai/) is a **workforce data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

## Kassette's Java SDK

Kassette’s Java SDK allows you to track your workforce event data from your Java code. Once enabled, the event requests hit the Kassette servers. Kassette then routes the events to the specified destination platforms as configured by you.


## Getting Started with the Kassette's Java SDK

*Add to `pom.xml`:*

```xml
<dependency>
    <groupId>com.kassette.ai.java.analytics</groupId>
    <artifactId>analytics</artifactId>
    <version>1.0.0</version>
</dependency>

```

*or if you're using Gradle:*

```bash
implementation 'com.kassette.ai.sdk.java.analytics:analytics:1.0.0'
```

## Initializing ```KassetteClient```

```java 
KassetteAnalytics analytics = KassetteAnalytics
         .builder("<WRITE_KEY>")
         .setDataPlaneUrl("<DATA_PLANE_URL>")
         .build();
```

## Sending events

```java
Map<String, Object> map = new HashMap<>();
map.put("name", "John Marshal");
map.put("email", "john@example.com");
analytics.enqueue(ReportingMessage.builder()
        .userId("6754ds7d9")
        .traits(map)
);
```


