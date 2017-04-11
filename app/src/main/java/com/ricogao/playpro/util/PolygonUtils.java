package com.ricogao.playpro.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricogao on 2017/4/10.
 */

public class PolygonUtils {


    public PolygonUtils() {
    }

    public boolean isInsidePolygon(LatLng p, List<LatLng> bound) {

        ArrayList<Double> polygonXA = new ArrayList<>();
        ArrayList<Double> polygonYA = new ArrayList<>();

        for (LatLng latLng : bound) {
            polygonXA.add(latLng.longitude);
            polygonYA.add(latLng.latitude);
        }

        //repeat first reading to close polygon
        polygonXA.add(bound.get(0).longitude);
        polygonYA.add(bound.get(0).latitude);


        return isPointInPolygon(p.longitude, p.latitude, polygonXA, polygonYA);


    }


    //(magic) when the count of point intersect with the polygon is odd then the point is inside
    //the polygon, otherwise outside.(special case when the point is on line or conner is included)

    /**
     * @param px        x-coordinate of given point
     * @param py        y-coordinate of given point
     * @param polygonXA List of x-coordinates which forms the polygon
     * @param polygonYA List of y-coordinates which forms the polygon
     * @return true when the point is inside the given polygon, vice versa
     */
    private boolean isPointInPolygon(double px, double py,
                                     ArrayList<Double> polygonXA, ArrayList<Double> polygonYA) {
        boolean isInside = false;
        double ESP = 1e-9;
        int count = 0;
        double linePoint1x;
        double linePoint1y;
        double linePoint2x = 180;
        double linePoint2y;

        linePoint1x = px;
        linePoint1y = py;
        linePoint2y = py;

        for (int i = 0; i < polygonXA.size() - 1; i++) {
            double cx1 = polygonXA.get(i);
            double cy1 = polygonYA.get(i);
            double cx2 = polygonXA.get(i + 1);
            double cy2 = polygonYA.get(i + 1);
            if (isPointOnLine(px, py, cx1, cy1, cx2, cy2)) {
                return true;
            }
            if (Math.abs(cy2 - cy1) < ESP) {
                continue;
            }

            if (isPointOnLine(cx1, cy1, linePoint1x, linePoint1y, linePoint2x,
                    linePoint2y)) {
                if (cy1 > cy2)
                    count++;
            } else if (isPointOnLine(cx2, cy2, linePoint1x, linePoint1y,
                    linePoint2x, linePoint2y)) {
                if (cy2 > cy1)
                    count++;
            } else if (isIntersect(cx1, cy1, cx2, cy2, linePoint1x,
                    linePoint1y, linePoint2x, linePoint2y)) {
                count++;
            }
        }
        if (count % 2 == 1) {
            isInside = true;
        }

        return isInside;
    }

    private double Multiply(double px0, double py0, double px1, double py1,
                            double px2, double py2) {
        return ((px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0));
    }


    //check if the point is on the line
    private boolean isPointOnLine(double px0, double py0, double px1,
                                  double py1, double px2, double py2) {
        boolean flag = false;
        double ESP = 1e-9;
        if ((Math.abs(Multiply(px0, py0, px1, py1, px2, py2)) < ESP)
                && ((px0 - px1) * (px0 - px2) <= 0)
                && ((py0 - py1) * (py0 - py2) <= 0)) {
            flag = true;
        }
        return flag;
    }

    //check if the point intersect with lines
    private boolean isIntersect(double px1, double py1, double px2, double py2,
                                double px3, double py3, double px4, double py4) {
        boolean flag = false;
        double d = (px2 - px1) * (py4 - py3) - (py2 - py1) * (px4 - px3);
        if (d != 0) {
            double r = ((py1 - py3) * (px4 - px3) - (px1 - px3) * (py4 - py3))
                    / d;
            double s = ((py1 - py3) * (px2 - px1) - (px1 - px3) * (py2 - py1))
                    / d;
            if ((r >= 0) && (r <= 1) && (s >= 0) && (s <= 1)) {
                flag = true;
            }
        }
        return flag;
    }
}