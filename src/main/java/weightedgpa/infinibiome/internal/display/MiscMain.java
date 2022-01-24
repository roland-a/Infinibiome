package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.applet.Applet;

public final class MiscMain extends Applet {
    public MiscMain(){
        //super(0, "1");

        try {
            main();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        System.out.println("done");
    }

    /*
    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        return Color.WHITE;
    }

     */

    private void main() throws Throwable{
        System.out.println(
            Log2helper.mod(
                16,
                4
            )
        );
    }
}
