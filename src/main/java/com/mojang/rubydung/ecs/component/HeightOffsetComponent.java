package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class HeightOffsetComponent implements Component {
    public float heightOffset;

    public HeightOffsetComponent(float heightOffset) {
        this.heightOffset = heightOffset;
    }
}
