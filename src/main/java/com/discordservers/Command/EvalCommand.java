package com.discordservers.Command;

import com.discordservers.Bot;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

public class EvalCommand extends Command {
    private final Bot bot;

    public EvalCommand(Bot bot) {
        this.bot = bot;
        this.name = "eval";
        this.help = "This command evaluates Javascript code.";
        this.arguments = "<code>";
        this.ownerCommand = true;
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String toEval = event.getArgs();
        ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", this.bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());

        try {
            event.reply(new EmbedBuilder()
                    .setAuthor("Execution Success!")
                    .setTimestamp(OffsetDateTime.now())
                    .setColor(Color.getColor("#2196F3"))
                    .addField("__Input:__", "```js\n" + toEval + "\n```", false)
                    .addField("__Output:__", "```js\n" + se.eval(toEval) + "\n```", false)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);

            event.reply("Error with eval. Check logs");
        }
    }
}
