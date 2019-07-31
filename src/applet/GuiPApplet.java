package applet;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
public class GuiPApplet extends PApplet {
    //utility quick sketching variables
    private String sketchName = this.getClass().getSimpleName();
    private String id = sketchName + "_" + year() + nf(month(), 2) + nf(day(), 2) + "-" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    protected String captureDir = "out/capture/" + id + "/";

    // gui grid variables
    private float rowWidthWindowFraction = 1 / 3f;
    private float rowHeightWindowFraction = 1 / 14f;
    private float elementPaddingFractionX = .9f;
    private float elementPaddingFractionY = .8f;
    private float buttonsPerRow = 2;
    private float togglesPerRow = 2;
    private float slidersPerRow = 1;
    private float textActive = 0;
    private float textPassive = .5f;
    private float pressedFill = .5f;
    private float mouseOutsideStroke = .5f;
    private float mouseOverStroke = 1f;
    private ArrayList<Slider> sliders = new ArrayList<Slider>();
    private ArrayList<Button> buttons = new ArrayList<Button>();
    private ArrayList<Toggle> toggles = new ArrayList<Toggle>();
    private ArrayList<GuiElement> bin = new ArrayList<GuiElement>();
    private float baseR;
    private float vertexCount = 50;
    private ArrayList<PVector> cogShape;
    private ArrayList<PVector> arrowShape;
    private PVector extensionTogglePos = new PVector();
    private boolean extensionTogglePressedLastFrame = false;
    private float extensionAnimationDuration = 60;
    private float extensionAnimationStarted = -extensionAnimationDuration;
    private float extensionEasing = -1;
    private float extensionAnimationTarget = 1;
    private float offsetXextended = (1 / 24f);
    private float offsetXretracted = -rowWidthWindowFraction;
    private float offsetYWindowFraction = (1 / 24f);

    private float backgroundAlpha = .3f;

    private int lastInteractedWithExtensionToggle = -1;
    private int extensionToggleFadeoutDuration = 30;
    private int extensionToggleFadeoutDelay = 180;

    //Optionally call these with super.setup() and super.draw() from the extending class constructor
    public void setup() {
        if (width < 1000) {
            surface.setLocation(1920 - width - 20, 20);
        }
    }

    public void draw() {
        float nonFlickeringFrameRate = frameRate > 58 && frameRate < 62 ? 60 : frameRate;
        surface.setTitle(sketchName + " (" + floor(nonFlickeringFrameRate) + " fps)");
    }

    protected void gui() {
        gui(true);
    }

    protected void gui(boolean extendedByDefault) {
        if (isGuiEmpty()) {
            return;
        }
        pushMatrix();
        pushStyle();
        resetMatrixInAnyRenderer();
        colorMode(HSB, 1, 1, 1, 1);
        drawBackground();
        updateExtension(extendedByDefault);
        drawExtensionToggle();
        for (Toggle t : toggles) {
            if (t.lastQueried == frameCount) {
                updateElement(t);
            }
        }
        for (Button b : buttons) {
            if (b.lastQueried == frameCount) {
                updateElement(b);
            }
        }
        for (Slider s : sliders) {
            if (s.lastQueried == frameCount) {
                updateElement(s);
            }
        }
        popStyle();
        popMatrix();
    }

    private boolean isGuiEmpty() {
        return toggles.size() == 0 && buttons.size() == 0 && sliders.size() == 0;
    }

    private void drawBackground() {
        noStroke();
        fill(0, backgroundAlpha);
        PVector offset = getOffset();
        float w = width * rowWidthWindowFraction + offset.x;
        float h = height * rowHeightWindowFraction * .5f + getPositionOfLastItem().y + offset.y;
        rectMode(CORNER);
        rect(0, 0, w, h);
    }

    protected boolean button(String name) {
        Button button = findButtonByName(name);
        if (button == null) {
            button = new Button(name);
        }
        button.lastQueried = frameCount;
        return button.value;
    }

    protected boolean toggle(String name) {
        return toggle(name, false);
    }

    protected boolean toggle(String name, boolean initial) {
        Toggle toggle = findToggleByName(name);
        if (toggle == null) {
            toggle = new Toggle(name, initial);
        }
        toggle.lastQueried = frameCount;
        return toggle.value;
    }

    protected float slider(String name) {
        return slider(name, 0, 1);
    }

    protected float slider(String name, float max) {
        return slider(name, 0, max);
    }

    protected float slider(String name, float min, float max) {
        float range = max - min;
        return slider(name, min, max, min + range / 2);
    }

    protected float slider(String name, float min, float max, float initial) {
        Slider slider = findSliderByName(name);
        if (slider == null) {
            slider = new Slider(name, min, max, initial);
        }
        slider.lastQueried = frameCount;
        return slider.value;
    }

    private void updateElement(GuiElement element) {
        PVector pos = getPosition(element);
        String simpleName = element.getClass().getSimpleName();
        if ("Slider".equals(simpleName)) {
            updateSlider((Slider) element, pos);
        } else if ("Button".equals(simpleName)) {
            updateButton((Button) element, pos);
        } else if ("Toggle".equals(simpleName)) {
            updateToggle((Toggle) element, pos);
        }
    }

    private void resetMatrixInAnyRenderer() {
        if (sketchRenderer().equals(P3D)) {
            camera();
        } else {
            resetMatrix();
        }
    }

    private void updateButton(Button button, PVector pos) {
        float w = ((width * rowWidthWindowFraction) / buttonsPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = button.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        button.pressed = mousePressed && mouseOver;
        button.value = wasPressedLastFrame && !button.pressed && !mousePressed;
        noFill();
        if (button.pressed) {
            fill(pressedFill);
        }
        stroke(mouseOver ? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(button.pressed ? textActive : textPassive);
        textSize(h * .5f);
        textAlign(CENTER, CENTER);
        text(button.name, pos.x, pos.y, w, h);
    }

    private void updateToggle(Toggle toggle, PVector pos) {
        float w = ((width * rowWidthWindowFraction) / togglesPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        boolean wasPressedLastFrame = toggle.pressed;
        boolean mouseOver = isPointInRect(mouseX, mouseY, pos.x, pos.y, w, h);
        toggle.pressed = mousePressed && mouseOver;
        if (wasPressedLastFrame && !toggle.pressed && !mousePressed) {
            toggle.value = !toggle.value;
        }
        noFill();
        if (toggle.value) {
            fill(pressedFill);
        }
        stroke(mouseOver ? mouseOverStroke : mouseOutsideStroke);
        strokeWeight(1);
        rectMode(CORNER);
        rect(pos.x, pos.y, w, h);
        fill(toggle.value ? textActive : textPassive);
        if (mouseOver) {
            fill(mouseOverStroke);
        }
        textSize(h * .5f);
        textAlign(CENTER, CENTER);
        text(toggle.name, pos.x, pos.y, w, h);
    }

    private void updateSlider(Slider slider, PVector pos) {
        float w = ((width * rowWidthWindowFraction) / slidersPerRow) * elementPaddingFractionX;
        float h = height * rowHeightWindowFraction * elementPaddingFractionY;
        float extraSensitivity = 5;
        float gray = mouseOutsideStroke;
        float alpha = 1;

        // update values
        if (isPointInRect(mouseX, mouseY, pos.x - extraSensitivity, pos.y, w + extraSensitivity * 2, h)) {
            gray = mouseOverStroke;
            stroke(gray, alpha);
            if (mousePressed) {
                slider.value = map(mouseX, pos.x, pos.x + w, slider.min, slider.max);
                slider.value = constrain(slider.value, slider.min, slider.max);
            }
        }

        // draw slider
        strokeCap(PROJECT);
        strokeWeight(1);
        stroke(gray, alpha);
        rectMode(CORNER);
        float sliderY = pos.y + h * .5f;
        line(pos.x, sliderY, pos.x + w, sliderY);
        float valueX = map(slider.value, slider.min, slider.max, pos.x, pos.x + w);

        // draw selection bar
        strokeWeight(3);
        stroke(gray, alpha);
        line(valueX, pos.y, valueX, pos.y + h * .6f);

        // draw text
        fill(gray, alpha);
        textAlign(LEFT, CENTER);
        float textOffsetX = w * .05f;
        float textOffsetY = h * .25f;
        textSize(h * .5f);
        text(slider.name, pos.x + textOffsetX, pos.y + textOffsetY);
        textAlign(RIGHT, CENTER);

        // disregard values after floating point if value > floorBoundary
        int floorBoundary = 10;
        String humanReadableValue;
        if (abs(slider.value) < floorBoundary) {
            humanReadableValue = nf(slider.value, 0, 0);
        } else {
            humanReadableValue = String.valueOf(round(slider.value));
        }
        text(humanReadableValue, pos.x + w - textOffsetX, pos.y + textOffsetY);
    }

    private PVector getPosition(GuiElement element) {
        float rowHeight = height * rowHeightWindowFraction;
        float rowWidth = width * rowWidthWindowFraction;
        PVector offset = getOffset();

        int buttonRows = ceil(buttons.size() / buttonsPerRow);
        int toggleRows = ceil(toggles.size() / togglesPerRow);
        int row = 0;
        int column = 0;
        int itemsPerRow = 1;
        String simpleName = element.getClass().getSimpleName();
        if ("Button".equals(simpleName)) {
            int index = buttons.indexOf(element);
            itemsPerRow = floor(buttonsPerRow);
            row = floor(index / buttonsPerRow);
            column = floor(index % buttonsPerRow);
        } else if ("Toggle".equals(simpleName)) {
            int index = toggles.indexOf(element);
            itemsPerRow = floor(togglesPerRow);
            row = buttonRows + floor(index / togglesPerRow);
            column = floor(index % togglesPerRow);
        } else if ("Slider".equals(simpleName)) {
            int index = sliders.indexOf(element);
            itemsPerRow = floor(slidersPerRow);
            row = buttonRows + toggleRows + floor(index / slidersPerRow);
            column = floor(index % slidersPerRow);
        }
        return new PVector(offset.x + column * (rowWidth / itemsPerRow), offset.y + row * rowHeight);
    }

    private PVector getPositionOfLastItem() {
        if (sliders.size() > 0) {
            for (int i = sliders.size() - 1; i >= 0; i--) {
                Slider s = sliders.get(i);
                if (s.lastQueried == frameCount) {
                    return getPosition(s);
                }
            }
        }
        if (toggles.size() > 0) {
            for (int i = toggles.size() - 1; i >= 0; i--) {
                Toggle t = toggles.get(i);
                if (t.lastQueried == frameCount) {
                    return getPosition(t);
                }
            }
        }
        if (buttons.size() > 0) {
            for (int i = buttons.size() - 1; i >= 0; i--) {
                Button b = buttons.get(i);
                if (b.lastQueried == frameCount) {
                    return getPosition(b);
                }
            }
        }
        return new PVector();
    }

    private void updateExtension(boolean extendedByDefault) {
        float previousBaseR = baseR;
        baseR = min(width, height) * rowHeightWindowFraction * .5f;
        if (cogShape == null || previousBaseR != baseR) {
            cogShape = createCog();
            arrowShape = createArrow();
        }
        float extensionLinearNormalized = constrain(map(frameCount, extensionAnimationStarted, extensionAnimationStarted + extensionAnimationDuration, 0, 1), 0, 1);
        extensionEasing = ease(extensionLinearNormalized, 3);
        if (extensionAnimationTarget == -1) {
            if (extendedByDefault) {
                extensionAnimationTarget = 1;
            } else {
                extensionAnimationTarget = 0;
            }
        }
        if (extensionAnimationTarget == 0) {
            extensionEasing = 1 - extensionEasing;
        }
    }

    private ArrayList<PVector> createArrow() {
        ArrayList<PVector> arrow = new ArrayList<PVector>();
        for (int i = 0; i < vertexCount; i++) {
            float iN = map(i, 0, vertexCount - 1, 0, 2);
            if (iN < 1) {
                float x = baseR * abs(.5f - iN) * 2;
                float y = lerp(-baseR, baseR, iN);
                arrow.add(new PVector(x, y));
            } else {
                iN = 2 - iN;
                float x = baseR * abs(.5f - iN) * 2 + baseR * .3f;
                float y = lerp(-baseR, baseR, iN);
                arrow.add(new PVector(x, y));
            }
        }
        return arrow;
    }

    private ArrayList<PVector> createCog() {
        ArrayList<PVector> cog = new ArrayList<PVector>();
        float toothR = baseR * .2f;
        for (int i = 0; i < vertexCount; i++) {
            float iNormalized = map(i, 0, vertexCount - 1, 0, 1);
            float a = iNormalized * TWO_PI;
            float r = baseR + toothR * (sin(iNormalized * 50) > 0 ? 0 : 1);
            cog.add(new PVector(r * cos(a), r * sin(a)));
        }
        return cog;
    }

    private void drawExtensionToggle() {
        PVector offset = getOffset();
        extensionTogglePos.x = offset.x + width * rowWidthWindowFraction + baseR * 1.5f;
        extensionTogglePos.y = offset.y + baseR * .5f;
        float alpha = 1 - constrain(map(frameCount,
                lastInteractedWithExtensionToggle + extensionToggleFadeoutDelay,
                lastInteractedWithExtensionToggle + extensionToggleFadeoutDelay + extensionToggleFadeoutDuration,
                0, 1),
                0, 1);
        stroke(mouseOutsideStroke, alpha);
        noFill();
        strokeWeight(2);
        boolean atEitherEnd = extensionEasing == 0 || extensionEasing == 1;
        boolean justReleasedMouse = extensionTogglePressedLastFrame && !mousePressed;
        if (isPointInRect(mouseX, mouseY, extensionTogglePos.x - baseR, extensionTogglePos.y - baseR, baseR * 2, baseR * 2)) {
            stroke(mouseOverStroke);
            lastInteractedWithExtensionToggle = frameCount;
            if (atEitherEnd && justReleasedMouse) {
                startExtensionAnimation();
            }
            if (mousePressed) {
                extensionTogglePressedLastFrame = true;
            }
        }
        if (!mousePressed) {
            extensionTogglePressedLastFrame = false;
        }
        beginShape();
        for (int i = 0; i < vertexCount; i++) {
            PVector cogVertex = cogShape.get(i);
            PVector arrowVertex = arrowShape.get(i);
            vertex(extensionTogglePos.x + lerp(cogVertex.x, arrowVertex.x, extensionEasing), extensionTogglePos.y + lerp(cogVertex.y, arrowVertex.y, extensionEasing));
        }
        endShape(CLOSE);
    }

    private void startExtensionAnimation() {
        boolean extend = extensionEasing == 0;
        extensionAnimationStarted = frameCount;
        if (extend) {
            extensionAnimationTarget = 1;
        } else {
            extensionAnimationTarget = 0;
        }
    }

    private PVector getOffset() {
        float xOffsetWindowFraction = map(extensionEasing, 0, 1, offsetXretracted, offsetXextended);
        float xOffset = width * xOffsetWindowFraction;
        float yOffset = height * offsetYWindowFraction;
        return new PVector(xOffset, yOffset);
    }

    protected float ease(float p, float g) {
        if (p < 0.5)
            return 0.5f * pow(2 * p, g);
        else
            return 1 - 0.5f * pow(2 * (1 - p), g);
    }

    private boolean isPointInRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    private Slider findSliderByName(String query) {
        for (Slider s : sliders) {
            if (s.name.equals(query)) {
                return s;
            }
        }
        return null;
    }

    private Button findButtonByName(String query) {
        for (Button b : buttons) {
            if (b.name.equals(query)) {
                return b;
            }
        }
        return null;
    }

    private Toggle findToggleByName(String query) {
        for (Toggle t : toggles) {
            if (t.name.equals(query)) {
                return t;
            }
        }
        return null;
    }

    private class GuiElement {
        String name;
        int lastQueried = 0;

        GuiElement(String name) {
            this.name = name;
        }
    }

    private class Slider extends GuiElement {
        float min, max, initial, value;

        Slider(String name, float min, float max, float initial) {
            super(name);
            this.min = min;
            this.max = max;
            this.initial = initial;
            this.value = initial;
            sliders.add(this);
        }
    }

    private class Button extends GuiElement {
        boolean pressed, value;

        Button(String name) {
            super(name);
            buttons.add(this);
        }
    }

    private class Toggle extends GuiElement {
        boolean value, initial, pressed;

        Toggle(String name, boolean initial) {
            super(name);
            toggles.add(this);
            this.initial = initial;
            this.value = initial;
        }
    }
}
