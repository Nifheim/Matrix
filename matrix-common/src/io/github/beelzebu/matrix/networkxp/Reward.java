package io.github.beelzebu.matrix.networkxp;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Reward {

    private final int level;
    private final Set<String> commands;
    private final List<String> messages;
}
