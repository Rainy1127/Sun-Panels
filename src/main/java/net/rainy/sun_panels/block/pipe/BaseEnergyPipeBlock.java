package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for energy pipe blocks
 * This block creates a central node with optional connections to all six directions.
 * The model is built from a center piece plus optional connection pieces for each direction.
 */
public abstract class BaseEnergyPipeBlock extends Block implements EntityBlock {
    // Block state properties for each connection direction
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    // Shapes for the center pipe and each connection
    protected static final VoxelShape CENTER_SHAPE = Block.box(6, 6, 6, 10, 10, 10);
    protected static final VoxelShape NORTH_SHAPE = Block.box(6, 6, 0, 10, 10, 6);
    protected static final VoxelShape EAST_SHAPE = Block.box(10, 6, 6, 16, 10, 10);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(6, 6, 10, 10, 10, 16);
    protected static final VoxelShape WEST_SHAPE = Block.box(0, 6, 6, 6, 10, 10);
    protected static final VoxelShape UP_SHAPE = Block.box(6, 10, 6, 10, 16, 10);
    protected static final VoxelShape DOWN_SHAPE = Block.box(6, 0, 6, 10, 6, 10);

    // Transfer rate in RF/t
    protected final int transferRate;

    public BaseEnergyPipeBlock(BlockBehaviour.Properties properties, int transferRate) {
        super(properties);
        this.transferRate = transferRate;

        // Set default state with no connections
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape = CENTER_SHAPE; // Start with the center

        // Add shapes for each connected side
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE);

        return shape;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectionState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                  @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        // Update the connection state when a neighbor changes
        return getConnectionState(state, level, currentPos);
    }

    /**
     * Determines the connection state for all sides of the pipe
     */
    private BlockState getConnectionState(BlockState state, LevelAccessor level, BlockPos pos) {
        // Check each direction for a connectable energy handler
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            boolean canConnect = false;

            // Connect to other pipes
            if (neighborState.getBlock() instanceof BaseEnergyPipeBlock) {
                canConnect = true;
            }
            // Connect to energy-capable blocks
            else if (level instanceof Level actualLevel) {
                BlockEntity blockEntity = level.getBlockEntity(neighborPos);
                if (blockEntity != null) {
                    // Check if the neighbor can provide or receive energy using the Level instead of BlockGetter
                    IEnergyStorage energyStorage = actualLevel.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
                    if (energyStorage != null && (energyStorage.canExtract() || energyStorage.canReceive())) {
                        canConnect = true;
                    }
                }
            }

            // Set the appropriate property for this direction
            BooleanProperty property = getPropertyForDirection(direction);
            if (property != null) {
                state = state.setValue(property, canConnect);
            }
        }

        return state;
    }

    /**
     * Gets the BlockState property for a given direction
     */
    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                           @NotNull BlockEntityType<T> blockEntityType);

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state);

    /**
     * Returns the energy transfer rate of this pipe
     */
    public int getTransferRate() {
        return transferRate;
    }
}