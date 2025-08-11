package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.ecs.Component;

public class ZombieAIComponent implements Component {
    public double rotation = Math.random() * Math.PI * 2;
    public double rotationMotionFactor = (Math.random() + 1.0) * 0.01F;
}
