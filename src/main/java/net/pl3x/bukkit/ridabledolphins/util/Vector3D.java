package net.pl3x.bukkit.ridabledolphins.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Vector3D {
    public static final Vector3D ORIGIN = new Vector3D(0, 0, 0);
    public final double x;
    public final double y;
    public final double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Location location) {
        this(location.toVector());
    }

    public Vector3D(Vector vector) {
        if (vector == null) throw new IllegalArgumentException("Vector cannot be NULL.");
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Vector3D add(Vector3D other) {
        if (other == null) throw new IllegalArgumentException("other cannot be NULL");
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    public Vector3D add(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D subtract(Vector3D other) {
        if (other == null) throw new IllegalArgumentException("other cannot be NULL");
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    public Vector3D subtract(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D multiply(int factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D multiply(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    public Vector3D divide(int divisor) {
        if (divisor == 0) throw new IllegalArgumentException("Cannot divide by null.");
        return new Vector3D(x / divisor, y / divisor, z / divisor);
    }

    public Vector3D divide(double divisor) {
        if (divisor == 0) throw new IllegalArgumentException("Cannot divide by null.");
        return new Vector3D(x / divisor, y / divisor, z / divisor);
    }

    public Vector3D abs() {
        return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public double distanceSquared(Vector3D other) {
        double d1 = other.x - x;
        double d2 = other.y - y;
        double d3 = other.z - z;
        return d1 * d1 + d2 * d2 + d3 * d3;
    }
}
