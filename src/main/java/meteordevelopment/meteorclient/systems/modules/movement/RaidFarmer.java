/*
 * This file is part of the AlfredOverflow distribution (https://github.com/melektron/AlfredOverflow).
 * Copyright (c) ELEKTRON.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class RaidFarmer extends Module {

    // == Settings

    public enum ActivationMode {
        Continue,
        Restart
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final SettingGroup sgTiming = settings.createGroup("Timing");

    private final Setting<ActivationMode> activationMode = sgGeneral.add(new EnumSetting.Builder<ActivationMode>()
        .name("Activation Mode")
        .description("What should happen when the module is (re)activated")
        .defaultValue(ActivationMode.Restart)
        .build()
    );

    private final Setting<Double> backOffDuration = sgTiming.add(new DoubleSetting.Builder()
        .name("Back off duration")
        .description("The time the back off stage is active in seconds.")
        .defaultValue(5)
        .min(0.0)
        .build()
    );

    private final Setting<Double> tubeChangeDuration = sgTiming.add(new DoubleSetting.Builder()
        .name("Tube change duration")
        .description("The time the tube change stage is active in seconds.")
        .defaultValue(3)
        .min(0.0)
        .build()
    );

    private final Setting<Double> diagApproachDuration = sgTiming.add(new DoubleSetting.Builder()
        .name("Diagonal approach duration")
        .description("The time the diagonal approach stage is active in seconds.")
        .defaultValue(5)
        .min(0.0)
        .build()
    );

    private final Setting<Double> approachDuration = sgTiming.add(new DoubleSetting.Builder()
        .name("Approach duration")
        .description("The time the non-diagonal approach stage is active in seconds.")
        .defaultValue(2)
        .min(0.0)
        .build()
    );

    private final Setting<Double> waitDuration = sgTiming.add(new DoubleSetting.Builder()
        .name("Wait duration")
        .description("The time to wait after every cycle before restarting is active in seconds.")
        .defaultValue(50)
        .min(0.0)
        .build()
    );


    // == Constructor

    public RaidFarmer() {
        super(Categories.Movement, "raid-farmer", "Automatically maneuvers around the Raid Farm.");
    }


    // == Implementation
    private enum WalkingStage {
        BackOff,
        TubeChange,
        DiagApproach,
        Approach,
        Wait;

        // gets the next stage
        public WalkingStage next() {
            return switch (this) {
                case BackOff -> TubeChange;
                case TubeChange -> DiagApproach;
                case DiagApproach -> Approach;
                case Approach -> Wait;
                default -> BackOff;
            };
        }
    }

    private WalkingStage currentStage = WalkingStage.BackOff;
    private boolean active = false;
    private int stageTimer = 0;


    @Override
    public void onActivate() {
        if (activationMode.get() == ActivationMode.Restart) {
            stageTimer = 0;
            currentStage = WalkingStage.BackOff;
        }
        pressStageKeys(currentStage);   // starts the stage execution
    }

    @Override
    public void onDeactivate() {
        releaseAll();
    }

    @EventHandler()
    private void onTick(TickEvent.Pre event) {
        if (!isActive()) return;

        double duration = switch (currentStage) {
            case BackOff -> backOffDuration.get();
            case TubeChange -> tubeChangeDuration.get();
            case DiagApproach -> diagApproachDuration.get();
            case Approach -> approachDuration.get();
            case Wait -> waitDuration.get();
        };
        int durationTicks = (int)(duration * 20);

        if (stageTimer > durationTicks) {
            stageTimer = 0;
            currentStage = currentStage.next();
            pressStageKeys(currentStage);

            MeteorClient.LOG.info("Changed Stage to: " + currentStage);
        }

        stageTimer++;
    }

    private void pressStageKeys(WalkingStage stage) {
        switch (stage)
        {
            case BackOff -> setKeysFBLR(false, true, false, false);
            case TubeChange -> setKeysFBLR(false, false, true, false);
            case DiagApproach -> setKeysFBLR(true, false, false, true);
            case Approach -> setKeysFBLR(true, false, false, false);
            case Wait -> releaseAll();
        }
    }

    private void releaseAll() {
        setKeysFBLR(false, false, false, false);
    }

    private void setKeysFBLR(boolean fwd, boolean back, boolean left, boolean right) {
        mc.options.forwardKey.setPressed(fwd);
        mc.options.backKey.setPressed(back);
        mc.options.leftKey.setPressed(left);
        mc.options.rightKey.setPressed(right);
    }
}
