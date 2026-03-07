package com.example.opsmonitor.api.support;

public record PageMeta(
    int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {}
