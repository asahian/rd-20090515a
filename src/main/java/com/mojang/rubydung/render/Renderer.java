package com.mojang.rubydung.render;

import com.mojang.rubydung.Textures;
import com.mojang.rubydung.level.Chunk;
import com.mojang.rubydung.level.Frustum;
import com.mojang.rubydung.level.Level;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    private final Level level;

    public Renderer(Level level) {
        this.level = level;
    }

    public void render(int layer) {
        // Get camera frustum
        Frustum frustum = Frustum.getFrustum();

        // Enable texture
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, Textures.loadTexture("/terrain.png", GL_NEAREST));

        // For each chunk in level
        for (Chunk chunk : this.level.getChunks()) {
            // Render chunk if in frustum
            if (frustum.isVisible(chunk.boundingBox)) {
                chunk.getChunkMesh(layer).render();
            }
        }

        // Disable texture
        glDisable(GL_TEXTURE_2D);
    }

    public void destroy() {
        // For each chunk in level
        for (Chunk chunk : this.level.getChunks()) {
            chunk.destroy();
        }
    }
}
