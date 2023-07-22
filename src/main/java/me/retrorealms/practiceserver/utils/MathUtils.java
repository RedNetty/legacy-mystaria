package me.retrorealms.practiceserver.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility and fast math functions.
 * <p>
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/atan2/floor/ceil.
 *
 * @author Nathan Sweet and (a bit lol) iSach.
 */
public class MathUtils {

    static public final float nanoToSec = 1 / 1000000000f;

    static public final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits

    static public final float PI = 3.141592653589793238462643383279f;

    static public final float PI2 = PI * 2;

    static public final float SQRT_3 = 1.73205080757f;

    static public final float E = 2.7182818284590452354f;

    static public final int SIN_BITS = 14; // 16KB. Adjust for accuracy.

    static public final int SIN_MASK = ~(-1 << SIN_BITS);

    static public final int SIN_COUNT = SIN_MASK + 1;

    static public final float radFull = PI * 2;

    static public final float degFull = 360;

    static public final float radToIndex = SIN_COUNT / radFull;

    static public final float degToIndex = SIN_COUNT / degFull;

    /**
     * multiply by this to convert from radians to degrees
     */
    static public final float radiansToDegrees = 180f / PI;

    static public final float radDeg = radiansToDegrees;
    /**
     * multiply by this to convert from degrees to radians
     */
    static public final float degreesToRadians = PI / 180;

    static public final float degRad = degreesToRadians;

    static public class Sin {

        static final float[] table = new float[SIN_COUNT];

        static {
            for (int i = 0; i < SIN_COUNT; i++) {
                table[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            }
            for (int i = 0; i < 360; i += 90) {
                table[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * degreesToRadians);
            }
        }
    }

    /**
     * Returns the sine in radians from a lookup table.
     */
    static public final float sin(float radians) {
        return Sin.table[(int) (radians * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     */
    static public final float cos(float radians) {
        return Sin.table[(int) ((radians + PI / 2) * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the sine in radians from a lookup table.
     */
    static public final float sinDeg(float degrees) {
        return Sin.table[(int) (degrees * degToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     */
    static public final float cosDeg(float degrees) {
        return Sin.table[(int) ((degrees + 90) * degToIndex) & SIN_MASK];
    }
    public static Color getRandomColor() {
        Random random = new Random();
        int colors = random.nextInt(12);
        switch (colors) {
            case 0: return Color.YELLOW;
            case 1: return Color.BLUE;
            case 2: return Color.PURPLE;
            case 4: return Color.OLIVE;
            case 5: return Color.MAROON;
            case 6: return Color.RED;
            case 7: return Color.AQUA;
            case 8: return Color.WHITE;
            case 9: return Color.FUCHSIA;
            case 10: return Color.LIME;
            default: return Color.ORANGE;
        }
    }
    static public final int ATAN2_BITS = 7; // Adjust for accuracy.

    static public final int ATAN2_BITS2 = ATAN2_BITS << 1;

    static public final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);

    static public final int ATAN2_COUNT = ATAN2_MASK + 1;

    static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

    static public final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

    static public class Atan2 {

        static final float[] table = new float[ATAN2_COUNT];

        static {
            for (int i = 0; i < ATAN2_DIM; i++) {
                for (int j = 0; j < ATAN2_DIM; j++) {
                    float x0 = (float) i / ATAN2_DIM;
                    float y0 = (float) j / ATAN2_DIM;
                    table[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
                }
            }
        }
    }

    public static Location getCenterBlock(Location location) {
        double x = location.getX();
        double z = location.getZ();
        return location.add(x > 0 ? 0.5 : -0.5, 0.0, z > 0 ? 0.5 : -0.5);
    }

    public static List<Location> circle(Location loc, Integer r, Integer h, Boolean hollow, Boolean sphere, int plus_y) {
        List<Location> circleblocks = new ArrayList<Location>();
        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        for (int x = cx - r; x <= cx + r; x++)
            for (int z = cz - r; z <= cz + r; z++)
                for (int y = (sphere ? cy - r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        Location l = new Location(loc.getWorld(), x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }

        return circleblocks;
    }

    public Location generateNewTarget(Player p) {
        return p.getEyeLocation()
                .add(Math.random() * 6 - 3,
                        Math.random() * 1.5,
                        Math.random() * 6 - 3);
    }

    boolean o = false;
    boolean x = true;
    public boolean[][] shape = {
            {o, o, o, o, o, o, o, o, o, o, o, o, o, o, o, o, o, o},
            {o, x, x, x, x, o, o, o, o, o, o, o, x, x, x, x, o, o},
            {o, o, x, x, x, x, x, o, o, o, x, x, x, x, x, o, o, o},
            {o, o, o, x, x, x, x, x, x, x, x, x, x, x, o, o, o, o},
            {o, o, o, o, x, x, x, x, x, x, x, x, x, o, o, o, o, o},
            {o, o, o, o, x, x, x, x, o, x, x, x, x, o, o, o, o, o},
            {o, o, o, o, o, x, x, x, o, x, x, x, o, o, o, o, o, o},
            {o, o, o, o, o, x, x, o, o, o, x, x, o, o, o, o, o, o},
            {o, o, o, o, x, x, o, o, o, o, o, x, x, o, o, o, o, o}
    };
    public boolean[][] shape1 = {
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
            {x, x, x, x, x,},
    };
    public int particles = 12;

    public void drawCircle(float radius, float height, Location location, boolean red) {
        for (int i = 0; i < particles; i++) {
            double inc = (2 * Math.PI) / particles;
            float angle = (float) (i * inc);
            float x = MathUtils.cos(angle) * radius;
            float z = MathUtils.sin(angle) * radius;
            location.add(x, height, z);
            UtilParticles.display(255, red ? 0 : 255, red ? 0 : 255, location);
            location.subtract(x, height, z);
        }
    }

    public void drawParticles(Location location) {
        double space = 0.2;
        double defX = location.getX() - (space * shape[0].length / 2) + space;
        double x = defX;
        double y = location.clone().getY() + 2;
        double angle = -((location.getYaw() + 180) / 60);
        angle += (location.getYaw() < -180 ? 3.25 : 2.985);

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j]) {

                    Location target = location.clone();
                    target.setX(x);
                    target.setY(y);

                    Vector v = target.toVector().subtract(location.toVector());
                    Vector v2 = getBackVector(location);
                    v = rotateAroundAxisY(v, angle);
                    v2.setY(0).multiply(-0.2);

                    location.add(v);
                    location.add(v2);
                    for (int k = 0; k < 3; k++)
                        UtilParticles.display(255, 255, 255, location);
                    location.subtract(v2);
                    location.subtract(v);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }

    public void drawCapeParticles(Player p, Location location) {
        double space = 0.2;
        double defX = location.getX() - (space * shape1[0].length / 2) + space / 2;
        double x = defX;
        double defY = location.getY() + 1.5;
        double y = defY;
        double angle = -((location.getYaw() + 180) / 60);
        angle += (location.getYaw() < -180 ? 3.25 : 2.985);
        for (int i = 0; i < shape1.length; i++) {
            for (int j = 0; j < shape1[i].length; j++) {
                if (shape1[i][j]) {
                    Location target = location.clone();
                    target.setX(x);
                    target.setY(y);

                    Vector v = target.toVector().subtract(location.toVector());
                    Vector v2 = getBackVector(location);
                    v = rotateAroundAxisY(v, angle);
                    double iT = ((double) i) / 10;
                    v2.setY(0).multiply(-0.2 - iT);

                    Location loc = location.clone();

                    loc.add(v);
                    loc.add(v2);
                    if (p.isSprinting()) {
                        loc.setY(defY);
                    }

                    for (int k = 0; k < 3; k++)
                        UtilParticles.display(255, 0, 0, loc);
                    loc.subtract(v2);
                    loc.subtract(v);
                }
                x += space;
            }
            y -= space;
            x = defX;
        }
    }


    public static Vector getBackVector(Location loc) {
        final float newZ = (float) (loc.getZ() + (1 * Math.sin(Math.toRadians(loc.getYaw() + 90))));
        final float newX = (float) (loc.getX() + (1 * Math.cos(Math.toRadians(loc.getYaw() + 90))));
        return new Vector(newX - loc.getX(), 0, newZ - loc.getZ());
    }

    public static Vector getLeftVector(Location loc) {
        final float newX = (float) (loc.getX() + (1 * Math.cos(Math.toRadians(loc.getYaw() + 0))));
        final float newZ = (float) (loc.getZ() + (1 * Math.sin(Math.toRadians(loc.getYaw() + 0))));

        return new Vector(newX - loc.getX(), 0, newZ - loc.getZ());
    }

    public static Vector getRightVector(Location loc) {
        final float newX = (float) (loc.getX() + (-1 * Math.cos(Math.toRadians(loc.getYaw() + 0))));
        final float newZ = (float) (loc.getZ() + (-1 * Math.sin(Math.toRadians(loc.getYaw() + 0))));

        return new Vector(newX - loc.getX(), 0, newZ - loc.getZ());
    }

    public static boolean isInteger(Object object) {
        try {
            Integer.parseInt(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static boolean isDouble(Object object) {
        try {
            Double.parseDouble(object.toString());
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    /**
     * Returns atan2 in radians from a lookup table.
     */
    static public final float atan2(float y, float x) {
        float add, mul;
        if (x < 0) {
            if (y < 0) {
                y = -y;
                mul = 1;
            } else {
                mul = -1;
            }
            x = -x;
            add = -PI;
        } else {
            if (y < 0) {
                y = -y;
                mul = -1;
            } else {
                mul = 1;
            }
            add = 0;
        }
        float invDiv = 1 / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);

        if (invDiv == Float.POSITIVE_INFINITY) {
            return ((float) Math.atan2(y, x) + add) * mul;
        }

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);
        return (Atan2.table[yi * ATAN2_DIM + xi] + add) * mul;
    }

    static public Random random = new Random();

    /**
     * Returns a random number between 0 (inclusive) and the specified value (inclusive).
     */
    static public final int random(int range) {
        return random.nextInt(range + 1);
    }

    /**
     * Returns a random number between start (inclusive) and end (inclusive).
     */
    static public final int random(int start, int end) {
        return start + random.nextInt(end - start + 1);
    }

    /**
     * Returns a random boolean value.
     */
    static public final boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns true if a random value between 0 and 1 is less than the specified value.
     */
    static public final boolean randomBoolean(float chance) {
        return MathUtils.random() < chance;
    }

    /**
     * Returns random number between 0.0 (inclusive) and 1.0 (exclusive).
     */
    static public final float random() {
        return random.nextFloat();
    }

    /**
     * Returns a random number between 0 (inclusive) and the specified value (exclusive).
     */
    static public final float random(float range) {
        return random.nextFloat() * range;
    }

    /**
     * Returns a random number between start (inclusive) and end (exclusive).
     */
    static public final float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

    // ---

    /**
     * Returns the next power of two. Returns the specified value if the value is already a power of two.
     */
    static public int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    static public boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    // ---
    static public int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    // ---
    static public final int BIG_ENOUGH_INT = 16 * 1024;
    static public final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    static public final double CEIL = 0.9999999;
    // static public final double BIG_ENOUGH_CEIL = NumberUtils
// .longBitsToDouble(NumberUtils.doubleToLongBits(BIG_ENOUGH_INT + 1) - 1);
    static public final double BIG_ENOUGH_CEIL = 16384.999999999996;
    static public final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

    /**
     * Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).
     */
    static public int floor(float x) {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the largest integer less than or equal to the specified float. This method will only properly floor floats that are
     * positive. Note this method simply casts the float to int.
     */
    static public int floorPositive(float x) {
        return (int) x;
    }

    /**
     * Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
     * -(2^14) to (Float.MAX_VALUE - 2^14).
     */
    static public int ceil(float x) {
        return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats that
     * are positive.
     */
    static public int ceilPositive(float x) {
        return (int) (x + CEIL);
    }

    /**
     * Returns the closest integer to the specified float. This method will only properly round floats from -(2^14) to
     * (Float.MAX_VALUE - 2^14).
     */
    static public int round(float x) {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the closest integer to the specified float. This method will only properly round floats that are positive.
     */
    static public int roundPositive(float x) {
        return (int) (x + 0.5f);
    }

    /**
     * Returns true if the value is zero (using the default tolerance as upper bound)
     */
    static public boolean isZero(float value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    /**
     * Returns true if the value is zero.
     *
     * @param tolerance represent an upper bound below which the value is considered zero.
     */
    static public boolean isZero(float value, float tolerance) {
        return Math.abs(value) <= tolerance;
    }

    /**
     * Returns true if a is nearly equal to b. The function uses the default floating error tolerance.
     *
     * @param a the first value.
     * @param b the second value.
     */
    static public boolean isEqual(float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }

    /**
     * Returns true if a is nearly equal to b.
     *
     * @param a         the first value.
     * @param b         the second value.
     * @param tolerance represent an upper bound below which the two values are considered equal.
     */
    static public boolean isEqual(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    public static final Vector rotateAroundAxisX(Vector v, double angle) {
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }

    public static final Vector rotateAroundAxisY(Vector v, double angle) {
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static final Vector rotateAroundAxisZ(Vector v, double angle) {
        double x, y, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos - v.getY() * sin;
        y = v.getX() * sin + v.getY() * cos;
        return v.setX(x).setY(y);
    }

    public static final Vector rotateVector(Vector v, double angleX, double angleY, double angleZ) {
        rotateAroundAxisX(v, angleX);
        rotateAroundAxisY(v, angleY);
        rotateAroundAxisZ(v, angleZ);
        return v;
    }

    public static final double angleToXAxis(Vector vector) {
        return Math.atan2(vector.getX(), vector.getY());
    }

    public static Vector getRandomVector() {
        double x = random.nextDouble() * 2.0D - 1.0D;
        double y = random.nextDouble() * 2.0D - 1.0D;
        double z = random.nextDouble() * 2.0D - 1.0D;

        return new Vector(x, y, z).normalize();
    }


    public static Vector getRandomCircleVector() {
        double rnd = random.nextDouble() * 2.0D * 3.141592653589793D;
        double x = Math.cos(rnd);
        double z = Math.sin(rnd);

        return new Vector(x, 0.0D, z);
    }

    public static Material getRandomMaterial(Material[] materials) {
        return materials[random.nextInt(materials.length)];
    }

    public static double getRandomAngle() {
        return random.nextDouble() * 2 * Math.PI;
    }

    public static double randomDouble(double min, double max) {
        return Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min);
    }


    public static float randomRangeFloat(float min, float max) {
        return (float) (Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min));
    }

    /**
     * get random byte between 0 and max (included).
     *
     * @param max random byte between 0 and max (included).
     */
    public static byte randomByte(int max) {
        return (byte) random.nextInt(max + 1);
    }

    /**
     * Returns a random integer between the value min and the value max.
     *
     * @param min the minimum integer value.
     * @param max the maximum integer value.
     * @return a random integer between two values.
     */
    public static int randomRangeInt(int min, int max) {
        return (int) (Math.random() < 0.5 ? ((1 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min));
    }

    public static double offset(Entity a, Entity b) {
        return offset(a.getLocation().toVector(), b.getLocation().toVector());
    }

    public static double offset(Location a, Location b) {
        return offset(a.toVector(), b.toVector());
    }

    public static double offset(Vector a, Vector b) {
        return a.subtract(b).length();
    }

}