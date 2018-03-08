package raycast;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;

/**
 * This class implements an application that represents a XY-diagram with real
 * graphics. You can plot points, figures and new pixels to any given
 * mathematical points.
 *
 * @author Thomas
 */
public class Diagram extends Canvas {

    //All relevant diagram points in mathematical diagram context
    private double min_x, max_x, min_y, max_y, cur_pos_x, cur_pos_y;

    //Pixel height and width of the canvas
    private int pixel_height, pixel_width;

    //Buffered image for faster rendering
    private BufferedImage buffer;

    //Used by the run method. False if not rendering
    private boolean running = true;

    //All points that are in this diagram
    private ArrayList<Figure> figures = new ArrayList<>();

    /**
     * Initiates the diagram values and triggers
     *
     * @param pixel_width The diagram width in pixels
     * @param pixel_height The diagram height in pixels
     */
    public Diagram(int pixel_width, int pixel_height) {

        this.pixel_height = pixel_height;
        this.pixel_width = pixel_width;

        min_x = -10;
        max_x = 10;
        min_y = -10;
        max_y = 10;

        //Repositions on click
        this.setOnMouseClicked(evt -> {
            viewportToCursor(evt.getX(), evt.getY());
            setCurPosition(evt.getX(), evt.getY());
        });

        //Showing current position on move
        setOnMouseMoved(evt -> {
            setCurPosition(evt.getX(), evt.getY());
        });

        setHeight(pixel_height);
        setWidth(pixel_width);
    }

    /**
     * Rendering the canvas once by updating all pixels using a BufferedImage
     */
    public void render() {

        for (int y = 0; y < pixel_height; y++) {
            for (int x = 0; x < pixel_width; x++) {
                buffer.setRGB(x, y, Color.DARK_GRAY.getRGB());
            }
        }

        figures.forEach((f) -> {
            f.drawFigure();
        });

        drawXYaxis();
        showStats();

        this.getGraphicsContext2D().drawImage(SwingFXUtils.toFXImage(buffer, null), 0, 0);
    }

    /**
     * Run method for this diagram. This method controls vital run factors like
     * FPS and more to come
     */
    public void run() {

        if (buffer == null) {
            buffer = new BufferedImage(
                    pixel_width,
                    pixel_height,
                    BufferedImage.TYPE_INT_RGB
            );
        }

        int fps = 40;

        Thread task = new Thread(() -> {

            while (running) {

                render();

                try {
                    TimeUnit.MILLISECONDS.sleep(1000 / fps);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });

        task.setDaemon(true);
        task.start();
    }

    /**
     * Shows diagram information
     */
    private void showStats() {
        Graphics g = buffer.getGraphics();
        g.drawString("x " + cur_pos_x, 10, 20);
        g.drawString("y " + cur_pos_y, 10, 40);
    }

    /**
     * Draws a new pixel at given position
     *
     * @param p Mathematical position in diagram
     */
    public void drawPixel(Point p) {
        buffer.setRGB(toPixelValX(p.getX()), toPixelValY(p.getY()), p.getColor().getRGB());
    }

    /**
     * Draws a point at a given position
     *
     * @param p Mathematical position in diagram
     */
    public void drawPoint(Point p) {
        Graphics g = buffer.getGraphics();
        g.setColor(p.getColor());
        g.fillRect(toPixelValX(p.getX()) - 1, toPixelValY(p.getY()) - 1, 3, 3);
    }

    /**
     * Draws a line from one position to another
     *
     * @param start Mathematical start position in diagram
     * @param end Mathematical end position in diagram
     * @param color Color of line
     */
    public void drawLine(Point start, Point end, Color color) {
        Graphics g = buffer.getGraphics();
        g.setColor(color);
        g.drawLine(
                toPixelValX(start.getX()),
                toPixelValY(start.getY()),
                toPixelValX(end.getX()),
                toPixelValY(end.getY())
        );
    }

    /**
     * Draws the X and Y -axis on diagram
     */
    private void drawXYaxis() {
        if (min_y < 0 && max_y > 0) {
            drawLine(new Point(min_x, 0), new Point(max_x, 0), Color.LIGHT_GRAY);
        }
        if (min_x < 0 && max_x > 0) {
            drawLine(new Point(0, min_y), new Point(0, max_y), Color.LIGHT_GRAY);
        }
    }

    /**
     * Repositions the diagram to focus on the triggering x and y pixel values
     * (e.g. getX(), getY())
     *
     * @param x Pixel value for X-axis
     * @param y Pixel value for Y-axis
     */
    private void viewportToCursor(double x, double y) {
        setViewportConstraints(
                toPointValX(x - pixel_width / 2),
                toPointValX(x + pixel_width / 2),
                toPointValY(y + pixel_height / 2),
                toPointValY(y - pixel_height / 2)
        );
    }

    /**
     * Sets the position to origin (default position)
     */
    public void viewportToCenter() {
        setViewportConstraints(
                -(max_x - min_x) / 2,
                (max_x - min_x) / 2,
                -(max_y - min_y) / 2,
                (max_y - min_y) / 2
        );
    }

    /**
     * Give the current viewport new constraints
     *
     * @param min_x Left constraint
     * @param max_x Right constraint
     * @param min_y Top constraint
     * @param max_y Bottom constraint
     */
    public void setViewportConstraints(double min_x, double max_x, double min_y, double max_y) {
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
    }

    /**
     * Add a new point to the drawing table
     *
     * @param x Horizontal position (mathematical points)
     * @param y Vertical position (mathematical points)
     */
    public void addNewPoint(double x, double y) {
        figures.add(new Point(x, y, Color.cyan));
    }

    /**
     * Sets the upper-left position to current cursor position
     *
     * @param x Pixel value for X-axis
     * @param y Pixel value for Y-axis
     */
    private void setCurPosition(double x, double y) {
        cur_pos_x = (int) (toPointValX(x) * 100 + 0.5) / 100.0;
        cur_pos_y = (int) (toPointValY(y) * 100 + 0.5) / 100.0;
    }

    /**
     * Converts a mathematical point to a pixel point in X-axis
     *
     * @param point The pixel which is being converted
     * @return The converted mathematical point
     */
    private int toPixelValX(double point) {
        return (int) (pixel_width * (point - min_x) / (max_x - min_x) + 0.5);
    }

    /**
     * Converts a mathematical point to a pixel point in Y-axis
     *
     * @param point The pixel which is being converted
     * @return The converted mathematical point
     */
    private int toPixelValY(double point) {
        return (int) (pixel_height * (max_y - point) / (max_y - min_y) + 0.5);
    }

    /**
     * Converts a pixel point to a mathematical point in X-axis
     *
     * @param pixel The pixel which is being converted
     * @return The converted mathematical point
     */
    private double toPointValX(double pixel) {
        return pixel * (max_x - min_x) / pixel_width + min_x;
    }

    /**
     * Converts a pixel point to a mathematical point in Y-axis
     *
     * @param pixel The pixel which is being converted
     * @return The converted mathematical point
     */
    private double toPointValY(double pixel) {
        return -(pixel * (max_y - min_y) / pixel_height - max_y);
    }

    /**
     * TODO
     */
    interface Figure {

        public void drawFigure();

        public Color getColor();

    }

    /**
     * TODO
     */
    public class Block implements Figure {

        private final Point TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
        //private Image image_top, image_right, image_bottom, image_left; TODO

        private Color color;

        public Block(Point top_left, Point top_right, Point bottom_right, Point bottom_left, Color color) {
            this.color = color;

            TOP_LEFT = top_left;
            TOP_RIGHT = top_right;
            BOTTOM_RIGHT = bottom_right;
            BOTTOM_LEFT = bottom_left;
        }

        @Override
        public void drawFigure() {
            drawLine(TOP_LEFT, TOP_RIGHT, color);
            drawLine(TOP_RIGHT, BOTTOM_RIGHT, color);
            drawLine(BOTTOM_RIGHT, BOTTOM_LEFT, color);
            drawLine(BOTTOM_LEFT, TOP_LEFT, color);
        }

        @Override
        public Color getColor() {
            return color;
        }
    }

    /**
     * TODO
     */
    public class Point implements Figure {

        private final double X;
        private final double Y;
        private final Color color;

        public Point(double X, double Y, Color color) {
            this.color = color;
            this.X = X;
            this.Y = Y;
        }

        public Point(double X, double Y) {
            this.X = X;
            this.Y = Y;
            color = Color.WHITE;
        }

        public double getX() {
            return X;
        }

        public double getY() {
            return Y;
        }

        @Override
        public void drawFigure() {
            drawPoint(this);
        }

        @Override
        public Color getColor() {
            return color;
        }
    }

}
