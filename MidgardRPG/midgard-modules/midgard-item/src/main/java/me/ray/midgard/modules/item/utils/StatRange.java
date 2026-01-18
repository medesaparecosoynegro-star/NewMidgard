package me.ray.midgard.modules.item.utils;

import java.util.concurrent.ThreadLocalRandom;

public class StatRange {
    private final double min;
    private final double max;

    public StatRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getRandom() {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static StatRange parse(String str) {
        try {
            String[] parts = str.split("-");
            if (parts.length == 2) {
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());
                return new StatRange(Math.min(min, max), Math.max(min, max));
            }
            double val = Double.parseDouble(str.trim());
            return new StatRange(val, val);
        } catch (NumberFormatException e) {
            return new StatRange(0, 0);
        }
    }
}
