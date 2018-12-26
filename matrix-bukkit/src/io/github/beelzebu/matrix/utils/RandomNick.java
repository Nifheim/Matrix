package io.github.beelzebu.matrix.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomNick {

    private static final Random r = new Random();
    private static final List<String> nicks = Arrays.asList(
            "BobSed",
            "Ssshack14",
            "Globatina",
            "Xx_Mr_Hugo_xX",
            "BobEsmonja",
            "Joasjoas_GG",
            "Clickbayyyt",
            "PvPHGMaster"
    );

    public static String getRandomNick() {
        return nicks.get(r.nextInt(nicks.size()));
    }
}
