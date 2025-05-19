package me.kall.newton.mixin;

import me.kall.newton.Newton;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    protected MixinServerWorld(ISpawnWorldInfo p_i241925_1_, RegistryKey<World> p_i241925_2_, DimensionType p_i241925_3_, Supplier<IProfiler> p_i241925_4_, boolean p_i241925_5_, boolean p_i241925_6_, long p_i241925_7_) {
        super(p_i241925_1_, p_i241925_2_, p_i241925_3_, p_i241925_4_, p_i241925_5_, p_i241925_6_, p_i241925_7_);
    }

    @Shadow @Nullable public abstract Entity getEntity(UUID pUniqueId);
    @Shadow public abstract boolean addFreshEntity(Entity pEntity);

    @Unique private int newton$maxCount;

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isRandomlyTicking()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newton$onTickBlock(Chunk pChunk, int pRandomTickSpeed, CallbackInfo ci, ChunkPos chunkpos, boolean flag, int i, int j, IProfiler iprofiler, ChunkSection[] var8, int var9, int var10, ChunkSection chunksection, int k, int l, BlockPos blockpos1, BlockState blockstate) {
        if (ThreadLocalRandom.current().nextInt(Newton.POSSIBILITY.get()) == 1 && BlockTags.LEAVES.contains(blockstate.getBlock()) && !blockstate.getValue(LeavesBlock.PERSISTENT) && Objects.equals(this.getBlockState(blockpos1.below()), Blocks.AIR.defaultBlockState())) {
            int x = blockpos1.getX();
            int y = blockpos1.getY() - 1;
            int z = blockpos1.getZ();
            ItemEntity apple = new ItemEntity((ServerWorld)(Object)this, x, y, z, Items.APPLE.getDefaultInstance());
            UUID id = this.newton$getUniqueId();
            apple.setUUID(id);

            this.addFreshEntity(apple);
            this.newton$maxCount +=1;
            Newton.LOGGER.info("Add an apple at {},{},{}, current apples: {}", x, y, z, this.newton$maxCount);
            Newton.APPLES.add(id);
            if (this.newton$maxCount == Newton.MAX_COUNT.get()) {
                for (UUID uuid : Newton.APPLES) {
                    Entity entity = this.getEntity(uuid);
                    if (entity != null) entity.remove();
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
