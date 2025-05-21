package me.kall.newton.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.kall.newton.Newton;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@ParametersAreNonnullByDefault
@Mixin(ServerLevel.class)
public abstract class MixinServerWorld extends Level {

    protected MixinServerWorld(WritableLevelData arg, ResourceKey<Level> arg2, RegistryAccess arg3, Holder<DimensionType> arg4, boolean bl, boolean bl2, long l, int i) {
        super(arg, arg2, arg3, arg4, bl, bl2, l, i);
    }

    @Shadow public abstract boolean addFreshEntity(Entity pEntity);

    @Unique private int newton$maxCount;

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isRandomlyTicking()Z"))
    private void newton$onTickBlock(LevelChunk levelChunk, int m, CallbackInfo ci, @Local BlockPos pos, @Local BlockState state) {
        if (ThreadLocalRandom.current().nextInt(Newton.POSSIBILITY.get()) == 1 && state.is(BlockTags.LEAVES)  && !state.getValue(LeavesBlock.PERSISTENT) && Objects.equals(this.getBlockState(pos.below()), Blocks.AIR.defaultBlockState())) {
            int x = pos.getX();
            int y = pos.getY() - 1;
            int z = pos.getZ();
            ItemEntity apple = new ItemEntity((ServerLevel)(Object)this, x, y, z, Items.APPLE.getDefaultInstance());
            UUID id = this.newton$getUniqueId();
            apple.setUUID(id);

            this.addFreshEntity(apple);
            this.newton$maxCount +=1;
            Newton.LOGGER.info("Add an apple at {},{},{}, current apples: {}", x, y, z, this.newton$maxCount);
            Newton.APPLES.add(id);
            if (this.newton$maxCount == Newton.MAX_COUNT.get()) {
                for (UUID uuid : Newton.APPLES) {
                    Entity entity = this.getEntity(uuid);
                    if (entity != null) entity.remove(Entity.RemovalReason.KILLED);
                }
                Newton.APPLES.clear();
                this.newton$maxCount = 0;
                Newton.LOGGER.info("Apples are cleared");
            }
        }
    }

    @Unique
    private UUID newton$getUniqueId() {
        UUID id = UUID.randomUUID();
        while (this.getEntity(id) != null) id = UUID.randomUUID();
        return id;
    }
}