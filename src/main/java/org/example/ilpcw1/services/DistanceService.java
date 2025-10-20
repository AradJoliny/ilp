package org.example.ilpcw1.services;

import org.example.ilpcw1.model.LngLat;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.awt.*;
import java.awt.geom.Path2D;


@Service
public class DistanceService {
    public double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        return Math.sqrt(Math.pow(lng1 - lng2, 2) + Math.pow(lat1 - lat2, 2));
    }

    public boolean isClose(double lng1, double lat1, double lng2, double lat2) {
        double distance = calculateDistance(lng1, lat1, lng2, lat2);

        return distance < 0.00015;
    }

    public LngLat calculateNextPosition(double lng, double lat, double angle) {
        BigDecimal forward_step = new BigDecimal("0.00015");
        double angle_radians = Math.toRadians(angle);

        BigDecimal delta_lat = forward_step.multiply(BigDecimal.valueOf(Math.sin(angle_radians)));
        BigDecimal delta_lng = forward_step.multiply(BigDecimal.valueOf(Math.cos(angle_radians)));

        BigDecimal new_lat = BigDecimal.valueOf(lat).add(delta_lat);
        BigDecimal new_lng = BigDecimal.valueOf(lng).add(delta_lng);

        System.out.println("new_lat = " + new_lat);
        System.out.println("new_lng = " + new_lng);

        return new LngLat(new_lng.doubleValue(), new_lat.doubleValue());
    }

    /**
     * Checks if a given point (px, py) lies exactly on any edge of a polygon defined by the arrays xpoints and ypoints.
     * The polygon is assumed to be defined by consecutive pairs of points (xpoints[i], ypoints[i]) and (xpoints[i+1], ypoints[i+1]).
     *
     * @param px the x-coordinate (longitude) of the point to check
     * @param py the y-coordinate (latitude) of the point to check
     * @param xpoints array of x-coordinates (longitudes) of the polygon vertices
     * @param ypoints array of y-coordinates (latitudes) of the polygon vertices
     * @return true if the point lies on any edge of the polygon, false otherwise
     */
    private boolean isPointOnEdge(double px, double py, double[] xpoints, double[] ypoints) {
        for (int i = 0; i < xpoints.length - 1; i++) {
            double x1 = xpoints[i], y1 = ypoints[i];
            double x2 = xpoints[i + 1], y2 = ypoints[i + 1];
            double cross = (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
            if (Math.abs(cross) < 1e-10) {
                double dot = (px - x1) * (px - x2) + (py - y1) * (py - y2);
                if (dot <= 0) return true;
            }
        }
        return false;
    }

    public boolean isPointInRegion(double px, double py, double[] xpoints, double[] ypoints) {
        int npoints = xpoints.length;
        Path2D polyPath = new Path2D.Double();
        polyPath.moveTo(xpoints[0], ypoints[0]);
        for (int i = 1; i < npoints; i++) {
            polyPath.lineTo(xpoints[i], ypoints[i]);
        }
        polyPath.closePath();

        boolean contains = polyPath.contains(px, py);
        if (!contains && isPointOnEdge(px, py, xpoints, ypoints)) {
            contains = true;
        }
        return contains;
    }
}
