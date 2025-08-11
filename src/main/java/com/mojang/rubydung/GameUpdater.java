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
        // Tick random tile in level
        this.rubyDung.level.onTick();

        // Update world
        this.rubyDung.world.update(0);
    }
}
