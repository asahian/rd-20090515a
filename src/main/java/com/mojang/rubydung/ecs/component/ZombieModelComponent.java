package com.mojang.rubydung.ecs.component;

import com.mojang.rubydung.character.Cube;
import com.mojang.rubydung.ecs.Component;

public class ZombieModelComponent implements Component {
    public Cube head;
    public Cube body;
    public Cube rightArm;
    public Cube leftArm;
    public Cube rightLeg;
    public Cube leftLeg;
    public float timeOffset = (float) (Math.random() * 1239813.0F);
    public float speed = 1.0F;
}
