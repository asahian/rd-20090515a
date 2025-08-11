package com.mojang.rubydung;

/**
 * Target tile over mouse
 *
 * @param x    Tile position x
 * @param y    Tile position y
 * @param z    Tile position z
 * @param type Type of result
 * @param face Face id of the tile
 */
public record HitResult(int x, int y, int z, int type, int face) {
}
