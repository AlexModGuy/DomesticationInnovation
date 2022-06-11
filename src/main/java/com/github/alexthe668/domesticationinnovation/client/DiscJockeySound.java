package com.github.alexthe668.domesticationinnovation.client;

import com.github.alexthe668.domesticationinnovation.server.entity.FollowingJukeboxEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class DiscJockeySound  extends AbstractTickableSoundInstance {
    private final FollowingJukeboxEntity box;
    private int ticksExisted = 0;
    private SoundEvent recordSound;

    public DiscJockeySound(SoundEvent record, FollowingJukeboxEntity box) {
        super(record, SoundSource.RECORDS, box.getLevel().getRandom());
        this.box = box;
        this.attenuation = Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.x = this.box.getX();
        this.y = this.box.getY();
        this.z = this.box.getZ();
        this.recordSound = record;
    }

    public boolean canPlaySound() {
        return !this.box.isSilent() && ClientProxy.DISC_JOCKEY_SOUND_MAP.get(this.box.getId()) == this;
    }

    public boolean isNearest() {
        return true;
    }

    public void tick() {
        if (!this.box.isRemoved() && this.box.isAlive()) {
            this.volume = 1;
            this.pitch = 1;
            this.x = this.box.getX();
            this.y = this.box.getY();
            this.z = this.box.getZ();
        } else {
            this.stop();
            ClientProxy.DISC_JOCKEY_SOUND_MAP.remove(box.getId());
        }
        ticksExisted++;
    }

    public SoundEvent getRecordSound(){
        return recordSound;
    }
}
