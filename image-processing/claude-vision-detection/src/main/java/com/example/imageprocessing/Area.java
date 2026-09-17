package com.example.imageprocessing;

/**
 * A detected area, as a pixel bounding box with top-left origin
 * (x increases right, y increases down) - the same convention used
 * by java.awt.Graphics2D, so it can be drawn directly.
 */
public record Area(String name, int x, int y, int width, int height) {
}
