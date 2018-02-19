package com.discordservers.Command;

import com.discordservers.Bot;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.swagger.client.ApiException;
import net.dv8tion.jda.core.EmbedBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.time.OffsetDateTime;

public class CleanCommand extends Command {
    private final Bot bot;

    public CleanCommand(Bot bot) {
        this.bot = bot;
        this.name = "clean";
        this.help = "This command cleans the database.";
        this.ownerCommand = true;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply("Cleaning database, please wait.");

        try {
            this.bot.serverCleaner.process();
        } catch (ApiException e) {
            event.reply("Failed! Please check the console.");
            e.printStackTrace();

            return;
        }

        event.reply("Finished!");
    }
}
