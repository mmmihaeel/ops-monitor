package com.example.opsmonitor.api.support;

import java.time.Instant;

public record ResponseMeta(Instant timestamp, PageMeta page) {}
