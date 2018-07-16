package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.database.MongoStorage;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * @author Beelzebu
 */
@Entity("players")
@Getter
@Setter
@NoArgsConstructor
public abstract class MongoMatrixPlayer implements MatrixPlayer {

    @Id
    private ObjectId id;
    @Property("uuid")
    private UUID uniqueId;
    private String name;
    private String displayname;
    private ChatColor chatColor;
    private boolean watcher;
    private boolean authed;
    private long exp;
    private Date lastLogin;
    private String currentIP;
    private Set<PlayerOptionType> options;
    private Set<String> ipHistory;
    private transient String IP;

    @Override
    public boolean getOption(PlayerOptionType option) {
        return options.contains(option);
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        if (status) {
            options.add(option);
        } else {
            options.remove(option);
        }
    }

    void setIP(String IP) {
        this.IP = IP;
        currentIP = IP;
        ipHistory.add(IP);
    }

    public void save() {
        ((MongoStorage) Matrix.getAPI().getDatabase()).getUserDAO().save(this);
    }
}
