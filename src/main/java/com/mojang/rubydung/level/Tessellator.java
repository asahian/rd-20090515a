package com.mojang.rubydung.level;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Tessellator {

    private static final int MAX_VERTICES = 100000;

    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 9);

    private int vertices = 0;

    // Texture
    private float textureU;
    private float textureV;

    // Color
    private float red;
    private float green;
    private float blue;
    private float alpha = 1.0f;

    // States
    private boolean hasTexture = false;
    private boolean hasColor = false;

    /**
     * Reset the buffer
     */
    public void init() {
        clear();
    }

    /**
     * Add a vertex point to buffer
     *
     * @param x Vertex point x
     * @param y Vertex point y
     * @param z Vertex point z
     */
    public void vertex(float x, float y, float z) {
        // Add vertex data to buffer
        this.buffer.put(x).put(y).put(z);

        // Add texture coordinates to buffer
        if (this.hasTexture) {
            this.buffer.put(this.textureU).put(this.textureV);
        } else {
            this.buffer.put(0.0f).put(0.0f);
        }

        // Add color to buffer
        if (this.hasColor) {
            this.buffer.put(this.red).put(this.green).put(this.blue).put(this.alpha);
        } else {
            this.buffer.put(1.0f).put(1.0f).put(1.0f).put(1.0f);
        }

        this.vertices++;
    }

    /**
     * Add a vertex and set the texture UV mappings
     *
     * @param x        Vertex point x
     * @param y        Vertex point y
     * @param z        Vertex point z
     * @param textureU Texture U point
     * @param textureV Texture V point
     */
    public void vertexUV(float x, float y, float z, float textureU, float textureV) {
        texture(textureU, textureV);
        vertex(x, y, z);
    }

    /**
     * Set texture UV mappings
     *
     * @param textureU Texture U point
     * @param textureV Texture V point
     */
    public void texture(float textureU, float textureV) {
        this.hasTexture = true;
        this.textureU = textureU;
        this.textureV = textureV;
    }

    /**
     * Set the RGB color
     *
     * @param red   Red (0.0 - 1.0)
     * @param green Green (0.0 - 1.0)
     * @param blue  Blue (0.0 - 1.0)
     */
    public void color(float red, float green, float blue) {
        this.color(red, green, blue, 1.0f);
    }

    /**
     * Set the RGBA color
     *
     * @param red   Red (0.0 - 1.0)
     * @param green Green (0.0 - 1.0)
     * @param blue  Blue (0.0 - 1.0)
     * @param alpha Alpha (0.0 - 1.0)
     */
    public void color(float red, float green, float blue, float alpha) {
        this.hasColor = true;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /**
     * Render the buffer
     */
    public void flush() {
        this.buffer.flip();

        // Enable client states
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        // Set pointers
        int stride = 36;
        this.buffer.position(0);
        glVertexPointer(3, stride, this.buffer.asReadOnlyBuffer());

        this.buffer.position(3);
        glTexCoordPointer(2, stride, this.buffer.asReadOnlyBuffer());

        this.buffer.position(5);
        glColorPointer(4, stride, this.buffer.asReadOnlyBuffer());

        // Draw quads
        glDrawArrays(GL_QUADS, 0, this.vertices);

        // Disable client states
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);

        clear();
    }

    /**
     * Reset vertex buffer
     */
    private void clear() {
        this.buffer.clear();
        this.vertices = 0;
        this.hasTexture = false;
        this.hasColor = false;
    }

    public FloatBuffer getBuffer() {
        this.buffer.flip();
        return this.buffer;
    }

    public int getVertexCount() {
        return this.vertices;
    }
}