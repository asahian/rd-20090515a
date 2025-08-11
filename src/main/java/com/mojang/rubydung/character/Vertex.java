package com.mojang.rubydung.character;

public record Vertex(Vec3 position, float u, float v) {

    /**
     * A vertex contains a 3 float vector position and UV coordinates
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param u U mapping
     * @param v V mapping
     */
    public Vertex(float x, float y, float z, float u, float v) {
        this(new Vec3(x, y, z), u, v);
    }

    /**
     * A vertex contains a 3 float vector position and UV coordinates
     *
     * @param vertex Vertex for the position
     * @param u      U mapping
     * @param v      V mapping
     */
    public Vertex(Vertex vertex, float u, float v) {
        this(vertex.position(), u, v);
    }

    /**
     * Create a new vertex of the current one with different UV mappings
     *
     * @param u New U mapping
     * @param v New V mapping
     * @return New vertex with the vector position of the current one
     */
    public Vertex remap(float u, float v) {
        return new Vertex(this, u, v);
    }
}
