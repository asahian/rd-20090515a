package com.mojang.rubydung.level;

import com.mojang.rubydung.level.tile.Tile;
import com.mojang.rubydung.phys.AABB;
import com.mojang.rubydung.render.ChunkMesh;

import java.nio.FloatBuffer;

public class Chunk {

    /**
     * Global rebuild statistic
     */
    public static int updates;
    public long dirtiedTime;

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
    private final FloatBuffer[] buffers = new FloatBuffer[2];
    private final int[] vertexCounts = new int[2];
    private boolean rebuilt = false;
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
     * Tessellate all tiles in this chunk (Worker Thread)
     *
     * @param layer       The layer of the chunk (For shadows)
     * @param tessellator The tessellator to use
     */
    public void rebuild(int layer, Tessellator tessellator) {
        updates++;

        // Setup tile rendering
        tessellator.init();

        // For each tile in this chunk
        for (int x = this.minX; x < this.maxX; ++x) {
            for (int y = this.minY; y < this.maxY; ++y) {
                for (int z = this.minZ; z < this.maxZ; ++z) {
                    int tileId = this.level.getTile(x, y, z);

                    // Is a tile at this location?
                    if (tileId > 0) {
                        // Render the tile
                        Tile.tiles[tileId].render(tessellator, this.level, layer, x, y, z);
                    }
                }
            }
        }

        // Store result
        this.buffers[layer] = tessellator.getBuffer();
        this.vertexCounts[layer] = tessellator.getVertexCount();
    }

    /**
     * Upload the tessellated mesh to the GPU (Main Thread)
     */
    public void rebuild() {
        // Not rebuilt yet
        if (!this.rebuilt) {
            return;
        }

        // Upload layers
        this.chunkMeshes[0].rebuild(this.buffers[0], this.vertexCounts[0]);
        this.chunkMeshes[1].rebuild(this.buffers[1], this.vertexCounts[1]);

        // Mark as no longer rebuilt
        this.rebuilt = false;
    }

    /**
     * Mark this chunk as rebuilt (called by worker thread)
     */
    public void setRebuilt() {
        this.rebuilt = true;
        this.dirty = false;
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
     * State of the chunk for upload
     *
     * @return Chunk is rebuilt
     */
    public boolean isRebuilt() {
        return rebuilt;
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
