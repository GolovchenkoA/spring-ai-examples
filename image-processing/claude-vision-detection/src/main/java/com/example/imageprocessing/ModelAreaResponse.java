package com.example.imageprocessing;

import java.util.List;

/**
 * Raw structured-output shape requested from the vision model - just the
 * areas it found. Wrapped in AreaDetectionResult (with locally-measured
 * image dimensions) before being written to disk.
 */
public record ModelAreaResponse(List<Area> areas) {
}
