package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class PositionComponent implements Component {
    public double x, y, z;
    public double prevX, prevY, prevZ;

    public PositionComponent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }
}
