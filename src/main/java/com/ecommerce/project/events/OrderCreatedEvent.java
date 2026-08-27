package com.ecommerce.project.events;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(UUID eventId,Long orderId,
                                Long userId,
                                Instant occurredAt) {
}
