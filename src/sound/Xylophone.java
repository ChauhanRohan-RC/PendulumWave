package sound;

import processing.core.PApplet;
import processing.event.KeyEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Xylophone extends PApplet {

//    private static final Dimension WINDOW_SIZE = new Dimension(round(U.SCREEN_SIZE.width * 0.8f), round(U.SCREEN_SIZE.height * 0.8f));

    static float midiToFreq(int note, float baseFreq) {
        return (pow(2, ((note - 69) / 12.0f))) * baseFreq;
    }

    static float midiToFreq(int note) {
        return midiToFreq(note, 440.0f);
    }


    final int[] notes;
    final char[] keys;
    final Map<Character, Integer> keyToNotes;

    MidiNotePlayer player;
    final Set<Character> selectedKeys = new HashSet<>();

    public Xylophone() {
        notes = new int[]{ 50, 52, 54, 55, 57, 59, 61, 62 };
        keys = new char[] { 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k' };

        keyToNotes = new HashMap<>();

        for (int i=0; i < keys.length; i++) {
            keyToNotes.put(keys[i], notes[i]);
        }
    }

    @Override
    public void settings() {
//        fullScreen(JAVA2D);
        size(round(displayWidth * 0.6f), round(displayHeight * 0.6f), JAVA2D);
    }

    @Override
    public void setup() {
        surface.setTitle("sound.Xylophone");
        surface.setResizable(true);
        surface.setLocation((displayWidth - width) / 2, (displayHeight - height) / 2);

        player = new MidiNotePlayer(this)
                .setPolyRhythmEnabled(true)
                .setAmplitude(0.5f)
                .setAttackTime(0.01f)
                .setSustainTime(0.2f)
                .setSustainLevel(0.5f)
                .setReleaseTime(0.25f);
    }

    private void preDraw() {

    }

    public void draw() {
        preDraw();

        background(0);

        final int n = keys.length;
        float hgap = width / 20f;
        float vgap = height / 10f;
        float keyW = (width - ((n + 1) * hgap)) / n;
        float keyH = height - (vgap * 2);

        rectMode(CORNER);

        for (int i=0; i < n; i++) {
            boolean selected = selectedKeys.contains(keys[i]);
            if (selected) {
                fill(255, 163, 245);
                stroke(255);
                strokeWeight(4);
            } else {
                fill(225);
                stroke(150);
                strokeWeight(2);
            }

            float x = (i * keyW) + (i + 1) * hgap;
            float y = vgap;
            rect(x, y, keyW, keyH);

            String text = String.valueOf(Character.toUpperCase(keys[i]));
            float textW = textWidth(text);

            fill(0);
            textSize(40);
            text(text, x + (keyW / 2f) - (textW / 2f), y + (keyH / 2f));
        }

        postDraw();
    }


    private void postDraw() {

    }

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);

        final char key = event.getKey();
        if (keyToNotes.containsKey(key)) {
            println("Pressed: " + Character.toUpperCase(key));
            selectedKeys.add(key);
            player.play(keyToNotes.get(key));
        } else {
            switch (key) {
                case ' ' -> {
                    player.togglePolyRhythmEnabled();
                    println("Multiple Notes: " + (player.isPolyRhythmEnabled()? "Enabled": "Disabled"));
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        super.keyReleased(event);

        final char key = event.getKey();
        if (selectedKeys.remove(key)) {
            println("Released: " + Character.toUpperCase(key));
        }
    }

    public static void main(String[] args) {
        final PApplet test = new Xylophone();

        runSketch(concat(new String[]{test.getClass().getName()}, args), test);
    }

}
