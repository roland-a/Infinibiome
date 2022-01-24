package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.internal.misc.ProgressPrinter;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class DisplayMapBase extends Applet implements KeyListener, MouseListener {
    private ShiftImage<Color> image = null;

    final int scale;

    DisplayMapBase(int scale) {
        this.scale = scale;

        addKeyListener(this);
        addMouseListener(this);

    }

    protected abstract Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ);

    protected Object displayStringAt(int x, int z){
        return "";
    }

    protected void onFinished(){

    }

    @Override
    public void paint(Graphics graphic) {
        if (image == null){
            image = new ShiftImage<>(
                getWidth(),
                getHeight(),
                (x, z) -> getColor(x * scale, z * scale, x, z)
            );
        }

        ProgressPrinter progressPrinter = new ProgressPrinter(getWidth() * getHeight());

        AtomicBoolean lock = new AtomicBoolean(false);

        image.iterParallel(
            (x, z, color) -> {
                while (!lock.compareAndSet(false, true)){}

                graphic.setColor(color);
                graphic.fillRect(x, z, 1, 1);
                lock.set(false);

                progressPrinter.incrementAndTryPrintProgress();
            }
        );

        onFinished();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP){
            image.shift(0, -getHeight() / 2);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            image.shift(0, getHeight() / 2);
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT){
            image.shift(-getWidth() / 2, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            image.shift(getWidth() / 2, 0);
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int x = (mouseEvent.getX() + image.getXOffset())*scale;
        int z = (mouseEvent.getY() + image.getZOffset())*scale;
        showStatus(
            String.format(
                "%s %s %s",
                x,
                z,
                displayStringAt(x, z)
            )
        );
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {}

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}
}
