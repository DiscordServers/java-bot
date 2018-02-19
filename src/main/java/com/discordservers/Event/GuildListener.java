package com.discordservers.Event;

import com.discordservers.Bot;
import com.discordservers.Queue.UpdateMemberCountQueue;
import io.swagger.client.ApiException;
import io.swagger.client.api.ServerApi;
import io.swagger.client.model.ServerForm;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.core.events.user.GenericUserPresenceEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.math.BigDecimal;

import static com.discordservers.Builder.ServerFormBuilder.getServerForm;

public class GuildListener extends ListenerAdapter {
    private final Bot bot;
    private final ServerApi api;
    private final UpdateMemberCountQueue updateMemberCountQueue;
    private int ready = 0;

    public GuildListener(Bot bot) {
        this.bot = bot;
        this.api = bot.api.serverApi;
        this.updateMemberCountQueue = new UpdateMemberCountQueue(bot);
    }

    @Override
    public void onReady(ReadyEvent event) {
        this.ready++;
        System.out.println("Shards Ready: " + this.ready);
        if (this.ready == this.bot.jda.getShardsTotal()) {
            System.out.println("Bot is Ready!");

            this.bot.jda.getGuilds().forEach(guild->{
                try {
                    this.editGuild(guild);
                } catch (ApiException e) {
                    System.out.println(e.getResponseBody());
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        this.ready--;
        System.out.println("Shards Ready: " + this.ready);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        this.bot.statsd.increment("events.guildJoin");
        Guild guild = event.getGuild();
        try {
            this.addGuild(guild);
            this.bot.statsd.increment("events.success");
        } catch (ApiException e) {
            System.out.println(e.getResponseBody());
            e.printStackTrace();
            this.bot.statsd.increment("events.failure");
        }
    }

    private void editGuild(Guild guild) throws ApiException {
        try {
            this.api.upsert(guild.getIdLong(), getServerForm(guild));
            this.updateMemberCount(guild);
        } catch (ApiException e) {
            if (e.getMessage().equals("Not Found")) {
                this.addGuild(guild);

                return;
            }

            throw e;
        }
    }

    private void addGuild(Guild guild) throws ApiException {
        this.api.upsert(guild.getIdLong(), getServerForm(guild, true));
        this.updateMemberCount(guild);
        System.out.println("Adding guild: " + guild.getId());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        this.bot.statsd.increment("event.guildLeave");
        try {
            this.api.delist(event.getGuild().getIdLong());
            this.bot.statsd.increment("events.success");
        } catch (ApiException e) {
            if (e.getMessage().contains("Not Found")) {
                return;
            }

            System.out.println(e.getResponseBody());
            e.printStackTrace();
            this.bot.statsd.increment("events.failure");
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        this.updateMemberCount(event.getGuild());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        this.updateMemberCount(event.getGuild());
    }

    @Override
    public void onGenericGuildUpdate(GenericGuildUpdateEvent event) {
        this.bot.statsd.increment("event.guildUpdate");
        Guild guild = event.getGuild();
        try {
            this.editGuild(guild);
            this.bot.statsd.increment("events.success");
        } catch (ApiException e) {
            if (e.getMessage().contains("Not Found")) {
                try {
                    this.addGuild(guild);
                    this.bot.statsd.increment("events.success");

                    return;
                } catch (ApiException e1) {
                    e = e1;
                }
            }

            System.out.println(e.getResponseBody());
            e.printStackTrace();
            this.bot.statsd.increment("events.failure");
        }
    }

    @Override
    public void onGenericUserPresence(GenericUserPresenceEvent event) {
        this.updateMemberCount(event.getGuild());
    }

    private void updateMemberCount(Guild guild) {
        int count = Math.round(guild.getMembers().size() / 10) * 10;

        this.updateMemberCountQueue.push(guild.getIdLong(), count);
    }
}
