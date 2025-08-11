package com.mojang.rubydung.level;

import com.mojang.rubydung.level.tile.Tile;
import com.mojang.rubydung.phys.AABB;
import com.mojang.rubydung.render.ChunkMesh;

public class Chunk {

    private static final Tessellator TESSELLATOR = new Tessellator();

    /**
     * Global rebuild statistic
     */
    public static int rebuiltThisFrame;
    public static int updates;
    public long dirtiedTime;

    /**
     * Internal rebuild statistic
     */
    private static long totalTime;
    private static int totalUpdates;

    /**
     * The game level
     */
    private final Level level;

    /**
     * Bounding box values
     */
    public AABB boundingBox;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final float x, y, z;

    /**
     * Rendering states
     */
    private final ChunkMesh[] chunkMeshes = new ChunkMesh[2];
    private boolean dirty = true;

    /**
     * Chunk containing a part of the tiles in a level
     *
     * @param level The game level
     * @param minX  Minimal location X
     * @param minY  Minimal location Y
     * @param minZ  Minimal location Z
     * @param maxX  Maximal location X
     * @param maxY  Maximal location Y
     * @param maxZ  Maximal location Z
     */
    public Chunk(Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.level = level;

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        // Center of chunk
        this.x = (minX + maxX) / 2.0f;
        this.y = (minY + maxY) / 2.0f;
        this.z = (minZ + maxZ) / 2.0f;

        // Create chunk meshes
        this.chunkMeshes[0] = new ChunkMesh();
        this.chunkMeshes[1] = new ChunkMesh();

        // Create bounding box object of chunk
        this.boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Render all tiles in this chunk
     *
     * @param layer The layer of the chunk (For shadows)
     */
    public void rebuild(int layer) {
        if (rebuiltThisFrame == 2) {
            // Rebuild limit reached for this frame
            return;
        }

        // Update global stats
        updates++;
        rebuiltThisFrame++;

        // Mark chunk as no longer dirty
        this.dirty = false;

        // Tile render counter
        int tiles = 0;
        long timeRebuildStart = System.nanoTime();

        // Setup tile rendering
        TESSELLATOR.init();

        // For each tile in this chunk
        for (int x = this.minX; x < this.maxX; ++x) {
            for (int y = this.minY; y < this.maxY; ++y) {
                for (int z = this.minZ; z < this.maxZ; ++z) {
                    int tileId = this.level.getTile(x, y, z);

                    // Is a tile at this location?
                    if (tileId > 0) {
                        // Render the tile
                        Tile.tiles[tileId].render(TESSELLATOR, this.level, layer, x, y, z);

                        // Increase tile render counter
                        tiles++;
                    }
                }
            }
        }

        // Finish tile rendering
        this.chunkMeshes[layer].rebuild(TESSELLATOR);

        // Update chunk update counter
        if (tiles > 0) {
            totalTime += System.nanoTime() - timeRebuildStart;
            totalUpdates++;
        }
    }

    /**
     * Rebuild the chunk for all layers
     */
    public void rebuild() {
        rebuild(0);
        rebuild(1);
    }

    /**
     * Get chunk mesh by layer
     *
     * @param layer The layer of the chunk mesh
     * @return The chunk mesh
     */
    public ChunkMesh getChunkMesh(int layer) {
        return this.chunkMeshes[layer];
    }

    /**
     * Mark chunk as dirty. The chunk will rebuild in the next frame
     */
    public void setDirty() {
        if (!this.dirty) {
            this.dirtiedTime = System.currentTimeMillis();
        }

        this.dirty = true;
    }

    /**
     * State of the chunk for rebuild
     *
     * @return Chunk is dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Calculate squared distance to the player
     *
     * @param x The player x for the location
     * @param y The player y for the location
     * @param z The player z for the location
     * @return The squared distance from the center of the chunk to the player
     */
    public double distanceToSqr(double x, double y, double z) {
        double distanceX = x - this.x;
        double distanceY = y - this.y;
        double distanceZ = z - this.z;
        return distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
    }

    /**
     * Destroy the chunk meshes
     */
    public void destroy() {
        this.chunkMeshes[0].destroy();
        this.chunkMeshes[1].destroy();
    }
}
