package com.mojang.rubydung;

import com.mojang.rubydung.level.tile.Tile;
import org.lwjgl.input.Keyboard;

public class GameUpdater implements Runnable {

    private final RubyDung rubyDung;

    private final Timer timer = new Timer(20.0f);

    public GameUpdater(RubyDung rubyDung) {
        this.rubyDung = rubyDung;
    }

    @Override
    public void run() {
        while (this.rubyDung.running) {
            this.timer.advanceTime();
            this.rubyDung.setPartialTicks(this.timer.partialTicks);

            for (int i = 0; i < this.timer.ticks; i++) {
                this.tick();
            }

            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Game tick, called exactly 20 times per second
     */
    public void tick() {
        // Listen for keyboard inputs
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {

                if (Keyboard.getEventKey() == 1) { // Escape
                    this.rubyDung.running = false;
                }

                // Save the level
                if (Keyboard.getEventKey() == 28) { // Enter
                    this.rubyDung.level.save();
                }

                // Tile selection
                this.rubyDung.selectedTileId = switch (Keyboard.getEventKey()) {
                    case 2 -> Tile.rock.id; // 1
                    case 3 -> Tile.dirt.id; // 2
                    case 4 -> Tile.stoneBrick.id; // 3
                    case 5 -> Tile.wood.id; // 4
                    default -> this.rubyDung.selectedTileId;
                };

                // Spawn zombie
                if (Keyboard.getEventKey() == 34) { // G
                    this.rubyDung.createZombie();
                }
            }
        }

        // Tick random tile in level
        this.rubyDung.level.onTick();

        // Update world
        this.rubyDung.world.update(0);
    }
}
