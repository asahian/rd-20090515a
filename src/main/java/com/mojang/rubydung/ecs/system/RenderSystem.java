package com.mojang.rubydung.ecs.system;

import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.Textures;
import com.mojang.rubydung.ecs.component.*;
import com.mojang.rubydung.level.Frustum;
import com.mojang.rubydung.level.Level;

import static org.lwjgl.opengl.GL11.*;

public class RenderSystem implements com.mojang.rubydung.ecs.System {

    private Level level;

    public RenderSystem() {
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void update(World world, float partialTicks) {
        // Not used
    }

    public void render(World world, float partialTicks, int layer) {
        Frustum frustum = Frustum.getFrustum();

        world.getEntitiesWith(ZombieComponent.class, PositionComponent.class, ZombieModelComponent.class, ZombieAIComponent.class, BoundingBoxComponent.class).forEach(entity -> {
            PositionComponent position = world.getComponent(entity, PositionComponent.class);
            ZombieModelComponent model = world.getComponent(entity, ZombieModelComponent.class);
            ZombieAIComponent ai = world.getComponent(entity, ZombieAIComponent.class);
            BoundingBoxComponent boundingBox = world.getComponent(entity, BoundingBoxComponent.class);

            boolean isLit = this.level.isLit((int) position.x, (int) position.y, (int) position.z);
            if ((layer == 0 && isLit) || (layer == 1 && !isLit)) {
                if (frustum.isVisible(boundingBox.boundingBox)) {

                // Start rendering
                glPushMatrix();
                glEnable(GL_TEXTURE_2D);

                // Bind texture
                glBindTexture(GL_TEXTURE_2D, Textures.loadTexture("/char.png", GL_NEAREST));

                // Zombie animation time
                double time = System.nanoTime() / 1000000000D * 10.0 * model.speed + model.timeOffset;

                // Interpolate entity position
                double interpolatedX = position.prevX + (position.x - position.prevX) * partialTicks;
                double interpolatedY = position.prevY + (position.y - position.prevY) * partialTicks;
                double interpolatedZ = position.prevZ + (position.z - position.prevZ) * partialTicks;

                // Translate using interpolated position
                glTranslated(interpolatedX, interpolatedY, interpolatedZ);

                // Flip the entity because it's upside down
                glScalef(1.0F, -1.0F, 1.0F);

                // Actual size of the entity
                float size = 7.0F / 120.0F;
                glScalef(size, size, size);

                // Body offset animation
                double offsetY = Math.abs(Math.sin(time * 2.0D / 3.0D)) * 5.0 + 23.0D;
                glTranslated(0.0F, -offsetY, 0.0F);

                // Rotate the entity
                glRotated(Math.toDegrees(ai.rotation) + 180, 0.0F, 1.0F, 0.0F);

                // Set rotation of cubes
                model.head.yRotation = (float) Math.sin(time * 0.83);
                model.head.xRotation = (float) Math.sin(time) * 0.8F;
                model.rightArm.xRotation = (float) Math.sin(time * 0.6662 + Math.PI) * 2.0F;
                model.rightArm.zRotation = (float) (Math.sin(time * 0.2312) + 1.0);
                model.leftArm.xRotation = (float) Math.sin(time * 0.6662) * 2.0f;
                model.leftArm.zRotation = (float) (Math.sin(time * 0.2812) - 1.0);
                model.rightLeg.xRotation = (float) Math.sin(time * 0.6662) * 1.4f;
                model.leftLeg.xRotation = (float) Math.sin(time * 0.6662 + Math.PI) * 1.4F;

                // Render cubes
                model.head.render();
                model.body.render();
                model.rightArm.render();
                model.leftArm.render();
                model.rightLeg.render();
                model.leftLeg.render();

                // Stop rendering
                glDisable(GL_TEXTURE_2D);
                glPopMatrix();
                }
            }
        });
    }
}
