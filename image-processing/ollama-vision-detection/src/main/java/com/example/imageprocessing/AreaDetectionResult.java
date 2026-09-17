package com.example.imageprocessing;

import java.util.List;

/**
 * The Case 1 output written to disk. sourceImage is measured locally from
 * the actual image file (ground truth), not reported by the model - vision
 * models aren't reliable at knowing exact pixel dimensions.
 */
public record AreaDetectionResult(ImageDimensions sourceImage, List<Area> areas) {
}
