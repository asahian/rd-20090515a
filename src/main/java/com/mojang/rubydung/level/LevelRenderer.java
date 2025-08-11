package com.mojang.rubydung.level;

import com.mojang.rubydung.HitResult;
import com.mojang.rubydung.ecs.Entity;
import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.BoundingBoxComponent;
import com.mojang.rubydung.level.tile.Tile;
import com.mojang.rubydung.phys.AABB;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class LevelRenderer implements LevelListener {

    private final Tessellator tessellator;
    private final Level level;

    /**
     * Create renderer for level
     *
     * @param level The rendered level
     */
    public LevelRenderer(Level level) {
        level.addListener(this);

        this.tessellator = new Tessellator();
        this.level = level;
    }

    /**
     * Get all chunks with dirty flag
     *
     * @return List of dirty chunks
     */
    public List<Chunk> getAllDirtyChunks() {
        ArrayList<Chunk> dirty = new ArrayList<>();
        for (final Chunk chunk : this.level.getChunks()) {
            if (chunk.isDirty()) {
                dirty.add(chunk);
            }
        }
        return dirty;
    }

    /**
     * Rebuild all dirty chunks in a sorted order
     *
     * @param player The player for the sort priority. Chunks closer to the player will get a higher priority.
     */
    public void updateDirtyChunks(Entity player, World world) {
        // Reset global chunk rebuild stats
        Chunk.rebuiltThisFrame = 0;

        // Get all dirty chunks
        List<Chunk> dirty = getAllDirtyChunks();
        if (!dirty.isEmpty()) {

            // Sort the dirty chunk list
            dirty.sort(new DirtyChunkSorter(player, world, Frustum.getFrustum()));

            // Rebuild max 8 chunks per frame
            for (int i = 0; i < 8 && i < dirty.size(); i++) {
                dirty.get(i).rebuild();
            }
        }
    }

    /**
     * Render pick selection face on tile
     *
     * @param player The player
     */
    public void pick(Entity player, World world) {
        var radius = 3.0F;
        BoundingBoxComponent boundingBoxComponent = world.getComponent(player, BoundingBoxComponent.class);
        var boundingBox = boundingBoxComponent.boundingBox.grow(radius, radius, radius);

        var minX = (int) boundingBox.minX();
        var maxX = (int) (boundingBox.maxX() + 1.0f);
        var minY = (int) boundingBox.minY();
        var maxY = (int) (boundingBox.maxY() + 1.0f);
        var minZ = (int) boundingBox.minZ();
        var maxZ = (int) (boundingBox.maxZ() + 1.0f);

        glInitNames();
        for (int x = minX; x < maxX; x++) {
            // Name value x
            glPushName(x);
            for (int y = minY; y < maxY; y++) {
                // Name value y
                glPushName(y);
                for (int z = minZ; z < maxZ; z++) {
                    // Name value z
                    glPushName(z);

                    // Check for solid tile
                    if (this.level.isSolidTile(x, y, z)) {

                        // Name value type
                        glPushName(0);

                        // Render all faces
                        for (int face = 0; face < 6; face++) {

                            // Name value face id
                            glPushName(face);

                            // Render selection face
                            this.tessellator.init();
                            Tile.rock.renderFaceNoTexture(this.tessellator, x, y, z, face);
                            this.tessellator.flush();

                            glPopName();
                        }
                        glPopName();
                    }
                    glPopName();
                }
                glPopName();
            }
            glPopName();
        }
    }

    /**
     * Render hit face of the result
     *
     * @param hitResult The hit result to render
     */
    public void renderHit(HitResult hitResult) {
        // Setup blending and color
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_CURRENT_BIT);
        glColor4f(1.0F, 1.0F, 1.0F, ((float)Math.sin(System.currentTimeMillis() / 100.0D) * 0.2F + 0.4F) * 0.5F);

        // Render face
        this.tessellator.init();
        Tile.rock.renderFaceNoTexture(this.tessellator, hitResult.x(), hitResult.y(), hitResult.z(), hitResult.face());
        this.tessellator.flush();

        // Disable blending
        glDisable(GL_BLEND);
    }

    @Override
    public void lightColumnChanged(int x, int z, int minY, int maxY) {
        this.level.setDirty(x - 1, minY - 1, z - 1, x + 1, maxY + 1, z + 1);
    }

    @Override
    public void tileChanged(int x, int y, int z) {
        this.level.setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void allChanged() {
        this.level.setDirty(0, 0, 0, this.level.width, this.level.depth, this.level.height);
    }
}
