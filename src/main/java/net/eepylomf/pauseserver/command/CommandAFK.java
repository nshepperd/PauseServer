package net.eepylomf.pauseserver.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.eepylomf.pauseserver.PauseCountdown;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

public class CommandAFK extends CommandBase {

    @Override
    public String getCommandName() {
        return "unpause";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "unpause <duration>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args.length > 1) {
            throw new WrongUsageException("Usage:" + getCommandUsage(sender));
        }
        Pattern r = Pattern.compile("(-?[0-9]+)(h|m|s)");
        Matcher m = r.matcher(args[0]);
        if (!m.matches()) {
            throw new WrongUsageException("Invalid duration. Use format <number>h|m|s.");
        }
        int num = parseInt(sender, m.group(1));
        if (num < 0) {
            throw new WrongUsageException("Duration must be positive.");
        }
        int seconds = 0;
        switch (m.group(2)) {
            case "h":
                seconds = num * 3600;
                break;
            case "m":
                seconds = num * 60;
                break;
            case "s":
                seconds = num;
                break;
        }
        if (seconds > 3600 * 24) {
            throw new WrongUsageException("Duration must be less than 24 hours.");
        }
        PauseCountdown.getInstance()
            .setAFKCountdown(seconds * 20);
        sender.addChatMessage(new ChatComponentText("OK, server will continue running for " + seconds + " seconds."));
    }
}
