package com.example.imageprocessing;

/**
 * A detected area, as a quadrilateral of exactly 4 pixel points, ordered
 * clockwise starting from the top-left corner: top-left, top-right,
 * bottom-right, bottom-left. Top-left origin (x increases right, y
 * increases down), same convention as java.awt.Graphics2D.
 *
 * coordinates[i] is a 2-element [x, y] pair, e.g.:
 *   [[417, 211], [817, 211], [817, 611], [417, 611]]
 */
public record Area(String name, int[][] coordinates) {
}
