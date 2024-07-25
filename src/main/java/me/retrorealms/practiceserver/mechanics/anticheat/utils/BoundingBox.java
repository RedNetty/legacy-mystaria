package me.retrorealms.practiceserver.mechanics.anticheat.utils;


public class BoundingBox {
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean overlaps(BoundingBox other) {
        return this.minX < other.maxX && this.maxX > other.minX &&
                this.minY < other.maxY && this.maxY > other.minY &&
                this.minZ < other.maxZ && this.maxZ > other.minZ;
    }
}