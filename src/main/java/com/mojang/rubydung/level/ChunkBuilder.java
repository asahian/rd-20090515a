package com.mojang.rubydung.level;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkBuilder implements Runnable {

    private final ConcurrentLinkedQueue<Chunk> chunkQueue = new ConcurrentLinkedQueue<>();
    private final Tessellator tessellator = new Tessellator();

    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                // Process the next chunk in the queue
                Chunk chunk = this.chunkQueue.poll();
                if (chunk != null) {
                    // Rebuild the chunk and store the result
                    chunk.rebuild(0, this.tessellator);
                    chunk.rebuild(1, this.tessellator);

                    // Mark as rebuilt so the main thread can upload it
                    chunk.setRebuilt();
                }

                // Wait a bit to not burn CPU
                Thread.sleep(10L);

            } catch (InterruptedException e) {
                // Thread interrupted, exit loop
                running = false;
            } catch (Exception e) {
                // Log other exceptions
                e.printStackTrace();
            }
        }
    }

    /**
     * Add a chunk to the rebuild queue
     *
     * @param chunk The chunk to rebuild
     */
    public void rebuild(Chunk chunk) {
        if (!this.chunkQueue.contains(chunk)) {
            this.chunkQueue.add(chunk);
        }
    }

    /**
     * Stop the builder thread
     */
    public void stop() {
        this.running = false;
    }
}
