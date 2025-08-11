package com.mojang.rubydung.render;

import com.mojang.rubydung.level.Tessellator;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class ChunkMesh {

    private int vboId = -1;
    private int vertexCount = 0;

    public void rebuild(FloatBuffer buffer, int vertexCount) {
        // Clear old vbo
        if (this.vboId != -1) {
            destroy();
        }

        // Create vbo
        this.vboId = glGenBuffers();
        this.vertexCount = vertexCount;

        // Upload vbo
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        if (this.vboId == -1) {
            return;
        }

        // Bind vbo
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        // Setup vertex pointers
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        // Draw vbo
        int stride = 36;
        glVertexPointer(3, GL_FLOAT, stride, 0);
        glTexCoordPointer(2, GL_FLOAT, stride, 12);
        glColorPointer(4, GL_FLOAT, stride, 20);
        glDrawArrays(GL_QUADS, 0, this.vertexCount);

        // Disable vertex pointers
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);

        // Unbind vbo
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void destroy() {
        if (this.vboId != -1) {
            glDeleteBuffers(this.vboId);
            this.vboId = -1;
        }
    }
}
