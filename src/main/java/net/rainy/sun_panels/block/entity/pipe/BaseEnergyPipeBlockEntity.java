package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.block.pipe.BaseEnergyPipeBlock;
import net.rainy.sun_panels.energy.EnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all energy pipe block entities
 * Manages energy transfer to connected blocks
 */
public abstract class BaseEnergyPipeBlockEntity extends BlockEntity {
    // Small buffer to help with energy transfer (not for storage)
    protected final EnergyStorage buffer;
    // Cache of connected energy handlers
    protected final Map<Direction, IEnergyStorage> connectedEnergyHandlers = new HashMap<>();
    // Flag to track if we need to update connections
    protected boolean connectionsDirty = true;
    // The transfer rate of this pipe
    protected final int transferRate;
    // How often (in ticks) the pipe should attempt energy transfer
    protected static final int TRANSFER_COOLDOWN = 5;
    // Counter for transfer cooldown
    protected int transferTicks = 0;

    public BaseEnergyPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int transferRate) {
        super(type, pos, state);
        this.transferRate = transferRate;
        // Small buffer for energy transfer - not for storage
        this.buffer = new EnergyStorage(transferRate * 2, transferRate, transferRate, 0);
    }

    /**
     * Called every tick on the server side to handle energy transfer
     */
    public static <T extends BaseEnergyPipeBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T pipe) {
        // Increment transfer cooldown ticker
        pipe.transferTicks++;

        // Only transfer energy every TRANSFER_COOLDOWN ticks to reduce performance impact
        if (pipe.transferTicks >= TRANSFER_COOLDOWN) {
            pipe.transferTicks = 0;

            // Update connections if needed
            if (pipe.connectionsDirty) {
                pipe.updateConnections(level, pos, state);
                pipe.connectionsDirty = false;
            }

            // Transfer energy between connected handlers
            pipe.transferEnergy();
        }
    }

    /**
     * Update the cache of connected energy handlers
     */
    protected void updateConnections(Level level, BlockPos pos, BlockState state) {
        // Clear previous connections
        connectedEnergyHandlers.clear();

        // Check each direction for connections based on the block state
        for (Direction direction : Direction.values()) {
            boolean isConnected = false;

            // Check if the pipe is connected in this direction according to its block state
            if (state.getBlock() instanceof BaseEnergyPipeBlock) {
                switch (direction) {
                    case NORTH -> isConnected = state.getValue(BaseEnergyPipeBlock.NORTH);
                    case EAST -> isConnected = state.getValue(BaseEnergyPipeBlock.EAST);
                    case SOUTH -> isConnected = state.getValue(BaseEnergyPipeBlock.SOUTH);
                    case WEST -> isConnected = state.getValue(BaseEnergyPipeBlock.WEST);
                    case UP -> isConnected = state.getValue(BaseEnergyPipeBlock.UP);
                    case DOWN -> isConnected = state.getValue(BaseEnergyPipeBlock.DOWN);
                }
            }

            // If connected in this direction, try to get the energy handler
            if (isConnected) {
                BlockPos neighborPos = pos.relative(direction);
                IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());

                if (energyStorage != null && (energyStorage.canExtract() || energyStorage.canReceive())) {
                    connectedEnergyHandlers.put(direction, energyStorage);
                }
            }
        }
    }

    /**
     * Transfers energy between connected energy handlers
     */
    protected void transferEnergy() {
        // First, try to extract energy from all extractable sources into our buffer
        extractEnergyFromSources();

        // Then, try to distribute the buffered energy to all receivers
        distributeEnergyToReceivers();
    }

    /**
     * Extracts energy from connected sources into the buffer
     */
    private void extractEnergyFromSources() {
        // Get all connected handlers that can extract energy
        Map<Direction, IEnergyStorage> extractableHandlers = new HashMap<>();
        for (Map.Entry<Direction, IEnergyStorage> entry : connectedEnergyHandlers.entrySet()) {
            if (entry.getValue().canExtract()) {
                extractableHandlers.put(entry.getKey(), entry.getValue());
            }
        }

        if (extractableHandlers.isEmpty()) {
            return;
        }

        // Calculate how much energy we can extract per source
        int remainingCapacity = buffer.getMaxEnergyStored() - buffer.getEnergyStored();
        if (remainingCapacity <= 0) {
            return;
        }

        int extractPerSource = Math.min(transferRate, remainingCapacity / Math.max(1, extractableHandlers.size()));

        // Extract energy from each source
        for (IEnergyStorage source : extractableHandlers.values()) {
            if (source.canExtract()) {
                int extracted = source.extractEnergy(extractPerSource, false);
                buffer.receiveEnergy(extracted, false);

                // If buffer is full, stop extracting
                if (buffer.getEnergyStored() >= buffer.getMaxEnergyStored()) {
                    break;
                }
            }
        }
    }

    /**
     * Distributes buffered energy to connected receivers
     */
    private void distributeEnergyToReceivers() {
        // Get all connected handlers that can receive energy
        Map<Direction, IEnergyStorage> receivingHandlers = new HashMap<>();
        for (Map.Entry<Direction, IEnergyStorage> entry : connectedEnergyHandlers.entrySet()) {
            if (entry.getValue().canReceive()) {
                receivingHandlers.put(entry.getKey(), entry.getValue());
            }
        }

        if (receivingHandlers.isEmpty()) {
            return;
        }

        // Calculate how much energy we can send per target
        int availableEnergy = buffer.getEnergyStored();
        if (availableEnergy <= 0) {
            return;
        }

        int sendPerTarget = Math.min(transferRate, availableEnergy / Math.max(1, receivingHandlers.size()));

        // Send energy to each receiver
        for (IEnergyStorage receiver : receivingHandlers.values()) {
            if (receiver.canReceive()) {
                int energyToSend = Math.min(sendPerTarget, buffer.getEnergyStored());
                int accepted = receiver.receiveEnergy(energyToSend, false);
                buffer.extractEnergy(accepted, false);

                // If buffer is empty, stop sending
                if (buffer.getEnergyStored() <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        // Mark connections as dirty whenever the block entity changes
        connectionsDirty = true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        // Save buffer energy level
        tag.putInt("Energy", buffer.getEnergyStored());
    }

    // Load method to handle data loading from tag
    public void loadEnergy(@NotNull CompoundTag tag) {
        // Load buffer energy level
        if (tag.contains("Energy")) {
            buffer.setEnergy(tag.getInt("Energy"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}