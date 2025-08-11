package com.mojang.rubydung;

import com.mojang.rubydung.ecs.Entity;
import com.mojang.rubydung.ecs.World;
import com.mojang.rubydung.ecs.component.*;
import com.mojang.rubydung.ecs.system.*;
import com.mojang.rubydung.level.*;
import com.mojang.rubydung.level.tile.Tile;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import static org.lwjgl.util.glu.GLU.gluPickMatrix;

public class RubyDung implements Runnable {

    private static final boolean FULLSCREEN_MODE = true;

    public volatile boolean running;

    private float partialTicks;

    volatile Level level;
    volatile LevelRenderer levelRenderer;
    volatile World world;
    volatile Entity player;

    /**
     * Fog
     */
    private final FloatBuffer fogColorDaylight = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer fogColorShadow = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(16);

    /**
     * Screen resolution
     */
    private int width = 1024;
    private int height = 768;

    /**
     * Tile picking
     */
    private final IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
    private final IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
    private HitResult hitResult;

    /**
     * HUD rendering
     */
    private final Tessellator tessellator = new Tessellator();

    /**
     * Selected tile in hand
     */
    int selectedTileId = 1;

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    /**
     * Initialize the game.
     * Setup display, keyboard, mouse, rendering and camera
     *
     * @throws LWJGLException Game could not be initialized
     */
    public void init() throws LWJGLException {
        // Write fog color for daylight
        this.fogColorDaylight.put(new float[]{
                254 / 255.0f,
                251 / 255.0f,
                250 / 255.0f,
                255 / 255.0F
        }).flip();

        // Write fog color for shadow
        this.fogColorShadow.put(new float[]{
                14 / 255.0F,
                11 / 255.0F,
                10 / 255.0F,
                255 / 255.0F
        }).flip();

        // Set screen size
        Display.setFullscreen(FULLSCREEN_MODE);

        // Set defined window size
        if (!FULLSCREEN_MODE) {
            Display.setDisplayMode(new DisplayMode(this.width, this.height));
        }

        // Setup I/O
        Display.create();
        Keyboard.create();
        Mouse.create();

        // Use monitor size in fullscreen
        if (FULLSCREEN_MODE) {
            this.width = Display.getDisplayMode().getWidth();
            this.height = Display.getDisplayMode().getHeight();
        }

        // Setup rendering
        glEnable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glClearColor(0.5F, 0.8F, 1.0F, 0.0F);
        glClearDepth(1.0);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDepthFunc(GL_LEQUAL);

        // Create level and player (Has to be in main thread)
        this.level = new Level(256, 256, 64);
        this.levelRenderer = new LevelRenderer(this.level);

        // Create world and systems
        this.world = new World();
        this.world.addSystem(new PhysicsSystem(this.level));
        this.world.addSystem(new PlayerInputSystem());
        this.world.addSystem(new MouseInputSystem());
        this.world.addSystem(new CollisionSystem(this.level));
        this.world.addSystem(new ZombieAISystem());
        RenderSystem renderSystem = new RenderSystem();
        renderSystem.setLevel(this.level);
        this.world.addSystem(renderSystem);

        // Create player entity
        this.player = this.world.createEntity();
        float x = (float) Math.random() * this.level.width;
        float y = (float) (this.level.depth + 3);
        float z = (float) Math.random() * this.level.height;
        this.world.addComponent(this.player, new PositionComponent(x, y, z));
        this.world.addComponent(this.player, new MotionComponent(0, 0, 0));
        this.world.addComponent(this.player, new RotationComponent(0, 0));
        this.world.addComponent(this.player, new BoundingBoxComponent(new AABB(x - 0.3f, y - 0.9f, z - 0.3f, x + 0.3f, y + 0.9f, z + 0.3f)));
        this.world.addComponent(this.player, new OnGroundComponent(false));
        this.world.addComponent(this.player, new HeightOffsetComponent(1.62f));
        this.world.addComponent(this.player, new PlayerInputComponent());


        // Grab mouse cursor
        Mouse.setGrabbed(true);

        // Spawn some zombies
        for (int i = 0; i < 10; ++i) {
            createZombie();
        }
    }

    /**
     * Destroy mouse, keyboard and display
     */
    public void destroy() {
        this.running = false;
        this.level.save();

        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    /**
     * Main game thread
     * Responsible for the game loop
     */
    @Override
    public void run() {
        try {
            // Initialize the game
            init();
        } catch (Exception e) {
            // Show error message dialog and stop the game
            JOptionPane.showMessageDialog(null, e, "Failed to start RubyDung", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // To keep track of framerate
        var frames = 0;
        var lastTime = System.currentTimeMillis();

        this.running = true;
        new Thread(new GameUpdater(this)).start();

        try {
            // Start the game loop
            while (this.running) {
                if (Display.isCloseRequested()) {
                    this.running = false;
                }

                // Handle user input
                handleInput();

                // Render the game
                render(getPartialTicks());

                // Increase rendered frame
                frames++;

                // Loop if a second passed
                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    // Print amount of frames
                    System.out.println(frames + " fps, " + Chunk.updates);

                    // Reset global rebuild stats
                    Chunk.updates = 0;

                    // Increase last time printed and reset frame counter
                    lastTime += 1000L;
                    frames = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Destroy I/O and save game
            destroy();
        }
    }

    void createZombie() {
        Entity zombie = this.world.createEntity();
        float x = (float) Math.random() * this.level.width;
        float y = (float) (this.level.depth + 3);
        float z = (float) Math.random() * this.level.height;
        this.world.addComponent(zombie, new PositionComponent(x, y, z));
        this.world.addComponent(zombie, new MotionComponent(0, 0, 0));
        this.world.addComponent(zombie, new BoundingBoxComponent(new AABB(x - 0.3f, y - 0.9f, z - 0.3f, x + 0.3f, y + 0.9f, z + 0.3f)));
        this.world.addComponent(zombie, new OnGroundComponent(false));
        this.world.addComponent(zombie, new HeightOffsetComponent(0f));
        this.world.addComponent(zombie, new ZombieComponent());
        this.world.addComponent(zombie, new ZombieAIComponent());
        this.world.addComponent(zombie, new ZombieModelComponent());
    }

    /**
     * Move and rotate the camera to players location and rotation
     *
     * @param partialTicks Overflow ticks to interpolate
     */
    private void moveCameraToPlayer(float partialTicks) {
        PositionComponent position = this.world.getComponent(this.player, PositionComponent.class);
        RotationComponent rotation = this.world.getComponent(this.player, RotationComponent.class);

        // Eye height
        glTranslatef(0.0f, 0.0f, -0.3f);

        // Smooth rotation
        float rX = (float) (rotation.prevXRotation + (rotation.xRotation - rotation.prevXRotation) * partialTicks);
        float rY = (float) (rotation.prevYRotation + (rotation.yRotation - rotation.prevYRotation) * partialTicks);

        // Rotate camera
        glRotatef(rX, 1.0f, 0.0f, 0.0f);
        glRotatef(rY, 0.0f, 1.0f, 0.0f);

        // Smooth movement
        double x = position.prevX + (position.x - position.prevX) * partialTicks;
        double y = position.prevY + (position.y - position.prevY) * partialTicks;
        double z = position.prevZ + (position.z - position.prevZ) * partialTicks;

        // Move camera to players location
        glTranslated(-x, -y, -z);
    }


    /**
     * Setup the normal player camera
     *
     * @param partialTicks Overflow ticks to interpolate
     */
    private void setupCamera(float partialTicks) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        // Set camera perspective
        gluPerspective(70, width / (float) height, 0.05F, 1000F);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Move camera to middle of level
        moveCameraToPlayer(partialTicks);
    }

    /**
     * Setup the HUD camera
     */
    private void setupOrthoCamera() {
        GL11.glMatrixMode(GL_PROJECTION);
        GL11.glLoadIdentity();

        // Set camera perspective
        GL11.glOrtho(0.0, this.width, this.height, 0.0, 100.0F, 300.0F);

        GL11.glMatrixMode(GL_MODELVIEW);
        GL11.glLoadIdentity();

        // Move camera to Z level -200
        GL11.glTranslatef(0.0f, 0.0f, -200.0f);
    }

    /**
     * Setup tile picking camera
     *
     * @param partialTicks Overflow ticks to calculate smooth a movement
     * @param x            Screen position x
     * @param y            Screen position y
     */
    private void setupPickCamera(float partialTicks, int x, int y) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        // Reset buffer
        this.viewportBuffer.clear();

        // Get viewport value
        glGetInteger(GL_VIEWPORT, this.viewportBuffer);

        // Flip
        this.viewportBuffer.flip();
        this.viewportBuffer.limit(16);

        // Set matrix and camera perspective
        gluPickMatrix(x, y, 5.0f, 5.0f, this.viewportBuffer);
        gluPerspective(70.0f, this.width / (float) this.height, 0.05F, 1000.0F);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Move camera to middle of level
        moveCameraToPlayer(partialTicks);
    }

    /**
     * @param partialTicks Overflow ticks to interpolate
     */
    private void pick(float partialTicks) {
        // Reset select buffer
        this.selectBuffer.clear();

        glSelectBuffer(this.selectBuffer);
        glRenderMode(GL_SELECT);

        // Setup pick camera
        this.setupPickCamera(partialTicks, this.width / 2, this.height / 2);

        // Render all possible pick selection faces to the target
        this.levelRenderer.pick(this.player, this.world);

        // Flip buffer
        this.selectBuffer.flip();
        this.selectBuffer.limit(this.selectBuffer.capacity());

        var closest = 0L;
        var names = new int[10];
        var hitNameCount = 0;

        // Get amount of hits
        var hits = glRenderMode(GL_RENDER);
        for (var hitIndex = 0; hitIndex < hits; hitIndex++) {

            // Get name count
            var nameCount = this.selectBuffer.get();
            var minZ = this.selectBuffer.get();
            this.selectBuffer.get();

            // Check if the hit is closer to the camera
            if (minZ < closest || hitIndex == 0) {
                closest = minZ;
                hitNameCount = nameCount;

                // Fill names
                for (int nameIndex = 0; nameIndex < nameCount; nameIndex++) {
                    names[nameIndex] = this.selectBuffer.get();
                }
            } else {
                // Skip names
                for (int nameIndex = 0; nameIndex < nameCount; ++nameIndex) {
                    this.selectBuffer.get();
                }
            }
        }

        // Update hit result
        if (hitNameCount > 0) {
            this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
        } else {
            this.hitResult = null;
        }
    }


    /**
     * Rendering the game
     *
     * @param partialTicks Overflow ticks to interpolate
     */
    private void render(float partialTicks) {
        // Pick tile
        pick(partialTicks);

        // Clear color and depth buffer and reset the camera
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Setup normal player camera
        setupCamera(partialTicks);
        glEnable(GL_CULL_FACE);

        // Get current frustum
        var frustum = Frustum.getFrustum();

        // Update dirty chunks
        this.levelRenderer.updateDirtyChunks(this.player, this.world);

        // Setup daylight fog
        setupFog(0);
        glEnable(GL_FOG);

        // Render bright tiles
        this.levelRenderer.render(0);

        // Render zombies in sunlight
        this.world.getSystem(RenderSystem.class).render(this.world, partialTicks, 0);


        // Setup shadow fog
        setupFog(1);

        // Render dark tiles in shadow
        this.levelRenderer.render(1);

        // Render zombies in shadow
        this.world.getSystem(RenderSystem.class).render(this.world, partialTicks, 1);


        // Finish rendering
        glDisable(GL_LIGHTING);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_FOG);

        // Render the actual hit
        if (this.hitResult != null) {
            this.levelRenderer.renderHit(this.hitResult);
        }

        // Draw player HUD
        drawGui(partialTicks);

        // Update the display
        Display.update();
    }

    /**
     * Draw HUD
     *
     * @param partialTicks Overflow ticks to interpolate
     */
    private void drawGui(float partialTicks) {
        // Clear depth
        glClear(GL_DEPTH_BUFFER_BIT);

        // Setup HUD camera
        setupOrthoCamera();

        // Start tile display
        glPushMatrix();

        // Transform tile position to the top right corner
        glTranslated(this.width - 48, 48.0F, 0.0F);
        glScalef(48.0F, 48.0F, 48.0F);
        glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
        glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        glTranslatef(1.5F, -0.5F, -0.5F);

        // Setup tile rendering
        var id = Textures.loadTexture("/terrain.png", 9728);
        glBindTexture(GL_TEXTURE_2D, id);
        glEnable(GL_TEXTURE_2D);

        // Render selected tile in hand
        this.tessellator.init();
        Tile.tiles[this.selectedTileId].render(this.tessellator, this.level, 0, -2, 0, 0);
        this.tessellator.flush();

        // Finish tile rendering
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        // Cross hair position
        var x = this.width / 2;
        var y = this.height / 2;

        // Cross hair color
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // Render cross hair
        this.tessellator.init();
        this.tessellator.vertex((float) (x + 1), (float) (y - 8), 0.0f);
        this.tessellator.vertex((float) (x - 0), (float) (y - 8), 0.0f);
        this.tessellator.vertex((float) (x - 0), (float) (y + 9), 0.0f);
        this.tessellator.vertex((float) (x + 1), (float) (y + 9), 0.0f);
        this.tessellator.vertex((float) (x + 9), (float) (y - 0), 0.0f);
        this.tessellator.vertex((float) (x - 8), (float) (y - 0), 0.0f);
        this.tessellator.vertex((float) (x - 8), (float) (y + 1), 0.0f);
        this.tessellator.vertex((float) (x + 9), (float) (y + 1), 0.0f);
        this.tessellator.flush();
    }

    /**
     * Setup fog with type
     *
     * @param fogType Type of the fog. (0: daylight, 1: shadow)
     */
    private void setupFog(int fogType) {
        // Daylight fog
        if (fogType == 0) {
            // Fog distance
            glFogi(GL_FOG_MODE, GL_EXP);
            glFogf(GL_FOG_DENSITY, 0.001F);

            // Set fog color
            glFog(GL_FOG_COLOR, this.fogColorDaylight);

            glDisable(GL_LIGHTING);
        }

        // Shadow fog
        if (fogType == 1) {
            // Fog distance
            glFogi(GL_FOG_MODE, GL_EXP);
            glFogf(GL_FOG_DENSITY, 0.06F);

            // Set fog color
            glFog(GL_FOG_COLOR, this.fogColorShadow);

            glEnable(GL_LIGHTING);
            glEnable(GL_COLOR_MATERIAL);

            var brightness = 0.6F;
            glLightModel(GL_LIGHT_MODEL_AMBIENT, this.getBuffer(brightness, brightness, brightness, 1.0F));
        }
    }

    /**
     * Fill float buffer with color values and return it
     *
     * @param red   Red value
     * @param green Green value
     * @param blue  Blue value
     * @param alpha Alpha value
     * @return Float buffer filled in RGBA order
     */
    private FloatBuffer getBuffer(float red, float green, float blue, float alpha) {
        this.colorBuffer.clear();
        this.colorBuffer.put(red).put(green).put(blue).put(alpha);
        this.colorBuffer.flip();
        return this.colorBuffer;
    }

    /**
     * Handle input
     */
    private void handleInput() {
        // Listen for keyboard inputs
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {

                if (Keyboard.getEventKey() == 1) { // Escape
                    this.running = false;
                }

                // Save the level
                if (Keyboard.getEventKey() == 28) { // Enter
                    this.level.save();
                }

                // Tile selection
                this.selectedTileId = switch (Keyboard.getEventKey()) {
                    case 2 -> Tile.rock.id; // 1
                    case 3 -> Tile.dirt.id; // 2
                    case 4 -> Tile.stoneBrick.id; // 3
                    case 5 -> Tile.wood.id; // 4
                    default -> this.selectedTileId;
                };

                // Spawn zombie
                if (Keyboard.getEventKey() == 34) { // G
                    createZombie();
                }
            }
        }

        // Listen for mouse inputs
        while (Mouse.next()) {
            // Right click
            if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && this.hitResult != null) {
                // Destroy the tile
                this.level.setTile(this.hitResult.x(), this.hitResult.y(), this.hitResult.z(), 0);
            }

            // Left click
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.hitResult != null) {
                // Get target tile position
                var x = this.hitResult.x();
                var y = this.hitResult.y();
                var z = this.hitResult.z();

                // Get position of the tile using face direction
                switch (this.hitResult.face()) {
                    case 0 -> y--;
                    case 1 -> y++;
                    case 2 -> z--;
                    case 3 -> z++;
                    case 4 -> x--;
                    case 5 -> x++;
                }

                // Set the tile
                this.level.setTile(x, y, z, this.selectedTileId);
            }
        }
    }

    /**
     * Entry point of the game
     *
     * @param args Program arguments (unused)
     */
    public static void main(String[] args) {
        new RubyDung().run();
    }
}
