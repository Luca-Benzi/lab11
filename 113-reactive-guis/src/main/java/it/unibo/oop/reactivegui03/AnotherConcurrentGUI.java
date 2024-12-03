package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third example of reactive GUI.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final int SLEEP_TIME = 10_000;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final Agent agent = new Agent();
    private final AgentCountDown countDown = new AgentCountDown();

    /**
     * Builds a new CGUI.
     */
    @SuppressWarnings("CPD-START")
    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        new Thread(agent).start();
        new Thread(countDown).start();
        up.addActionListener((e) -> agent.setDirection(false));
        down.addActionListener((e) -> agent.setDirection(true));
        stop.addActionListener((e) ->  this.stopCounting());
    }
    private void stopCounting() {
        agent.stopCounting();
        up.setEnabled(false);
        down.setEnabled(false);
        stop.setEnabled(false);
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable, Serializable {

        @Serial
        private static final long serialVersionUID = 1L;
        private volatile boolean stop;
        private volatile boolean countDirection;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (!countDirection) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        public void setDirection(final Boolean direction) {
            this.countDirection = direction;
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }

    private final class AgentCountDown implements Runnable, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private volatile boolean stop;
        @Override
        public void run() {
            while (!this.stop) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                AnotherConcurrentGUI.this.stopCounting();
                this.stop = true;
            } 
        }
    }

}
