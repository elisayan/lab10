package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private static final String FILE_NAME = "src/main/resources/config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     * @throws IOException
     */
    public DrawNumberApp(final DrawNumberView... views) throws FileNotFoundException, IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Map<String, Integer> map = new LinkedHashMap<>();
        try (final BufferedReader r = new BufferedReader(new FileReader(FILE_NAME));) {
            String line;
            while ((line = r.readLine()) != null){
                String[] part = line.split(":");
                String key = part[0].trim();
                int value = Integer.parseInt(part[1].trim());    
                map.put(key, value);
            }
                
            
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            displayError(e.getMessage());
        }
        this.model = new DrawNumberImpl(map.get("minimum"), map.get("maximum"), map.get("attempts"));
    }

    private void displayError(final String err) {
        for (final DrawNumberView view: views) {
            view.displayError(err);
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws IOException
     */
    public static void main(final String... args) throws FileNotFoundException, IOException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(),
                new PrintStreamView(System.out),
                new PrintStreamView("output.log"));
    }

}
