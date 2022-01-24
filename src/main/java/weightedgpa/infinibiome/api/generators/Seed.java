package weightedgpa.infinibiome.api.generators;

import com.google.common.primitives.Ints;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Allows for easy random seeding without accidental seed reuse.
 */
public final class Seed {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Set<String> siblings = new HashSet<>();
    private final int innerSeed;
    private boolean usedInnerSeed = false;

    private Seed(int innerSeed) {
        this.innerSeed = innerSeed;
    }

    /**
     * The only standalone seed available.
     * Every seed is derived or has an ancestor derived from this seed.
     */
    public static final Seed ROOT = new Seed(0);

    /**
     * Creates a new seed.
     * The resulting seed is the hash of this seed plus the seedName
     *
     * @param newSeedName
     * The string used to creates a new seed.
     * The same string but used on a different seed will result in different seeds.
     *
     * @throws
     * IllegalArgumentException
     * If seedName was already used before with this seed.
     */
    public Seed newSeed(String newSeedName){
        Validate.isTrue(
            !siblings.contains(newSeedName),
            "branch %s already in used\n",
            newSeedName
        );

        //siblings.put(newSeedName, Thread.currentThread().getStackTrace());

        byte[] preHashed = (innerSeed + newSeedName).getBytes(CHARSET);

        byte[] hashed = DigestUtils.getSha1Digest().digest(preHashed);

        int newSeed = Ints.fromByteArray(hashed);

        return new Seed(newSeed);
    }

    /**
     * @return
     * The seed as an int.
     *
     * @throws IllegalArgumentException
     * If the int seed was obtained from this seed before.
     * This is to prevent accidental seed reuseage.
     */
    public int getAsInt(){
        Validate.isTrue(
            !usedInnerSeed,
            "already used as int before"
        );
        this.usedInnerSeed = true;

        return innerSeed;
    }


    public Seed copyWithoutWarnings(){
        return new Seed(innerSeed);
    }
}
