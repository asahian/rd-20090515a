package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class OnGroundComponent implements Component {
    public boolean onGround;

    public OnGroundComponent(boolean onGround) {
        this.onGround = onGround;
    }
}
