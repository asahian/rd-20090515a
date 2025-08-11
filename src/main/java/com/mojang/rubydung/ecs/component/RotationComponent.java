package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class RotationComponent implements Component {
    public float xRotation, yRotation;
    public float prevXRotation, prevYRotation;

    public RotationComponent(float xRotation, float yRotation) {
        this.xRotation = this.prevXRotation = xRotation;
        this.yRotation = this.prevYRotation = yRotation;
    }
}
