package com.dls.base.utils;

import java.util.HashMap;

public class BlockSkin {

    private long block;
    private byte blockType;
    private HashMap <Long, Object> elements = new HashMap<Long, Object>();
    private BlockSkin prevBlock;
    private BlockSkin nextBlock;

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

    public HashMap<Long, Object> getElements() {
        return elements;
    }

    public void setElements(HashMap<Long, Object> elements) {
        this.elements = elements;
    }

    public BlockSkin getPrevBlock() {
        return prevBlock;
    }

    public void setPrevBlock(BlockSkin prevBlock) {
        this.prevBlock = prevBlock;
    }

    public BlockSkin getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(BlockSkin nextBlock) {
        this.nextBlock = nextBlock;
    }
}
