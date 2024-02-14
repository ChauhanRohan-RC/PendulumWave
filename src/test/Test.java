package test;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.event.KeyEvent;
import util.U;

import java.awt.*;

public class Test extends PApplet {

//    private static final Dimension WINDOW_SIZE = new Dimension(round(U.SCREEN_SIZE.width * 0.8f), round(U.SCREEN_SIZE.height * 0.8f));

    PeasyCam cam;

    @Override
    public void settings() {
        super.settings();


        size(800, 600, P3D);
    }

    @Override
    public void setup() {
        super.setup();
        colorMode(HSB);

        cam = new PeasyCam(this, 500);

        sphereDetail(10);
    }

    private void preDraw() {

    }

    public void draw() {
        preDraw();

        background(0);
        lights();

        pushMatrix();
        pushStyle();

        float count = 14;
        float rad = 10;
        float gap = 25;

        for (int i = 0; i < count; i++) {
            translate(0, 0, -gap);

            strokeWeight(1);
            stroke(255);
            line(0, 0, 0, 0, -100, 0);

            fill(color((int) map(i, 0, count - 1, 255, 0), 255, 255));
            strokeWeight(0);
            sphere(rad);
        }

        popStyle();
        popMatrix();

        postDraw();
    }


    private void postDraw() {

    }

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);

    }


    public static void main(String[] args) {
        println(Toolkit.getDefaultToolkit().getScreenSize());

        println(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode());

        for (DisplayMode mode: GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayModes()) {
            println(mode);
        }

//        final PApplet test = new Test();
//
//        runSketch(concat(new String[]{test.getClass().getName()}, args), test);
    }

}
