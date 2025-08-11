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

    public ZombieModelComponent() {
        // Create head cube
        this.head = new Cube(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8);

        // Create body cube
        this.body = new Cube(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4);

        // Right arm cube
        this.rightArm = new Cube(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
        this.rightArm.setPosition(-5.0F, 2.0F, 0.0F);

        // Left arm cube
        this.leftArm = new Cube(40, 16)
                .addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
        this.leftArm.setPosition(5.0F, 2.0F, 0.0F);

        // Right Legs cube
        this.rightLeg = new Cube(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        this.rightLeg.setPosition(-2.0F, 12.0F, 0.0F);

        // Left leg cube
        this.leftLeg = new Cube(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        this.leftLeg.setPosition(2.0F, 12.0F, 0.0F);
    }
}
