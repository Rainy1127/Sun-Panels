package net.rainy.sun_panels.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Energy pipe. The six boolean properties only drive the model and hitbox; energy routing is done by the
 * pipe network and does not depend on them. No ticker: pipes are completely passive.
 */
public class EnergyPipeBlock extends Block implements EntityBlock {
    public static final MapCodec<EnergyPipeBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PipeTier.CODEC.fieldOf("tier").forGetter(EnergyPipeBlock::tier),
            propertiesCodec()
    ).apply(instance, EnergyPipeBlock::new));

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    private static final BooleanProperty[] BY_DIRECTION = new BooleanProperty[6];
    private static final Direction[] DIRECTIONS = Direction.values();

    private static final VoxelShape CENTER = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape[] ARMS = new VoxelShape[6];
    // One precomputed shape per connection combination, so getShape is a plain array lookup.
    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        BY_DIRECTION[Direction.NORTH.ordinal()] = NORTH;
        BY_DIRECTION[Direction.EAST.ordinal()] = EAST;
        BY_DIRECTION[Direction.SOUTH.ordinal()] = SOUTH;
        BY_DIRECTION[Direction.WEST.ordinal()] = WEST;
        BY_DIRECTION[Direction.UP.ordinal()] = UP;
        BY_DIRECTION[Direction.DOWN.ordinal()] = DOWN;

        ARMS[Direction.NORTH.ordinal()] = Block.box(6, 6, 0, 10, 10, 6);
        ARMS[Direction.EAST.ordinal()] = Block.box(10, 6, 6, 16, 10, 10);
        ARMS[Direction.SOUTH.ordinal()] = Block.box(6, 6, 10, 10, 10, 16);
        ARMS[Direction.WEST.ordinal()] = Block.box(0, 6, 6, 6, 10, 10);
        ARMS[Direction.UP.ordinal()] = Block.box(6, 10, 6, 10, 16, 10);
        ARMS[Direction.DOWN.ordinal()] = Block.box(6, 0, 6, 10, 6, 10);

        for (int mask = 0; mask < SHAPES.length; mask++) {
            VoxelShape shape = CENTER;
            for (Direction direction : DIRECTIONS) {
                if ((mask & (1 << direction.ordinal())) != 0) {
                    shape = Shapes.or(shape, ARMS[direction.ordinal()]);
                }
            }
            SHAPES[mask] = shape;
        }
    }

    private final PipeTier tier;

    public EnergyPipeBlock(PipeTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty property : BY_DIRECTION) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    public PipeTier tier() {
        return tier;
    }

    public static BooleanProperty property(Direction direction) {
        return BY_DIRECTION[direction.ordinal()];
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int mask = 0;
        for (Direction direction : DIRECTIONS) {
            if (state.getValue(BY_DIRECTION[direction.ordinal()])) {
                mask |= 1 << direction.ordinal();
            }
        }
        return SHAPES[mask];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(direction);
            state = state.setValue(property(direction), canConnect(level, neighborPos, level.getBlockState(neighborPos), direction));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
                                     RandomSource random) {
        return state.setValue(property(direction), canConnect(level, neighborPos, neighborState, direction));
    }

    private static boolean canConnect(LevelReader level, BlockPos neighborPos, BlockState neighborState, Direction direction) {
        if (neighborState.getBlock() instanceof EnergyPipeBlock) {
            return true;
        }
        if (!neighborState.hasBlockEntity() || !(level instanceof Level realLevel)) {
            return false;
        }
        return realLevel.getCapability(Capabilities.Energy.BLOCK, neighborPos, direction.getOpposite()) != null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyPipeBlockEntity(pos, state);
    }
}
