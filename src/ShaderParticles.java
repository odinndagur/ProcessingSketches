import applet.KrabApplet;
import processing.core.PGraphics;

/**
 * Created by Jakub 'Krab' Rak on 2020-01-19
 */
public class ShaderParticles extends KrabApplet {
    private PGraphics pg;

    public static void main(String[] args) {
        KrabApplet.main("ShaderParticles");
    }

    public void settings() {
        size(800, 800, P2D);
    }

    public void setup() {
        surface.setAlwaysOnTop(true);
        surface.setLocation(1920-820,20);
        pg = createGraphics(width, height, P2D);
        pg.beginDraw();
        pg.background(0);
        pg.endDraw();
//        frameRecordingDuration *= 3;
    }

    public void draw() {
        pg.beginDraw();
        String sdf2D = "particles.glsl";
        uniform(sdf2D).set("time", t);
        hotFilter(sdf2D, pg);
        pg.endDraw();
        image(pg, 0, 0);
        rec(pg);
        gui();
    }
}
