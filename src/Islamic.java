import applet.GuiSketch;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class Islamic extends GuiSketch {

    private PGraphics tex;
    private ArrayList<PImage> images = new ArrayList<PImage>();
    private ArrayList<PImage> imagesToReset = new ArrayList<>();
    private int imageCount = 6;
    private PVector off;
    private float t;
    private float size = 1920 / 15;
    private float w = size * 2;
    private float h = sqrt(3) * size;
    private float xstep = w * (3 / 4f);
    private float ystep = h;
    private int hexCountX;
    private int hexCountY;
    private float equilateralTriangleHeight = sqrt(3) / 2;

    private float frameRecStart = -1;
    private float frameRecFinish;
    private float timeMultiplier = 3;

    private String randomImageUrl;

    public static void main(String[] args) {
        GuiSketch.main("Islamic");
    }

    public void settings() {
        size(800, 800, P2D);
//        fullScreen(P2D);
    }

    public void setup() {
        hexCountX = floor(width / xstep + 2);
        hexCountY = floor(height / ystep + 2);
        tex = createGraphics(floor(size), floor(size), P2D);
        reset();
    }

    public void draw() {
        t += radians(1/timeMultiplier);
        background(0);
        updateTexture();
        drawHexagonGrid();

        if (frameRecStart > 0 && frameCount <= frameRecFinish) {
            saveFrame(captureFilename);
        }

        float w = (width / (float) imageCount);
        for (PImage img : images) {
            int i = images.indexOf(img);
            float x = i * w;
            imageMode(CORNER);
            image(img, x, 0, w, w);
            if (mousePressed && isPointInRect(mouseX, mouseY, x, 0, w, w)) {
                imagesToReset.add(img);
            }
        }

        for (PImage img : imagesToReset) {
            pause();
            images.remove(img);
            images.add(loadImage(randomImageUrl));
            resume();
        }
        imagesToReset.clear();


        stroke(255, 0, 0);
        float lineX = t % TWO_PI;
        lineX = map(lineX, 0, TWO_PI, 0, width);
        line(lineX, 0, lineX, w);

        drawTextureForDebugging();

        if (mousePressed) {
            PVector move = new PVector(pmouseX - mouseX, pmouseY - mouseY);
            move.rotate(-t);
            off.add(move);
        }
    }

    public void keyPressed() {
        if (key == 'k') {
            frameRecStart = frameCount;
            frameRecFinish = frameRecStart + 360 * timeMultiplier;
        }
        if (key == 'r') {
            reset();
        }
    }


    private void reset() {
        randomImageUrl = randomImageUrl(size*2);
        images.clear();
        for (int i = 0; i < imageCount; i++) {
            images.add(loadImage(randomImageUrl));
            println("downloaded " + (i + 1) + " of " + imageCount);
        }
        off = new PVector();
    }

    private void drawHexagonGrid() {
        for (float xi = -hexCountX / 2; xi <= hexCountX / 2f; xi++) {
            for (float yi = -hexCountY / 2; yi <= hexCountY / 2f; yi++) {
                float x = width / 2f + xi * xstep;
                float y = height / 2f + yi * ystep - ystep / 2f;
                if (xi % 2 == 0) {
                    y += ystep / 2f;
                }
                pushMatrix();
                translate(x, y);
                drawHexagon();
                popMatrix();
            }
        }
    }

    private void drawHexagon() {
        int cornerCount = 6;
        for (int triangleIndex = 0; triangleIndex <= cornerCount; triangleIndex++) {
            float angle0 = map(triangleIndex, 0, cornerCount, 0, TWO_PI);
            float angle1 = map(triangleIndex + 1, 0, cornerCount, 0, TWO_PI);
            float x0 = size * cos(angle0);
            float y0 = size * sin(angle0);
            float x1 = size * cos(angle1);
            float y1 = size * sin(angle1);
            beginShape();
            noStroke();
//            strokeWeight(2);
//            stroke(255);
            textureMode(NORMAL);
            texture(tex);
            vertex(0, 0, 0.5f, 1 - equilateralTriangleHeight);
            if (triangleIndex % 2 == 0) { // mirror the texture every second triangle
                vertex(x0, y0, 0, 1);
                vertex(x1, y1, 1, 1);
            } else {
                vertex(x0, y0, 1, 1);
                vertex(x1, y1, 0, 1);
            }
            endShape();
        }
    }

    private void updateTexture() {
        float srcPos = t % TWO_PI;
        srcPos = map(srcPos, 0, TWO_PI, 0, imageCount);
        int srcIndex = floor(srcPos)%imageCount;
        int srcNext = ceil(srcPos)%imageCount;
        float srcFract = srcPos % 1;
        float w = tex.width;
        float h = tex.height;
        tex.beginDraw();
        tex.colorMode(HSB, 1, 1, 1, 1);
        tex.background(0);
        tex.imageMode(CENTER);
        tex.translate(w / 2, h / 2);
        tex.rotate(t);
        tex.tint(1, 1);
        tex.image(images.get(srcIndex), off.x, off.y);
        tex.tint(1, srcFract);
        tex.image(images.get(srcNext), off.x, off.y);
        tex.endDraw();
    }

    private void drawTextureForDebugging() {
        pushMatrix();
        translate(width - tex.width * .6f, width - tex.height * .6f);
        rectMode(CENTER);
        noStroke();
        fill(0);
        rect(0, 0, tex.width * 1.2f, tex.height * 1.2f);
        imageMode(CENTER);
        image(tex, 0, 0);
        noFill();
        strokeWeight(3);
        stroke(255, 0, 0);
        triangle(0, -(tex.height * equilateralTriangleHeight) / 2, -tex.width / 2, tex.height / 2, tex.width / 2, tex.height / 2);
        popMatrix();
    }
}
