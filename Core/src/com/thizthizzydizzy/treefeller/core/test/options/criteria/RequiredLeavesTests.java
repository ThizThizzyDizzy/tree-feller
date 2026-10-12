package com.thizthizzydizzy.treefeller.core.test.options.criteria;
import com.thizthizzydizzy.treefeller.core.test.ITestPlatform;
import com.thizthizzydizzy.treefeller.core.test.TestTrees;
public final class RequiredLeavesTests{
    private RequiredLeavesTests(){}
    public static void register(ITestPlatform platform){
        TestTrees.Tree small = TestTrees.specimen(TestTrees.Kind.SPRUCE, 4);
        TestTrees.Tree large = TestTrees.specimen(TestTrees.Kind.SPRUCE, 8);
        RangeCriteriaTests.register(platform, "required-leaves", small, count(small), large, count(large));
    }
    private static int count(TestTrees.Tree tree){
        return (int)tree.blocks.values().stream().filter(block -> block.material.endsWith("_leaves")).count();
    }
}
