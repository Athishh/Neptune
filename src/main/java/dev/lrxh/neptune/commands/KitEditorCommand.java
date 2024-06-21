package dev.lrxh.neptune.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.impl.MessagesLocale;
import dev.lrxh.neptune.kit.Kit;
import dev.lrxh.neptune.profile.Profile;
import dev.lrxh.neptune.profile.ProfileState;
import dev.lrxh.neptune.providers.clickable.Replacement;
import dev.lrxh.neptune.utils.CC;
import org.bukkit.entity.Player;

import java.util.Arrays;

@CommandAlias("kiteditor")
@Description("Kit Editor command")
public class KitEditorCommand extends BaseCommand {
    private final Neptune plugin = Neptune.get();

    @Subcommand("reset")
    @Syntax("<kit>")
    @CommandCompletion("@kits")
    public void reset(Player player, String kitName) {
        if (player == null) return;
        if (!checkKit(kitName)) {
            player.sendMessage(CC.error("Kit doesn't exist!"));
            return;
        }
        Profile profile = plugin.getProfileManager().getByUUID(player.getUniqueId());
        if (profile == null) return;
        Kit kit = plugin.getKitManager().getKitByName(kitName);

        profile.getGameData().getKitData().get(kit).setKitLoadout(kit.getItems());

        if (profile.getState().equals(ProfileState.IN_KIT_EDITOR)) {
            profile.getGameData().getKitData().get(profile.getGameData().getKitEditor()).setKitLoadout
                    (Arrays.asList(player.getInventory().getContents()));

            MessagesLocale.KIT_EDITOR_STOP.send(player.getUniqueId());
            profile.setState(ProfileState.LOBBY);
        }

        MessagesLocale.KIT_EDITOR_RESET.send(player.getUniqueId(), new Replacement("<kit>", kit.getDisplayName()));
    }

    private boolean checkKit(String kitName) {
        return plugin.getKitManager().getKitByName(kitName) != null;
    }
}
