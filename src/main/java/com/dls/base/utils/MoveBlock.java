package com.dls.base.utils;

import java.util.List;

public class MoveBlock {

    enum BlockStatus {START, PROCESS, END}

    private long block;
    private byte blockType;
    private List<MoveBlock> childBlocks;
    private BlockStatus blockStatus;
    private MoveBlock prevBlock;
    private MoveBlock nextBlock;

    public long getBlock() {
        return block;
    }

    public void setBlock(long block) {
        this.block = block;
    }

    public byte getBlockType() {
        return blockType;
    }

    public void setBlockType(byte blockType) {
        this.blockType = blockType;
    }

    public List<MoveBlock> getChildBlocks() {
        return childBlocks;
    }

    public void setChildBlocks(List<MoveBlock> childBlocks) {
        this.childBlocks = childBlocks;
    }

    public BlockStatus getBlockStatus() {
        return blockStatus;
    }

    public void setBlockStatus(BlockStatus blockStatus) {
        this.blockStatus = blockStatus;
    }

    public MoveBlock getPrevBlock() {
        return prevBlock;
    }

    public void setPrevBlock(MoveBlock prevBlock) {
        this.prevBlock = prevBlock;
    }

    public MoveBlock getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(MoveBlock nextBlock) {
        this.nextBlock = nextBlock;
    }
}
