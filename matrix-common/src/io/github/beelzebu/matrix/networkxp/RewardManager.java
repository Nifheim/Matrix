package io.github.beelzebu.matrix.networkxp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RewardManager {

    private static final Multimap<Integer, Reward> rewardsByLevel = ArrayListMultimap.create();

    public static void loadRewards() {
        addReward(new Reward(1, Arrays.asList("").stream().collect(Collectors.toSet()), Arrays.asList("")));
    }

    public static void addReward(Reward reward) {
        rewardsByLevel.put(reward.getLevel(), reward);
    }

    public static Multimap<Integer, Reward> getRewardsByLevel() {
        return RewardManager.rewardsByLevel;
    }
}
