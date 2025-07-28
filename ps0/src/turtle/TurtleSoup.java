/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package turtle;

import java.util.List;
import java.util.ArrayList;

public class TurtleSoup {

    /**
     * Draw a square.
     * 
     * @param turtle the turtle context
     * @param sideLength length of each side
     */
    public static void drawSquare(Turtle turtle, int sideLength) {
        turtle.forward(sideLength);
        turtle.turn(90);
        turtle.forward(sideLength);
        turtle.turn(90);
        turtle.forward(sideLength);
        turtle.turn(90);
        turtle.forward(sideLength);
        turtle.turn(90);
    }

    /**
     * Determine inside angles of a regular polygon.
     * 
     * There is a simple formula for calculating the inside angles of a polygon;
     * you should derive it and use it here.
     * 
     * @param sides number of sides, where sides must be > 2
     * @return angle in degrees, where 0 <= angle < 360
     */
    public static double calculateRegularPolygonAngle(int sides) {
        return (sides - 2) * 180.0 / sides;
    }

    /**
     * Determine number of sides given the size of interior angles of a regular polygon.
     * 
     * There is a simple formula for this; you should derive it and use it here.
     * Make sure you *properly round* the answer before you return it (see java.lang.Math).
     * HINT: it is easier if you think about the exterior angles.
     * 
     * @param angle size of interior angles in degrees, where 0 < angle < 180
     * @return the integer number of sides
     */
    public static int calculatePolygonSidesFromAngle(double angle) {
        return (int) Math.round(360.0 / (180.0 - angle));
    }

    /**
     * Given the number of sides, draw a regular polygon.
     * 
     * (0,0) is the lower-left corner of the polygon; use only right-hand turns to draw.
     * 
     * @param turtle the turtle context
     * @param sides number of sides of the polygon to draw
     * @param sideLength length of each side
     */
    public static void drawRegularPolygon(Turtle turtle, int sides, int sideLength) {
        assert sides > 2 : "sides must be greater than 2";
        double exteriorAngle = 360.0 / sides;  // This is what you need!

        for (int i = 0; i < sides; i++) {
            turtle.forward(sideLength);
            turtle.turn(exteriorAngle);
        }
    }

    /**
     * Given the current direction, current location, and a target location, calculate the heading
     * towards the target point.
     * <p>
     * The return value is the angle input to turn() that would point the turtle in the direction of
     * the target point (targetX,targetY), given that the turtle is already at the point
     * (currentX,currentY) and is facing at angle currentHeading. The angle must be expressed in
     * degrees, where 0 <= angle < 360. 
     * <p>
     * HINT: look at <a href="http://en.wikipedia.org/wiki/Atan2">...</a> and Java's math libraries
     *
     * @param currentHeading current direction as clockwise from north
     * @param currentX current location x-coordinate
     * @param currentY current location y-coordinate
     * @param targetX target point x-coordinate
     * @param targetY target point y-coordinate
     * @return adjustment to heading (right turn amount) to get to target point,
     *         must be 0 <= angle < 360
     */
    public static double calculateHeadingToPoint(double currentHeading, int currentX, int currentY,
                                                 int targetX, int targetY) {
        double deltaX = targetX - currentX;
        double deltaY = targetY - currentY;

        double angleToTarget = Math.toDegrees(Math.atan2(deltaX, deltaY));
        if (angleToTarget < 0) {
            angleToTarget += 360; // Ensure angle is in the range [0, 360)
        }

        double turnAngle = angleToTarget - currentHeading;

        turnAngle = ((turnAngle % 360) + 360) % 360;

        return turnAngle;
    }

    /**
     * Given a sequence of points, calculate the heading adjustments needed to get from each point
     * to the next.
     * <p>
     * Assumes that the turtle starts at the first point given, facing up (i.e. 0 degrees).
     * For each subsequent point, assumes that the turtle is still facing in the direction it was
     * facing when it moved to the previous point.
     * You should use calculateHeadingToPoint() to implement this function.
     * 
     * @param xCoords list of x-coordinates (must be same length as yCoords)
     * @param yCoords list of y-coordinates (must be same length as xCoords)
     * @return list of heading adjustments between points, of size 0 if (# of points) == 0,
     *         otherwise of size (# of points) - 1
     */
    public static List<Double> calculateHeadings(List<Integer> xCoords, List<Integer> yCoords) {
        List<Double> headings = new ArrayList<>();

        // Handle edge case: 0 or 1 points
        if (xCoords.size() <= 1) {
            return headings;
        }

        double currentHeading = 0.0; // Start facing north (0 degrees)

        // Calculate heading adjustments for each segment
        for (int i = 0; i < xCoords.size() - 1; i++) {
            int currentX = xCoords.get(i);
            int currentY = yCoords.get(i);
            int targetX = xCoords.get(i + 1);
            int targetY = yCoords.get(i + 1);

            // Calculate turn angle needed to face next point
            double turnAngle = calculateHeadingToPoint(currentHeading, currentX, currentY, targetX, targetY);
            headings.add(turnAngle);

            // Update current heading for next iteration
            currentHeading = (currentHeading + turnAngle) % 360;
        }

        return headings;
    }

    /**
     * Draw your personal, custom art.
     * <p>
     * Many interesting images can be drawn using the simple implementation of a turtle.  For this
     * function, draw something interesting; the complexity can be as little or as much as you want.
     * 
     * @param turtle the turtle context
     */
    public static void drawPersonalArt(Turtle turtle) {
        // Draw a colorful spiral flower
        PenColor[] colors = {PenColor.RED, PenColor.ORANGE, PenColor.YELLOW,
                PenColor.GREEN, PenColor.BLUE, PenColor.MAGENTA};

        // Draw 8 spiral petals
        for (int i = 0; i < 8; i++) {
            turtle.color(colors[i % colors.length]);
            drawSpiralPetal(turtle, 100);
            turtle.turn(45); // 360/8 = 45 degrees between petals
        }

        // Draw center
        turtle.color(PenColor.YELLOW);
        drawStar(turtle);
    }

    /**
     * Draws a single spiral petal
     * @param turtle the turtle context
     * @param length length of the petal
     */
    private static void drawSpiralPetal(Turtle turtle, int length) {
        for (int i = 0; i < 6; i++) {
            turtle.forward(length / 2);
            turtle.turn(60);
            length = (int)(length * 0.8);
        }
        // Return roughly to center
        turtle.turn(180);
        turtle.forward(15);
        turtle.turn(180);
    }

    /**
     * Draws a small star
     *
     * @param turtle the turtle context
     */
    private static void drawStar(Turtle turtle) {
        for (int i = 0; i < 5; i++) {
            turtle.forward(5);
            turtle.turn(144); // 360/5 * 2 for star shape
        }
    }

    /**
     * Main method.
     * <p>
     * This is the method that runs when you run "java TurtleSoup".
     * 
     * @param args unused
     */
    public static void main(String[] args) {
        DrawableTurtle turtle = new DrawableTurtle();

        drawPersonalArt(turtle);

        // draw the window
        turtle.draw();
    }

}
