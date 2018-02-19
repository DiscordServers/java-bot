package com.discordservers;

import com.discordservers.Api.ReauthedApiClient;
import com.discordservers.Command.CleanCommand;
import com.discordservers.Command.EvalCommand;
import com.discordservers.Event.GuildListener;
import com.discordservers.Service.ServerCleaner;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jdautilities.examples.command.ShutdownCommand;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClientException;
import io.swagger.client.ApiException;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;

import javax.security.auth.login.LoginException;

public class Bot {
    public ShardManager jda;
    public NonBlockingStatsDClient statsd = null;
    public final ReauthedApiClient api;
    public final ServerCleaner serverCleaner;

    public static void main(String[] args) throws LoginException, ApiException {
        new Bot();
    }

    private String getToken() {
        try {
            return Unirest.get("http://token:3000").asString().getBody();
        } catch (UnirestException e) {
            return System.getenv("TOKEN");
        }
    }

    private CommandClient buildCommandClient() {
        return new CommandClientBuilder()
                .setOwnerId("108432868149035008")
                .setCoOwnerIds("97774439319486464")
                .setPrefix("|")
                .setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26")
                .setGame(Game.playing("https://discord.chat/"))
                .addCommands(
                        new ShutdownCommand(),
                        new PingCommand(),
                        new EvalCommand(this),
                        new CleanCommand(this)
                )
                .build();
    }

    private Bot() throws LoginException, ApiException {
        this.api = new ReauthedApiClient(this);
        this.serverCleaner = new ServerCleaner(this);

        try {
            this.statsd = new NonBlockingStatsDClient(
                    "bot.",
                    "datadog",
                    8215
            );
        } catch (StatsDClientException e) {
            System.err.println("Failed to connect to statsd host.");
        }

        this.jda = new DefaultShardManagerBuilder()
                .setToken(getToken())
                .addEventListeners()
                .addEventListeners(new GuildListener(this))
                .addEventListeners(buildCommandClient())
                .build();
    }
}
