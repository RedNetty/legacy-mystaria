package me.retrorealms.practiceserver.utils;

import org.bukkit.util.Vector;

import java.util.ArrayList;

public class RayTrace {

    private Vector origin, direction;

    public RayTrace(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector getPos(double distance) {
        return origin.clone().add(direction.clone().multiply(distance));
    }

    public ArrayList<Vector> traverse(double distance, double precision) {
        ArrayList<Vector> positions = new ArrayList<>();
        for (double d = 0; d <= distance; d += precision)
            positions.add(getPos(d));

        return positions;
    }

    public boolean intersects(Vector position, Vector min, Vector max) {
        if (position.getX() < min.getX() || position.getX() > max.getX()) {
            return false;
        } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
            return false;
        } else if (position.getZ() < min.getZ() || position.getZ() > max.getZ()) {
            return false;
        }
        return true;
    }

    public boolean intersectsBox(Vector position, BoundingBox box) {
        return intersects(position, box.min, box.max);
    }

    public boolean intersectsBox(Vector position, double radius, BoundingBox box) {
        return  (position.getX() - radius <= box.max.getX() && position.getX() + radius >= box.min.getX() &&
                (position.getY() - radius <= box.max.getY() && position.getY() + radius >= box.min.getY()) &&
                (position.getZ() - radius <= box.max.getZ() && position.getZ() + radius >= box.min.getZ()));
    }
}
