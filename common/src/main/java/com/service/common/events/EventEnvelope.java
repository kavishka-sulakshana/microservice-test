package com.service.common.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private String eventId;
    private String eventType;
    private Integer eventVersion;

    private String aggregateId;
    private String aggregateType;

    private Instant occurredAt;

    private String source;
    private String traceId;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class",
            defaultImpl = Object.class
    )
    private T payload;
}
