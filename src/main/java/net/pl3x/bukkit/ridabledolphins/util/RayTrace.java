package net.pl3x.bukkit.ridabledolphins.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class RayTrace {
    private final Vector origin;
    private final Vector direction;

    public RayTrace(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public RayTrace(Location location) {
        this(location.toVector(), location.getDirection().normalize());
    }

    public Vector getPosition(double blocksAway) {
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }

    public ArrayList<Vector> traverse(double blocksAway, double accuracy) {
        ArrayList<Vector> positions = new ArrayList<>();
        for (double d = 0; d <= blocksAway; d += accuracy) {
            positions.add(getPosition(d));
        }
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
}
