import java.util.*;

public class CastTheDieImpure {
    static int castTheDieImpure() {
        System.out.println("The die is cast");
        Random rand = new Random();
        return rand.nextInt(6) + 1;
    }
}
