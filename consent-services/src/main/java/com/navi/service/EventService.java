package com.navi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.repository.ConsentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.navi.domain.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final ConsentEventRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public void append(UUID consentId, EventType eventType, ActorType actor, String requestId, Object payload) {
        JsonNode payloadJson = null;
        try {
            if (payload != null) payloadJson = om.valueToTree(payload);
        } catch (Exception ignored) {}

        repo.save(ConsentEventEntity.builder()
                .eventId(UUID.randomUUID())
                .consentId(consentId)
                .eventType(eventType.name())
                .occurredAt(OffsetDateTime.now())
                .requestId(requestId)
                .actor(actor.name())
                .payloadJson(payloadJson)
                .published(false)
                .build());
    }
}
