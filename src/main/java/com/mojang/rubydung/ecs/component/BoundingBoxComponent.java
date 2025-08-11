package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;
import com.mojang.rubydung.phys.AABB;

public class BoundingBoxComponent implements Component {
    public AABB boundingBox;

    public BoundingBoxComponent(AABB boundingBox) {
        this.boundingBox = boundingBox;
    }
}
