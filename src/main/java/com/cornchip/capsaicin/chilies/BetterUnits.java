package com.cornchip.capsaicin.chilies;

import com.cornchip.capsaicin.Chili;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.LinkedList;
import java.util.List;

public class BetterUnits extends Chili {
    //Return true if command is executed successfully
    private boolean handleBetterUnitsDigital(SlashCommandEvent event) {
        double standardUnitValue, betterUnitValue;
        OptionMapping standardUnitType = event.getOption("sunit");
        OptionMapping betterUnitType = event.getOption("bunit");

        standardUnitValue = event.getOption("value").getAsDouble();

        //take advantage of cases going into next case if not broken
        switch (standardUnitType.getAsString()) {
            case "TB":
                standardUnitValue *= 1000;
            case "GB":
                standardUnitValue *= 1000;
            case "MB":
                standardUnitValue *= 1000;
            case "KB":
                standardUnitValue *= 1000;
            case "B":
                break;
        }

        betterUnitValue = 0;
        switch (betterUnitType.getAsString()) {
            case "swally" -> betterUnitValue = standardUnitValue / 128000;
            case "cwally" -> betterUnitValue = (standardUnitValue - 59903) / 18;
            case "mistake" -> betterUnitValue = standardUnitValue / 4000000000000.0;
        }

        event.reply(event.getOption("value").getAsDouble() + " " + standardUnitType.getAsString() + " equals " + betterUnitValue + " " + betterUnitType.getAsString()).setEphemeral(true).queue();

        return true;
    }

    private boolean handleBetterUnitsMetric(SlashCommandEvent event) {
        if (event.getSubcommandName().equals("length"))
            handleBetterUnitsMetricLength(event);
        else if (event.getSubcommandName().equals("volume"))
            handleBetterUnitsMetricVolume(event);

        return true;
    }
    private void handleBetterUnitsMetricLength(SlashCommandEvent event) {
        double standardUnitValue, betterUnitValue;
        OptionMapping standardUnitType = event.getOption("sunit");
        OptionMapping betterUnitType = event.getOption("bunit");

        standardUnitValue = event.getOption("value").getAsDouble();
        switch (standardUnitType.getAsString()) {
            case "m":
                break;
            case "cm":
                standardUnitValue /= 1000;
                break;
            case "km":
                standardUnitValue *= 1000;
                break;
        }

        betterUnitValue = 0;
        switch (betterUnitType.getAsString()) {
            case "freedomeagles" -> betterUnitValue = standardUnitValue / 2;
        }

        event.reply(event.getOption("value").getAsDouble() + " " + standardUnitType.getAsString() + " equals " + betterUnitValue + " " + betterUnitType.getAsString()).setEphemeral(true).queue();
    }
    private void handleBetterUnitsMetricVolume(SlashCommandEvent event) {
        double standardUnitValue, betterUnitValue;
        OptionMapping standardUnitType = event.getOption("sunit");
        OptionMapping betterUnitType = event.getOption("bunit");

        standardUnitValue = event.getOption("value").getAsDouble();
        switch (standardUnitType.getAsString()) {
            case "L":
                break;
            case "mL":
                standardUnitValue /= 1000;
                break;
        }

        betterUnitValue = 0;
        switch (betterUnitType.getAsString()) {
            case "children" -> betterUnitValue = (35.19508 * standardUnitValue)/512;
        }

        event.reply(event.getOption("value").getAsDouble() + " " + standardUnitType.getAsString() + " equals " + betterUnitValue + " " + betterUnitType.getAsString()).setEphemeral(true).queue();
    }

    @Override
    protected void runEvents(GenericEvent event) {
        if (event instanceof SlashCommandEvent) {
            SlashCommandEvent commandEvent = (SlashCommandEvent) event;
            switch (commandEvent.getName()) {
                case "better-units-digital":
                    handleBetterUnitsDigital(commandEvent);
                    break;
                case "better-units-metric":
                    handleBetterUnitsMetric(commandEvent);
                    break;
            }

        }
    }

    @Override
    protected void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {

    }

    @Override
    protected List<CommandData> getCommands() {
        LinkedList<CommandData> commandData = new LinkedList<>();

        //Digital units
        CommandData betterUnitsDigital = new CommandData("better-units-digital", "Convert from standard digital units to better digital units");
        betterUnitsDigital.addOption(OptionType.NUMBER, "value", "Amount of base unit", true);
        OptionData standardDigitalType = new OptionData(OptionType.STRING, "sunit", "Standard unit type", true);
        standardDigitalType.addChoice("TeraBytes", "TB");
        standardDigitalType.addChoice("GigaBytes", "GB");
        standardDigitalType.addChoice("MegaBytes", "MB");
        standardDigitalType.addChoice("KiloBytes", "KB");
        standardDigitalType.addChoice("Bytes", "B");

        OptionData betterDigitalType = new OptionData(OptionType.STRING, "bunit", "Better unit type", true);
        betterDigitalType.addChoice("Compressed Wally", "cwally");
        betterDigitalType.addChoice("Standard Wally", "swally");
        betterDigitalType.addChoice("Mistake", "mistake");

        betterUnitsDigital.addOptions(standardDigitalType, betterDigitalType);

        //Metric units
        CommandData betterUnitsMetric = new CommandData("better-units-metric", "Convert from standard metric units to better units");

        //length
        SubcommandData betterUnitsMetricLength = new SubcommandData("length", "Length units");
        betterUnitsMetricLength.addOption(OptionType.NUMBER, "value", "Amount of base unit", true);

        OptionData standardMetricLengthType = new OptionData(OptionType.STRING, "sunit", "Standard unit type", true);
        standardMetricLengthType.addChoice("Meter", "m");
        standardMetricLengthType.addChoice("CentiMeter", "cm");
        standardMetricLengthType.addChoice("KiloMeter", "km");

        OptionData betterMetricLengthType = new OptionData(OptionType.STRING, "bunit", "Better unit type", true);
        betterMetricLengthType.addChoice("Freedom Eagles", "freedomeagles");

        betterUnitsMetricLength.addOptions(standardMetricLengthType, betterMetricLengthType);

        //volume
        SubcommandData betterUnitsMetricVolume = new SubcommandData("volume", "Volume units");
        betterUnitsMetricVolume.addOption(OptionType.NUMBER, "value", "Amount of base unit", true);

        OptionData standardMetricVolumeType = new OptionData(OptionType.STRING, "sunit", "Standard unit type", true);
        standardMetricVolumeType.addChoice("Liter", "L");
        standardMetricVolumeType.addChoice("MilliLiter", "mL");

        OptionData betterMetricVolumeType = new OptionData(OptionType.STRING, "bunit", "Better unit type", true);
        betterMetricVolumeType.addChoice("Children", "children");

        betterUnitsMetricVolume.addOptions(standardMetricVolumeType, betterMetricVolumeType);


        betterUnitsMetric.addSubcommands(betterUnitsMetricLength, betterUnitsMetricVolume);

        commandData.add(betterUnitsDigital);
        commandData.add(betterUnitsMetric);
        return commandData;
    }
}
