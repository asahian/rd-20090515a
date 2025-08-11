package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class MotionComponent implements Component {
    public double motionX, motionY, motionZ;

    public MotionComponent(double motionX, double motionY, double motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }
}
