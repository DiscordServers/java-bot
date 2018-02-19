package com.discordservers.Builder;

import io.swagger.client.model.ServerForm;
import net.dv8tion.jda.core.entities.Guild;

public class ServerFormBuilder {
    public static ServerForm getServerForm(Guild guild, boolean newGuild) {
        ServerForm form = new ServerForm();
        form
                .identifier(guild.getIdLong())
                .name(guild.getName())
                .region(guild.getRegion().getKey())
                .icon(guild.getIconId())
                .splash(guild.getSplashId())
                .hasBot(true);

        try {
            form.setOwner(guild.getOwner().getUser().getIdLong());
        } catch (NullPointerException ignored) {
            // Ignoring.
        }

        if (newGuild) {
            form.setListed(true);
            form.setEnabled(true);
        }

        return form;
    }

    public static ServerForm getServerForm(Guild guild) {
        return getServerForm(guild, false);
    }
}
