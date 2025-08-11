package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class RotationComponent implements Component {
    public float xRotation, yRotation;

    public RotationComponent(float xRotation, float yRotation) {
        this.xRotation = xRotation;
        this.yRotation = yRotation;
    }
}
